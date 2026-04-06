package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.Bug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BugRepository extends JpaRepository<Bug, UUID> {
    Optional<Bug> findByIdAndDeletedFalse(UUID id);
    Page<Bug> findByDeletedFalse(Pageable pageable);
    Page<Bug> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<Bug> findByPriorityAndDeletedFalse(String priority, Pageable pageable);
    Page<Bug> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);

    @Query("SELECT b FROM Bug b WHERE b.deleted = false AND " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Bug> searchBugs(@Param("search") String search, Pageable pageable);

    long countByDeletedFalse();
}
