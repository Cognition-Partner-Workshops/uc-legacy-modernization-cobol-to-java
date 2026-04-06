package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.Dashlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashletRepository extends JpaRepository<Dashlet, UUID> {
    List<Dashlet> findByDashboardIdAndDeletedFalseOrderByPositionAsc(UUID dashboardId);
}
