package com.suitecrm.kb.repository;

import com.suitecrm.kb.entity.KBCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KBCategoryRepository extends JpaRepository<KBCategory, UUID> {

    List<KBCategory> findByDeletedFalseOrderByDisplayOrderAsc();

    List<KBCategory> findByParentIdAndDeletedFalse(UUID parentId);

    List<KBCategory> findByParentIdIsNullAndDeletedFalse();
}
