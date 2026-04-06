package com.suitecrm.account.repository;

import com.suitecrm.account.entity.AccountContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountContactRepository extends JpaRepository<AccountContact, UUID> {
    List<AccountContact> findByAccountIdAndDeletedFalse(UUID accountId);
    List<AccountContact> findByContactIdAndDeletedFalse(UUID contactId);
}
