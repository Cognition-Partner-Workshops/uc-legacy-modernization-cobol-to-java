package com.carddemo.auth.dto;

import com.carddemo.auth.model.User;
import com.carddemo.auth.model.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * User maintenance payload / view.
 *
 * <p>Replaces the BMS map fields used by the user-management programs
 * ({@code COUSR00C}/{@code COUSR01C}/{@code COUSR02C}/{@code COUSR03C}). The
 * field length constraints mirror copybook {@code CSUSR01Y}, and the non-empty
 * validation mirrors the "can NOT be empty..." edits in {@code COUSR01C}.
 *
 * <p>The {@code password} is write-only: it is accepted on create/update and
 * BCrypt-hashed before persistence, but is never echoed back in responses
 * (a deliberate departure from the legacy plaintext {@code SEC-USR-PWD}).
 *
 * @param userId    legacy {@code SEC-USR-ID PIC X(08)}
 * @param firstName legacy {@code SEC-USR-FNAME PIC X(20)}
 * @param lastName  legacy {@code SEC-USR-LNAME PIC X(20)}
 * @param password  plaintext password on input only; {@code null} on output
 * @param userType  legacy {@code SEC-USR-TYPE PIC X(01)}
 */
public record UserDto(
        @NotBlank(message = "User ID can NOT be empty...")
        @Size(max = 8, message = "User ID must be at most 8 characters")
        String userId,

        @NotBlank(message = "First Name can NOT be empty...")
        @Size(max = 20, message = "First Name must be at most 20 characters")
        String firstName,

        @NotBlank(message = "Last Name can NOT be empty...")
        @Size(max = 20, message = "Last Name must be at most 20 characters")
        String lastName,

        @NotBlank(message = "Password can NOT be empty...")
        String password,

        @NotNull(message = "User Type can NOT be empty...")
        UserType userType
) {

    /**
     * Projects a persisted {@link User} into a DTO for API responses.
     * The password is intentionally omitted (never returned to clients).
     *
     * @param user the entity to project
     * @return a DTO with a {@code null} password
     */
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                null,
                user.getUserType()
        );
    }
}
