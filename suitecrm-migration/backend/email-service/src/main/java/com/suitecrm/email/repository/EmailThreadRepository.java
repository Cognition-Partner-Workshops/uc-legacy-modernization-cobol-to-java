package com.suitecrm.email.repository;

import com.suitecrm.email.entity.EmailThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailThreadRepository extends JpaRepository<EmailThread, UUID> {

    Page<EmailThread> findByDeletedFalse(Pageable pageable);

    Page<EmailThread> findByAssignedUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<EmailThread> findByRelatedModuleAndRelatedModuleIdAndDeletedFalse(String relatedModule, UUID relatedModuleId);
}
