package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Security User entity - converted from COBOL copybook CSUSR01Y.cpy
 * Original VSAM record length: 80 bytes
 */
@Entity
@Table(name = "security_users")
public class SecurityUser {

    @Id
    @Column(name = "sec_usr_id", length = 8)
    private String userId;

    @Column(name = "sec_usr_fname", length = 20)
    private String firstName;

    @Column(name = "sec_usr_lname", length = 20)
    private String lastName;

    @Column(name = "sec_usr_pwd", length = 8)
    private String password;

    @Column(name = "sec_usr_type", length = 1)
    private String userType;

    public SecurityUser() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isAdmin() { return "A".equals(userType); }
    public boolean isRegularUser() { return "U".equals(userType); }
}
