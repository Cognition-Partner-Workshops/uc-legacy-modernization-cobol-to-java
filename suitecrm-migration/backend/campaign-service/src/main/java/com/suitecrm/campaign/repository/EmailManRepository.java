package com.suitecrm.campaign.repository;

import com.suitecrm.campaign.entity.EmailMan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailManRepository extends JpaRepository<EmailMan, UUID> {
    List<EmailMan> findByCampaignIdAndInQueueTrueAndDeletedFalse(UUID campaignId);
    List<EmailMan> findByMarketingIdAndDeletedFalse(UUID marketingId);
}
