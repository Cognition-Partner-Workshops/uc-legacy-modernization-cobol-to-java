package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.ExternalOAuthConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalOAuthConnectionRepository extends JpaRepository<ExternalOAuthConnection, UUID> {
    Page<ExternalOAuthConnection> findByDeletedFalse(Pageable pageable);
    Optional<ExternalOAuthConnection> findByIdAndDeletedFalse(UUID id);
    List<ExternalOAuthConnection> findByAssignedUserIdAndDeletedFalse(UUID userId);
}
