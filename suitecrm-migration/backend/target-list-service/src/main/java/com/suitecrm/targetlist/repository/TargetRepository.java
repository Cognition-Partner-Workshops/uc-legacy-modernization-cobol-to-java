package com.suitecrm.targetlist.repository;

import com.suitecrm.targetlist.entity.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TargetRepository extends JpaRepository<Target, UUID> {

    Page<Target> findByDeletedFalse(Pageable pageable);

    @Query("SELECT t FROM Target t WHERE t.deleted = false AND (LOWER(t.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.emailAddress) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Target> search(@Param("query") String query, Pageable pageable);
}
