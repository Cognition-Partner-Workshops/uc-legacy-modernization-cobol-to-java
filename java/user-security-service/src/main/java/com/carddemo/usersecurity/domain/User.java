package com.carddemo.usersecurity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the USRSEC record layout defined by copybook CSUSR01Y (SEC-USER-DATA).
 */
@Entity
@Table(name = "usrsec")
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    @Column(name = "password", length = 8, nullable = false)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;

    protected User() {
    }

    public User(String userId, String firstName, String lastName, String password, String userType) {
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isAdmin() {
        return "A".equals(userType);
    }
}
