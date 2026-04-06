package com.suitecrm.email.engine;

import com.suitecrm.email.entity.InboundEmail;
import com.suitecrm.email.repository.InboundEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;
import jakarta.mail.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImapPollingService {

    private final InboundEmailRepository inboundEmailRepository;
    private final EmailImportService emailImportService;

    @Scheduled(fixedDelayString = "${email.polling.interval:60000}")
    public void pollInboundMailboxes() {
        List<InboundEmail> activeMailboxes = inboundEmailRepository.findByStatusAndDeletedFalse("Active");
        for (InboundEmail mailbox : activeMailboxes) {
            try {
                pollMailbox(mailbox);
            } catch (Exception e) {
                log.error("Failed to poll mailbox: {}", mailbox.getName(), e);
            }
        }
    }

    private void pollMailbox(InboundEmail config) throws MessagingException {
        Properties props = new Properties();
        String protocol = Boolean.TRUE.equals(config.getIsSsl()) ? "imaps" : "imap";
        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".host", config.getServerUrl());
        props.put("mail." + protocol + ".port", String.valueOf(config.getPort() != null ? config.getPort() : 993));

        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol);
        store.connect(config.getEmailUser(), config.getEmailPassword());

        String mailboxName = config.getMailbox() != null ? config.getMailbox() : "INBOX";
        Folder folder = store.getFolder(mailboxName);
        folder.open(Folder.READ_ONLY);

        Message[] messages = folder.getMessages();
        log.info("Found {} messages in mailbox {}", messages.length, config.getName());

        for (Message message : messages) {
            try {
                emailImportService.importMessage(message, config);
            } catch (Exception e) {
                log.error("Failed to import message", e);
            }
        }

        folder.close(false);
        store.close();
    }
}
