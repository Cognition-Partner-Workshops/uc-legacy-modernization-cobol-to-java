package com.carddemo.authorization.service;

import com.carddemo.authorization.dto.AuthorizationRequest;
import com.carddemo.authorization.dto.AuthorizationResponse;
import com.carddemo.authorization.model.PendingAuthorization;
import com.carddemo.authorization.repository.PendingAuthorizationRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final PendingAuthorizationRepository repository;

    @Transactional
    public AuthorizationResponse processAuthorization(AuthorizationRequest request) {
        PendingAuthorization auth = PendingAuthorization.builder()
                .cardNum(request.getCardNum())
                .transactionId(request.getTransactionId())
                .transactionAmt(request.getTransactionAmt())
                .authTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getCardNum() == null || request.getCardNum().isBlank()) {
            return buildDecline(auth, "3100", "Card not found");
        }

        if (request.getTransactionAmt() == null || request.getTransactionAmt().compareTo(BigDecimal.ZERO) <= 0) {
            return buildDecline(auth, "4100", "Invalid amount");
        }

        auth.setAuthRespCode("00");
        auth.setAuthRespReason(null);
        auth.setApprovedAmt(request.getTransactionAmt());
        auth.setStatus("APPROVED");

        PendingAuthorization saved = repository.save(auth);
        return toResponse(saved);
    }

    public List<AuthorizationResponse> getPendingByCard(String cardNum) {
        return repository.findByCardNumAndStatus(cardNum, "PENDING").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AuthorizationResponse getAuthorization(Long authId) {
        PendingAuthorization auth = repository.findById(authId)
                .orElseThrow(() -> new ResourceNotFoundException("Authorization", "authId", authId.toString()));
        return toResponse(auth);
    }

    @Transactional
    public void purgeExpired() {
        repository.deleteByStatus("EXPIRED");
    }

    private AuthorizationResponse buildDecline(PendingAuthorization auth, String reason, String description) {
        auth.setAuthRespCode("05");
        auth.setAuthRespReason(reason);
        auth.setApprovedAmt(BigDecimal.ZERO);
        auth.setStatus("DECLINED");

        PendingAuthorization saved = repository.save(auth);
        return toResponse(saved);
    }

    private AuthorizationResponse toResponse(PendingAuthorization auth) {
        return AuthorizationResponse.builder()
                .authId(auth.getAuthId())
                .cardNum(auth.getCardNum())
                .transactionId(auth.getTransactionId())
                .transactionAmt(auth.getTransactionAmt())
                .authRespCode(auth.getAuthRespCode())
                .authRespReason(auth.getAuthRespReason())
                .approvedAmt(auth.getApprovedAmt())
                .status(auth.getStatus())
                .build();
    }
}
