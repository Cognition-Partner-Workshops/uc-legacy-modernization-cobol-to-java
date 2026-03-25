package com.cardemo.repository;

import com.cardemo.model.DisclosureGroup;
import com.cardemo.model.DisclosureGroupKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DisclosureGroup entity - replaces VSAM KSDS file access
 * (DISCGRP file in COBOL programs)
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupKey> {

    Optional<DisclosureGroup> findByAcctGroupIdAndTranTypeCdAndTranCatCd(
            String acctGroupId, String tranTypeCd, Integer tranCatCd);
}
