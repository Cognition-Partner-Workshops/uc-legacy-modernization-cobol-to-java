package com.cardemo.repository;

import com.cardemo.model.DisclosureGroup;
import java.util.Optional;

/**
 * Repository interface for disclosure group data access.
 * Equivalent of COBOL DISCGRP VSAM file operations.
 */
public interface DisclosureGroupRepository {

    Optional<DisclosureGroup> findByKey(String accountGroupId, String transactionTypeCode,
                                         int transactionCategoryCode);
}
