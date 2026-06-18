package pt.devoteam.pagamentos.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void enviarHtmlEmail(String para, String assunto, String corpoHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String emailOrigem = "nao-responder@cm-loures.pt";
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

    public void enviarEmailCandidaturaAprovada(String email, String nomeFeirante, String nomeMercado, Long candidaturaId, Double valor) {
        String linkUI = "http://localhost:4200/painel/pagamentos?id=" + candidaturaId;

        String templateHtml = """
            <div style='font-family: sans-serif; color: #334155; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);'>
                <div style='background: #eab308; padding: 24px; text-align: center; color: white;'>
                    <h2 style='margin: 0; font-size: 20px;'>🪙 Taxas de Ocupação Emitidas</h2>
                </div>
                <div style='padding: 24px; line-height: 1.6; font-size: 14px;'>
                    <p>Olá <b>%s</b>,</p>
                    <p>A organização do evento <b>%s</b> validou com sucesso o seu dossiê documental digital.</p>
                    <p>Para trancar em definitivo a reserva do seu espaço no recinto público, foi emitida a respetiva taxa de licenciamento municipal no valor de:</p>
                   \s
                    <div style='background: #f8fafc; border: 1px solid #f1f5f9; padding: 16px; text-align: center; border-radius: 12px; margin: 20px 0;'>
                        <span style='font-size: 24px; font-weight: 900; color: #1e293b;'>%.2f €</span>
                    </div>

                    <p style='text-align: center; margin: 30px 0;'>
                        <a href='%s' style='background: #eab308; color: white; padding: 14px 28px; text-decoration: none; font-weight: bold; border-radius: 10px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(234,179,8,0.3); font-size: 13px; text-transform: uppercase; tracking-wider;'>
                            💳 Proceder ao Pagamento Seguro
                        </a>
                    </p>
                   \s
                    <p style='font-size: 11px; color: #94a3b8; margin-top: 30px;'>
                        Caso não consiga clicar no botão, copie o seguinte endereço para o seu navegador:<br>
                        <a href='%s' style='color: #2563eb;'>%s</a>
                    </p>
                </div>
                <div style='background: #f8fafc; padding: 16px; text-align: center; font-size: 11px; color: #64748b; border-top: 1px solid #f1f5f9;'>
                    Serviço de Notificações Eletrónicas Autárquicas • Devoteam Smart Cities
                </div>
            </div>
           \s""".formatted(nomeFeirante, nomeMercado, valor, linkUI, linkUI, linkUI);
        enviarHtmlEmail(email, "💳 Taxas Emitidas: " + nomeMercado, templateHtml);
    }
}