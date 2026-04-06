package com.suitecrm.kb.repository;

import com.suitecrm.kb.entity.KBContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KBContentRepository extends JpaRepository<KBContent, UUID> {

    Page<KBContent> findByDeletedFalse(Pageable pageable);

    Page<KBContent> findByStatusAndDeletedFalse(String status, Pageable pageable);

    Page<KBContent> findByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    @Query("SELECT k FROM KBContent k WHERE k.deleted = false AND " +
            "(LOWER(k.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(k.body) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(k.summary) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KBContent> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT k FROM KBContent k WHERE k.deleted = false ORDER BY k.viewCount DESC")
    List<KBContent> findMostViewed(Pageable pageable);

    @Query("SELECT k FROM KBContent k WHERE k.deleted = false AND k.status = 'published' ORDER BY k.dateEntered DESC")
    List<KBContent> findRecentPublished(Pageable pageable);

    List<KBContent> findByAssignedUserIdAndDeletedFalse(UUID userId);
}
