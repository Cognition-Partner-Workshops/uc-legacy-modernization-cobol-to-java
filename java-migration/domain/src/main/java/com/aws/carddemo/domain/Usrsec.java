package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="usrsec")
public class Usrsec {
 @Id @Column(name="sec_usr_id",length=8) String secUsrId;
 @Column(name="sec_usr_fname",length=20) String secUsrFname; @Column(name="sec_usr_lname",length=20) String secUsrLname;
 @Column(name="sec_usr_pwd",length=8) String secUsrPwd; @Column(name="sec_usr_type",length=1) String secUsrType;
}
