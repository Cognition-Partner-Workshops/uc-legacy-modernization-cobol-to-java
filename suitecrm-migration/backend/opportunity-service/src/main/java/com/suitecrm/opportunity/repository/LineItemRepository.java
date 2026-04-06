package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, UUID> {
    List<LineItem> findByParentIdAndParentTypeAndDeletedFalse(UUID parentId, String parentType);
    List<LineItem> findByGroupIdAndDeletedFalse(UUID groupId);
    List<LineItem> findByProductIdAndDeletedFalse(UUID productId);
}
