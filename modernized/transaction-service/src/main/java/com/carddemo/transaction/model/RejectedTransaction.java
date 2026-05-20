package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rejected_transactions", schema = "transaction")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RejectedTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tran_id", length = 16) private String tranId;
    @Column(name = "card_num", length = 16) private String cardNum;
    @Column(name = "amount", precision = 11, scale = 2) private BigDecimal amount;
    @Column(name = "reason_code") private Integer reasonCode;
    @Column(name = "reason_desc", length = 100) private String reasonDesc;
    @Column(name = "rejected_at") private LocalDateTime rejectedAt;
}
