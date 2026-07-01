package pt.devoteam.identidade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.devoteam.identidade.dto.RegistoDTO;
import pt.devoteam.identidade.entity.Utilizador;
import pt.devoteam.identidade.repository.UtilizadorRepository;
import java.util.Optional;

@Service
public class UserService {
    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserService(UtilizadorRepository utilizadorRepository,
                       PasswordEncoder passwordEncoder,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       ObjectMapper objectMapper) {
        this.utilizadorRepository = utilizadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Regista qualquer um dos 6 perfis na tabela única global.
     */
    @Transactional
    public Utilizador registar(RegistoDTO dto) {
        if (utilizadorRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já se encontra registado no sistema.");
        }
        Utilizador utilizador = new Utilizador();
        utilizador.setEmail(dto.getEmail());
        utilizador.setPassword(passwordEncoder.encode(dto.getPassword()));
        utilizador.setRole(dto.getRole());
        utilizador.setAtivo(true);
        Utilizador utilizadorSalvo = utilizadorRepository.save(utilizador);
        try {
            String payloadJson = objectMapper.writeValueAsString(dto);
            if ("ROLE_FEIRANTE".equals(dto.getRole())) {
                kafkaTemplate.send("utilizador-registado", payloadJson);
                // O Kafka recebe uma String e o StringSerializer faz o trabalho sem reclamar
                kafkaTemplate.send("feirante-registado-topic", payloadJson);
                System.out.println("🟢 [Kafka] Evento de sincronização emitido com sucesso: " + payloadJson);
            } else if ("ROLE_MUNICIPIO".equalsIgnoreCase(dto.getRole())) {
                // 🎯 NOVO: Envia para o microsserviço de municípios
                kafkaTemplate.send("municipio-registado-topic", payloadJson);
                System.out.println("🟢 [Kafka] Evento de Município emitido para o topico institucional.");
            }
            return utilizadorSalvo;
        } catch (Exception e) {
            System.out.println("Erro: "+e.getMessage());
            throw new RuntimeException("Erro ao serializar dados do feirante para o Kafka", e);
        }
    }

    /**
     * Valida credenciais contra a base de dados única.
     */
    public Optional<Utilizador> autenticar(String email, String password) {
        return utilizadorRepository.findByEmail(email)
                .filter(Utilizador::isAtivo)
                .filter(utilizador -> passwordEncoder.matches(password, utilizador.getPassword()));
    }
}