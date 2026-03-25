/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User Security entity - migrated from COBOL copybook CSUSR01Y.cpy.
 * Original COBOL record length: 80 bytes.
 */
@Entity
@Table(name = "user_security")
public class UserSecurityRecord {

    @Id
    @Column(name = "usr_id", length = 8)
    private String usrId;

    @Column(name = "usr_fname", length = 20)
    private String usrFirstName;

    @Column(name = "usr_lname", length = 20)
    private String usrLastName;

    @Column(name = "usr_pwd", length = 8)
    private String usrPassword;

    @Column(name = "usr_type", length = 1)
    private String usrType;

    public UserSecurityRecord() {
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUsrFirstName() {
        return usrFirstName;
    }

    public void setUsrFirstName(String usrFirstName) {
        this.usrFirstName = usrFirstName;
    }

    public String getUsrLastName() {
        return usrLastName;
    }

    public void setUsrLastName(String usrLastName) {
        this.usrLastName = usrLastName;
    }

    public String getUsrPassword() {
        return usrPassword;
    }

    public void setUsrPassword(String usrPassword) {
        this.usrPassword = usrPassword;
    }

    public String getUsrType() {
        return usrType;
    }

    public void setUsrType(String usrType) {
        this.usrType = usrType;
    }

    public boolean isAdmin() {
        return "A".equals(usrType);
    }

    public boolean isRegularUser() {
        return "U".equals(usrType);
    }

    @Override
    public String toString() {
        return "UserSecurityRecord{" +
                "usrId='" + usrId + '\'' +
                ", usrFirstName='" + usrFirstName + '\'' +
                ", usrLastName='" + usrLastName + '\'' +
                ", usrType='" + usrType + '\'' +
                '}';
    }
}
