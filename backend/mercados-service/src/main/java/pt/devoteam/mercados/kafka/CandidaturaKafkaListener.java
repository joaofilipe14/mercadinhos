package pt.devoteam.mercados.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.devoteam.mercados.service.CandidaturaService;
import pt.devoteam.mercados.event.PagamentoConcluidoEvent;

@Component
public class CandidaturaKafkaListener {
    private final CandidaturaService candidaturaService;
    private final ObjectMapper objectMapper;

    public CandidaturaKafkaListener(CandidaturaService candidaturaService,
                                 ObjectMapper objectMapper) {
        this.candidaturaService = candidaturaService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "pagamento-concluido", groupId = "mercados-group")
    public void finalizarSagaCandidatura(String mensagemBruta) {
        try {
            System.out.println("📥 [Kafka - Saga] Resposta da Gateway de Pagamento intercetada: " + mensagemBruta);
            PagamentoConcluidoEvent resposta = objectMapper.readValue(mensagemBruta, PagamentoConcluidoEvent.class);

            candidaturaService.confirmarPagamentoCandidatura(resposta.candidaturaId(), resposta.transacaoId());
            System.out.println("🏆 [Saga Concluída] Candidatura #" + resposta.candidaturaId() + " atualizada para CONFIRMADO!");
        } catch (Exception e) {
            System.err.println("🔴 [Kafka Erro - Saga] Falha no fecho da transação distribuída: " + e.getMessage());
        }
    }
}
