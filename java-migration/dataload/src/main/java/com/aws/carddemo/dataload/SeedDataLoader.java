package com.aws.carddemo.dataload;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefId;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.DailyTransaction;
import com.aws.carddemo.domain.DailyTransactionRepository;
import com.aws.carddemo.domain.DisclosureGroup;
import com.aws.carddemo.domain.DisclosureGroupId;
import com.aws.carddemo.domain.DisclosureGroupRepository;
import com.aws.carddemo.domain.TranCategoryBalance;
import com.aws.carddemo.domain.TranCategoryBalanceId;
import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.TransactionCategory;
import com.aws.carddemo.domain.TransactionCategoryId;
import com.aws.carddemo.domain.TransactionCategoryRepository;
import com.aws.carddemo.domain.TransactionRepository;
import com.aws.carddemo.domain.TransactionType;
import com.aws.carddemo.domain.TransactionTypeRepository;
import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.domain.UsrsecRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class SeedDataLoader {
  private static final int BATCH_SIZE = 100;

  private final Path dataRoot;
  private final UsrsecRepository usrsecRepository;
  private final CustomerRepository customerRepository;
  private final AccountRepository accountRepository;
  private final CardRepository cardRepository;
  private final CardXrefRepository cardXrefRepository;
  private final DailyTransactionRepository dailyTransactionRepository;
  private final TransactionRepository transactionRepository;
  private final DisclosureGroupRepository disclosureGroupRepository;
  private final TranCategoryBalanceRepository tranCategoryBalanceRepository;
  private final TransactionCategoryRepository transactionCategoryRepository;
  private final TransactionTypeRepository transactionTypeRepository;

  @Autowired
  public SeedDataLoader(
      @Value("${carddemo.data-root:../app/data}") Path dataRoot,
      UsrsecRepository usrsecRepository,
      CustomerRepository customerRepository,
      AccountRepository accountRepository,
      CardRepository cardRepository,
      CardXrefRepository cardXrefRepository,
      DailyTransactionRepository dailyTransactionRepository,
      TransactionRepository transactionRepository,
      DisclosureGroupRepository disclosureGroupRepository,
      TranCategoryBalanceRepository tranCategoryBalanceRepository,
      TransactionCategoryRepository transactionCategoryRepository,
      TransactionTypeRepository transactionTypeRepository) {
    this.dataRoot = dataRoot;
    this.usrsecRepository = usrsecRepository;
    this.customerRepository = customerRepository;
    this.accountRepository = accountRepository;
    this.cardRepository = cardRepository;
    this.cardXrefRepository = cardXrefRepository;
    this.dailyTransactionRepository = dailyTransactionRepository;
    this.transactionRepository = transactionRepository;
    this.disclosureGroupRepository = disclosureGroupRepository;
    this.tranCategoryBalanceRepository = tranCategoryBalanceRepository;
    this.transactionCategoryRepository = transactionCategoryRepository;
    this.transactionTypeRepository = transactionTypeRepository;
  }

  public int load(SeedDataset dataset) {
    try {
      List<byte[]> records =
          FixedWidthReader.read(dataset.resolve(dataRoot), dataset.recordLength());
      return switch (dataset) {
        case USRSEC -> loadUsrsec(records, dataset);
        case CUSTOMER -> loadCustomers(records, dataset);
        case ACCOUNT -> loadAccounts(records, dataset);
        case CARD -> loadCards(records, dataset);
        case CARD_XREF -> loadCardXrefs(records, dataset);
        case DAILY_TRANSACTION -> loadDailyTransactions(records, dataset);
        case DISCLOSURE_GROUP -> loadDisclosureGroups(records, dataset);
        case TRAN_CATEGORY_BALANCE -> loadBalances(records, dataset);
        case TRANSACTION_CATEGORY -> loadCategories(records, dataset);
        case TRANSACTION_TYPE -> loadTypes(records, dataset);
      };
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read " + dataset + " seed data", e);
    }
  }

  public int loadAll() {
    clearAll();
    int count = 0;
    for (SeedDataset dataset :
        List.of(
            SeedDataset.USRSEC,
            SeedDataset.TRANSACTION_TYPE,
            SeedDataset.TRANSACTION_CATEGORY,
            SeedDataset.CUSTOMER,
            SeedDataset.ACCOUNT,
            SeedDataset.CARD,
            SeedDataset.CARD_XREF,
            SeedDataset.DISCLOSURE_GROUP,
            SeedDataset.TRAN_CATEGORY_BALANCE,
            SeedDataset.DAILY_TRANSACTION)) {
      count += load(dataset);
    }
    return count;
  }

  public void clearAll() {
    dailyTransactionRepository.deleteAllInBatch();
    transactionRepository.deleteAllInBatch();
    cardXrefRepository.deleteAllInBatch();
    cardRepository.deleteAllInBatch();
    tranCategoryBalanceRepository.deleteAllInBatch();
    disclosureGroupRepository.deleteAllInBatch();
    accountRepository.deleteAllInBatch();
    customerRepository.deleteAllInBatch();
    transactionCategoryRepository.deleteAllInBatch();
    transactionTypeRepository.deleteAllInBatch();
    usrsecRepository.deleteAllInBatch();
  }

  private int loadUsrsec(List<byte[]> records, SeedDataset dataset) {
    usrsecRepository.deleteAllInBatch();
    List<Usrsec> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      Usrsec entity = new Usrsec();
      entity.setSecUsrId(text(row, "secUsrId"));
      entity.setSecUsrFname(text(row, "secUsrFname"));
      entity.setSecUsrLname(text(row, "secUsrLname"));
      entity.setSecUsrPwd(text(row, "secUsrPwd"));
      entity.setSecUsrType(text(row, "secUsrType"));
      values.add(entity);
    }
    save(usrsecRepository, values);
    return values.size();
  }

  private int loadCustomers(List<byte[]> records, SeedDataset dataset) {
    customerRepository.deleteAllInBatch();
    List<Customer> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      Customer entity = new Customer();
      entity.setCustId(integer(row, "custId"));
      entity.setFirstName(text(row, "firstName"));
      entity.setMiddleName(text(row, "middleName"));
      entity.setLastName(text(row, "lastName"));
      entity.setAddrLine1(text(row, "addrLine1"));
      entity.setAddrLine2(text(row, "addrLine2"));
      entity.setAddrLine3(text(row, "addrLine3"));
      entity.setAddrStateCd(text(row, "addrStateCd"));
      entity.setAddrCountryCd(text(row, "addrCountryCd"));
      entity.setAddrZip(text(row, "addrZip"));
      entity.setPhoneNum1(text(row, "phoneNum1"));
      entity.setPhoneNum2(text(row, "phoneNum2"));
      entity.setSsn(integer(row, "ssn"));
      entity.setGovtIssuedId(text(row, "govtIssuedId"));
      entity.setDobYyyyMmDd(text(row, "dobYyyyMmDd"));
      entity.setEftAccountId(text(row, "eftAccountId"));
      entity.setPriCardHolderInd(text(row, "priCardHolderInd"));
      entity.setFicoCreditScore(integer(row, "ficoCreditScore"));
      values.add(entity);
    }
    save(customerRepository, values);
    return values.size();
  }

  private int loadAccounts(List<byte[]> records, SeedDataset dataset) {
    accountRepository.deleteAllInBatch();
    List<Account> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      Account entity = new Account();
      entity.setAcctId(longValue(row, "acctId"));
      entity.setActiveStatus(text(row, "activeStatus"));
      entity.setCurrBal(decimal(row, "currBal"));
      entity.setCreditLimit(decimal(row, "creditLimit"));
      entity.setCashCreditLimit(decimal(row, "cashCreditLimit"));
      entity.setOpenDate(text(row, "openDate"));
      entity.setExpiraionDate(text(row, "expiraionDate"));
      entity.setReissueDate(text(row, "reissueDate"));
      entity.setCurrCycCredit(decimal(row, "currCycCredit"));
      entity.setCurrCycDebit(decimal(row, "currCycDebit"));
      entity.setAddrZip(text(row, "addrZip"));
      entity.setGroupId(text(row, "groupId"));
      values.add(entity);
    }
    save(accountRepository, values);
    return values.size();
  }

  private int loadCards(List<byte[]> records, SeedDataset dataset) {
    cardRepository.deleteAllInBatch();
    List<Card> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      Card entity = new Card();
      entity.setCardNum(text(row, "cardNum"));
      entity.setAcctId(longValue(row, "acctId"));
      entity.setCvvCd(integer(row, "cvvCd"));
      entity.setEmbossedName(text(row, "embossedName"));
      entity.setExpiraionDate(text(row, "expiraionDate"));
      entity.setActiveStatus(text(row, "activeStatus"));
      values.add(entity);
    }
    save(cardRepository, values);
    return values.size();
  }

  private int loadCardXrefs(List<byte[]> records, SeedDataset dataset) {
    cardXrefRepository.deleteAllInBatch();
    List<CardXref> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      CardXref entity = new CardXref();
      entity.setId(new CardXrefId(text(row, "cardNum")));
      entity.setCustId(integer(row, "custId"));
      entity.setAcctId(longValue(row, "acctId"));
      values.add(entity);
    }
    save(cardXrefRepository, values);
    return values.size();
  }

  private int loadDailyTransactions(List<byte[]> records, SeedDataset dataset) {
    dailyTransactionRepository.deleteAllInBatch();
    List<DailyTransaction> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      DailyTransaction entity = new DailyTransaction();
      entity.setTranId(text(row, "tranId"));
      entity.setTypeCd(text(row, "typeCd"));
      entity.setCatCd(integer(row, "catCd"));
      entity.setSource(text(row, "source"));
      entity.setTranDesc(text(row, "tranDesc"));
      entity.setAmt(decimal(row, "amt"));
      entity.setMerchantId(integer(row, "merchantId"));
      entity.setMerchantName(text(row, "merchantName"));
      entity.setMerchantCity(text(row, "merchantCity"));
      entity.setMerchantZip(text(row, "merchantZip"));
      entity.setCardNum(text(row, "cardNum"));
      entity.setOrigTs(text(row, "origTs"));
      entity.setProcTs(text(row, "procTs"));
      values.add(entity);
    }
    save(dailyTransactionRepository, values);
    return values.size();
  }

  private int loadDisclosureGroups(List<byte[]> records, SeedDataset dataset) {
    disclosureGroupRepository.deleteAllInBatch();
    List<DisclosureGroup> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      DisclosureGroup entity = new DisclosureGroup();
      entity.setId(
          new DisclosureGroupId(
              text(row, "acctGroupId"), text(row, "tranTypeCd"), integer(row, "tranCatCd")));
      entity.setIntRate(decimal(row, "intRate"));
      values.add(entity);
    }
    save(disclosureGroupRepository, values);
    return values.size();
  }

  private int loadBalances(List<byte[]> records, SeedDataset dataset) {
    tranCategoryBalanceRepository.deleteAllInBatch();
    List<TranCategoryBalance> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      TranCategoryBalance entity = new TranCategoryBalance();
      entity.setId(
          new TranCategoryBalanceId(
              longValue(row, "acctId"), text(row, "typeCd"), integer(row, "catCd")));
      entity.setTranCatBal(decimal(row, "tranCatBal"));
      values.add(entity);
    }
    save(tranCategoryBalanceRepository, values);
    return values.size();
  }

  private int loadCategories(List<byte[]> records, SeedDataset dataset) {
    transactionCategoryRepository.deleteAllInBatch();
    List<TransactionCategory> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      TransactionCategory entity = new TransactionCategory();
      entity.setId(new TransactionCategoryId(text(row, "typeCd"), integer(row, "catCd")));
      entity.setCatTypeDesc(text(row, "catTypeDesc"));
      values.add(entity);
    }
    save(transactionCategoryRepository, values);
    return values.size();
  }

  private int loadTypes(List<byte[]> records, SeedDataset dataset) {
    transactionTypeRepository.deleteAllInBatch();
    List<TransactionType> values = new ArrayList<>();
    for (Map<String, Object> row : rows(records, dataset)) {
      TransactionType entity = new TransactionType();
      entity.setTypeCd(text(row, "typeCd"));
      entity.setTypeDesc(text(row, "typeDesc"));
      values.add(entity);
    }
    save(transactionTypeRepository, values);
    return values.size();
  }

  private List<Map<String, Object>> rows(List<byte[]> records, SeedDataset dataset) {
    RecordLayout layout = SeedLayouts.forDataset(dataset);
    List<Map<String, Object>> values = new ArrayList<>(records.size());
    for (byte[] record : records) {
      values.add(
          layout.parse(
              record,
              dataset.resolve(dataRoot).toString().contains("EBCDIC")
                  ? java.nio.charset.Charset.forName("Cp037")
                  : java.nio.charset.StandardCharsets.US_ASCII));
    }
    return values;
  }

  private static String text(Map<String, Object> row, String field) {
    return (String) row.get(field);
  }

  private static Integer integer(Map<String, Object> row, String field) {
    return (Integer) row.get(field);
  }

  private static Long longValue(Map<String, Object> row, String field) {
    Object value = row.get(field);
    return value instanceof Integer integer ? integer.longValue() : (Long) value;
  }

  private static BigDecimal decimal(Map<String, Object> row, String field) {
    return (BigDecimal) row.get(field);
  }

  private static <T> void save(JpaRepository<T, ?> repository, List<T> values) {
    for (int start = 0; start < values.size(); start += BATCH_SIZE) {
      repository.saveAll(values.subList(start, Math.min(start + BATCH_SIZE, values.size())));
      repository.flush();
    }
  }
}
