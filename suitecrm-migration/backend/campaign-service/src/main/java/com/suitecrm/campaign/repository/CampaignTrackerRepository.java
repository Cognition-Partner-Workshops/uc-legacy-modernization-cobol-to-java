package com.suitecrm.campaign.repository;

import com.suitecrm.campaign.entity.CampaignTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignTrackerRepository extends JpaRepository<CampaignTracker, UUID> {
    List<CampaignTracker> findByCampaignIdAndDeletedFalse(UUID campaignId);
    Optional<CampaignTracker> findByTrackerKeyAndDeletedFalse(String trackerKey);
    Optional<CampaignTracker> findByIdAndDeletedFalse(UUID id);
}
