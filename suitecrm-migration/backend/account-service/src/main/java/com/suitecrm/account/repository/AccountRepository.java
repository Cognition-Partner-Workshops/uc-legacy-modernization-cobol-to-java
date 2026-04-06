package com.suitecrm.account.repository;

import com.suitecrm.account.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIdAndDeletedFalse(UUID id);
    Page<Account> findByDeletedFalse(Pageable pageable);
    Page<Account> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.deleted = false AND " +
            "(LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.industry) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.phoneOffice) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> searchAccounts(@Param("search") String search, Pageable pageable);
}
