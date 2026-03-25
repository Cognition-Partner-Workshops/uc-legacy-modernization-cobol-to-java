package com.carddemo.common.model.enums;

/**
 * User type enum mapping from COBOL COCOM01Y.cpy:
 * - CDEMO-USRTYP-ADMIN VALUE 'A'
 * - CDEMO-USRTYP-USER  VALUE 'U'
 */
public enum UserType {
    ADMIN('A'),
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    public static UserType fromCode(char code) {
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown UserType code: " + code);
    }
}
