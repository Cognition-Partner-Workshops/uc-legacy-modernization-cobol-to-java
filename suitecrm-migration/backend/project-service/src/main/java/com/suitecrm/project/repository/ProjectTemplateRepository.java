package com.suitecrm.project.repository;

import com.suitecrm.project.entity.ProjectTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, UUID> {

    Optional<ProjectTemplate> findByIdAndDeletedFalse(UUID id);

    Page<ProjectTemplate> findByDeletedFalse(Pageable pageable);

    @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.deleted = false AND pt.status = 'Active'")
    List<ProjectTemplate> findActiveTemplates();

    @Query("SELECT pt FROM ProjectTemplate pt WHERE pt.deleted = false AND LOWER(pt.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ProjectTemplate> searchByName(@Param("query") String query, Pageable pageable);
}
