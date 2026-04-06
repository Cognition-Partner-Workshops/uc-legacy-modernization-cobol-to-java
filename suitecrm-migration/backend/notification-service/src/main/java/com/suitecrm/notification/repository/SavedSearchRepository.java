package com.suitecrm.notification.repository;
import com.suitecrm.notification.entity.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.UUID;
@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
    List<SavedSearch> findByAssignedUserIdAndDeletedFalse(UUID userId);
    List<SavedSearch> findByAssignedUserIdAndSearchModuleAndDeletedFalse(UUID userId, String searchModule);
}
