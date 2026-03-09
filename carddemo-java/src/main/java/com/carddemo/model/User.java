package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User Security entity - mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 * Original VSAM dataset: AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS (RECLN 80).
 * Primary key: SEC-USR-ID PIC X(08).
 *
 * <p>In the original COBOL program COSGN00C, passwords were stored as plain text.
 * In this Java version, passwords are BCrypt-hashed.</p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "usr_id", length = 8)
    private String usrId;

    @Column(name = "usr_first_name", length = 20)
    private String usrFirstName;

    @Column(name = "usr_last_name", length = 20)
    private String usrLastName;

    @Column(name = "usr_pwd", length = 255)
    private String usrPwd;

    @Column(name = "usr_type", length = 1, nullable = false)
    private String usrType = "U";

    public User() {
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

    public String getUsrPwd() {
        return usrPwd;
    }

    public void setUsrPwd(String usrPwd) {
        this.usrPwd = usrPwd;
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
}
