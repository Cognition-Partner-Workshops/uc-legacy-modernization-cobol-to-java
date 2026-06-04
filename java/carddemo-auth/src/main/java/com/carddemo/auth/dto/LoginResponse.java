package com.carddemo.auth.dto;

import com.carddemo.auth.model.UserType;

/**
 * Successful sign-on response.
 *
 * <p>Replaces the COMMAREA hand-off that {@code COSGN00C} performed: instead of
 * placing {@code CDEMO-USER-ID}/{@code CDEMO-USER-TYPE} in shared memory and
 * {@code XCTL}-ing to {@code COADM01C}/{@code COMEN01C}, the service returns a
 * signed JWT whose claims carry the identity downstream.
 *
 * @param token    the signed JWT bearer token
 * @param userId   the authenticated user ID
 * @param userType the authenticated user's authority level
 */
public record LoginResponse(
        String token,
        String userId,
        UserType userType
) {
}
