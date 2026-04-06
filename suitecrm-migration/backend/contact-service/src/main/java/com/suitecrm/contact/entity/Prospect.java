package com.suitecrm.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prospects", schema = "contact_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prospect {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "salutation", length = 25)
    private String salutation;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "phone_work", length = 50)
    private String phoneWork;

    @Column(name = "phone_mobile", length = 50)
    private String phoneMobile;

    @Column(name = "phone_home", length = 50)
    private String phoneHome;

    @Column(name = "phone_fax", length = 50)
    private String phoneFax;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "primary_address_street", length = 255)
    private String primaryAddressStreet;

    @Column(name = "primary_address_city", length = 100)
    private String primaryAddressCity;

    @Column(name = "primary_address_state", length = 100)
    private String primaryAddressState;

    @Column(name = "primary_address_postalcode", length = 20)
    private String primaryAddressPostalcode;

    @Column(name = "primary_address_country", length = 100)
    private String primaryAddressCountry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "tracker_key", length = 36)
    private String trackerKey;

    @Column(name = "do_not_call", length = 3)
    @Builder.Default
    private String doNotCall = "off";

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "created_by")
    private UUID createdBy;

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
        if (trackerKey == null) trackerKey = UUID.randomUUID().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
