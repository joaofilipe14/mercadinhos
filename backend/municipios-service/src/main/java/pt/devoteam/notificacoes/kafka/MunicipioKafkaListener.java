package pt.devoteam.notificacoes.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.devoteam.notificacoes.entity.Municipio;
import pt.devoteam.notificacoes.service.MunicipioService;
import java.util.Map;

@Component
public class MunicipioKafkaListener {

    private final MunicipioService municipioService;
    private final ObjectMapper objectMapper;

    public MunicipioKafkaListener(MunicipioService municipioService, ObjectMapper objectMapper) {
        this.municipioService = municipioService;
        this.objectMapper = objectMapper;
    }

    /**
     * 📥 Interceta a criação de contas autárquicas vindas do identidade-service.
     * Recebe o payload como String JSON pura, evitando problemas de serialização.
     */
    @KafkaListener(topics = "municipio-registado-topic", groupId = "municipios-group")
    public void consumirMunicipioRegistado(String payloadJson) {
        try {
            System.out.println("📥 [Kafka] Novo evento de Município intercetado: " + payloadJson);

            // Ler o JSON como um mapa genérico para extrair as propriedades base
            Map<?, ?> dados = objectMapper.readValue(payloadJson, Map.class);

            String email = (String) dados.get("email");
            String nome = (String) dados.get("nome");

            // Converter e delegar para a camada de serviço salvar na tabela especifica
            Municipio novoMunicipio = new Municipio();
            novoMunicipio.setEmail(email);
            novoMunicipio.setNomeCamara(nome != null ? nome : "Câmara Municipal por Classificar");

            municipioService.atualizarPerfil(novoMunicipio);
            System.out.println("🟢 [Kafka] Perfil base da autarquia espelhado com sucesso: " + email);

        } catch (Exception e) {
            // Tratamento de exceção local para evitar loops de Poison Pill na fila
            System.err.println("🔴 [Kafka Erro] Falha ao processar o payload do Município: " + e.getMessage());
        }
    }
}