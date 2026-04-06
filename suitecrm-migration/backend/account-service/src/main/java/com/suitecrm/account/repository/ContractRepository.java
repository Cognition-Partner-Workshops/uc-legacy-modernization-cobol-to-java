package com.suitecrm.account.repository;

import com.suitecrm.account.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Optional<Contract> findByIdAndDeletedFalse(UUID id);
    Page<Contract> findByDeletedFalse(Pageable pageable);
    Page<Contract> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);
    List<Contract> findByStatusAndDeletedFalse(String status);
    Page<Contract> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    long countByAccountIdAndDeletedFalse(UUID accountId);
}
