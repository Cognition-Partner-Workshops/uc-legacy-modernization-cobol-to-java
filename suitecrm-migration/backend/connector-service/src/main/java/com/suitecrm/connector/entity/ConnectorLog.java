package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connector_logs", schema = "connector_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
    }
}
