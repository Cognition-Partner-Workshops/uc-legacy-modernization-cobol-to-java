package com.cardemo.repository;

import com.cardemo.model.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SecurityUser entity - replaces VSAM KSDS file access
 * (USRSEC file in COBOL programs)
 */
@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, String> {

    List<SecurityUser> findByUserType(String userType);
}
