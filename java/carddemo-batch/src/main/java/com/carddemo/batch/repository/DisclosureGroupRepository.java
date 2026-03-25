package com.carddemo.batch.repository;

import com.carddemo.common.model.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisclosureGroupRepository
        extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {

    Optional<DisclosureGroup> findByAccountGroupIdAndTransactionTypeCodeAndTransactionCategoryCode(
            String accountGroupId, String transactionTypeCode, String transactionCategoryCode);
}
