package com.suitecrm.account.repository;

import com.suitecrm.account.entity.AccountAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountAuditRepository extends JpaRepository<AccountAudit, UUID> {
    List<AccountAudit> findByAccountIdOrderByChangedAtDesc(UUID accountId);
    Page<AccountAudit> findByAccountId(UUID accountId, Pageable pageable);
}
