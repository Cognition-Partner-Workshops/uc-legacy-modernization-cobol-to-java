package com.suitecrm.campaign.repository;

import com.suitecrm.campaign.entity.EmailMarketing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailMarketingRepository extends JpaRepository<EmailMarketing, UUID> {
    Optional<EmailMarketing> findByIdAndDeletedFalse(UUID id);
    List<EmailMarketing> findByCampaignIdAndDeletedFalse(UUID campaignId);
    Page<EmailMarketing> findByDeletedFalse(Pageable pageable);
    Page<EmailMarketing> findByStatusAndDeletedFalse(String status, Pageable pageable);
}
