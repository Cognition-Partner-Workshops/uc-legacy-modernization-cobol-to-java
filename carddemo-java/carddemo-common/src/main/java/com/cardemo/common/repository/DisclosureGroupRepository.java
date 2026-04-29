package com.cardemo.common.repository;

import com.cardemo.common.entity.DisclosureGroup;
import com.cardemo.common.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for DisclosureGroup entity (CVTRA02Y.cpy → disclosure_groups table).
 *
 * TODO: Used by CBACT04C.cbl to look up interest rates by account group and transaction type
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    List<DisclosureGroup> findByAcctGroupId(String acctGroupId);
}
