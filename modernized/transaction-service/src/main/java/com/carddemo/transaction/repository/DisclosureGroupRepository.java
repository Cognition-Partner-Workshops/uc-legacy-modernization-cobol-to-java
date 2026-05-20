package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {
    Optional<DisclosureGroup> findByAcctGroupIdAndTranTypeCdAndTranCatCd(String acctGroupId, String tranTypeCd, Integer tranCatCd);
}
