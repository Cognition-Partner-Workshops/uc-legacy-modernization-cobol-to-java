package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps to COBOL USRSEC file record layout (CSUSR01Y.cpy, 80 bytes):
 * SEC-USR-ID (8), SEC-USR-FNAME (20), SEC-USR-LNAME (20),
 * SEC-USR-PWD (8), SEC-USR-TYPE (1), FILLER (23)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;
}
