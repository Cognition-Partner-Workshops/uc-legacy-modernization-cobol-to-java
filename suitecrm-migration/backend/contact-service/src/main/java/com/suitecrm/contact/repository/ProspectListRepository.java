package com.suitecrm.contact.repository;

import com.suitecrm.contact.entity.ProspectList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProspectListRepository extends JpaRepository<ProspectList, UUID> {
    Optional<ProspectList> findByIdAndDeletedFalse(UUID id);
    Page<ProspectList> findByDeletedFalse(Pageable pageable);
    Page<ProspectList> findByListTypeAndDeletedFalse(String listType, Pageable pageable);
    Page<ProspectList> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    long countByDeletedFalse();
}
