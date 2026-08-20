package com.aws.carddemo.web.bill;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillPaymentService {
  private final AccountRepository accounts;
  private final CardXrefRepository xrefs;
  private final TransactionRepository transactions;

  public BillPaymentService(
      AccountRepository accounts, CardXrefRepository xrefs, TransactionRepository transactions) {
    this.accounts = accounts;
    this.xrefs = xrefs;
    this.transactions = transactions;
  }

  @Transactional
  public Response<BillPaymentResult> pay(BillPaymentRequest request) {
    if (request.accountId() == null) {
      return Response.error("Acct ID can NOT be empty...", "accountId", request.context());
    }
    Account account = accounts.findById(request.accountId()).orElse(null);
    if (account == null) {
      return Response.error("Account ID NOT found...", "accountId", request.context());
    }
    if (account.getCurrBal() == null || account.getCurrBal().signum() <= 0) {
      return Response.error("You have nothing to pay...", "accountId", request.context());
    }
    if (!request.confirm()) {
      return Response.error("Confirm to make a bill payment...", "confirm", request.context());
    }
    String card =
        xrefs.findByAcctId(request.accountId()).stream()
            .findFirst()
            .map(x -> x.getId().getCardNum())
            .orElse(null);
    if (card == null)
      return Response.error("Account ID NOT found...", "accountId", request.context());
    String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss.000000"));
    Transaction transaction = new Transaction();
    transaction.setTranId(nextId());
    transaction.setTypeCd("02");
    transaction.setCatCd(2);
    transaction.setSource("POS TERM");
    transaction.setTranDesc("BILL PAYMENT - ONLINE");
    transaction.setAmt(account.getCurrBal());
    transaction.setCardNum(card);
    transaction.setMerchantId(999999999);
    transaction.setMerchantName("BILL PAYMENT");
    transaction.setMerchantCity("N/A");
    transaction.setMerchantZip("N/A");
    transaction.setOrigTs(timestamp);
    transaction.setProcTs(timestamp);
    transactions.save(transaction);
    account.setCurrBal(account.getCurrBal().subtract(transaction.getAmt()));
    accounts.save(account);
    return Response.ok(
        new BillPaymentResult(transaction.getTranId(), transaction.getAmt(), account.getCurrBal()),
        "COBIL00C",
        request.context());
  }

  private String nextId() {
    return transactions.findAll().stream()
        .map(Transaction::getTranId)
        .max(Comparator.naturalOrder())
        .map(
            id -> {
              try {
                return String.format("%016d", Long.parseLong(id) + 1);
              } catch (NumberFormatException exception) {
                return String.format("%016d", transactions.count() + 1);
              }
            })
        .orElse("0000000000000001");
  }

  public record BillPaymentRequest(Long accountId, boolean confirm, Context context) {}

  public record BillPaymentResult(
      String transactionId, java.math.BigDecimal amount, java.math.BigDecimal remainingBalance) {}
}
