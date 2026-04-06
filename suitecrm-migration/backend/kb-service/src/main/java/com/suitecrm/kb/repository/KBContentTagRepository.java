package com.suitecrm.kb.repository;

import com.suitecrm.kb.entity.KBContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KBContentTagRepository extends JpaRepository<KBContentTag, UUID> {

    List<KBContentTag> findByKbContentId(UUID kbContentId);

    List<KBContentTag> findByTagId(UUID tagId);

    void deleteByKbContentIdAndTagId(UUID kbContentId, UUID tagId);
}
