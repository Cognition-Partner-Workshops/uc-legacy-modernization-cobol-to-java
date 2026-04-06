package com.suitecrm.kb.repository;

import com.suitecrm.kb.entity.KBDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KBDocumentRepository extends JpaRepository<KBDocument, UUID> {

    List<KBDocument> findByKbContentIdAndDeletedFalse(UUID kbContentId);

    List<KBDocument> findByCategoryIdAndDeletedFalse(UUID categoryId);
}
