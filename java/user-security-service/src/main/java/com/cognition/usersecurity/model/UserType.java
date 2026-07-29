package com.cognition.usersecurity.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserType {
    ADMIN("A"), USER("U");

    private final String code;
    UserType(String code) { this.code = code; }
    public String getCode() { return code; }

    @JsonCreator
    public static UserType fromCode(String value) {
        for (UserType type : values()) if (type.code.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) return type;
        throw new IllegalArgumentException("User type must be A, U, ADMIN, or USER");
    }
}
