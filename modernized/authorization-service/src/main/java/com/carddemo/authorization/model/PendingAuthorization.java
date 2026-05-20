package com.carddemo.authorization.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_authorizations", schema = "authorization")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PendingAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Column(name = "transaction_id", length = 16)
    private String transactionId;

    @Column(name = "transaction_amt", precision = 11, scale = 2)
    private BigDecimal transactionAmt;

    @Column(name = "auth_time")
    private LocalDateTime authTime;

    @Column(name = "auth_resp_code", columnDefinition = "CHAR(2)")
    private String authRespCode;

    @Column(name = "auth_resp_reason", columnDefinition = "CHAR(4)")
    private String authRespReason;

    @Column(name = "approved_amt", precision = 11, scale = 2)
    private BigDecimal approvedAmt;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
