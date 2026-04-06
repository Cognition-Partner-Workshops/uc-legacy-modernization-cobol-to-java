package com.suitecrm.activity.repository;

import com.suitecrm.activity.entity.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID> {
    Optional<Call> findByIdAndDeletedFalse(UUID id);
    Page<Call> findByDeletedFalse(Pageable pageable);
    Page<Call> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
}
