package pt.devoteam.pagamentos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.pagamentos.dto.ProcessarPagamentoDTO;
import pt.devoteam.pagamentos.entity.Transacao;
import pt.devoteam.pagamentos.event.PagamentoConcluidoEvent;
import pt.devoteam.pagamentos.repository.TransacaoRepository;

import java.util.UUID;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.transacaoRepository = transacaoRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String registarEProcessarPagamento(ProcessarPagamentoDTO dto) {

        // 1. 🛡️ FILTRO DE IDEMPOTÊNCIA (Segurança Máxima)
        // Se já existir um registo "PAGO" para este ID de candidatura, bloqueia imediatamente.
        if (transacaoRepository.existsByCandidaturaIdAndEstado(dto.candidaturaId(), "PAGO")) {
            throw new IllegalStateException("Esta guia de liquidação já foi paga com sucesso.");
        }
        String tokenRecibo = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. PERSISTÊNCIA DE AUDITORIA (Grava na BD local 'db_pagamentos')
        Transacao transacao = new Transacao();
        transacao.setCandidaturaId(dto.candidaturaId());
        transacao.setValor(dto.valor());
        transacao.setEmailFeirante(dto.emailFeirante());
        transacao.setTransacaoId(tokenRecibo);
        transacao.setEstado("PAGO");

        transacaoRepository.save(transacao);
        System.out.println("💾 [db_pagamentos] Auditoria guardada com sucesso para a Candidatura #" + dto.candidaturaId());

        // 4. COREOGRAFIA DA SAGA (Notifica o ecossistema via Kafka)
        try {
            PagamentoConcluidoEvent eventoFim = new PagamentoConcluidoEvent(
                    dto.candidaturaId(),
                    tokenRecibo,
                    "PAGO"
            );

            String jsonPayload = objectMapper.writeValueAsString(eventoFim);
            kafkaTemplate.send("pagamento-concluido", jsonPayload);

            System.out.println("📣 [Kafka] Evento 'pagamento-concluido' despachado com sucesso.");

        } catch (Exception e) {
            // Fazemos rollback da transação na BD caso o Kafka falhe, mantendo a integridade!
            throw new RuntimeException("Erro ao propagar o fim da Saga para o broker: " + e.getMessage());
        }

        return tokenRecibo;
    }
}