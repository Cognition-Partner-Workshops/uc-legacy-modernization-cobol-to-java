package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_addr_bean_rel", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailAddressBean {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email_address_id", nullable = false) private UUID emailAddressId;
    @Column(name = "bean_id", nullable = false) private UUID beanId;
    @Column(name = "bean_module", nullable = false, length = 100) private String beanModule;
    @Column(name = "primary_address") @Builder.Default private Boolean primaryAddress = false;
    @Column(name = "reply_to_address") @Builder.Default private Boolean replyToAddress = false;
    @Column(name = "date_created", nullable = false, updatable = false) private LocalDateTime dateCreated;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateCreated = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
