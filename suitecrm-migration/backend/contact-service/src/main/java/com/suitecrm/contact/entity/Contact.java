package com.suitecrm.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contacts", schema = "contact_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contact {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "salutation", length = 25) private String salutation;
    @Column(name = "first_name", length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(name = "full_name", length = 255) private String fullName;
    @Column(name = "title", length = 100) private String title;
    @Column(name = "department", length = 100) private String department;
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
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "account_id") private UUID accountId;
    @Column(name = "account_name", length = 255) private String accountName;
    @Column(name = "reports_to_id") private UUID reportsToId;
    @Column(name = "reports_to_name", length = 255) private String reportsToName;
    @Column(name = "assistant", length = 100) private String assistant;
    @Column(name = "assistant_phone", length = 50) private String assistantPhone;
    @Column(name = "birthdate") private LocalDate birthdate;
    @Column(name = "lead_source", length = 100) private String leadSource;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "campaign_name", length = 255) private String campaignName;
    @Column(name = "portal_name", length = 255) private String portalName;
    @Column(name = "portal_active") @Builder.Default private Boolean portalActive = false;
    @Column(name = "portal_password", length = 255) private String portalPassword;
    @Column(name = "portal_app", length = 255) private String portalApp;
    @Column(name = "portal_user_type", length = 50) private String portalUserType;
    @Column(name = "portal_account_disabled") @Builder.Default private Boolean portalAccountDisabled = false;
    @Column(name = "preferred_language", length = 20) private String preferredLanguage;
    @Column(name = "sync_contact") @Builder.Default private Boolean syncContact = false;
    @Column(name = "photo", length = 255) private String photo;
    @Column(name = "lawful_basis", length = 100) private String lawfulBasis;
    @Column(name = "date_reviewed") private LocalDate dateReviewed;
    @Column(name = "lawful_basis_source", length = 100) private String lawfulBasisSource;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
