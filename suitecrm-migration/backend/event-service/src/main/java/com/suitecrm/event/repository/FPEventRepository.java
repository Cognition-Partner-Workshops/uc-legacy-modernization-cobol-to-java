package com.suitecrm.event.repository;

import com.suitecrm.event.entity.FPEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FPEventRepository extends JpaRepository<FPEvent, UUID> {

    Page<FPEvent> findByDeletedFalse(Pageable pageable);

    Page<FPEvent> findByStatusAndDeletedFalse(String status, Pageable pageable);

    @Query("SELECT e FROM FPEvent e WHERE e.deleted = false AND e.dateStart >= :start AND e.dateStart <= :end")
    List<FPEvent> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<FPEvent> findByLocationIdAndDeletedFalse(UUID locationId);

    List<FPEvent> findByAssignedUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT e FROM FPEvent e WHERE e.deleted = false AND LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<FPEvent> search(@Param("query") String query, Pageable pageable);
}
