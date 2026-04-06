package com.suitecrm.targetlist.repository;

import com.suitecrm.targetlist.entity.TargetListMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetListMemberRepository extends JpaRepository<TargetListMember, UUID> {

    List<TargetListMember> findByProspectListIdAndDeletedFalse(UUID prospectListId);

    List<TargetListMember> findByRelatedIdAndDeletedFalse(UUID relatedId);

    long countByProspectListIdAndDeletedFalse(UUID prospectListId);

    List<TargetListMember> findByProspectListIdAndRelatedTypeAndDeletedFalse(UUID prospectListId, String relatedType);
}
