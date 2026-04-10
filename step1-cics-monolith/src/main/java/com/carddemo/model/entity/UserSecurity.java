package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mirrors CSUSR01Y copybook (SEC-USER-DATA).
 */
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "sec_usr_id", length = 8)
    private String secUsrId;

    @Column(name = "sec_usr_fname", length = 20)
    private String secUsrFname;

    @Column(name = "sec_usr_lname", length = 20)
    private String secUsrLname;

    @Column(name = "sec_usr_pwd", length = 8)
    private String secUsrPwd;

    @Column(name = "sec_usr_type", length = 1)
    private String secUsrType;

    public UserSecurity() {}

    public String getSecUsrId() { return secUsrId; }
    public void setSecUsrId(String secUsrId) { this.secUsrId = secUsrId; }

    public String getSecUsrFname() { return secUsrFname; }
    public void setSecUsrFname(String secUsrFname) { this.secUsrFname = secUsrFname; }

    public String getSecUsrLname() { return secUsrLname; }
    public void setSecUsrLname(String secUsrLname) { this.secUsrLname = secUsrLname; }

    public String getSecUsrPwd() { return secUsrPwd; }
    public void setSecUsrPwd(String secUsrPwd) { this.secUsrPwd = secUsrPwd; }

    public String getSecUsrType() { return secUsrType; }
    public void setSecUsrType(String secUsrType) { this.secUsrType = secUsrType; }
}
