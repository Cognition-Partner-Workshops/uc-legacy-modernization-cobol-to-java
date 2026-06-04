package com.carddemo.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Security user record.
 *
 * <p>Modernizes the {@code SEC-USER-DATA} record defined in copybook
 * {@code CSUSR01Y} (the 80-byte {@code USRSEC} VSAM record, keyed on
 * {@code SEC-USR-ID}):
 * <pre>
 *   05 SEC-USR-ID    PIC X(08).   -- userId  (primary key)
 *   05 SEC-USR-FNAME PIC X(20).   -- firstName
 *   05 SEC-USR-LNAME PIC X(20).   -- lastName
 *   05 SEC-USR-PWD   PIC X(08).   -- password (was PLAINTEXT; now BCrypt hash)
 *   05 SEC-USR-TYPE  PIC X(01).   -- userType ('A' admin / 'U' user)
 *   05 SEC-USR-FILLER PIC X(23).  -- dropped (mainframe padding)
 * </pre>
 *
 * <p><strong>Security uplift:</strong> the legacy {@code SEC-USR-PWD} stored an
 * 8-character plaintext password that {@code COSGN00C} compared directly. Here
 * {@code password} holds a BCrypt hash and is never stored or compared in
 * plaintext.
 */
@Entity
@Table(name = "users")
public class User {

    /** Maps {@code SEC-USR-ID PIC X(08)} - the VSAM key. */
    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    /** Maps {@code SEC-USR-FNAME PIC X(20)}. */
    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    /** Maps {@code SEC-USR-LNAME PIC X(20)}. */
    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    /**
     * BCrypt hash of the user's password. Replaces the plaintext
     * {@code SEC-USR-PWD PIC X(08)}; column is widened to hold the hash.
     */
    @Column(name = "password", length = 100, nullable = false)
    private String password;

    /** Maps {@code SEC-USR-TYPE PIC X(01)} - persisted as 'A'/'U'. */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 5, nullable = false)
    private UserType userType;

    protected User() {
        // for JPA
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
