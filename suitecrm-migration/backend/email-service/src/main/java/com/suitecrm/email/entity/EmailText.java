package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "emails_text", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailText {
    @Id @Column(name = "email_id", updatable = false, nullable = false)
    private UUID emailId;

    @Column(name = "from_addr", length = 500) private String fromAddr;
    @Column(name = "reply_to_addr", length = 500) private String replyToAddr;
    @Column(name = "to_addrs", columnDefinition = "TEXT") private String toAddrs;
    @Column(name = "cc_addrs", columnDefinition = "TEXT") private String ccAddrs;
    @Column(name = "bcc_addrs", columnDefinition = "TEXT") private String bccAddrs;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "description_html", columnDefinition = "TEXT") private String descriptionHtml;
    @Column(name = "raw_source", columnDefinition = "TEXT") private String rawSource;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
