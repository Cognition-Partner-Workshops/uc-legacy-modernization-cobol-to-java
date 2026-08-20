package com.aws.carddemo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "usrsec")
public class Usrsec {
  @Id
  @Column(name = "sec_usr_id", length = 8)
  String secUsrId;

  @Column(name = "sec_usr_fname", length = 20)
  String secUsrFname;

  @Column(name = "sec_usr_lname", length = 20)
  String secUsrLname;

  @Column(name = "sec_usr_pwd", length = 8)
  String secUsrPwd;

  @Column(name = "sec_usr_type", length = 1)
  String secUsrType;

  public String getSecUsrId() {
    return secUsrId;
  }

  public void setSecUsrId(String v) {
    secUsrId = v;
  }

  public String getSecUsrFname() {
    return secUsrFname;
  }

  public void setSecUsrFname(String v) {
    secUsrFname = v;
  }

  public String getSecUsrLname() {
    return secUsrLname;
  }

  public void setSecUsrLname(String v) {
    secUsrLname = v;
  }

  public String getSecUsrPwd() {
    return secUsrPwd;
  }

  public void setSecUsrPwd(String v) {
    secUsrPwd = v;
  }

  public String getSecUsrType() {
    return secUsrType;
  }

  public void setSecUsrType(String v) {
    secUsrType = v;
  }
}
