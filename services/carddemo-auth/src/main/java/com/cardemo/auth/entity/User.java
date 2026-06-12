package com.cardemo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the COBOL SEC-USER-DATA record layout from copybook CSUSR01Y.
 *
 * COBOL layout:
 *   05 SEC-USR-ID      PIC X(08)  -> userId (primary key, max 8 chars)
 *   05 SEC-USR-FNAME   PIC X(20)  -> firstName (max 20 chars)
 *   05 SEC-USR-LNAME   PIC X(20)  -> lastName (max 20 chars)
 *   05 SEC-USR-PWD     PIC X(08)  -> password (BCrypt hash replaces plaintext)
 *   05 SEC-USR-TYPE    PIC X(01)  -> userType ('A'=ADMIN, 'U'=USER)
 */
@Entity
@Table(name = "users")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 5, nullable = false)
    private UserType userType;

    public enum UserType {
        ADMIN,
        USER
    }

    public User() {
    }

    public User(String userId, String firstName, String lastName, String password, UserType userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
