package com.aws.carddemo.web.transaction;

import com.aws.carddemo.common.DateValidator;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
  private static final int PAGE_SIZE = 10;
  private final TransactionRepository transactions;
  private final CardXrefRepository xrefs;

  public TransactionService(TransactionRepository transactions, CardXrefRepository xrefs) {
    this.transactions = transactions;
    this.xrefs = xrefs;
  }

  public Response<TransactionPage> list(TransactionListRequest request) {
    if (request.accountId() != null && request.accountId() <= 0) {
      return Response.error("Account ID must be Numeric...", "accountId", request.context());
    }
    List<Transaction> rows = transactions.findAllByOrderByTranIdAsc();
    if (request.cardNumber() != null && !request.cardNumber().isBlank()) {
      rows = rows.stream().filter(t -> request.cardNumber().equals(t.getCardNum())).toList();
    }
    if (request.accountId() != null) {
      rows =
          rows.stream()
              .filter(
                  t ->
                      xrefs
                          .findById(new com.aws.carddemo.domain.CardXrefId(t.getCardNum()))
                          .map(x -> request.accountId().equals(x.getAcctId()))
                          .orElse(false))
              .toList();
    }
    int page = Math.max(0, request.page());
    if ("BACK".equalsIgnoreCase(request.direction())) page = Math.max(0, page - 1);
    int from = Math.min(page * PAGE_SIZE, rows.size());
    int to = Math.min(from + PAGE_SIZE, rows.size());
    return Response.ok(
        new TransactionPage(rows.subList(from, to), page, PAGE_SIZE, to < rows.size(), page > 0),
        "COTRN00C",
        request.context());
  }

  public Response<Transaction> view(String transactionId, Context context) {
    if (transactionId == null || transactionId.isBlank()) {
      return Response.error("Tran ID can NOT be empty...", "transactionId", context);
    }
    return transactions
        .findById(transactionId.trim())
        .map(t -> Response.ok(t, "COTRN01C", context))
        .orElseGet(() -> Response.error("Transaction ID NOT found...", "transactionId", context));
  }

  @Transactional
  public Response<Transaction> add(TransactionAddRequest request) {
    String validation = validate(request);
    if (!validation.isEmpty()) {
      return Response.error(validation, validationField(validation), request.context());
    }
    CardXref xref = null;
    if (request.accountId() != null && !request.accountId().isBlank()) {
      try {
        Long accountId = Long.valueOf(request.accountId());
        xref = xrefs.findByAcctId(accountId).stream().findFirst().orElse(null);
      } catch (NumberFormatException exception) {
        return Response.error("Account ID must be Numeric...", "accountId", request.context());
      }
    } else {
      xref =
          xrefs.findById(new com.aws.carddemo.domain.CardXrefId(request.cardNumber())).orElse(null);
    }
    if (xref == null) {
      return Response.error(
          request.accountId() != null && !request.accountId().isBlank()
              ? "Account ID NOT found..."
              : "Card Number NOT found...",
          request.accountId() != null && !request.accountId().isBlank()
              ? "accountId"
              : "cardNumber",
          request.context());
    }
    String cardNumber = xref.getId().getCardNum();
    String id = nextId();
    if (!request.confirm()) {
      return Response.error("Confirm to add this transaction...", "confirm", request.context());
    }
    Transaction transaction = new Transaction();
    transaction.setTranId(id);
    transaction.setTypeCd(request.typeCode());
    transaction.setCatCd(Integer.valueOf(request.categoryCode()));
    transaction.setSource(request.source());
    transaction.setTranDesc(request.description());
    transaction.setAmt(new BigDecimal(request.amount()));
    transaction.setMerchantId(Integer.valueOf(request.merchantId()));
    transaction.setMerchantName(request.merchantName());
    transaction.setMerchantCity(request.merchantCity());
    transaction.setMerchantZip(request.merchantZip());
    transaction.setCardNum(cardNumber);
    transaction.setOrigTs(request.origDate());
    transaction.setProcTs(request.procDate());
    return Response.ok(transactions.save(transaction), "COTRN02C", request.context());
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

  private static String validate(TransactionAddRequest r) {
    if (blank(r.accountId()) && blank(r.cardNumber())) {
      return "Account or Card Number must be entered...";
    }
    if (!blank(r.accountId()) && !r.accountId().matches("\\d{1,11}"))
      return "Account ID must be Numeric...";
    if (!blank(r.cardNumber()) && !r.cardNumber().matches("\\d{1,16}"))
      return "Card Number must be Numeric...";
    if (blank(r.typeCode())) return "Type CD can NOT be empty...";
    if (!r.typeCode().matches("\\d+")) return "Type CD must be Numeric...";
    if (blank(r.categoryCode())) return "Category CD can NOT be empty...";
    if (!r.categoryCode().matches("\\d+")) return "Category CD must be Numeric...";
    if (blank(r.source())) return "Source can NOT be empty...";
    if (blank(r.description())) return "Description can NOT be empty...";
    if (blank(r.amount()) || !r.amount().matches("[+-]\\d{8}\\.\\d{2}"))
      return "Amount should be in format -99999999.99";
    if (!dateFormat(r.origDate())) return "Orig Date should be in format YYYY-MM-DD";
    if (!dateFormat(r.procDate())) return "Proc Date should be in format YYYY-MM-DD";
    if (!validDate(r.origDate())) return "Orig Date - Not a valid date...";
    if (!validDate(r.procDate())) return "Proc Date - Not a valid date...";
    if (blank(r.merchantId()) || !r.merchantId().matches("\\d+"))
      return "Merchant ID must be Numeric...";
    if (blank(r.merchantName())) return "Merchant Name can NOT be empty...";
    if (blank(r.merchantCity())) return "Merchant City can NOT be empty...";
    if (blank(r.merchantZip())) return "Merchant Zip can NOT be empty...";
    if (r.confirm() && !r.confirmValue().matches("(?i)[YN]"))
      return "Invalid value. Valid values are (Y/N)...";
    return "";
  }

  private static boolean validDate(String value) {
    return value != null && DateValidator.validate(value.replace("-", ""), "Date").valid();
  }

  private static boolean dateFormat(String value) {
    return value != null && value.matches("\\d{4}-\\d{2}-\\d{2}");
  }

  private static String validationField(String message) {
    if (message.startsWith("Amount")) return "amount";
    if (message.startsWith("Orig")) return "origDate";
    if (message.startsWith("Proc")) return "procDate";
    if (message.startsWith("Merchant")) return "merchantId";
    return "transaction";
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  public record TransactionListRequest(
      Long accountId, String cardNumber, int page, String direction, Context context) {}

  public record TransactionPage(
      List<Transaction> transactions,
      int page,
      int pageSize,
      boolean nextPage,
      boolean previousPage) {}

  public record TransactionAddRequest(
      String accountId,
      String cardNumber,
      String typeCode,
      String categoryCode,
      String source,
      String description,
      String amount,
      String origDate,
      String procDate,
      String merchantId,
      String merchantName,
      String merchantCity,
      String merchantZip,
      String confirmValue,
      boolean confirm,
      Context context) {}
}
