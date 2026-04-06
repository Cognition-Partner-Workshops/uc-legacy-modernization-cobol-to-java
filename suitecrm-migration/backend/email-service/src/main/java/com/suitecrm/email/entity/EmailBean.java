package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emails_beans", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailBean {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email_id", nullable = false) private UUID emailId;
    @Column(name = "bean_id", nullable = false) private UUID beanId;
    @Column(name = "bean_module", nullable = false, length = 100) private String beanModule;
    @Column(name = "campaign_data", columnDefinition = "TEXT") private String campaignData;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
