package com.aws.carddemo.web.account;

import com.aws.carddemo.common.DateValidator;
import com.aws.carddemo.common.LookupTables;
import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
  private final AccountRepository accounts;
  private final CustomerRepository customers;
  private final CardXrefRepository xrefs;
  private final CardRepository cards;

  public AccountService(
      AccountRepository accounts,
      CustomerRepository customers,
      CardXrefRepository xrefs,
      CardRepository cards) {
    this.accounts = accounts;
    this.customers = customers;
    this.xrefs = xrefs;
    this.cards = cards;
  }

  public Response<AccountView> view(Long accountId, Context context) {
    if (accountId == null || accountId <= 0) {
      return Response.error(
          "Account Number if supplied must be a 11 digit Non-Zero Number", "accountId", context);
    }
    Account account = accounts.findById(accountId).orElse(null);
    if (account == null)
      return Response.error(
          "Account:" + accountId + " not found in Acct Master file.", "accountId", context);
    CardXref xref = xrefs.findByAcctId(accountId).stream().findFirst().orElse(null);
    if (xref == null)
      return Response.error(
          "Account:" + accountId + " not found in Cross ref file.", "accountId", context);
    Customer customer = customers.findById(xref.getCustId()).orElse(null);
    if (customer == null)
      return Response.error(
          "CustId:" + xref.getCustId() + " not found in customer master.", "accountId", context);
    Card card = cards.findById(xref.getId().getCardNum()).orElse(null);
    Context next = withAccount(context, accountId, xref.getId().getCardNum(), "COACTVWC");
    return Response.ok(new AccountView(account, customer, card), "COACTVWC", next);
  }

  @Transactional
  public Response<AccountView> update(AccountUpdateRequest request) {
    if (request.accountId() == null || request.accountId() <= 0) {
      return Response.error(
          "Account Number if supplied must be a 11 digit Non-Zero Number",
          "accountId",
          request.context());
    }
    Account account = accounts.findById(request.accountId()).orElse(null);
    if (account == null)
      return Response.error(
          "Account:" + request.accountId() + " not found in Acct Master file.",
          "accountId",
          request.context());
    CardXref xref = xrefs.findByAcctId(request.accountId()).stream().findFirst().orElse(null);
    Customer customer = xref == null ? null : customers.findById(xref.getCustId()).orElse(null);
    if (customer == null)
      return Response.error("Customer record not found.", "accountId", request.context());
    String validation = validate(request);
    if (!validation.isEmpty())
      return Response.error(validation, validationField(validation), request.context());
    if (!same(account, customer, request.preImage())) {
      return Response.error(
          "Record changed by some one else. Please review", "accountId", request.context());
    }
    if (!changesRequested(account, customer, request)) {
      return Response.error(
          "No change detected with respect to values fetched.", "accountId", request.context());
    }
    if (!request.confirm()) {
      return Response.ok(
          new AccountView(account, customer, null),
          "COACTUPC",
          withAccount(
              request.context(), request.accountId(), xref.getId().getCardNum(), "COACTUPC"));
    }
    copy(request, account, customer);
    accounts.save(account);
    customers.save(customer);
    return Response.ok(
        new AccountView(account, customer, null),
        "COACTUPC",
        withAccount(request.context(), request.accountId(), xref.getId().getCardNum(), "COACTUPC"));
  }

  private static String validate(AccountUpdateRequest r) {
    if (blank(r.activeStatus())) return "Active Status must be supplied.";
    if (!r.activeStatus().trim().equalsIgnoreCase("Y")
        && !r.activeStatus().trim().equalsIgnoreCase("N")) return "Active Status must be Y or N.";
    if (blank(r.firstName())) return "First Name must be supplied.";
    if (!r.firstName().matches("[A-Za-z ]+")) return "First Name can have alphabets only.";
    if (blank(r.lastName())) return "Last Name must be supplied.";
    if (!r.lastName().matches("[A-Za-z ]+")) return "Last Name can have alphabets only.";
    if (blank(r.addressLine1())) return "Address Line 1 must be supplied.";
    if (blank(r.state())) return "State must be supplied.";
    if (!LookupTables.isValidState(r.state())) return "State is not a valid US state code.";
    if (blank(r.zip()) || !r.zip().matches("\\d{5}")) return "Zip must be all numeric.";
    if (!LookupTables.isValidStateZipPrefix(r.state(), r.zip()))
      return "State and Zip combination is not valid.";
    if (blank(r.city())) return "City must be supplied.";
    if (blank(r.country())) return "Country must be supplied.";
    if (blank(r.phone1()) || !validPhone(r.phone1())) return "Phone Number 1 is not valid";
    if (!blank(r.phone2()) && !validPhone(r.phone2())) return "Phone Number 2 is not valid";
    if (blank(r.eftAccountId()) || !r.eftAccountId().matches("[0-9A-Za-z]+"))
      return "EFT Account Id must be all numeric.";
    if (blank(r.primaryCardHolder()) || !r.primaryCardHolder().matches("(?i)[YN]"))
      return "Primary Card Holder must be Y or N.";
    if (r.dob() != null && !r.dob().isBlank()) {
      DateValidator.Result result =
          DateValidator.validateDateOfBirth(r.dob().replace("-", ""), "Date of Birth");
      if (!result.valid()) return result.message();
    }
    return "";
  }

  private static boolean validPhone(String phone) {
    String digits = phone.replaceAll("[() .-]", "");
    return digits.matches("\\d{10}") && LookupTables.isValidPhoneAreaCode(digits.substring(0, 3));
  }

  private static String validationField(String message) {
    int end = message.indexOf(' ');
    return end > 0 ? Character.toLowerCase(message.charAt(0)) + message.substring(1, end) : "";
  }

  private static boolean same(Account a, Customer c, AccountPreImage p) {
    return p != null
        && Objects.equals(a.getActiveStatus(), p.activeStatus())
        && eq(a.getCurrBal(), p.currBal())
        && eq(a.getCreditLimit(), p.creditLimit())
        && eq(a.getCashCreditLimit(), p.cashCreditLimit())
        && Objects.equals(a.getOpenDate(), p.openDate())
        && Objects.equals(a.getExpiraionDate(), p.expiraionDate())
        && Objects.equals(a.getReissueDate(), p.reissueDate())
        && eq(a.getCurrCycCredit(), p.currCycCredit())
        && eq(a.getCurrCycDebit(), p.currCycDebit())
        && Objects.equals(a.getGroupId(), p.groupId())
        && Objects.equals(c.getFirstName(), p.firstName())
        && Objects.equals(c.getMiddleName(), p.middleName())
        && Objects.equals(c.getLastName(), p.lastName())
        && Objects.equals(c.getAddrLine1(), p.addressLine1())
        && Objects.equals(c.getAddrLine2(), p.addressLine2())
        && Objects.equals(c.getAddrLine3(), p.city())
        && Objects.equals(c.getAddrStateCd(), p.state())
        && Objects.equals(c.getAddrCountryCd(), p.country())
        && Objects.equals(c.getAddrZip(), p.zip())
        && Objects.equals(c.getPhoneNum1(), p.phone1())
        && Objects.equals(c.getPhoneNum2(), p.phone2())
        && Objects.equals(c.getEftAccountId(), p.eftAccountId())
        && Objects.equals(c.getPriCardHolderInd(), p.primaryCardHolder())
        && Objects.equals(c.getDobYyyyMmDd(), p.dob())
        && Objects.equals(c.getFicoCreditScore(), p.ficoScore());
  }

  private static void copy(AccountUpdateRequest r, Account a, Customer c) {
    a.setActiveStatus(r.activeStatus().toUpperCase());
    a.setCreditLimit(r.creditLimit());
    a.setCashCreditLimit(r.cashCreditLimit());
    a.setOpenDate(r.openDate());
    a.setExpiraionDate(r.expiraionDate());
    a.setReissueDate(r.reissueDate());
    a.setGroupId(r.groupId());
    c.setFirstName(r.firstName());
    c.setMiddleName(r.middleName());
    c.setLastName(r.lastName());
    c.setAddrLine1(r.addressLine1());
    c.setAddrLine2(r.addressLine2());
    c.setAddrLine3(r.city());
    c.setAddrStateCd(r.state().toUpperCase());
    c.setAddrCountryCd(r.country());
    c.setAddrZip(r.zip());
    c.setPhoneNum1(r.phone1());
    c.setPhoneNum2(r.phone2());
    c.setEftAccountId(r.eftAccountId());
    c.setPriCardHolderInd(r.primaryCardHolder().toUpperCase());
    c.setDobYyyyMmDd(r.dob());
    c.setFicoCreditScore(r.ficoScore());
  }

  private static boolean changesRequested(Account a, Customer c, AccountUpdateRequest r) {
    return !Objects.equals(a.getActiveStatus(), r.activeStatus())
        || !eq(a.getCreditLimit(), r.creditLimit())
        || !eq(a.getCashCreditLimit(), r.cashCreditLimit())
        || !Objects.equals(a.getOpenDate(), r.openDate())
        || !Objects.equals(a.getExpiraionDate(), r.expiraionDate())
        || !Objects.equals(a.getReissueDate(), r.reissueDate())
        || !Objects.equals(a.getGroupId(), r.groupId())
        || !Objects.equals(c.getFirstName(), r.firstName())
        || !Objects.equals(c.getMiddleName(), r.middleName())
        || !Objects.equals(c.getLastName(), r.lastName())
        || !Objects.equals(c.getAddrLine1(), r.addressLine1())
        || !Objects.equals(c.getAddrLine2(), r.addressLine2())
        || !Objects.equals(c.getAddrLine3(), r.city())
        || !Objects.equals(c.getAddrStateCd(), r.state())
        || !Objects.equals(c.getAddrCountryCd(), r.country())
        || !Objects.equals(c.getAddrZip(), r.zip())
        || !Objects.equals(c.getPhoneNum1(), r.phone1())
        || !Objects.equals(c.getPhoneNum2(), r.phone2())
        || !Objects.equals(c.getEftAccountId(), r.eftAccountId())
        || !Objects.equals(c.getPriCardHolderInd(), r.primaryCardHolder())
        || !Objects.equals(c.getDobYyyyMmDd(), r.dob())
        || !Objects.equals(c.getFicoCreditScore(), r.ficoScore());
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static boolean eq(BigDecimal a, BigDecimal b) {
    return Objects.equals(a, b) || (a != null && b != null && a.compareTo(b) == 0);
  }

  private static Context withAccount(Context c, Long id, String card, String program) {
    return new Context(
        c == null ? "" : c.toProgram(),
        program,
        c == null ? "" : c.fromTransaction(),
        "COAC",
        c == null ? "" : c.userId(),
        c == null ? "U" : c.userType(),
        id,
        card);
  }

  public record AccountPreImage(
      String activeStatus,
      BigDecimal currBal,
      BigDecimal creditLimit,
      BigDecimal cashCreditLimit,
      String openDate,
      String expiraionDate,
      String reissueDate,
      BigDecimal currCycCredit,
      BigDecimal currCycDebit,
      String groupId,
      String firstName,
      String middleName,
      String lastName,
      String addressLine1,
      String addressLine2,
      String city,
      String state,
      String country,
      String zip,
      String phone1,
      String phone2,
      String eftAccountId,
      String primaryCardHolder,
      String dob,
      Integer ficoScore) {}

  public record AccountUpdateRequest(
      Long accountId,
      String activeStatus,
      BigDecimal creditLimit,
      BigDecimal cashCreditLimit,
      String openDate,
      String expiraionDate,
      String reissueDate,
      String groupId,
      String firstName,
      String middleName,
      String lastName,
      String addressLine1,
      String addressLine2,
      String city,
      String state,
      String country,
      String zip,
      String phone1,
      String phone2,
      String eftAccountId,
      String primaryCardHolder,
      String dob,
      Integer ficoScore,
      AccountPreImage preImage,
      boolean confirm,
      Context context) {}

  public record AccountView(Account account, Customer customer, Card card) {}
}
