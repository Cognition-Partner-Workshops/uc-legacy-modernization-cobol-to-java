package com.aws.carddemo.web.authorization;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.PendingAuthDetail;
import com.aws.carddemo.domain.PendingAuthDetailRepository;
import com.aws.carddemo.domain.PendingAuthSummary;
import com.aws.carddemo.domain.PendingAuthSummaryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizationService {
  public record Request(
      String authDate,
      String authTime,
      String cardNum,
      String authType,
      String cardExpiryDate,
      String messageType,
      String messageSource,
      String processingCode,
      BigDecimal transactionAmount,
      String merchantCategoryCode,
      String acquirerCountryCode,
      Integer posEntryMode,
      String merchantId,
      String merchantName,
      String merchantCity,
      String merchantState,
      String merchantZip,
      String transactionId) {}

  public record Reply(
      String cardNum,
      String transactionId,
      String authIdCode,
      String authResponseCode,
      String authResponseReason,
      BigDecimal approvedAmount) {}

  private final CardXrefRepository xrefs;
  private final AccountRepository accounts;
  private final PendingAuthSummaryRepository summaries;
  private final PendingAuthDetailRepository details;

  public AuthorizationService(
      CardXrefRepository xrefs,
      AccountRepository accounts,
      PendingAuthSummaryRepository summaries,
      PendingAuthDetailRepository details) {
    this.xrefs = xrefs;
    this.accounts = accounts;
    this.summaries = summaries;
    this.details = details;
  }

  @Transactional
  public Reply authorize(Request request) {
    CardXref xref = xrefs.findByIdCardNum(request.cardNum()).orElse(null);
    Account account = xref == null ? null : accounts.findById(xref.getAcctId()).orElse(null);
    boolean approved =
        account != null
            && request.transactionAmount() != null
            && account.getCreditLimit() != null
            && account
                    .getCreditLimit()
                    .subtract(account.getCurrBal() == null ? BigDecimal.ZERO : account.getCurrBal())
                    .compareTo(request.transactionAmount())
                >= 0;
    String responseCode = approved ? "00" : "05";
    BigDecimal amount = approved ? request.transactionAmount() : BigDecimal.ZERO;
    String authId = right(request.authTime(), 6);
    if (account != null) {
      PendingAuthSummary summary =
          summaries
              .findById(account.getAcctId())
              .orElseGet(
                  () -> {
                    PendingAuthSummary value = new PendingAuthSummary();
                    value.setAcctId(account.getAcctId());
                    value.setCustId(xref.getCustId());
                    value.setAccountStatus("");
                    value.setApprovedAuthCount(0);
                    value.setDeclinedAuthCount(0);
                    value.setApprovedAuthAmount(BigDecimal.ZERO);
                    value.setDeclinedAuthAmount(BigDecimal.ZERO);
                    value.setCreditLimit(account.getCreditLimit());
                    value.setCashLimit(account.getCashCreditLimit());
                    value.setCreditBalance(account.getCurrBal());
                    value.setCashBalance(BigDecimal.ZERO);
                    return value;
                  });
      if (approved) {
        summary.setApprovedAuthCount(summary.getApprovedAuthCount() + 1);
        summary.setApprovedAuthAmount(
            summary.getApprovedAuthAmount().add(request.transactionAmount()));
        summary.setCreditBalance(summary.getCreditBalance().add(request.transactionAmount()));
      } else {
        summary.setDeclinedAuthCount(summary.getDeclinedAuthCount() + 1);
        summary.setDeclinedAuthAmount(
            summary.getDeclinedAuthAmount().add(request.transactionAmount()));
      }
      summaries.save(summary);
      PendingAuthDetail detail = new PendingAuthDetail();
      detail.setAcctId(account.getAcctId());
      detail.setCustId(xref.getCustId());
      detail.setAuthDate(request.authDate());
      detail.setAuthTime(request.authTime());
      detail.setCardNum(request.cardNum());
      detail.setAuthType(request.authType());
      detail.setCardExpiryDate(request.cardExpiryDate());
      detail.setMessageType(request.messageType());
      detail.setMessageSource(request.messageSource());
      detail.setProcessingCode(request.processingCode());
      detail.setTransactionAmt(request.transactionAmount());
      detail.setApprovedAmt(amount);
      detail.setMerchantCategoryCode(request.merchantCategoryCode());
      detail.setAcqrCountryCode(request.acquirerCountryCode());
      detail.setPosEntryMode(
          request.posEntryMode() == null ? null : request.posEntryMode().shortValue());
      detail.setMerchantId(request.merchantId());
      detail.setMerchantName(request.merchantName());
      detail.setMerchantCity(request.merchantCity());
      detail.setMerchantState(request.merchantState());
      detail.setMerchantZip(request.merchantZip());
      detail.setTransactionId(request.transactionId());
      detail.setAuthIdCode(authId);
      detail.setAuthRespCode(responseCode);
      detail.setAuthRespReason(approved ? "0000" : "0001");
      detail.setMatchStatus(approved ? "P" : "D");
      details.save(detail);
    }
    return new Reply(
        request.cardNum(),
        request.transactionId(),
        authId,
        responseCode,
        approved ? "0000" : "0001",
        amount);
  }

  @Transactional(readOnly = true)
  public List<PendingAuthDetail> details(Long accountId) {
    return details.findByAcctIdOrderByAuthDateAscAuthTimeAsc(accountId);
  }

  @Transactional(readOnly = true)
  public java.util.Optional<PendingAuthDetail> detail(Long id) {
    return details.findById(id);
  }

  @Transactional
  public String setFraud(Long id, boolean fraud) {
    PendingAuthDetail detail =
        details
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Authorization not found"));
    detail.setAuthFraud(fraud ? "F" : "R");
    detail.setFraudRptDate(fraud ? java.time.LocalDate.now().toString().replace("-", "") : "");
    details.save(detail);
    return fraud ? "AUTH MARKED FRAUD..." : "AUTH FRAUD REMOVED...";
  }

  private static String right(String value, int length) {
    if (value == null) return "";
    return value.length() <= length ? value : value.substring(value.length() - length);
  }
}
