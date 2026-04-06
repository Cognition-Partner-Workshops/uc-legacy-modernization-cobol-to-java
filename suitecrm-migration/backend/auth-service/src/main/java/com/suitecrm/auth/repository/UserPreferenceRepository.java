package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    List<UserPreference> findByUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT up FROM UserPreference up WHERE up.userId = :userId AND up.category = :category AND up.deleted = false")
    List<UserPreference> findByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") String category);

    @Query("SELECT up FROM UserPreference up WHERE up.userId = :userId AND up.preferenceKey = :key AND up.deleted = false")
    Optional<UserPreference> findByUserIdAndKey(@Param("userId") UUID userId, @Param("key") String key);
}
