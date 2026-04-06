package com.suitecrm.campaign.repository;

import com.suitecrm.campaign.entity.CampaignLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CampaignLogRepository extends JpaRepository<CampaignLog, UUID> {
    Page<CampaignLog> findByCampaignIdAndDeletedFalse(UUID campaignId, Pageable pageable);
    Page<CampaignLog> findByCampaignIdAndActivityTypeAndDeletedFalse(UUID campaignId, String activityType, Pageable pageable);

    @Query("SELECT COUNT(cl) FROM CampaignLog cl WHERE cl.campaignId = :campaignId AND cl.activityType = :activityType AND cl.deleted = false")
    long countByActivityType(@Param("campaignId") UUID campaignId, @Param("activityType") String activityType);

    @Query("SELECT cl.activityType, COUNT(cl) FROM CampaignLog cl WHERE cl.campaignId = :campaignId AND cl.deleted = false GROUP BY cl.activityType")
    Object[][] getCampaignStats(@Param("campaignId") UUID campaignId);
}
