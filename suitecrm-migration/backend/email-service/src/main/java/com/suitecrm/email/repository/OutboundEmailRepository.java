package com.suitecrm.email.repository;

import com.suitecrm.email.entity.OutboundEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboundEmailRepository extends JpaRepository<OutboundEmail, UUID> {
    List<OutboundEmail> findByDeletedFalse();
    List<OutboundEmail> findByUserIdAndDeletedFalse(UUID userId);
}
