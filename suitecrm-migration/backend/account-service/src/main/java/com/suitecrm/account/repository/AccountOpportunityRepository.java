package com.suitecrm.account.repository;

import com.suitecrm.account.entity.AccountOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountOpportunityRepository extends JpaRepository<AccountOpportunity, UUID> {
    List<AccountOpportunity> findByAccountIdAndDeletedFalse(UUID accountId);
}
