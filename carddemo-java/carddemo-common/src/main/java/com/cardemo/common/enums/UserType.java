package com.cardemo.common.enums;

/**
 * User type enumeration mapped from COBOL CDEMO-USER-TYPE in COCOM01Y.cpy.
 * 'A' = Admin (CDEMO-USRTYP-ADMIN), 'U' = Regular User (CDEMO-USRTYP-USER).
 */
public enum UserType {

    ADMIN("A"),
    USER("U");

    private final String code;

    UserType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown user type code: " + code);
    }
}
