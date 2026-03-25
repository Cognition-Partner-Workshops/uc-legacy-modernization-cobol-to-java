/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.DisclosureGroupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Disclosure Group data access - replaces VSAM DISCGRP file.
 * Migrated from COBOL batch I/O on DISCGRP dataset.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroupRecord, DisclosureGroupRecord.DisGroupKey> {

    List<DisclosureGroupRecord> findByDisAcctGroupIdAndDisTranTypeCd(String groupId, String tranTypeCd);
}
