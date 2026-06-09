package pt.devoteam.notificacoes.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String emailOrigem = "nao-responder@cm-loures.pt";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void enviarHtmlEmail(String para, String assunto, String corpoHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailOrigem);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true); // true = processa como HTML
            mailSender.send(message);
            System.out.println("📬 E-mail enviado com sucesso para o Mailhog: " + para);
        } catch (Exception e) {
            System.err.println("❌ Falha crítica ao enviar e-mail para " + para + ": " + e.getMessage());
        }
    }

    // 🎯 E-MAIL 1: Registo de Boas-Vindas
    public void enviarEmailBoasVindas(String email, String nome, String role) {
        String template = """
            <div style='font-family: sans-serif; color: #334155; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;'>
                <div style='background: #4f46e5; padding: 24px; text-align: center; color: white;'>
                    <h2 style='margin: 0;'>🎪 Bem-vindo à Rede de Mercados</h2>
                </div>
                <div style='padding: 24px;'>
                    <p>Olá <b>%s</b>,</p>
                    <p>A sua conta de acesso à plataforma regulamentar de mercados municipais foi criada com sucesso como <b>%s</b>.</p>
                    <p>A sua conta encontra-se atualmente a aguardar a ativação do perfil de feirante.</p>
                </div>
                <div style='background: #f8fafc; padding: 16px; text-align: center; font-size: 11px; color: #64748b;'>
                    © Câmara Municipal de Loures - Gestão de Ecossistemas Comerciais
                </div>
            </div>
            """.formatted(nome, role.replace("ROLE_", ""));
        enviarHtmlEmail(email, "🔑 Registo Efetuado com Sucesso!", template);
    }

    // 🎯 E-MAIL 2: Confirmação de Ativação (Kafka Mirror)
    public void enviarEmailPerfilAtivado(String email, String nome) {
        String template = """
            <div style='font-family: sans-serif; color: #334155; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;'>
                <div style='background: #0ea5e9; padding: 24px; text-align: center; color: white;'>
                    <h2 style='margin: 0;'>✅ Perfil Digital Ativado</h2>
                </div>
                <div style='padding: 24px;'>
                    <p>Olá <b>%s</b>,</p>
                    <p>Excelentes notícias! O seu perfil profissional foi espelhado com sucesso e a sua <b>Pasta Digital de Feirante está ativa</b>.</p>
                    <p>Já pode navegar no mapa interativo e efetuar a sua candidatura eletrónica a qualquer feira municipal disponível.</p>
                </div>
            </div>
            """.formatted(nome);
        enviarHtmlEmail(email, "⚡ O seu Perfil de Feirante está Ativo!", template);
    }

    // 🎯 E-MAIL 3: Parabéns, tens lugar na feira!
    public void enviarEmailCandidaturaAprovada(String email, String nome, String nomeMercado) {
        String template = """
            <div style='font-family: sans-serif; color: #334155; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden;'>
                <div style='background: #10b981; padding: 24px; text-align: center; color: white;'>
                    <h2 style='margin: 0;'>🎉 Candidatura Aprovada!</h2>
                </div>
                <div style='padding: 24px;'>
                    <p>Olá <b>%s</b>,</p>
                    <p>A Câmara Municipal de Loures terminou a avaliação técnica e jurídica do seu dossiê digital.</p>
                    <p style='font-size: 16px; color: #10b981; font-weight: bold;'>Parabéns, tem lugar assegurado na feira: %s!</p>
                    <p>Por favor, aceda ao seu painel privado no portal para consultar a sua banca atribuída e emitir as respetivas guias de liquidação de taxas.</p>
                </div>
            </div>
            """.formatted(nome, nomeMercado);
        enviarHtmlEmail(email, "🎪 Parabéns, tem lugar na feira: " + nomeMercado, template);
    }
}