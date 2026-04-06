package com.suitecrm.quotes.repository;

import com.suitecrm.quotes.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Page<Contract> findByDeletedFalse(Pageable pageable);
    Optional<Contract> findByIdAndDeletedFalse(UUID id);
    List<Contract> findByAccountIdAndDeletedFalse(UUID accountId);
    List<Contract> findByStatusAndDeletedFalse(String status);
    List<Contract> findByContractTypeAndDeletedFalse(String contractType);
}
