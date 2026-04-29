package com.cardemo.common.repository;

import com.cardemo.common.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for UserSecurity entity (CSUSR01Y.cpy → user_security table).
 *
 * TODO: Used by COSGN00C.cbl for authentication and COUSR00C-03C.cbl for user management
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {

    List<UserSecurity> findByUsrType(String usrType);
}
