package com.suitecrm.lead.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads", schema = "lead_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 100)
    private String salutation;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String department;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "account_description", columnDefinition = "TEXT")
    private String accountDescription;

    @Column(name = "phone_work", length = 50)
    private String phoneWork;

    @Column(name = "phone_mobile", length = 50)
    private String phoneMobile;

    @Column(name = "phone_home", length = 50)
    private String phoneHome;

    @Column(name = "phone_other", length = 50)
    private String phoneOther;

    @Column(name = "phone_fax", length = 50)
    private String phoneFax;

    @Column(name = "email1", length = 255)
    private String primaryEmail;

    @Column(name = "email2", length = 255)
    private String alternateEmail;

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

    @Column(name = "alt_address_street", length = 255)
    private String altAddressStreet;

    @Column(name = "alt_address_city", length = 100)
    private String altAddressCity;

    @Column(name = "alt_address_state", length = 100)
    private String altAddressState;

    @Column(name = "alt_address_postalcode", length = 20)
    private String altAddressPostalcode;

    @Column(name = "alt_address_country", length = 100)
    private String altAddressCountry;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String status;

    @Column(name = "status_description", columnDefinition = "TEXT")
    private String statusDescription;

    @Column(name = "lead_source", length = 100)
    private String leadSource;

    @Column(name = "lead_source_description", columnDefinition = "TEXT")
    private String leadSourceDescription;

    @Column(length = 100)
    private String referedBy;

    @Column(length = 255)
    private String website;

    @Column(name = "do_not_call")
    @Builder.Default
    private Boolean doNotCall = false;

    @Column(name = "converted")
    @Builder.Default
    private Boolean converted = false;

    @Column(name = "converted_contact_id")
    private UUID convertedContactId;

    @Column(name = "converted_account_id")
    private UUID convertedAccountId;

    @Column(name = "converted_opportunity_id")
    private UUID convertedOpportunityId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
