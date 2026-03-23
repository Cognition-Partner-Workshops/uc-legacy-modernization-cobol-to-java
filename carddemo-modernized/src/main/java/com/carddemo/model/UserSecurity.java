package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * User Security entity - modernized from COBOL copybook CSUSR01Y (SEC-USER-DATA).
 *
 * Legacy COBOL layout (RECLN 80):
 *   SEC-USR-ID       PIC X(08)   -> userId
 *   SEC-USR-FNAME    PIC X(20)   -> firstName
 *   SEC-USR-LNAME    PIC X(20)   -> lastName
 *   SEC-USR-PWD      PIC X(08)   -> password
 *   SEC-USR-TYPE     PIC X(01)   -> userType ('A' = Admin, 'U' = User)
 *
 * Legacy data store: VSAM KSDS (USRSEC)
 * Legacy security: RACF
 * Modernized to: Cassandra table + Spring Security
 */
@Table("users")
public class UserSecurity {

    @PrimaryKey("user_id")
    private String userId;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("password")
    private String password;

    @Column("user_type")
    private String userType;

    public UserSecurity() {}

    public UserSecurity(String userId, String firstName, String lastName,
                        String password, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
    }

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

    public boolean isAdmin() {
        return "A".equalsIgnoreCase(this.userType);
    }
}
