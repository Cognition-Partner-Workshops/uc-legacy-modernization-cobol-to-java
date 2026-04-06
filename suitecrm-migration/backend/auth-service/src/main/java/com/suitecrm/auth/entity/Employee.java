package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees", schema = "auth_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

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

    @Column(name = "phone_fax", length = 50)
    private String phoneFax;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_postal_code", length = 20)
    private String addressPostalCode;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "reports_to_id")
    private UUID reportsToId;

    @Column(name = "employee_status", length = 50)
    @Builder.Default
    private String employeeStatus = "Active";

    @Column(name = "messenger_type", length = 50)
    private String messengerType;

    @Column(name = "messenger_id", length = 100)
    private String messengerId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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
