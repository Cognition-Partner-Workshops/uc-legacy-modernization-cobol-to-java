package com.suitecrm.email.engine;

import com.suitecrm.email.entity.Email;
import com.suitecrm.email.entity.EmailText;
import com.suitecrm.email.entity.InboundEmail;
import com.suitecrm.email.repository.EmailRepository;
import com.suitecrm.email.repository.EmailTextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailImportService {

    private final EmailRepository emailRepository;
    private final EmailTextRepository emailTextRepository;

    @Transactional
    public void importMessage(Message message, InboundEmail config) throws Exception {
        MimeMessage mimeMessage = (MimeMessage) message;

        Email email = Email.builder()
            .name(message.getSubject())
            .subject(message.getSubject())
            .type("inbound")
            .status("unread")
            .messageId(mimeMessage.getMessageID())
            .fromAddrName(formatAddresses(message.getFrom()))
            .toAddrsNames(formatAddresses(message.getRecipients(Message.RecipientType.TO)))
            .ccAddrsNames(formatAddresses(message.getRecipients(Message.RecipientType.CC)))
            .bccAddrsNames(formatAddresses(message.getRecipients(Message.RecipientType.BCC)))
            .dateSentReceived(message.getSentDate() != null
                ? LocalDateTime.ofInstant(message.getSentDate().toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now())
            .isImported(true)
            .mailboxId(config.getId())
            .folder("INBOX")
            .folderType("inbound")
            .build();

        email = emailRepository.save(email);

        String bodyText = getTextContent(message);
        String bodyHtml = getHtmlContent(message);

        EmailText emailText = EmailText.builder()
            .emailId(email.getId())
            .fromAddr(formatAddresses(message.getFrom()))
            .replyToAddr(formatAddresses(message.getReplyTo()))
            .toAddrs(formatAddresses(message.getRecipients(Message.RecipientType.TO)))
            .ccAddrs(formatAddresses(message.getRecipients(Message.RecipientType.CC)))
            .description(bodyText)
            .descriptionHtml(bodyHtml)
            .build();

        emailTextRepository.save(emailText);
        log.info("Imported email: {} ({})", email.getSubject(), email.getId());
    }

    private String formatAddresses(Address[] addresses) {
        if (addresses == null) return "";
        return Arrays.stream(addresses)
            .map(a -> a instanceof InternetAddress ia ? ia.toUnicodeString() : a.toString())
            .collect(Collectors.joining(", "));
    }

    private String getTextContent(Message message) throws Exception {
        if (message.isMimeType("text/plain")) return (String) message.getContent();
        if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) return (String) bp.getContent();
            }
        }
        return "";
    }

    private String getHtmlContent(Message message) throws Exception {
        if (message.isMimeType("text/html")) return (String) message.getContent();
        if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/html")) return (String) bp.getContent();
            }
        }
        return "";
    }
}
