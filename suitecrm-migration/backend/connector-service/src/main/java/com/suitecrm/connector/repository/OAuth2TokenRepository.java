package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.OAuth2Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OAuth2TokenRepository extends JpaRepository<OAuth2Token, UUID> {
    List<OAuth2Token> findByClientIdAndDeletedFalse(UUID clientId);
    List<OAuth2Token> findByUserIdAndDeletedFalse(UUID userId);
    @Modifying
    @Query("DELETE FROM OAuth2Token t WHERE t.accessTokenExpires < :cutoff")
    void deleteExpiredTokens(@Param("cutoff") LocalDateTime cutoff);
}
