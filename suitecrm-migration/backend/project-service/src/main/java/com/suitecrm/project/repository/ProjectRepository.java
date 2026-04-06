package com.suitecrm.project.repository;

import com.suitecrm.project.entity.Project;
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
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    Page<Project> findByDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND p.status = :status")
    Page<Project> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND p.assignedUserId = :userId")
    Page<Project> findByAssignedUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND p.priority = :priority")
    List<Project> findByPriority(@Param("priority") String priority);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND " +
           "p.estimatedEndDate BETWEEN :startDate AND :endDate")
    List<Project> findProjectsDueInRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Project p WHERE p.deleted = false AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Project> searchByName(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.status = :status")
    long countByStatus(@Param("status") String status);
}
