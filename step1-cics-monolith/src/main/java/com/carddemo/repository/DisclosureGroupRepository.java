package com.carddemo.repository;

import com.carddemo.model.entity.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {

    Optional<DisclosureGroup> findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
        String disAcctGroupId, String disTranTypeCd, Integer disTranCatCd);
}
