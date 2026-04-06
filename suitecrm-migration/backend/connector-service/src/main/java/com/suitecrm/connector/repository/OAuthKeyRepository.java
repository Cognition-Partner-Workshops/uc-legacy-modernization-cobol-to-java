package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.OAuthKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthKeyRepository extends JpaRepository<OAuthKey, UUID> {

    List<OAuthKey> findByDeletedFalse();

    Optional<OAuthKey> findByConnectorIdAndDeletedFalse(UUID connectorId);

    List<OAuthKey> findByOauthTypeAndDeletedFalse(String oauthType);
}
