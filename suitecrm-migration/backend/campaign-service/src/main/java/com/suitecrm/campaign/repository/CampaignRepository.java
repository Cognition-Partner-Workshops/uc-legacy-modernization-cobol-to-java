package com.suitecrm.campaign.repository;

import com.suitecrm.campaign.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    Optional<Campaign> findByIdAndDeletedFalse(UUID id);
    Page<Campaign> findByDeletedFalse(Pageable pageable);
    Page<Campaign> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<Campaign> findByCampaignTypeAndDeletedFalse(String campaignType, Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.deleted = false AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.campaignType) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Campaign> searchCampaigns(@Param("search") String search, Pageable pageable);
}
