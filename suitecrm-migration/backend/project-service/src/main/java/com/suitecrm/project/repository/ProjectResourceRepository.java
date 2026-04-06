package com.suitecrm.project.repository;

import com.suitecrm.project.entity.ProjectResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectResourceRepository extends JpaRepository<ProjectResource, UUID> {

    List<ProjectResource> findByProjectIdAndDeletedFalse(UUID projectId);

    @Query("SELECT pr FROM ProjectResource pr WHERE pr.userId = :userId AND pr.deleted = false")
    List<ProjectResource> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT pr FROM ProjectResource pr WHERE pr.projectId = :projectId AND pr.resourceType = :type AND pr.deleted = false")
    List<ProjectResource> findByProjectIdAndType(@Param("projectId") UUID projectId, @Param("type") String type);
}
