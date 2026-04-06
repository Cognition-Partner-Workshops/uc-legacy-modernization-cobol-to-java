package com.suitecrm.notification.repository;
import com.suitecrm.notification.entity.ActivityFeed;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface ActivityFeedRepository extends JpaRepository<ActivityFeed, UUID> {
    Page<ActivityFeed> findByDeletedFalseOrderByDateEnteredDesc(Pageable pageable);
    Page<ActivityFeed> findByAssignedUserIdAndDeletedFalseOrderByDateEnteredDesc(UUID userId, Pageable pageable);
}
