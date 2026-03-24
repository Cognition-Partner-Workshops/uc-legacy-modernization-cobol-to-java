package com.carddemo.usermanagement.entity;

/**
 * Represents the USRSEC user type field (SEC-USR-TYPE).
 * In the COBOL copybook CSUSR01Y.cpy, this is a PIC X(01) field.
 * The CardDemo application uses "R" for Regular and "A" for Admin.
 */
public enum UserType {

    REGULAR("R"),
    ADMIN("A");

    private final String code;

    UserType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Resolves a UserType from its single-character COBOL code.
     *
     * @param code the one-character code ("R" or "A")
     * @return the corresponding UserType
     * @throws IllegalArgumentException if the code is not recognized
     */
    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Invalid user type code: '" + code + "'. Valid values are 'R' (Regular) or 'A' (Admin).");
    }
}
