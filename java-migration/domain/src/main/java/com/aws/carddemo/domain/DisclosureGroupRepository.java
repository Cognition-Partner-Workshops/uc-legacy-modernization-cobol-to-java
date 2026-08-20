package com.aws.carddemo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup,DisclosureGroupId> { java.util.List<DisclosureGroup> findByIdAcctGroupId(String groupId); }
