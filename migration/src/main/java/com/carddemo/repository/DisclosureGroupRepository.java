package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
    Optional<DisclosureGroup> findByAcctGroupIdAndTranTypeCdAndTranCatCd(
            String acctGroupId, String tranTypeCd, Integer tranCatCd);
}
