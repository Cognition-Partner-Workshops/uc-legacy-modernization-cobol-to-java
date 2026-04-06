package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prospect_list_campaigns", schema = "campaign_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignProspectList {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "prospect_list_id", nullable = false) private UUID prospectListId;
    @Column(name = "campaign_id", nullable = false) private UUID campaignId;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
