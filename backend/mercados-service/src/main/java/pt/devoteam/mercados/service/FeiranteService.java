package pt.devoteam.mercados.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.CandidaturaDTO;
import pt.devoteam.mercados.dto.FeiranteDTO;
import pt.devoteam.mercados.dto.RegistoDTO;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.repository.CandidaturaRepository;
import pt.devoteam.mercados.repository.FeiranteRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeiranteService {
    private final FeiranteRepository feiranteRepository;
    private final CandidaturaRepository candidaturaRepository;

    public FeiranteService(FeiranteRepository feiranteRepository, CandidaturaRepository candidaturaRepository) {
        this.feiranteRepository = feiranteRepository;
        this.candidaturaRepository = candidaturaRepository;
    }

    @Transactional(readOnly = true)
    public Feirante obterFeirantePorId(Long id) {
        return feiranteRepository.findById(id).orElseThrow(() -> new RuntimeException("Feirante não encontrado"));
    }

    /**
     * Sincroniza o feirante na base de dados local de forma segura e transacional.
     */
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

    @Transactional
    public void guardarDocumentoNoPortfolio(String email, String tipoDocumento, MultipartFile file) throws IOException {
        Feirante feirante = feiranteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador feirante não localizado para o anexo."));
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro enviado encontra-se vazio.");
        }
        // Cria a pasta física se ela não existir no servidor
        String UPLOAD_DIR = "./uploads/portfolio/";
        File diretorio = new File(UPLOAD_DIR);
        if (!diretorio.exists()) {
            if (!diretorio.mkdirs()) {
                throw new IOException("Erro ao criar a pasta portfolio.");
            }
        }
        // Define um nome de ficheiro único para evitar sobreposições (ex: feirante_id_INICIO_ACTIVIDADE.pdf)
        String nomeFicheiro = "feirante_" + feirante.getId() + "_" + tipoDocumento + ".pdf";
        Path caminhoCompleto = Paths.get(UPLOAD_DIR, nomeFicheiro);
        // Escreve os bytes do PDF recebido para o disco rígido do servidor
        Files.write(caminhoCompleto, file.getBytes());
        // Vincula o caminho do ficheiro ao campo correto da entidade correspondente
        if ("INICIO_ACTIVIDADE".equals(tipoDocumento)) {
            feirante.setDocumentoAtividade(caminhoCompleto.toString());
        } else if ("NAO_DIVIDA_AT".equals(tipoDocumento)) {
            feirante.setDocumentoFinancas(caminhoCompleto.toString());
        } else {
            throw new IllegalArgumentException("Categoria de documento não catalogada pelo município.");
        }
        feiranteRepository.save(feirante);
    }
}