package com.aws.carddemo.batch;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefId;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ImportTasklet implements Tasklet {
  private final CustomerRepository customers;
  private final AccountRepository accounts;
  private final CardXrefRepository xrefs;
  private final TransactionRepository transactions;
  private final CardRepository cards;
  private final TranCategoryBalanceRepository balances;

  ImportTasklet(
      CustomerRepository customers,
      AccountRepository accounts,
      CardXrefRepository xrefs,
      TransactionRepository transactions,
      CardRepository cards,
      TranCategoryBalanceRepository balances) {
    this.customers = customers;
    this.accounts = accounts;
    this.xrefs = xrefs;
    this.transactions = transactions;
    this.cards = cards;
    this.balances = balances;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws IOException {
    String path =
        BatchSupport.parameter(
            context.getStepContext().getStepExecution().getJobParameters(),
            "exportPath",
            "target/input/carddemo-export.dat");
    List<Customer> customerRows = new ArrayList<>();
    List<Account> accountRows = new ArrayList<>();
    List<CardXref> xrefRows = new ArrayList<>();
    List<Transaction> transactionRows = new ArrayList<>();
    List<Card> cardRows = new ArrayList<>();
    int unknown = 0;
    int invalid = 0;
    int read = 0;
    List<String> errors = new ArrayList<>();
    for (String line : Files.readAllLines(Path.of(path))) {
      if (line.isBlank()) {
        continue;
      }
      read++;
      try {
        ExportCodec.ExportRecord record = ExportCodec.decode(line);
        switch (record.type()) {
          case 'C' -> customerRows.add(customer(record.fields()));
          case 'A' -> accountRows.add(account(record.fields()));
          case 'X' -> xrefRows.add(xref(record.fields()));
          case 'T' -> transactionRows.add(transaction(record.fields()));
          case 'D' -> cardRows.add(card(record.fields()));
          default -> unknown++;
        }
      } catch (RuntimeException e) {
        invalid++;
        errors.add(line + " | " + e.getMessage());
      }
    }
    var parameters = context.getStepContext().getStepExecution().getJobParameters();
    write(
        BatchSupport.parameter(parameters, "customerOutput", "target/output/CUSTOUT.dat"),
        customerRows.stream().map(row -> ExportCodec.customer(0, row)).toList());
    write(
        BatchSupport.parameter(parameters, "accountOutput", "target/output/ACCTOUT.dat"),
        accountRows.stream().map(row -> ExportCodec.account(0, row)).toList());
    write(
        BatchSupport.parameter(parameters, "xrefOutput", "target/output/XREFOUT.dat"),
        xrefRows.stream().map(row -> ExportCodec.xref(0, row)).toList());
    write(
        BatchSupport.parameter(parameters, "transactionOutput", "target/output/TRNXOUT.dat"),
        transactionRows.stream().map(row -> ExportCodec.transaction(0, row)).toList());
    write(
        BatchSupport.parameter(parameters, "cardOutput", "target/output/CARDOUT.dat"),
        cardRows.stream().map(row -> ExportCodec.card(0, row)).toList());
    write(BatchSupport.parameter(parameters, "errorOutput", "target/output/ERROUT.dat"), errors);
    var executionContext =
        context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    executionContext.putInt("totalRecordsRead", read);
    executionContext.putInt("customerRecordsImported", customerRows.size());
    executionContext.putInt("accountRecordsImported", accountRows.size());
    executionContext.putInt("xrefRecordsImported", xrefRows.size());
    executionContext.putInt("transactionRecordsImported", transactionRows.size());
    executionContext.putInt("cardRecordsImported", cardRows.size());
    executionContext.putInt("unknownRecordTypeCount", unknown);
    executionContext.putInt("invalidRecordCount", invalid);
    contribution.incrementWriteCount(
        customerRows.size()
            + accountRows.size()
            + xrefRows.size()
            + transactionRows.size()
            + cardRows.size()
            + errors.size());
    return RepeatStatus.FINISHED;
  }

  private void write(String path, List<String> lines) throws IOException {
    Path output = Path.of(path).toAbsolutePath();
    if (output.getParent() != null) Files.createDirectories(output.getParent());
    Files.write(output, lines);
  }

  private Customer customer(List<String> f) {
    require(f, 18);
    Customer row = new Customer();
    row.setCustId(integer(f.get(0)));
    row.setFirstName(f.get(1));
    row.setMiddleName(f.get(2));
    row.setLastName(f.get(3));
    row.setAddrLine1(f.get(4));
    row.setAddrLine2(f.get(5));
    row.setAddrLine3(f.get(6));
    row.setAddrStateCd(f.get(7));
    row.setAddrCountryCd(f.get(8));
    row.setAddrZip(f.get(9));
    row.setPhoneNum1(f.get(10));
    row.setPhoneNum2(f.get(11));
    row.setSsn(integer(f.get(12)));
    row.setGovtIssuedId(f.get(13));
    row.setDobYyyyMmDd(f.get(14));
    row.setEftAccountId(f.get(15));
    row.setPriCardHolderInd(f.get(16));
    row.setFicoCreditScore(integer(f.get(17)));
    return row;
  }

  private Account account(List<String> f) {
    require(f, 12);
    Account row = new Account();
    row.setAcctId(longValue(f.get(0)));
    row.setActiveStatus(f.get(1));
    row.setCurrBal(decimal(f.get(2)));
    row.setCreditLimit(decimal(f.get(3)));
    row.setCashCreditLimit(decimal(f.get(4)));
    row.setOpenDate(f.get(5));
    row.setExpiraionDate(f.get(6));
    row.setReissueDate(f.get(7));
    row.setCurrCycCredit(decimal(f.get(8)));
    row.setCurrCycDebit(decimal(f.get(9)));
    row.setAddrZip(f.get(10));
    row.setGroupId(f.get(11));
    return row;
  }

  private CardXref xref(List<String> f) {
    require(f, 3);
    CardXref row = new CardXref();
    row.setId(new CardXrefId(f.get(0)));
    row.setCustId(integer(f.get(1)));
    row.setAcctId(longValue(f.get(2)));
    return row;
  }

  private Transaction transaction(List<String> f) {
    require(f, 13);
    Transaction row = new Transaction();
    row.setTranId(f.get(0));
    row.setTypeCd(f.get(1));
    row.setCatCd(integer(f.get(2)));
    row.setSource(f.get(3));
    row.setTranDesc(f.get(4));
    row.setAmt(decimal(f.get(5)));
    row.setMerchantId(integer(f.get(6)));
    row.setMerchantName(f.get(7));
    row.setMerchantCity(f.get(8));
    row.setMerchantZip(f.get(9));
    row.setCardNum(f.get(10));
    row.setOrigTs(f.get(11));
    row.setProcTs(f.get(12));
    return row;
  }

  private Card card(List<String> f) {
    require(f, 6);
    Card row = new Card();
    row.setCardNum(f.get(0));
    row.setAcctId(longValue(f.get(1)));
    row.setCvvCd(integer(f.get(2)));
    row.setEmbossedName(f.get(3));
    row.setExpiraionDate(f.get(4));
    row.setActiveStatus(f.get(5));
    return row;
  }

  private void require(List<String> fields, int expected) {
    if (fields.size() != expected) {
      throw new IllegalArgumentException(
          "Expected " + expected + " fields but found " + fields.size());
    }
  }

  private Integer integer(String value) {
    return value.isBlank() ? null : Integer.valueOf(value);
  }

  private Long longValue(String value) {
    return value.isBlank() ? null : Long.valueOf(value);
  }

  private java.math.BigDecimal decimal(String value) {
    return value.isBlank() ? null : new java.math.BigDecimal(value);
  }
}
