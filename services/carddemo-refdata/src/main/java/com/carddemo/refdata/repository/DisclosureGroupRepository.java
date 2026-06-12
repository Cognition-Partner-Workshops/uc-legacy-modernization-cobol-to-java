package com.carddemo.refdata.repository;

import java.util.List;

import com.carddemo.refdata.entity.DisclosureGroup;
import com.carddemo.refdata.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    List<DisclosureGroup> findByAccountGroupId(String accountGroupId);

    List<DisclosureGroup> findByTransactionTypeCode(String transactionTypeCode);
}
