package com.suitecrm.notification.repository;
import com.suitecrm.notification.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.UUID;
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByAssignedUserIdAndDeletedFalse(UUID userId);
    List<Favorite> findByAssignedUserIdAndModuleNameAndDeletedFalse(UUID userId, String moduleName);
}
