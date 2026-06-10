package com.carddemo.repository;

import com.carddemo.model.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {

    Optional<UserSecurity> findByUserId(String userId);
}
