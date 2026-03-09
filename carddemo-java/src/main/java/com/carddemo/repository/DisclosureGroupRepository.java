package com.carddemo.repository;

import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Disclosure Group entity.
 * Replaces VSAM KSDS READ operations on DISCGRP file.
 * Used by batch program CBACT04C for interest rate lookups.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    List<DisclosureGroup> findByDisAcctGroupId(String acctGroupId);
}
