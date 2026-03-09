package com.carddemo.repository;

import com.carddemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for User Security entity.
 * Replaces VSAM KSDS READ/WRITE/REWRITE/DELETE operations on USRSEC file.
 * Used by online programs: COSGN00C (sign-on), COUSR00C-COUSR03C (user management)
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findByUsrType(String usrType);
}
