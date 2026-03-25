package com.cardemo.repository;

import com.cardemo.model.UserSecurityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User Security data access - replaces VSAM USRSEC file I/O.
 * Migrated from COBOL CICS READ/WRITE/REWRITE/DELETE on USRSEC dataset.
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurityRecord, String> {
}
