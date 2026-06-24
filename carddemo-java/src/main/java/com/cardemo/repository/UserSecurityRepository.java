package com.cardemo.repository;

import com.cardemo.model.UserSecurity;
import java.util.Optional;

/**
 * Repository interface for user security data access.
 * Equivalent of COBOL USRSEC VSAM file operations.
 */
public interface UserSecurityRepository {

    Optional<UserSecurity> findByUserId(String userId);
}
