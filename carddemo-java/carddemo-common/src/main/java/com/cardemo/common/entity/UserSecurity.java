package com.cardemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CSUSR01Y.cpy (User security, RECLN 80).
 * Source: app/cpy/CSUSR01Y.cpy
 *
 * TODO: Implement authentication from COSGN00C.cbl (Sign-on program)
 * TODO: Implement user CRUD from COUSR00C-COUSR03C.cbl (User management programs)
 * TODO: In production, usr_pwd should be stored as a BCrypt hash, not plaintext
 */
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "usr_id", length = 8)
    private String usrId;

    @Column(name = "usr_fname", length = 20)
    private String usrFname;

    @Column(name = "usr_lname", length = 20)
    private String usrLname;

    @Column(name = "usr_pwd", length = 8, nullable = false)
    private String usrPwd;

    @Column(name = "usr_type", length = 1, nullable = false)
    private String usrType;

    public UserSecurity() {
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUsrFname() {
        return usrFname;
    }

    public void setUsrFname(String usrFname) {
        this.usrFname = usrFname;
    }

    public String getUsrLname() {
        return usrLname;
    }

    public void setUsrLname(String usrLname) {
        this.usrLname = usrLname;
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
}
