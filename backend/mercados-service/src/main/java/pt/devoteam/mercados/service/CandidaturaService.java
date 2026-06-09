package pt.devoteam.mercados.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.entity.Candidatura;
import pt.devoteam.mercados.entity.enums.EstadoCandidatura;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.entity.enums.TipoDocumento;
import pt.devoteam.mercados.repository.CandidaturaRepository;
import pt.devoteam.mercados.repository.FeiranteRepository;
import pt.devoteam.mercados.repository.MercadoRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final MercadoRepository mercadoRepository;
    private final FeiranteRepository feiranteRepository;

    // Pasta local no servidor onde os PDFs vão ficar guardados
    private final String UPLOAD_DIR = "./uploads/candidaturas/";

    // 🎯 Repara que o RestClient desapareceu completamente daqui!
    public CandidaturaService(CandidaturaRepository candidaturaRepository,
                              MercadoRepository mercadoRepository,
                              FeiranteRepository feiranteRepository) {
        this.candidaturaRepository = candidaturaRepository;
        this.mercadoRepository = mercadoRepository;
        this.feiranteRepository = feiranteRepository;
    }

    @Transactional // A magia acontece aqui: Ou grava TUDO (pdf, vaga e candidatura) ou faz rollback de tudo!
    public void submeterCandidatura(Long mercadoId, String email, List<MultipartFile> pdfFiles) throws IOException {
        Mercado mercado = mercadoRepository.findById(mercadoId)
                .orElseThrow(() -> new RuntimeException("Mercado não encontrado"));
        if (mercado.getVagas() <= 0) {
            throw new RuntimeException("Lotação esgotada! Já não existem vagas disponíveis para esta feira.");
        }
        if (candidaturaRepository.existsByMercadoIdAndFeiranteEmail(mercadoId, email)) {
            throw new RuntimeException("Já existe uma candidatura para este feirante e mercado.");
        }
        Feirante feirante = feiranteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Feirante não registado no sistema"));
        // 2. Instancia a nova Candidatura
        Candidatura candidatura = new Candidatura();
        candidatura.setMercado(mercado);
        candidatura.setFeirante(feirante);
        if (pdfFiles != null) {
            Path pastaUpload = Paths.get(UPLOAD_DIR);
            if (!Files.exists(pastaUpload)) {
                Files.createDirectories(pastaUpload);
            }

            // 3. Salva os ficheiros fisicamente e preenche o mapa de tipos/paths
            for (MultipartFile arquivo : pdfFiles) {
                String nomeOriginal = arquivo.getOriginalFilename();
                if (nomeOriginal == null || nomeOriginal.isEmpty()) continue;

                String enumKey = nomeOriginal.replace(".pdf", "");
                TipoDocumento tipo = TipoDocumento.valueOf(enumKey);

                // 🎯 2. Usa a variável UPLOAD_DIR para construir o caminho final!
                String nomeFicheiroUnico = System.currentTimeMillis() + "_" + nomeOriginal;
                Path pathDestino = pastaUpload.resolve(nomeFicheiroUnico);

                // 🎯 3. Copia VERDADEIRAMENTE o PDF da memória RAM para o Disco Rígido
                Files.copy(arquivo.getInputStream(), pathDestino, StandardCopyOption.REPLACE_EXISTING);

                // Vincula o tipo ao path relativo dentro da entidade (ex: ./uploads/candidaturas/1234_INICIO_ACTIVIDADE.pdf)
                candidatura.getDocumentosAnexados().put(tipo, pathDestino.toString().replace("\\", "/"));
            }

            // 4. Valida se o feirante enviou tudo o que o mercado pedia antes de fechar a vaga
            for (TipoDocumento exigido : mercado.getDocumentosExigidos()) {
                if (!candidatura.getDocumentosAnexados().containsKey(exigido)) {
                    throw new RuntimeException("Dossiê incompleto! Falta o documento: " + exigido.name());
                }
            }
        }

        // 5. Decrementa a vaga no mercado de forma atómica e persiste a candidatura
        mercado.setVagas(mercado.getVagas() - 1);
        candidaturaRepository.save(candidatura);

        System.out.println("🟢 [Sucesso Transacional] Candidatura do feirante " + feirante.getNome() + " salva e vaga consumida instantaneamente no mercado " + mercado.getNome());
    }

    @Transactional(readOnly = true)
    public List<Long> obterMercadosInscritosPorFeirante(String email) {
        List<Candidatura> candidaturas = candidaturaRepository.findByFeiranteEmail(email);

        return candidaturas.stream()
                .map(candidatura -> candidatura.getMercado().getId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Candidatura> listarCandidaturasPorMercado(Long mercadoId) {
        return candidaturaRepository.findByMercadoId(mercadoId);
    }

    @Transactional
    public void atualizarEstadoCandidatura(Long candidaturaId, pt.devoteam.mercados.entity.enums.EstadoCandidatura novoEstado) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));
        candidatura.setEstado(novoEstado);
        if(EstadoCandidatura.REJEITADA == novoEstado){
            Mercado mercado = mercadoRepository.findById(candidatura.getMercado().getId())
                    .orElseThrow(() -> new RuntimeException("Mercado não encontrado."));
            mercado.setVagas(mercado.getVagas() + 1); // Reabre a vaga para outro feirante se esta candidatura for rejeitada
            mercadoRepository.save(mercado);
        }
        candidaturaRepository.save(candidatura);
    }

    @Transactional(readOnly = true)
    public Resource carregarDocumentoComoRecurso(Long candidaturaId, TipoDocumento tipoDocumento) throws IOException {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada"));

        String pathNoServidor = candidatura.getDocumentosAnexados().get(tipoDocumento);
        if (pathNoServidor == null) {
            throw new RuntimeException("Este documento não existe nesta candidatura.");
        }

        Path path = Paths.get(pathNoServidor);
        Resource recurso = new UrlResource(path.toUri());

        if (recurso.exists() || recurso.isReadable()) {
            return recurso;
        } else {
            throw new RuntimeException("O ficheiro físico não foi encontrado no disco do servidor.");
        }
    }
}