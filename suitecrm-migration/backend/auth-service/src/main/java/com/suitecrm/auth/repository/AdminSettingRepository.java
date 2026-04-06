package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.AdminSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminSettingRepository extends JpaRepository<AdminSetting, UUID> {
    java.util.Optional<AdminSetting> findByCategoryAndName(String category, String name);
    List<AdminSetting> findByCategory(String category);
}
