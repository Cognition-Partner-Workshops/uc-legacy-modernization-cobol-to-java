package com.carddemo.usermanagement.repository;

import com.carddemo.usermanagement.entity.User;
import com.carddemo.usermanagement.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity (USRSEC dataset).
 *
 * <p>Replaces the direct VSAM file I/O operations used in the COBOL programs:
 * <ul>
 *   <li>STARTBR / READNEXT / READPREV / ENDBR (COUSR00C browse operations)</li>
 *   <li>READ (COUSR02C, COUSR03C lookups)</li>
 *   <li>WRITE (COUSR01C inserts)</li>
 *   <li>REWRITE (COUSR02C updates)</li>
 *   <li>DELETE (COUSR03C deletions)</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find users by type with pagination.
     * Supports filtering the user list analogous to the COUSR00C type display.
     */
    Page<User> findByUserType(UserType userType, Pageable pageable);
}
