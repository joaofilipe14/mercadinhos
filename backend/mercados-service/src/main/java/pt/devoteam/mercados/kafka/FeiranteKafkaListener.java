package pt.devoteam.mercados.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.devoteam.mercados.dto.RegistoDTO;
import pt.devoteam.mercados.service.FeiranteService;
import java.util.Map;

@Component
public class FeiranteKafkaListener {
    private final FeiranteService feiranteService;
    private final ObjectMapper objectMapper;

    // Injetamos o Service em vez do Repository!
    public FeiranteKafkaListener(FeiranteService feiranteService,
                                 ObjectMapper objectMapper) {
        this.feiranteService = feiranteService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "feirante-registado-topic", groupId = "mercados-group")
    public void consumirFeiranteRegistado(String payloadJson) {
        try {
            System.out.println("📥 [Kafka] Novo evento de Feirante intercetado: " + payloadJson);

            RegistoDTO dto = objectMapper.readValue(payloadJson, RegistoDTO.class);

            // O Listener apenas delega para a camada correta
            feiranteService.criarFeirante(dto);

        } catch (Exception e) {
            System.err.println("🔴 [Kafka Erro] Falha ao processar o payload do Kafka: " + e.getMessage());
        }
    }
}