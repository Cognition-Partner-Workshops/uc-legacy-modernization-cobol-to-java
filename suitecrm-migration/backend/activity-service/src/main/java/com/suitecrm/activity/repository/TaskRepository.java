package com.suitecrm.activity.repository;

import com.suitecrm.activity.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByIdAndDeletedFalse(UUID id);
    Page<Task> findByDeletedFalse(Pageable pageable);
    Page<Task> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    Page<Task> findByStatusAndDeletedFalse(String status, Pageable pageable);
}
