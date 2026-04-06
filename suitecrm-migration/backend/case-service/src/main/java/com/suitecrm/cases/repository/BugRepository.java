package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.Bug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BugRepository extends JpaRepository<Bug, UUID> {
    Page<Bug> findByDeletedFalse(Pageable pageable);
    Optional<Bug> findByIdAndDeletedFalse(UUID id);
    List<Bug> findByStatusAndDeletedFalse(String status);
    List<Bug> findByPriorityAndDeletedFalse(String priority);
    List<Bug> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId);
}
