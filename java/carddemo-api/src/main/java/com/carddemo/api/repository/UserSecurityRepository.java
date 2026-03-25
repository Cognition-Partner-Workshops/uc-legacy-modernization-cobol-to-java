package com.carddemo.api.repository;

import com.carddemo.common.model.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for UserSecurity entity.
 * Replaces VSAM file I/O for USRSEC (User Security file).
 * Maps to COBOL copybook: CSUSR01Y.cpy
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {
}
