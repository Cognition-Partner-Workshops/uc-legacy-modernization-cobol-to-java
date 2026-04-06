package com.suitecrm.lead.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads", schema = "lead_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Lead {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 100) private String salutation;
    @Column(name = "first_name", length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(name = "full_name", length = 255) private String fullName;
    @Column(length = 100) private String title;
    @Column(length = 100) private String department;
    @Column(name = "do_not_call") @Builder.Default private Boolean doNotCall = false;
    @Column(name = "phone_home", length = 50) private String phoneHome;
    @Column(name = "phone_mobile", length = 50) private String phoneMobile;
    @Column(name = "phone_work", length = 50) private String phoneWork;
    @Column(name = "phone_other", length = 50) private String phoneOther;
    @Column(name = "phone_fax", length = 50) private String phoneFax;
    @Column(name = "email1", length = 255) private String email1;
    @Column(name = "email2", length = 255) private String email2;
    @Column(name = "email_opt_out") @Builder.Default private Boolean emailOptOut = false;
    @Column(name = "invalid_email") @Builder.Default private Boolean invalidEmail = false;
    @Column(name = "webtolead_email1", length = 255) private String webToLeadEmail1;
    @Column(name = "webtolead_email2", length = 255) private String webToLeadEmail2;
    @Column(name = "webtolead_email_opt_out", length = 255) private String webToLeadEmailOptOut;
    @Column(name = "webtolead_invalid_email", length = 255) private String webToLeadInvalidEmail;
    @Column(name = "primary_address_street", length = 255) private String primaryAddressStreet;
    @Column(name = "primary_address_city", length = 100) private String primaryAddressCity;
    @Column(name = "primary_address_state", length = 100) private String primaryAddressState;
    @Column(name = "primary_address_postalcode", length = 20) private String primaryAddressPostalcode;
    @Column(name = "primary_address_country", length = 100) private String primaryAddressCountry;
    @Column(name = "alt_address_street", length = 255) private String altAddressStreet;
    @Column(name = "alt_address_city", length = 100) private String altAddressCity;
    @Column(name = "alt_address_state", length = 100) private String altAddressState;
    @Column(name = "alt_address_postalcode", length = 20) private String altAddressPostalcode;
    @Column(name = "alt_address_country", length = 100) private String altAddressCountry;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "status_description", columnDefinition = "TEXT") private String statusDescription;
    @Column(name = "lead_source", length = 100) private String leadSource;
    @Column(name = "lead_source_description", columnDefinition = "TEXT") private String leadSourceDescription;
    @Column(name = "account_name", length = 255) private String accountName;
    @Column(name = "account_description", columnDefinition = "TEXT") private String accountDescription;
    @Column(name = "account_id") private UUID accountId;
    @Column(name = "contact_id") private UUID contactId;
    @Column(name = "opportunity_id") private UUID opportunityId;
    @Column(name = "opportunity_name", length = 255) private String opportunityName;
    @Column(name = "opportunity_amount", length = 50) private String opportunityAmount;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "campaign_name", length = 255) private String campaignName;
    @Column(name = "referred_by", length = 255) private String referredBy;
    @Column(name = "reports_to_id") private UUID reportsToId;
    @Column(name = "website", length = 255) private String website;
    @Column(name = "birthdate") private LocalDate birthdate;
    @Column(name = "portal_name", length = 255) private String portalName;
    @Column(name = "portal_app", length = 255) private String portalApp;
    @Column(name = "converted") @Builder.Default private Boolean converted = false;
    @Column(name = "converted_date") private LocalDate convertedDate;
    @Column(name = "lawful_basis", length = 100) private String lawfulBasis;
    @Column(name = "date_reviewed") private LocalDate dateReviewed;
    @Column(name = "lawful_basis_source", length = 100) private String lawfulBasisSource;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
