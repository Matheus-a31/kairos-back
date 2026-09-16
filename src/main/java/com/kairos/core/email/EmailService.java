package com.kairos.core.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${kairos.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${kairos.sendgrid.from-email:noreply@kairos.app}")
    private String fromEmail;

    @Value("${kairos.sendgrid.from-name:Kairos}")
    private String fromName;

    public void sendInvitationEmail(String toEmail, String projectName, String inviteLink) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            // Log only — don't fail if no API key is configured
            System.out.println("=================================================");
            System.out.println("[EMAIL] Convite para: " + toEmail);
            System.out.println("[EMAIL] Projeto: " + projectName);
            System.out.println("[EMAIL] Link: " + inviteLink);
            System.out.println("=================================================");
            return;
        }

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Você foi convidado para o projeto: " + projectName;

        String htmlBody = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: linear-gradient(135deg, #6366F1, #8B5CF6); padding: 32px; border-radius: 12px 12px 0 0;">
                <h1 style="color: white; margin: 0; font-size: 24px;">Kairos</h1>
              </div>
              <div style="background: #F8FAFC; padding: 32px; border-radius: 0 0 12px 12px; border: 1px solid #E2E8F0;">
                <h2 style="color: #1E293B;">Você foi convidado! 🎉</h2>
                <p style="color: #475569;">Você recebeu um convite para participar do projeto <strong>%s</strong> no Kairos.</p>
                <a href="%s" style="display: inline-block; background: #6366F1; color: white; padding: 14px 28px; border-radius: 8px; text-decoration: none; font-weight: bold; margin: 16px 0;">
                  Aceitar Convite
                </a>
                <p style="color: #94A3B8; font-size: 12px; margin-top: 24px;">
                  O link expira em 7 dias. Se você não esperava este convite, pode ignorar este email.
                </p>
              </div>
            </div>
            """.formatted(projectName, inviteLink);

        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("[EMAIL] Status: " + response.getStatusCode() + " para: " + toEmail);
        } catch (IOException e) {
            System.err.println("[EMAIL] Erro ao enviar email: " + e.getMessage());
        }
    }
}


