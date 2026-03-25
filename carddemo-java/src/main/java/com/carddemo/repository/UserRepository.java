package com.carddemo.repository;

import com.carddemo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for User entity - replaces USRSEC VSAM file operations.
 * Consolidates READ, WRITE, REWRITE, DELETE, STARTBR/READNEXT from
 * COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    Page<User> findAllByOrderByUserIdAsc(Pageable pageable);

    boolean existsByUserId(String userId);
}
