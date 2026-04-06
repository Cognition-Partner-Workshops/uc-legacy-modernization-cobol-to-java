package com.suitecrm.email.repository;

import com.suitecrm.email.entity.Email;
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
public interface EmailRepository extends JpaRepository<Email, UUID> {

    Page<Email> findByDeletedFalse(Pageable pageable);

    Page<Email> findByAssignedUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Page<Email> findByStatusAndDeletedFalse(String status, Pageable pageable);

    Page<Email> findByTypeAndDeletedFalse(String type, Pageable pageable);

    @Query("SELECT e FROM Email e WHERE e.deleted = false AND e.parentType = :parentType AND e.parentId = :parentId")
    List<Email> findByParentRecord(@Param("parentType") String parentType, @Param("parentId") UUID parentId);

    @Query("SELECT e FROM Email e WHERE e.deleted = false AND e.mailboxId = :mailboxId ORDER BY e.dateSent DESC")
    Page<Email> findByMailbox(@Param("mailboxId") UUID mailboxId, Pageable pageable);

    @Query("SELECT e FROM Email e WHERE e.deleted = false AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.fromAddr) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.toAddrs) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Email> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT e FROM Email e WHERE e.deleted = false AND e.flagged = true AND e.assignedUserId = :userId")
    List<Email> findFlaggedByUser(@Param("userId") UUID userId);

    List<Email> findByDateSentBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(e) FROM Email e WHERE e.deleted = false AND e.status = :status")
    long countByStatus(@Param("status") String status);
}
