package com.suitecrm.quotes.repository;

import com.suitecrm.quotes.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, UUID> {
    List<LineItem> findByGroupIdAndDeletedFalse(UUID groupId);
}
