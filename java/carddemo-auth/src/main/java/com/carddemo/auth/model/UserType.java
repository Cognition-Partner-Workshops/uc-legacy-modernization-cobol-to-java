package com.carddemo.auth.model;

/**
 * User authority level.
 *
 * <p>Modernizes the {@code SEC-USR-TYPE PIC X(01)} field of copybook
 * {@code CSUSR01Y} and the COMMAREA condition names in {@code COCOM01Y}:
 * <pre>
 *   88 CDEMO-USRTYP-ADMIN VALUE 'A'.
 *   88 CDEMO-USRTYP-USER  VALUE 'U'.
 * </pre>
 * In the legacy flow {@code COSGN00C} routed {@code 'A'} users to the admin
 * menu ({@code COADM01C}) and {@code 'U'} users to the main menu
 * ({@code COMEN01C}); here the distinction becomes a Spring Security role.
 */
public enum UserType {

    /** Administrator - legacy code {@code 'A'}. Maps to {@code ROLE_ADMIN}. */
    ADMIN('A'),

    /** Regular user - legacy code {@code 'U'}. Maps to {@code ROLE_USER}. */
    USER('U');

    private final char code;

    UserType(char code) {
        this.code = code;
    }

    /** @return the single-character legacy {@code SEC-USR-TYPE} code. */
    public char getCode() {
        return code;
    }

    /**
     * Resolves a {@link UserType} from its legacy single-character code.
     *
     * @param code the {@code SEC-USR-TYPE} value ('A' or 'U'), case-insensitive
     * @return the matching {@link UserType}
     * @throws IllegalArgumentException if the code is not recognised
     */
    public static UserType fromCode(char code) {
        char upper = Character.toUpperCase(code);
        for (UserType type : values()) {
            if (type.code == upper) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown user type code: " + code);
    }

    /** @return the Spring Security role name (e.g. {@code ROLE_ADMIN}). */
    public String getRole() {
        return "ROLE_" + name();
    }
}
