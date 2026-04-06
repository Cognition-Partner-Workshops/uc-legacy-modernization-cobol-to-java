package com.suitecrm.activity.repository;

import com.suitecrm.activity.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    Optional<Note> findByIdAndDeletedFalse(UUID id);
    Page<Note> findByDeletedFalse(Pageable pageable);
    Page<Note> findByParentTypeAndParentIdAndDeletedFalse(String parentType, UUID parentId, Pageable pageable);
}
