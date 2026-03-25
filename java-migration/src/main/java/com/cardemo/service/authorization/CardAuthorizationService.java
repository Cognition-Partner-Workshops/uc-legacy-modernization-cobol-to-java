/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.authorization;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Card Authorization Service - migrated from COBOL program COPAUA0C.cbl.
 * Processes card authorization requests: validates card, checks account status,
 * verifies available credit, and approves/declines transactions.
 * Original: CICS COBOL IMS MQ Program that reads auth requests from MQ,
 *           validates against VSAM files (XREF, ACCT, CUST, CARD),
 *           and writes auth responses back to MQ.
 */
@Service
public class CardAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(CardAuthorizationService.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;

    public CardAuthorizationService(CardXrefRepository cardXrefRepository,
                                    AccountRepository accountRepository,
                                    CustomerRepository customerRepository,
                                    CardRepository cardRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
    }

    /**
     * Process an authorization request - migrated from 5000-PROCESS-AUTH.
     *
     * @param request the authorization request
     * @return the authorization response
     */
    @Transactional
    public AuthorizationResponse processAuthorization(AuthorizationRequest request) {
        log.info("Processing authorization for card: {}", maskCardNumber(request.getCardNum()));

        AuthorizationResponse response = new AuthorizationResponse();
        response.setAuthDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        response.setAuthTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        response.setCardNum(request.getCardNum());

        // 5100-LOOKUP-XREF - Look up card cross-reference
        Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(request.getCardNum());
        if (xrefOpt.isEmpty()) {
            response.setStatus("DECLINED");
            response.setReasonCode("CARD_NOT_FOUND");
            response.setMessage("Card number not found in cross-reference");
            log.warn("Authorization declined: card {} not found in XREF", maskCardNumber(request.getCardNum()));
            return response;
        }

        CardXrefRecord xref = xrefOpt.get();

        // 5200-READ-ACCT - Read account record
        Optional<AccountRecord> acctOpt = accountRepository.findById(xref.getXrefAcctId());
        if (acctOpt.isEmpty()) {
            response.setStatus("DECLINED");
            response.setReasonCode("ACCOUNT_NOT_FOUND");
            response.setMessage("Account not found");
            log.warn("Authorization declined: account {} not found", xref.getXrefAcctId());
            return response;
        }

        AccountRecord account = acctOpt.get();

        // 5300-CHECK-ACCT-STATUS - Check account status
        if (!"Y".equals(account.getAcctActiveStatus())) {
            response.setStatus("DECLINED");
            response.setReasonCode("ACCOUNT_CLOSED");
            response.setMessage("Account is not active");
            log.warn("Authorization declined: account {} not active", account.getAcctId());
            return response;
        }

        // 5400-CHECK-CARD-STATUS - Check card status
        Optional<CardRecord> cardOpt = cardRepository.findById(request.getCardNum());
        if (cardOpt.isPresent()) {
            CardRecord card = cardOpt.get();
            if (!"Y".equals(card.getCardActiveStatus())) {
                response.setStatus("DECLINED");
                response.setReasonCode("CARD_NOT_ACTIVE");
                response.setMessage("Card is not active");
                log.warn("Authorization declined: card not active");
                return response;
            }
        }

        // 5500-CHECK-AVAILABLE-CREDIT - Check available credit
        BigDecimal creditLimit = account.getAcctCreditLimit() != null
                ? account.getAcctCreditLimit() : BigDecimal.ZERO;
        BigDecimal currentBalance = account.getAcctCurrBal() != null
                ? account.getAcctCurrBal() : BigDecimal.ZERO;
        BigDecimal availableCredit = creditLimit.subtract(currentBalance);
        BigDecimal transactionAmount = request.getTransactionAmount();

        if (transactionAmount.compareTo(availableCredit) > 0) {
            response.setStatus("DECLINED");
            response.setReasonCode("INSUFFICIENT_CREDIT");
            response.setMessage("Insufficient available credit");
            log.warn("Authorization declined: insufficient credit. Available: {}, Requested: {}",
                    availableCredit, transactionAmount);
            return response;
        }

        // Authorization approved
        response.setStatus("APPROVED");
        response.setReasonCode("00");
        response.setApprovedAmount(transactionAmount);
        response.setMessage("Transaction approved");
        log.info("Authorization approved for amount: {}", transactionAmount);

        return response;
    }

    private String maskCardNumber(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) return "****";
        return "****" + cardNum.substring(cardNum.length() - 4);
    }

    /**
     * Authorization Request DTO - migrated from PENDING-AUTH-REQUEST copybook.
     */
    public static class AuthorizationRequest {
        private String cardNum;
        private String authType;
        private String cardExpiryDate;
        private String messageType;
        private String messageSource;
        private String processingCode;
        private BigDecimal transactionAmount;
        private String merchantCategoryCode;
        private String merchantId;
        private String merchantName;
        private String merchantCity;
        private String merchantZip;

        public String getCardNum() { return cardNum; }
        public void setCardNum(String cardNum) { this.cardNum = cardNum; }
        public String getAuthType() { return authType; }
        public void setAuthType(String authType) { this.authType = authType; }
        public String getCardExpiryDate() { return cardExpiryDate; }
        public void setCardExpiryDate(String cardExpiryDate) { this.cardExpiryDate = cardExpiryDate; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        public String getMessageSource() { return messageSource; }
        public void setMessageSource(String messageSource) { this.messageSource = messageSource; }
        public String getProcessingCode() { return processingCode; }
        public void setProcessingCode(String processingCode) { this.processingCode = processingCode; }
        public BigDecimal getTransactionAmount() { return transactionAmount; }
        public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }
        public String getMerchantCategoryCode() { return merchantCategoryCode; }
        public void setMerchantCategoryCode(String merchantCategoryCode) { this.merchantCategoryCode = merchantCategoryCode; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public String getMerchantCity() { return merchantCity; }
        public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
        public String getMerchantZip() { return merchantZip; }
        public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
    }

    /**
     * Authorization Response DTO - migrated from PENDING-AUTH-RESPONSE copybook.
     */
    public static class AuthorizationResponse {
        private String authDate;
        private String authTime;
        private String cardNum;
        private String status;
        private String reasonCode;
        private BigDecimal approvedAmount;
        private String message;

        public String getAuthDate() { return authDate; }
        public void setAuthDate(String authDate) { this.authDate = authDate; }
        public String getAuthTime() { return authTime; }
        public void setAuthTime(String authTime) { this.authTime = authTime; }
        public String getCardNum() { return cardNum; }
        public void setCardNum(String cardNum) { this.cardNum = cardNum; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReasonCode() { return reasonCode; }
        public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
        public BigDecimal getApprovedAmount() { return approvedAmount; }
        public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
