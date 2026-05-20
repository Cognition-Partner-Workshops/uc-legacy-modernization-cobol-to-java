package com.carddemo.authorization.service;

import com.carddemo.authorization.dto.AuthorizationRequest;
import com.carddemo.authorization.dto.AuthorizationResponse;
import com.carddemo.authorization.model.PendingAuthorization;
import com.carddemo.authorization.repository.PendingAuthorizationRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TIER 2 — HIGH RISK: Authorization Service (Approval/Denial Logic)
 * Risk factors: unauthorized transactions approved, valid transactions wrongly declined,
 * missing decline reasons from COBOL COPAUA0C
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-2")
@DisplayName("Tier 2 (High): Authorization Service — Approval/Denial Logic")
class RiskBasedAuthorizationTest {

    @Mock
    private PendingAuthorizationRepository repository;

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(repository);
    }

    // --- Approval Path ---

    @Test
    @DisplayName("T2-AUTH-001: Valid card + valid amount → APPROVED with code '00'")
    void validRequest_approved() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionId("TXN001");
        request.setTransactionAmt(new BigDecimal("100.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(1L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("00");
        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getApprovedAmt()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("T2-AUTH-002: Approved amount matches request amount exactly")
    void approvedAmount_matchesRequestAmount() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionAmt(new BigDecimal("999.99"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(2L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);
        assertThat(response.getApprovedAmt()).isEqualByComparingTo("999.99");
    }

    // --- Decline: Card Not Found (reason 3100) ---

    @Test
    @DisplayName("T2-AUTH-003: Empty card number → DECLINED with reason 3100 (card not found)")
    void emptyCardNum_declined_3100() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("");
        request.setTransactionAmt(new BigDecimal("100.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(3L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("3100");
        assertThat(response.getStatus()).isEqualTo("DECLINED");
        assertThat(response.getApprovedAmt()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("T2-AUTH-004: Null card number → DECLINED with reason 3100")
    void nullCardNum_declined_3100() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum(null);
        request.setTransactionAmt(new BigDecimal("100.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(4L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("3100");
    }

    // --- Decline: Insufficient Funds (reason 4100) ---

    @Test
    @DisplayName("T2-AUTH-005: Zero amount → DECLINED with reason 4100 (insufficient/invalid)")
    void zeroAmount_declined_4100() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionAmt(BigDecimal.ZERO);

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(5L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("4100");
    }

    @Test
    @DisplayName("T2-AUTH-006: Negative amount → DECLINED with reason 4100")
    void negativeAmount_declined_4100() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionAmt(new BigDecimal("-50.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(6L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("4100");
    }

    @Test
    @DisplayName("T2-AUTH-007: Null amount → DECLINED with reason 4100")
    void nullAmount_declined_4100() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionAmt(null);

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(7L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);

        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("4100");
    }

    // --- Pending Authorization Queries ---

    @Test
    @DisplayName("T2-AUTH-008: Get pending by card returns only PENDING records")
    void getPendingByCard_returnsOnlyPending() {
        PendingAuthorization pending = PendingAuthorization.builder()
                .authId(6L).cardNum("4111111111111111")
                .transactionAmt(new BigDecimal("250.00"))
                .status("PENDING").build();
        when(repository.findByCardNumAndStatus("4111111111111111", "PENDING"))
                .thenReturn(List.of(pending));

        var results = service.getPendingByCard("4111111111111111");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("T2-AUTH-009: Get pending for card with no pending returns empty")
    void getPendingByCard_noPending_returnsEmpty() {
        when(repository.findByCardNumAndStatus("9999999999999999", "PENDING"))
                .thenReturn(Collections.emptyList());

        var results = service.getPendingByCard("9999999999999999");
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("T2-AUTH-010: Get authorization by ID — not found throws")
    void getAuthorization_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAuthorization(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("T2-AUTH-011: Get authorization by ID — found returns details")
    void getAuthorization_found_returnsDetails() {
        PendingAuthorization auth = PendingAuthorization.builder()
                .authId(1L).cardNum("4111111111111111")
                .transactionId("TXN001")
                .transactionAmt(new BigDecimal("125.50"))
                .authRespCode("00").status("APPROVED")
                .approvedAmt(new BigDecimal("125.50")).build();
        when(repository.findById(1L)).thenReturn(Optional.of(auth));

        var response = service.getAuthorization(1L);

        assertThat(response.getAuthId()).isEqualTo(1L);
        assertThat(response.getCardNum()).isEqualTo("4111111111111111");
        assertThat(response.getAuthRespCode()).isEqualTo("00");
    }

    // --- Purge ---

    @Test
    @DisplayName("T2-AUTH-012: Purge expired calls repository delete")
    void purgeExpired_callsDelete() {
        doNothing().when(repository).deleteByStatus("EXPIRED");

        service.purgeExpired();

        verify(repository).deleteByStatus("EXPIRED");
    }

    // --- Authorization persistence ---

    @Test
    @DisplayName("T2-AUTH-013: Authorization is persisted via repository.save")
    void processAuthorization_savesRecord() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4111111111111111");
        request.setTransactionAmt(new BigDecimal("100.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(10L);
            return auth;
        });

        service.processAuthorization(request);

        verify(repository).save(any());
    }
}
