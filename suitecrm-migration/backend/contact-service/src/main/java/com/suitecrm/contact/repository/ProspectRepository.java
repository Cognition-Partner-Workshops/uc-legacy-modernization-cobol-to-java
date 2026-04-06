package com.suitecrm.contact.repository;

import com.suitecrm.contact.entity.Prospect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProspectRepository extends JpaRepository<Prospect, UUID> {
    Optional<Prospect> findByIdAndDeletedFalse(UUID id);
    Page<Prospect> findByDeletedFalse(Pageable pageable);
    Page<Prospect> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    Optional<Prospect> findByTrackerKeyAndDeletedFalse(String trackerKey);

    @Query("SELECT p FROM Prospect p WHERE p.deleted = false AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Prospect> searchProspects(@Param("search") String search, Pageable pageable);

    long countByDeletedFalse();
}
