package com.suitecrm.email.engine;

import com.suitecrm.email.entity.OutboundEmail;
import com.suitecrm.email.repository.OutboundEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpSendingService {

    private final OutboundEmailRepository outboundEmailRepository;

    public void sendEmail(UUID outboundConfigId, String to, String cc, String bcc,
                          String subject, String bodyHtml, String bodyText) throws MessagingException {
        OutboundEmail config = outboundEmailRepository.findById(outboundConfigId)
            .orElseThrow(() -> new RuntimeException("Outbound email config not found: " + outboundConfigId));

        Properties props = new Properties();
        props.put("mail.smtp.host", config.getMailSmtpserver());
        props.put("mail.smtp.port", String.valueOf(config.getMailSmtpport()));
        props.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(config.getMailSmtpauthReq())));
        if (Boolean.TRUE.equals(config.getMailSmtptls())) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (Boolean.TRUE.equals(config.getMailSmtpssl())) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getMailSmtpuser(), config.getMailSmtppass());
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getMailSmtpuser()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        if (cc != null && !cc.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        }
        if (bcc != null && !bcc.isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
        }
        message.setSubject(subject);

        if (bodyHtml != null && !bodyHtml.isEmpty()) {
            MimeMultipart multipart = new MimeMultipart("alternative");
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(bodyText != null ? bodyText : "", "utf-8");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(bodyHtml, "text/html; charset=utf-8");
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);
        } else {
            message.setText(bodyText != null ? bodyText : "");
        }

        Transport.send(message);
        log.info("Email sent to {} via {}", to, config.getName());
    }
}
