package com.carddemo.common.model;

import com.carddemo.common.model.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping for the User Security VSAM record.
 * Maps to COBOL copybook: CSUSR01Y.cpy (80-byte record)
 *
 * COBOL record layout:
 *   SEC-USR-ID      PIC X(08)
 *   SEC-USR-FNAME   PIC X(20)
 *   SEC-USR-LNAME   PIC X(20)
 *   SEC-USR-PWD     PIC X(08)
 *   SEC-USR-TYPE    PIC X(01)
 *
 * The userType maps to the 88-level conditions in COCOM01Y.cpy:
 *   CDEMO-USRTYP-ADMIN VALUE 'A'
 *   CDEMO-USRTYP-USER  VALUE 'U'
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "first_name", length = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "password", length = 8)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 5)
    private UserType userType;
}
