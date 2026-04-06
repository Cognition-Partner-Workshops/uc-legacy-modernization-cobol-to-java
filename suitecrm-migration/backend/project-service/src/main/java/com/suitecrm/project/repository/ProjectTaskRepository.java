package com.suitecrm.project.repository;

import com.suitecrm.project.entity.ProjectTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, UUID> {

    Optional<ProjectTask> findByIdAndDeletedFalse(UUID id);

    List<ProjectTask> findByProjectIdAndDeletedFalseOrderByOrderNumberAsc(UUID projectId);

    Page<ProjectTask> findByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectId = :projectId AND pt.deleted = false AND pt.status = :status")
    List<ProjectTask> findByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") String status);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.deleted = false AND pt.assignedUserId = :userId")
    Page<ProjectTask> findByAssignedUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectId = :projectId AND pt.deleted = false AND pt.milestoneFlag = true")
    List<ProjectTask> findMilestonesByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.deleted = false AND pt.dateDue <= :date AND pt.status != 'Completed'")
    List<ProjectTask> findOverdueTasks(@Param("date") LocalDate date);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectId = :projectId AND pt.deleted = false AND pt.parentTaskId IS NULL ORDER BY pt.orderNumber")
    List<ProjectTask> findTopLevelTasksByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.parentTaskId = :parentTaskId AND pt.deleted = false ORDER BY pt.orderNumber")
    List<ProjectTask> findSubTasks(@Param("parentTaskId") UUID parentTaskId);

    @Query("SELECT COUNT(pt) FROM ProjectTask pt WHERE pt.projectId = :projectId AND pt.deleted = false")
    long countByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT AVG(pt.percentComplete) FROM ProjectTask pt WHERE pt.projectId = :projectId AND pt.deleted = false")
    Double getAverageCompletionByProjectId(@Param("projectId") UUID projectId);
}
