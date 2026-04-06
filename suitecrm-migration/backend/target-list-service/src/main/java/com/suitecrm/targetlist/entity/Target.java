package com.suitecrm.targetlist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prospects", schema = "target_list_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Target {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(length = 100)
    private String title;

    @Column(length = 100)
    private String department;

    @Column(name = "phone_work", length = 50)
    private String phoneWork;

    @Column(name = "phone_mobile", length = 50)
    private String phoneMobile;

    @Column(name = "phone_fax", length = 50)
    private String phoneFax;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "primary_address_street")
    private String primaryAddressStreet;

    @Column(name = "primary_address_city", length = 100)
    private String primaryAddressCity;

    @Column(name = "primary_address_state", length = 100)
    private String primaryAddressState;

    @Column(name = "primary_address_postalcode", length = 20)
    private String primaryAddressPostalcode;

    @Column(name = "primary_address_country", length = 100)
    private String primaryAddressCountry;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "do_not_call")
    private Boolean doNotCall;

    @Column(name = "tracker_key", length = 100)
    private String trackerKey;

    @Column(name = "lead_id")
    private UUID leadId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (doNotCall == null) doNotCall = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
