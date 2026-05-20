package com.carddemo.authorization.service;

import com.carddemo.authorization.dto.AuthorizationRequest;
import com.carddemo.authorization.dto.AuthorizationResponse;
import com.carddemo.authorization.model.PendingAuthorization;
import com.carddemo.authorization.repository.PendingAuthorizationRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private PendingAuthorizationRepository repository;

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(repository);
    }

    @Test
    void processAuthorization_validRequest_approved() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4000123456789010");
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
    void processAuthorization_missingCard_declined() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("");
        request.setTransactionAmt(new BigDecimal("100.00"));

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(2L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);
        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("3100");
        assertThat(response.getStatus()).isEqualTo("DECLINED");
    }

    @Test
    void processAuthorization_invalidAmount_declined() {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setCardNum("4000123456789010");
        request.setTransactionAmt(BigDecimal.ZERO);

        when(repository.save(any())).thenAnswer(i -> {
            PendingAuthorization auth = i.getArgument(0);
            auth.setAuthId(3L);
            return auth;
        });

        AuthorizationResponse response = service.processAuthorization(request);
        assertThat(response.getAuthRespCode()).isEqualTo("05");
        assertThat(response.getAuthRespReason()).isEqualTo("4100");
    }

    @Test
    void getAuthorization_notFound_throws() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getAuthorization(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
