package com.suitecrm.targetlist.repository;

import com.suitecrm.targetlist.entity.ProspectListLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProspectListLeadRepository extends JpaRepository<ProspectListLead, UUID> {
    List<ProspectListLead> findByProspectListIdAndDeletedFalse(UUID prospectListId);
    List<ProspectListLead> findByLeadIdAndDeletedFalse(UUID leadId);
}
