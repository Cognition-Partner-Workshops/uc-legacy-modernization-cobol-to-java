package com.suitecrm.quotes.repository;

import com.suitecrm.quotes.entity.LineItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LineItemGroupRepository extends JpaRepository<LineItemGroup, UUID> {
    List<LineItemGroup> findByParentIdAndParentTypeAndDeletedFalse(UUID parentId, String parentType);
}
