package com.suitecrm.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads", schema = "contact_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

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

    @Column(name = "company", length = 255)
    private String company;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "email_primary", length = 255)
    private String emailPrimary;

    @Column(name = "phone_work", length = 50)
    private String phoneWork;

    @Column(name = "phone_mobile", length = 50)
    private String phoneMobile;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "New";

    @Column(name = "lead_source", length = 100)
    private String leadSource;

    @Column(name = "lead_source_description", columnDefinition = "TEXT")
    private String leadSourceDescription;

    @Column(name = "rating", length = 50)
    private String rating;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "annual_revenue", length = 50)
    private String annualRevenue;

    @Column(name = "employees", length = 10)
    private String employees;

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

    @Column(name = "converted")
    @Builder.Default
    private Boolean converted = false;

    @Column(name = "converted_contact_id")
    private UUID convertedContactId;

    @Column(name = "converted_account_id")
    private UUID convertedAccountId;

    @Column(name = "converted_opportunity_id")
    private UUID convertedOpportunityId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
