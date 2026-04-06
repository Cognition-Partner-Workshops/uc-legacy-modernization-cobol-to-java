package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.OAuth2Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, UUID> {
    Page<OAuth2Client> findByDeletedFalse(Pageable pageable);
    Optional<OAuth2Client> findByClientIdAndDeletedFalse(String clientId);
}
