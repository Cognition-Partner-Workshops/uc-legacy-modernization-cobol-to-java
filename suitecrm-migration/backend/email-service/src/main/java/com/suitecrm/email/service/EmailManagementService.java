package com.suitecrm.email.service;

import com.suitecrm.email.dto.*;
import com.suitecrm.email.entity.*;
import com.suitecrm.email.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailManagementService {

    private final EmailRepository emailRepository;
    private final EmailAttachmentRepository attachmentRepository;
    private final InboundEmailRepository inboundEmailRepository;
    private final OutboundEmailRepository outboundEmailRepository;
    private final EmailThreadRepository emailThreadRepository;
    private final JavaMailSender mailSender;

    public Page<EmailDto> listEmails(Pageable pageable) {
        return emailRepository.findByDeletedFalse(pageable).map(this::toEmailDto);
    }

    public Page<EmailDto> listEmailsByUser(UUID userId, Pageable pageable) {
        return emailRepository.findByAssignedUserIdAndDeletedFalse(userId, pageable).map(this::toEmailDto);
    }

    public Page<EmailDto> listEmailsByMailbox(UUID mailboxId, Pageable pageable) {
        return emailRepository.findByMailbox(mailboxId, pageable).map(this::toEmailDto);
    }

    public EmailDto getEmail(UUID id) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
        EmailDto dto = toEmailDto(email);
        List<EmailAttachment> attachments = attachmentRepository.findByEmailIdAndDeletedFalse(id);
        dto.setAttachments(attachments.stream().map(this::toAttachmentDto).collect(Collectors.toList()));
        return dto;
    }

    public EmailDto createEmail(EmailCreateRequest request, UUID userId) {
        Email email = Email.builder()
                .name(request.getName())
                .fromAddr(request.getFromAddr())
                .fromName(request.getFromName())
                .toAddrs(request.getToAddrs())
                .ccAddrs(request.getCcAddrs())
                .bccAddrs(request.getBccAddrs())
                .replyToAddr(request.getReplyToAddr())
                .description(request.getDescription())
                .descriptionHtml(request.getDescriptionHtml())
                .type(request.getType() != null ? request.getType() : "out")
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .intent(request.getIntent())
                .parentType(request.getParentType())
                .parentId(request.getParentId())
                .mailboxId(request.getMailboxId())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        return toEmailDto(emailRepository.save(email));
    }

    public EmailDto sendEmail(UUID id) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(email.getFromAddr());
            message.setTo(email.getToAddrs().split(","));
            if (email.getCcAddrs() != null && !email.getCcAddrs().isEmpty()) {
                message.setCc(email.getCcAddrs().split(","));
            }
            message.setSubject(email.getName());
            message.setText(email.getDescription());
            mailSender.send(message);
            email.setStatus("sent");
            email.setDateSent(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email: {}", id, e);
            email.setStatus("send_error");
        }
        return toEmailDto(emailRepository.save(email));
    }

    public void deleteEmail(UUID id) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
        email.setDeleted(true);
        emailRepository.save(email);
    }

    public Page<EmailDto> searchEmails(String query, Pageable pageable) {
        return emailRepository.search(query, pageable).map(this::toEmailDto);
    }

    public List<EmailDto> getFlaggedEmails(UUID userId) {
        return emailRepository.findFlaggedByUser(userId).stream().map(this::toEmailDto).collect(Collectors.toList());
    }

    public EmailDto toggleFlag(UUID id) {
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
        email.setFlagged(!email.getFlagged());
        return toEmailDto(emailRepository.save(email));
    }

    // Inbound Email management
    public List<InboundEmailDto> listInboundAccounts() {
        return inboundEmailRepository.findByDeletedFalseAndStatus("Active").stream()
                .map(this::toInboundDto).collect(Collectors.toList());
    }

    public InboundEmailDto getInboundAccount(UUID id) {
        InboundEmail ie = inboundEmailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inbound email not found: " + id));
        return toInboundDto(ie);
    }

    // Outbound Email management
    public List<OutboundEmailDto> listOutboundAccounts() {
        return outboundEmailRepository.findByDeletedFalseAndType("system").stream()
                .map(this::toOutboundDto).collect(Collectors.toList());
    }

    public List<OutboundEmailDto> getUserOutboundAccounts(UUID userId) {
        return outboundEmailRepository.findByUserIdAndDeletedFalse(userId).stream()
                .map(this::toOutboundDto).collect(Collectors.toList());
    }

    // Email statistics
    public long countByStatus(String status) {
        return emailRepository.countByStatus(status);
    }

    private EmailDto toEmailDto(Email email) {
        return EmailDto.builder()
                .id(email.getId())
                .name(email.getName())
                .fromAddr(email.getFromAddr())
                .fromName(email.getFromName())
                .toAddrs(email.getToAddrs())
                .ccAddrs(email.getCcAddrs())
                .bccAddrs(email.getBccAddrs())
                .replyToAddr(email.getReplyToAddr())
                .description(email.getDescription())
                .descriptionHtml(email.getDescriptionHtml())
                .type(email.getType())
                .status(email.getStatus())
                .intent(email.getIntent())
                .messageId(email.getMessageId())
                .parentType(email.getParentType())
                .parentId(email.getParentId())
                .dateSent(email.getDateSent())
                .flagged(email.getFlagged())
                .mailboxId(email.getMailboxId())
                .assignedUserId(email.getAssignedUserId())
                .dateEntered(email.getDateEntered())
                .dateModified(email.getDateModified())
                .build();
    }

    private EmailAttachmentDto toAttachmentDto(EmailAttachment attachment) {
        return EmailAttachmentDto.builder()
                .id(attachment.getId())
                .emailId(attachment.getEmailId())
                .filename(attachment.getFilename())
                .fileMimeType(attachment.getFileMimeType())
                .fileSize(attachment.getFileSize())
                .fileExt(attachment.getFileExt())
                .storageLocation(attachment.getStorageLocation())
                .build();
    }

    private InboundEmailDto toInboundDto(InboundEmail ie) {
        return InboundEmailDto.builder()
                .id(ie.getId())
                .name(ie.getName())
                .status(ie.getStatus())
                .serverUrl(ie.getServerUrl())
                .emailUser(ie.getEmailUser())
                .port(ie.getPort())
                .protocol(ie.getProtocol())
                .mailboxType(ie.getMailboxType())
                .service(ie.getService())
                .isPersonal(ie.getIsPersonal())
                .isSsl(ie.getIsSsl())
                .deleteSeen(ie.getDeleteSeen())
                .mailbox(ie.getMailbox())
                .groupId(ie.getGroupId())
                .dateEntered(ie.getDateEntered())
                .dateModified(ie.getDateModified())
                .build();
    }

    private OutboundEmailDto toOutboundDto(OutboundEmail oe) {
        return OutboundEmailDto.builder()
                .id(oe.getId())
                .name(oe.getName())
                .type(oe.getType())
                .mailSendType(oe.getMailSendType())
                .mailSmtpType(oe.getMailSmtpType())
                .mailSmtpServer(oe.getMailSmtpServer())
                .mailSmtpPort(oe.getMailSmtpPort())
                .mailSmtpUser(oe.getMailSmtpUser())
                .mailSmtpAuthReq(oe.getMailSmtpAuthReq())
                .mailSmtpSsl(oe.getMailSmtpSsl())
                .userId(oe.getUserId())
                .dateEntered(oe.getDateEntered())
                .dateModified(oe.getDateModified())
                .build();
    }
}
