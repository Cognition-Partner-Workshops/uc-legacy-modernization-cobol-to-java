package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, UUID> {

    List<OAuthToken> findByUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT t FROM OAuthToken t WHERE t.userId = :userId AND t.clientId = :clientId AND t.deleted = false")
    Optional<OAuthToken> findByUserIdAndClientId(@Param("userId") UUID userId, @Param("clientId") String clientId);

    @Query("SELECT t FROM OAuthToken t WHERE t.expiresAt < :now AND t.deleted = false")
    List<OAuthToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OAuthToken t WHERE t.expiresAt < :cutoff")
    void deleteExpiredTokens(@Param("cutoff") LocalDateTime cutoff);
}
