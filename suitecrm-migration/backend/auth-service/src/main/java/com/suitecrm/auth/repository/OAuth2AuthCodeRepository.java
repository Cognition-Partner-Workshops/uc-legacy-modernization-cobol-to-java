package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.OAuth2AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuth2AuthCodeRepository extends JpaRepository<OAuth2AuthCode, UUID> {
    Optional<OAuth2AuthCode> findByCodeAndRevokedFalse(String code);
}
