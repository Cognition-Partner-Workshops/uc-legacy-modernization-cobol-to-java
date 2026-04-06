package com.suitecrm.targetlist.repository;

import com.suitecrm.targetlist.entity.TargetList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetListRepository extends JpaRepository<TargetList, UUID> {

    Page<TargetList> findByDeletedFalse(Pageable pageable);

    Page<TargetList> findByListTypeAndDeletedFalse(String listType, Pageable pageable);

    List<TargetList> findByAssignedUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT t FROM TargetList t WHERE t.deleted = false AND LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<TargetList> search(@Param("query") String query, Pageable pageable);
}
