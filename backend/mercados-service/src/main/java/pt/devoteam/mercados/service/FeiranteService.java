package pt.devoteam.mercados.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.CandidaturaDTO;
import pt.devoteam.mercados.dto.FeiranteDTO;
import pt.devoteam.mercados.dto.RegistoDTO;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.repository.CandidaturaRepository;
import pt.devoteam.mercados.repository.FeiranteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeiranteService {
    private final FeiranteRepository feiranteRepository;
    private final CandidaturaRepository candidaturaRepository;

    // 🎯 INJEÇÃO DO CLIENTE E DO BUCKET PRIVADO DE PORTFÓLIOS SENSÍVEIS
    private final MinioClient minioClient;

    @Value("${minio.bucket-feirantes:feirantes-bucket}")
    private String feirantesBucket;

    public FeiranteService(FeiranteRepository feiranteRepository,
                           CandidaturaRepository candidaturaRepository,
                           MinioClient minioClient) {
        this.feiranteRepository = feiranteRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.minioClient = minioClient;
    }

    @Transactional(readOnly = true)
    public Feirante obterFeirantePorId(Long id) {
        return feiranteRepository.findById(id).orElseThrow(() -> new RuntimeException("Feirante não encontrado"));
    }

    @Transactional
    public void criarFeirante(RegistoDTO dto) {
        if (feiranteRepository.findByEmail(dto.getEmail()).isEmpty()) {
            Feirante novoFeirante = new Feirante();
            novoFeirante.setEmail(dto.getEmail());
            novoFeirante.setNome(dto.getNome());

            feiranteRepository.save(novoFeirante);
            System.out.println("🟢 [Serviço de Negócio] Feirante '" + dto.getNome() + "' criado no db_mercados!");
        } else {
            System.out.println("🟡 [Serviço de Negócio] O feirante com e-mail '" + dto.getEmail() + "' já existia. Sincronização ignorada.");
        }
    }

    /**
     * 🎯 NOVO MÉTODO REATIVO: Avalia quais os campos de documentos da entidade não estão nulos
     * e devolve uma lista limpa de chaves de strings para o modal do Angular ler instantaneamente.
     */
    @Transactional(readOnly = true)
    public List<String> listarDocumentosAtivosDoPerfil(String email) {
        Feirante feirante = feiranteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Feirante não localizado."));

        List<String> documentosAtivos = new ArrayList<>();

        if (feirante.getDocumentoAtividade() != null && !feirante.getDocumentoAtividade().trim().isEmpty()) {
            documentosAtivos.add("INICIO_ACTIVIDADE");
        }
        if (feirante.getDocumentoFinancas() != null && !feirante.getDocumentoFinancas().trim().isEmpty()) {
            documentosAtivos.add("NAO_DIVIDA_AT");
        }

        return documentosAtivos;
    }

    @Transactional(readOnly = true)
    public FeiranteDTO obterFeirantePorEmail(String email) {
        Feirante feirante = feiranteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Feirante não encontrado"));

        FeiranteDTO feiranteDTO = new FeiranteDTO();
        feiranteDTO.setId(feirante.getId());
        feiranteDTO.setEmail(feirante.getEmail());
        feiranteDTO.setNome(feirante.getNome());
        feiranteDTO.setCandidaturas(candidaturaRepository.findByFeiranteEmail(feirante.getEmail()).stream()
                .map(c -> {
                    CandidaturaDTO candidaturaDTO = new CandidaturaDTO();
                    candidaturaDTO.setId(c.getId());
                    candidaturaDTO.setDias(c.getDias());
                    candidaturaDTO.setPrecoTotal(c.getPrecoTotal());
                    candidaturaDTO.setEstado(c.getEstado());
                    return candidaturaDTO;
                })
                .collect(Collectors.toList()));

        Map<String, String> docs = new HashMap<>();
        if (feirante.getDocumentoAtividade() != null) {
            docs.put("INICIO_ACTIVIDADE", feirante.getDocumentoAtividade());
        }
        if (feirante.getDocumentoFinancas() != null) {
            docs.put("NAO_DIVIDA_AT", feirante.getDocumentoFinancas());
        }
        feiranteDTO.setPortfolioDocumentos(docs);
        return feiranteDTO;
    }

    /**
     * 🎯 RESTRUTURADO PARA CLOUD-NATIVE STORAGE (MinIO):
     * Envia o PDF recebido diretamente para a infraestrutura do Bucket Privado, salvaguardando a integridade.
     */
    @Transactional
    public void guardarDocumentoNoPortfolio(String email, String tipoDocumento, MultipartFile file) throws IOException {
        Feirante feirante = feiranteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador feirante não localizado para o anexo."));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro enviado encontra-se vazio.");
        }

        // Padroniza a nomenclatura única do objeto S3 (Ex: feirante_42_INICIO_ACTIVIDADE.pdf)
        String nomeObjetoS3 = "feirante_" + feirante.getId() + "_" + tipoDocumento + ".pdf";

        try {
            // 🚀 STREAMING DIRETO PARA O BUCKET PRIVADO DO MINIO S3
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(feirantesBucket)
                            .object(nomeObjetoS3)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType("application/pdf")
                            .build()
            );
            System.out.println("🟢 [MinIO Portfolio] PDF enviado com sucesso para o Object Storage: " + nomeObjetoS3);

        } catch (Exception e) {
            throw new IOException("Falha crítica de comunicação com o servidor de Storage MinIO: " + e.getMessage());
        }

        // Vincula a referência amigável de chave ao campo correspondente do Hibernate
        if ("INICIO_ACTIVIDADE".equals(tipoDocumento)) {
            feirante.setDocumentoAtividade(nomeObjetoS3);
        } else if ("NAO_DIVIDA_AT".equals(tipoDocumento)) {
            feirante.setDocumentoFinancas(nomeObjetoS3);
        } else {
            throw new IllegalArgumentException("Categoria de documento não catalogada pelo município.");
        }

        feiranteRepository.save(feirante);
    }
}