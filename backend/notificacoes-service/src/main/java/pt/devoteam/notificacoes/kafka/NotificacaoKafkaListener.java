package pt.devoteam.notificacoes.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pt.devoteam.notificacoes.event.CandidaturaAprovadaEvent;
import pt.devoteam.notificacoes.event.PerfilAtivadoEvent;
import pt.devoteam.notificacoes.event.UtilizadorRegistadoEvent;
import pt.devoteam.notificacoes.service.EmailService;

@Component
public class NotificacaoKafkaListener {

    private final EmailService emailService;
    private final ObjectMapper objectMapper; // 🎯 Injetado para processar os JSONs de forma isolada

    public NotificacaoKafkaListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "utilizador-registado", groupId = "notificacao-group")
    public void ouvirRegistoInicial(String mensagemBruta) {
        try {
            System.out.println("🦅 Evento bruto recebido [Registo]: " + mensagemBruta);
            // Faz o mapeamento manual e seguro do texto para o Record Java
            UtilizadorRegistadoEvent evento = objectMapper.readValue(mensagemBruta, UtilizadorRegistadoEvent.class);
            emailService.enviarEmailBoasVindas(evento.email(), evento.nome(), evento.role());
        } catch (Exception e) {
            System.err.println("❌ Erro ao deserializar utilizador-registado: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "perfil-ativado", groupId = "notificacao-group")
    public void ouvirAtivacaoPerfil(String mensagemBruta) {
        try {
            System.out.println("🦅 Evento bruto recebido [Ativação]: " + mensagemBruta);
            PerfilAtivadoEvent evento = objectMapper.readValue(mensagemBruta, PerfilAtivadoEvent.class);
            emailService.enviarEmailPerfilAtivado(evento.email(), evento.nome());
        } catch (Exception e) {
            System.err.println("❌ Erro ao deserializar perfil-ativado: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "candidatura-aprovada", groupId = "notificacao-group")
    public void ouvirAprovacaoFeira(String mensagemBruta) {
        try {
            System.out.println("🦅 Evento bruto recebido [Aprovação]: " + mensagemBruta);
            CandidaturaAprovadaEvent evento = objectMapper.readValue(mensagemBruta, CandidaturaAprovadaEvent.class);
            emailService.enviarEmailCandidaturaAprovada(evento.emailFeirante(), evento.nomeFeirante(), evento.nomeMercado());
        } catch (Exception e) {
            System.err.println("❌ Erro ao deserializar candidatura-aprovada: " + e.getMessage());
        }
    }
}