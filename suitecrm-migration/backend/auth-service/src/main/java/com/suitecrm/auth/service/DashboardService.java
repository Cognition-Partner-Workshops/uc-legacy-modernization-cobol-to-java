package com.suitecrm.auth.service;

import com.suitecrm.auth.entity.Dashboard;
import com.suitecrm.auth.entity.Dashlet;
import com.suitecrm.auth.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public List<Dashboard> getUserDashboards(UUID userId) {
        return dashboardRepository.findByAssignedUserIdAndDeletedFalse(userId);
    }

    public Dashboard getDashboard(UUID dashboardId) {
        return dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new RuntimeException("Dashboard not found: " + dashboardId));
    }

    @Transactional
    public Dashboard createDashboard(Dashboard dashboard) {
        return dashboardRepository.save(dashboard);
    }

    @Transactional
    public Dashboard updateDashboard(UUID id, Dashboard updated) {
        Dashboard existing = getDashboard(id);
        existing.setName(updated.getName());
        existing.setLayout(updated.getLayout());
        existing.setDashboardModule(updated.getDashboardModule());
        return dashboardRepository.save(existing);
    }

    @Transactional
    public void deleteDashboard(UUID id) {
        Dashboard dashboard = getDashboard(id);
        dashboard.setDeleted(true);
        dashboardRepository.save(dashboard);
    }
}
