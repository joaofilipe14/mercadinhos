package pt.devoteam.mercados.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.devoteam.mercados.dto.CandidaturaDTO;
import pt.devoteam.mercados.entity.Candidatura;
import pt.devoteam.mercados.entity.enums.EstadoCandidatura;
import pt.devoteam.mercados.entity.Feirante;
import pt.devoteam.mercados.entity.Mercado;
import pt.devoteam.mercados.entity.enums.TipoDocumento;
import pt.devoteam.mercados.entity.enums.TipoInfraestrutura;
import pt.devoteam.mercados.entity.enums.TipoPreco;
import pt.devoteam.mercados.event.CandidaturaAprovadaEvent;
import pt.devoteam.mercados.repository.CandidaturaRepository;
import pt.devoteam.mercados.repository.FeiranteRepository;
import pt.devoteam.mercados.repository.MercadoRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CandidaturaService {
    private final CandidaturaRepository candidaturaRepository;
    private final MercadoRepository mercadoRepository;
    private final FeiranteRepository feiranteRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CandidaturaService(CandidaturaRepository candidaturaRepository,
                              MercadoRepository mercadoRepository,
                              FeiranteRepository feiranteRepository,
                              KafkaTemplate<String, String> kafkaTemplate,
                              ObjectMapper objectMapper) {
        this.candidaturaRepository = candidaturaRepository;
        this.mercadoRepository = mercadoRepository;
        this.feiranteRepository = feiranteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Candidatura obterCandidaturaPorId(Long id) {
        return candidaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada com o ID: " + id));
    }

    @Transactional
    public void submeterCandidatura(CandidaturaDTO dto) throws IOException {
        Mercado mercado = mercadoRepository.findById(dto.getMercadoId())
                .orElseThrow(() -> new RuntimeException("Mercado não encontrado"));
        if (mercado.getVagas() <= 0) {
            throw new RuntimeException("Lotação esgotada! Já não existem vagas disponíveis para esta feira.");
        }
        if (candidaturaRepository.existsByMercadoIdAndFeiranteEmail(dto.getMercadoId(), dto.getFeiranteEmail())) {
            throw new RuntimeException("Já existe uma candidatura para este feirante e mercado.");
        }
        Feirante feirante = feiranteRepository.findByEmail(dto.getFeiranteEmail())
                .orElseThrow(() -> new RuntimeException("Feirante não registado no sistema"));
        // 2. Instancia a nova Candidatura
        Candidatura candidatura = new Candidatura();
        candidatura.setMercado(mercado);
        candidatura.setFeirante(feirante);
        candidatura.setOpcaoInfraestrutura(dto.getOpcaoInfraestrutura());
        candidatura.setDias(dto.getDias());
        double valorCalculado = 0;
        if (TipoPreco.EVENTO == mercado.getTipoPreco()) {
            if(TipoInfraestrutura.PROPRIO == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoArtesanatoStandProprio();
            } else if (TipoInfraestrutura.ORGANIZACAO == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoArtesanatoStandOrganizacao();
            } else if (TipoInfraestrutura.STREET_FOOD == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoStreetFoodStandProprio();
            }
        } else {
            long dias = ChronoUnit.DAYS.between(
                    mercado.getDataInicio(),
                    mercado.getDataFim()
            ) + 1;
            if (dias <= 0) dias = 1; // Salvaguarda preventiva de segurança
            if(TipoInfraestrutura.PROPRIO == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoArtesanatoStandProprio() * dias;
            } else if (TipoInfraestrutura.ORGANIZACAO == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoArtesanatoStandOrganizacao() * dias;
            } else if (TipoInfraestrutura.STREET_FOOD == candidatura.getOpcaoInfraestrutura()) {
                valorCalculado = mercado.getPrecoStreetFoodStandProprio() * dias;
            }
        }
        candidatura.setPrecoTotal(valorCalculado);
        if (dto.getPdfFiles() != null) {
            // Pasta local no servidor onde os PDFs vão ficar guardados
            String UPLOAD_DIR = "./uploads/candidaturas/";
            Path pastaUpload = Paths.get(UPLOAD_DIR);
            if (!Files.exists(pastaUpload)) {
                Files.createDirectories(pastaUpload);
            }
            for (MultipartFile arquivo : dto.getPdfFiles()) {
                String nomeOriginal = arquivo.getOriginalFilename();
                if (nomeOriginal == null || nomeOriginal.isEmpty()) continue;

                String enumKey = nomeOriginal.replace(".pdf", "");
                TipoDocumento tipo = TipoDocumento.valueOf(enumKey);
                String nomeFicheiroUnico = System.currentTimeMillis() + "_" + nomeOriginal;
                Path pathDestino = pastaUpload.resolve(nomeFicheiroUnico);
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
    public void atualizarEstadoCandidatura(Long candidaturaId, EstadoCandidatura novoEstado) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));
        candidatura.setEstado(novoEstado);
        if(EstadoCandidatura.REJEITADA == novoEstado){
            Mercado mercado = mercadoRepository.findById(candidatura.getMercado().getId())
                    .orElseThrow(() -> new RuntimeException("Mercado não encontrado."));
            mercado.setVagas(mercado.getVagas() + 1); // Reabre a vaga para outro feirante se esta candidatura for rejeitada
            mercadoRepository.save(mercado);
        }else if (EstadoCandidatura.A_AGUARDAR_PAGAMENTO == novoEstado) {
            try {
                CandidaturaAprovadaEvent eventoSaga = new CandidaturaAprovadaEvent(
                        candidatura.getId(), candidatura.getFeirante().getEmail(), candidatura.getFeirante().getNome(),
                        candidatura.getMercado().getNome(), candidatura.getPrecoTotal());
                String jsonPayload = objectMapper.writeValueAsString(eventoSaga);
                kafkaTemplate.send("candidatura-aprovada", jsonPayload);
            } catch (Exception e) {
                System.err.println("🔴 [Kafka Erro] Falha crítica ao faturar candidatura: " + e.getMessage());
            }
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

    @Transactional
    public void confirmarPagamentoCandidatura(Long candidaturaId, String transacaoId) {
        Candidatura candidatura = candidaturaRepository.findById(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada com o ID: " + candidaturaId));
        candidatura.setEstado(EstadoCandidatura.APROVADA);
        candidaturaRepository.save(candidatura);
        System.out.println("🏆 [BD Transacional] Candidatura #" + candidaturaId
                + " liquidada com sucesso! Recibo: " + transacaoId + ". Estado alterado para APROVADA.");
    }
}