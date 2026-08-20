package com.aws.carddemo.dataload;

import static com.aws.carddemo.dataload.RecordLayout.Type.INTEGER;
import static com.aws.carddemo.dataload.RecordLayout.Type.LONG;
import static com.aws.carddemo.dataload.RecordLayout.Type.TEXT;
import static com.aws.carddemo.dataload.RecordLayout.Type.ZONED_DECIMAL;

import java.util.List;

final class SeedLayouts {
  private SeedLayouts() {}

  static RecordLayout forDataset(SeedDataset dataset) {
    return switch (dataset) {
      case USRSEC ->
          layout(
              80,
              f("secUsrId", 0, 8, TEXT),
              f("secUsrFname", 8, 20, TEXT),
              f("secUsrLname", 28, 20, TEXT),
              f("secUsrPwd", 48, 8, TEXT),
              f("secUsrType", 56, 1, TEXT));
      case CUSTOMER ->
          layout(
              500,
              f("custId", 0, 9, INTEGER),
              f("firstName", 9, 25, TEXT),
              f("middleName", 34, 25, TEXT),
              f("lastName", 59, 25, TEXT),
              f("addrLine1", 84, 50, TEXT),
              f("addrLine2", 134, 50, TEXT),
              f("addrLine3", 184, 50, TEXT),
              f("addrStateCd", 234, 2, TEXT),
              f("addrCountryCd", 236, 3, TEXT),
              f("addrZip", 239, 10, TEXT),
              f("phoneNum1", 249, 15, TEXT),
              f("phoneNum2", 264, 15, TEXT),
              f("ssn", 279, 9, INTEGER),
              f("govtIssuedId", 288, 20, TEXT),
              f("dobYyyyMmDd", 308, 10, TEXT),
              f("eftAccountId", 318, 10, TEXT),
              f("priCardHolderInd", 328, 1, TEXT),
              f("ficoCreditScore", 329, 3, INTEGER));
      case ACCOUNT ->
          layout(
              300,
              f("acctId", 0, 11, LONG),
              f("activeStatus", 11, 1, TEXT),
              f("currBal", 12, 12, ZONED_DECIMAL, 2),
              f("creditLimit", 24, 12, ZONED_DECIMAL, 2),
              f("cashCreditLimit", 36, 12, ZONED_DECIMAL, 2),
              f("openDate", 48, 10, TEXT),
              f("expiraionDate", 58, 10, TEXT),
              f("reissueDate", 68, 10, TEXT),
              f("currCycCredit", 78, 12, ZONED_DECIMAL, 2),
              f("currCycDebit", 90, 12, ZONED_DECIMAL, 2),
              f("addrZip", 102, 10, TEXT),
              f("groupId", 112, 10, TEXT));
      case CARD ->
          layout(
              150,
              f("cardNum", 0, 16, TEXT),
              f("acctId", 16, 11, LONG),
              f("cvvCd", 27, 3, INTEGER),
              f("embossedName", 30, 50, TEXT),
              f("expiraionDate", 80, 10, TEXT),
              f("activeStatus", 90, 1, TEXT));
      case CARD_XREF ->
          layout(
              36,
              f("cardNum", 0, 16, TEXT),
              f("custId", 16, 9, INTEGER),
              f("acctId", 25, 11, LONG));
      case DAILY_TRANSACTION ->
          transactionLayout(
              350,
              "tranId",
              "typeCd",
              "catCd",
              "source",
              "tranDesc",
              "amt",
              "merchantId",
              "merchantName",
              "merchantCity",
              "merchantZip",
              "cardNum",
              "origTs",
              "procTs");
      case DISCLOSURE_GROUP ->
          layout(
              50,
              f("acctGroupId", 0, 10, TEXT),
              f("tranTypeCd", 10, 2, TEXT),
              f("tranCatCd", 12, 4, INTEGER),
              f("intRate", 16, 6, ZONED_DECIMAL, 2));
      case TRAN_CATEGORY_BALANCE ->
          layout(
              50,
              f("acctId", 0, 11, LONG),
              f("typeCd", 11, 2, TEXT),
              f("catCd", 13, 4, INTEGER),
              f("tranCatBal", 17, 11, ZONED_DECIMAL, 2));
      case TRANSACTION_CATEGORY ->
          layout(
              60,
              f("typeCd", 0, 2, TEXT),
              f("catCd", 2, 4, INTEGER),
              f("catTypeDesc", 6, 50, TEXT));
      case TRANSACTION_TYPE -> layout(60, f("typeCd", 0, 2, TEXT), f("typeDesc", 2, 50, TEXT));
    };
  }

  private static RecordLayout transactionLayout(
      int length,
      String tranId,
      String typeCd,
      String catCd,
      String source,
      String tranDesc,
      String amt,
      String merchantId,
      String merchantName,
      String merchantCity,
      String merchantZip,
      String cardNum,
      String origTs,
      String procTs) {
    return layout(
        length,
        f(tranId, 0, 16, TEXT),
        f(typeCd, 16, 2, TEXT),
        f(catCd, 18, 4, INTEGER),
        f(source, 22, 10, TEXT),
        f(tranDesc, 32, 100, TEXT),
        f(amt, 132, 11, ZONED_DECIMAL, 2),
        f(merchantId, 143, 9, INTEGER),
        f(merchantName, 152, 50, TEXT),
        f(merchantCity, 202, 50, TEXT),
        f(merchantZip, 252, 10, TEXT),
        f(cardNum, 262, 16, TEXT),
        f(origTs, 278, 26, TEXT),
        f(procTs, 304, 26, TEXT));
  }

  private static RecordLayout layout(int length, RecordLayout.FieldDefinition... fields) {
    return new RecordLayout(length, List.of(fields));
  }

  private static RecordLayout.FieldDefinition f(
      String name, int offset, int length, RecordLayout.Type type) {
    return new RecordLayout.FieldDefinition(name, offset, length, type, 0);
  }

  private static RecordLayout.FieldDefinition f(
      String name, int offset, int length, RecordLayout.Type type, int scale) {
    return new RecordLayout.FieldDefinition(name, offset, length, type, scale);
  }
}
