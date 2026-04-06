package com.suitecrm.document.repository;

import com.suitecrm.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndDeletedFalse(UUID id);
    Page<Document> findByDeletedFalse(Pageable pageable);
    Page<Document> findByCategoryIdAndDeletedFalse(String categoryId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.deleted = false AND " +
            "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Document> searchDocuments(@Param("search") String search, Pageable pageable);
}
