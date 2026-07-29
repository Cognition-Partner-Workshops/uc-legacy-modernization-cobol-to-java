package com.cognition.usersecurity.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserTypeConverter implements AttributeConverter<UserType, String> {
    @Override public String convertToDatabaseColumn(UserType type) { return type == null ? null : type.getCode(); }
    @Override public UserType convertToEntityAttribute(String code) { return code == null ? null : UserType.fromCode(code); }
}
