package com.suitecrm.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_addresses", schema = "contact_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAddress {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email_address", nullable = false, length = 255)
    private String emailAddress;

    @Column(name = "email_address_caps", length = 255)
    private String emailAddressCaps;

    @Column(name = "invalid_email")
    @Builder.Default
    private Boolean invalidEmail = false;

    @Column(name = "opt_out")
    @Builder.Default
    private Boolean optOut = false;

    @Column(name = "confirm_opt_in", length = 50)
    private String confirmOptIn;

    @Column(name = "confirm_opt_in_date")
    private LocalDateTime confirmOptInDate;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (emailAddressCaps == null && emailAddress != null) {
            emailAddressCaps = emailAddress.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
