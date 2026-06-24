package com.cardemo.model;

/**
 * Java equivalent of COBOL copybook CSUSR01Y (SEC-USER-DATA).
 * Represents user security record for authentication.
 */
public class UserSecurity {

    private String userId;       // SEC-USR-ID PIC X(08)
    private String firstName;    // SEC-USR-FNAME PIC X(20)
    private String lastName;     // SEC-USR-LNAME PIC X(20)
    private String password;     // SEC-USR-PWD PIC X(08)
    private String userType;     // SEC-USR-TYPE PIC X(01): 'A' = Admin, 'U' = User

    public UserSecurity() {
    }

    public UserSecurity(String userId, String firstName, String lastName,
                        String password, String userType) {
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
        return "A".equalsIgnoreCase(userType);
    }
}
