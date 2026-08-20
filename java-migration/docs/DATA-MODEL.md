# COBOL to relational data model

## Rules

- `PIC X(n)` maps to `VARCHAR(n)`.
- `PIC 9(n)` maps to `NUMERIC(n,0)`.
- `PIC S9(p)V99` maps to `NUMERIC(p+2,2)`: therefore `S9(10)V99` is
  `NUMERIC(12,2)` and `S9(09)V99` is `NUMERIC(11,2)`.
- COBOL `FILLER` bytes are retained in the fixed-record boundary but dropped
  from the relational model.
- Date and timestamp fields that are `PIC X` remain `VARCHAR` in phase 1.
  DATE/TIMESTAMP conversion is deferred so spaces and other source values
  remain lossless.
- `ACCT-EXPIRAION-DATE` and `CARD-EXPIRAION-DATE` are normalized to the SQL
  name `expiration_date`.
- Java numeric fields use `BigDecimal`, including integer-like keys, with
  explicit JPA precision and scale.

Offsets below are one-based byte offsets in the copybook record.  All fields
in these seven core copybooks are display-character or unpacked numeric
fields; the record lengths include the dropped filler.

## `customer` from `CVCUS01Y` (RECLN 500)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| CUST-ID | 9(09) | 1/9 | cust_id | NUMERIC(9,0) | custId / BigDecimal | PK |
| CUST-FIRST-NAME | X(25) | 10/25 | first_name | VARCHAR(25) | firstName / String | |
| CUST-MIDDLE-NAME | X(25) | 35/25 | middle_name | VARCHAR(25) | middleName / String | |
| CUST-LAST-NAME | X(25) | 60/25 | last_name | VARCHAR(25) | lastName / String | |
| CUST-ADDR-LINE-1 | X(50) | 85/50 | addr_line_1 | VARCHAR(50) | addrLine1 / String | |
| CUST-ADDR-LINE-2 | X(50) | 135/50 | addr_line_2 | VARCHAR(50) | addrLine2 / String | |
| CUST-ADDR-LINE-3 | X(50) | 185/50 | addr_line_3 | VARCHAR(50) | addrLine3 / String | |
| CUST-ADDR-STATE-CD | X(2) | 235/2 | addr_state_cd | VARCHAR(2) | addrStateCd / String | |
| CUST-ADDR-COUNTRY-CD | X(3) | 237/3 | addr_country_cd | VARCHAR(3) | addrCountryCd / String | |
| CUST-ADDR-ZIP | X(10) | 240/10 | addr_zip | VARCHAR(10) | addrZip / String | |
| CUST-PHONE-NUM-1 | X(15) | 250/15 | phone_num_1 | VARCHAR(15) | phoneNum1 / String | |
| CUST-PHONE-NUM-2 | X(15) | 265/15 | phone_num_2 | VARCHAR(15) | phoneNum2 / String | |
| CUST-SSN | 9(09) | 280/9 | ssn | NUMERIC(9,0) | ssn / BigDecimal | |
| CUST-GOVT-ISSUED-ID | X(20) | 289/20 | govt_issued_id | VARCHAR(20) | govtIssuedId / String | |
| CUST-DOB-YYYY-MM-DD | X(10) | 309/10 | dob_yyyy_mm_dd | VARCHAR(10) | dobYyyyMmDd / String | date retained as text |
| CUST-EFT-ACCOUNT-ID | X(10) | 319/10 | eft_account_id | VARCHAR(10) | eftAccountId / String | |
| CUST-PRI-CARD-HOLDER-IND | X(1) | 329/1 | pri_card_holder_ind | VARCHAR(1) | priCardHolderInd / String | |
| CUST-FICO-CREDIT-SCORE | 9(03) | 330/3 | fico_credit_score | NUMERIC(3,0) | ficoCreditScore / BigDecimal | |
| FILLER | X(168) | 333/168 | — | — | — | dropped |

## `account` from `CVACT01Y` (RECLN 300)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| ACCT-ID | 9(11) | 1/11 | acct_id | NUMERIC(11,0) | acctId / BigDecimal | PK |
| ACCT-ACTIVE-STATUS | X(1) | 12/1 | active_status | VARCHAR(1) | activeStatus / String | |
| ACCT-CURR-BAL | S9(10)V99 | 13/12 | curr_bal | NUMERIC(12,2) | currBal / BigDecimal | |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 25/12 | credit_limit | NUMERIC(12,2) | creditLimit / BigDecimal | |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 37/12 | cash_credit_limit | NUMERIC(12,2) | cashCreditLimit / BigDecimal | |
| ACCT-OPEN-DATE | X(10) | 49/10 | open_date | VARCHAR(10) | openDate / String | |
| ACCT-EXPIRAION-DATE | X(10) | 59/10 | expiration_date | VARCHAR(10) | expirationDate / String | typo normalized |
| ACCT-REISSUE-DATE | X(10) | 69/10 | reissue_date | VARCHAR(10) | reissueDate / String | |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 79/12 | curr_cyc_credit | NUMERIC(12,2) | currCycCredit / BigDecimal | |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 91/12 | curr_cyc_debit | NUMERIC(12,2) | currCycDebit / BigDecimal | |
| ACCT-ADDR-ZIP | X(10) | 103/10 | addr_zip | VARCHAR(10) | addrZip / String | |
| ACCT-GROUP-ID | X(10) | 113/10 | group_id | VARCHAR(10) | groupId / String | |
| FILLER | X(178) | 123/178 | — | — | — | dropped |

## `card` from `CVACT02Y` (RECLN 150)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| CARD-NUM | X(16) | 1/16 | card_num | VARCHAR(16) | cardNum / String | PK |
| CARD-ACCT-ID | 9(11) | 17/11 | acct_id | NUMERIC(11,0) | acctId / BigDecimal | FK account |
| CARD-CVV-CD | 9(03) | 28/3 | cvv_cd | NUMERIC(3,0) | cvvCd / BigDecimal | |
| CARD-EMBOSSED-NAME | X(50) | 31/50 | embossed_name | VARCHAR(50) | embossedName / String | |
| CARD-EXPIRAION-DATE | X(10) | 81/10 | expiration_date | VARCHAR(10) | expirationDate / String | typo normalized |
| CARD-ACTIVE-STATUS | X(1) | 91/1 | active_status | VARCHAR(1) | activeStatus / String | |
| FILLER | X(59) | 92/59 | — | — | — | dropped |

## `card_xref` from `CVACT03Y` (RECLN 50)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| XREF-CARD-NUM | X(16) | 1/16 | card_num | VARCHAR(16) | cardNum / String | PK and FK card |
| XREF-CUST-ID | 9(09) | 17/9 | cust_id | NUMERIC(9,0) | custId / BigDecimal | FK customer |
| XREF-ACCT-ID | 9(11) | 26/11 | acct_id | NUMERIC(11,0) | acctId / BigDecimal | FK account |
| FILLER | X(14) | 37/14 | — | — | — | dropped |

The VSAM `CCXREF` file is therefore a relational join table.  `CARDAIX`
and `CXACAIX` are alternate indexes, not extra records: they become
relational indexes for card/account lookups.  The current migration creates
indexes on `card_xref(cust_id)` and `card_xref(acct_id)`; the source's
CARDAIX path is also represented by the card/account relationship and is
not a separate table.

## `transaction` from `CVTRA05Y` (RECLN 350)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| TRAN-ID | X(16) | 1/16 | tran_id | VARCHAR(16) | tranId / String | PK |
| TRAN-TYPE-CD | X(2) | 17/2 | tran_type_cd | VARCHAR(2) | tranTypeCd / String | |
| TRAN-CAT-CD | 9(04) | 19/4 | tran_cat_cd | NUMERIC(4,0) | tranCatCd / BigDecimal | |
| TRAN-SOURCE | X(10) | 23/10 | tran_source | VARCHAR(10) | tranSource / String | |
| TRAN-DESC | X(100) | 33/100 | tran_desc | VARCHAR(100) | tranDesc / String | |
| TRAN-AMT | S9(09)V99 | 133/11 | tran_amt | NUMERIC(11,2) | tranAmt / BigDecimal | |
| TRAN-MERCHANT-ID | 9(09) | 144/9 | merchant_id | NUMERIC(9,0) | merchantId / BigDecimal | |
| TRAN-MERCHANT-NAME | X(50) | 153/50 | merchant_name | VARCHAR(50) | merchantName / String | |
| TRAN-MERCHANT-CITY | X(50) | 203/50 | merchant_city | VARCHAR(50) | merchantCity / String | |
| TRAN-MERCHANT-ZIP | X(10) | 253/10 | merchant_zip | VARCHAR(10) | merchantZip / String | |
| TRAN-CARD-NUM | X(16) | 263/16 | card_num | VARCHAR(16) | cardNum / String | FK card |
| TRAN-ORIG-TS | X(26) | 279/26 | orig_ts | VARCHAR(26) | origTs / String | timestamp retained as text |
| TRAN-PROC-TS | X(26) | 305/26 | proc_ts | VARCHAR(26) | procTs / String | timestamp retained as text |
| FILLER | X(20) | 331/20 | — | — | — | dropped |

## `tran_cat_bal` from `CVTRA01Y` (RECLN 50)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| TRANCAT-ACCT-ID | 9(11) | 1/11 | acct_id | NUMERIC(11,0) | id.acctId / BigDecimal | composite PK, FK account |
| TRANCAT-TYPE-CD | X(2) | 12/2 | tran_type_cd | VARCHAR(2) | id.tranTypeCd / String | composite PK |
| TRANCAT-CD | 9(04) | 14/4 | tran_cat_cd | NUMERIC(4,0) | id.tranCatCd / BigDecimal | composite PK |
| TRAN-CAT-BAL | S9(09)V99 | 18/12 | tran_cat_bal | NUMERIC(11,2) | tranCatBal / BigDecimal | |
| FILLER | X(22) | 30/22 | — | — | — | dropped |

The composite VSAM `TRAN-CAT-KEY` is preserved as the JPA
`TranCatBalId`/SQL composite primary key `(acct_id, tran_type_cd,
tran_cat_cd)`.

## `usrsec` from `CSUSR01Y` (80 bytes)

| COBOL field | PIC | Offset/length | SQL column | SQL type | Java/JPA field | Notes |
|---|---|---:|---|---|---|---|
| SEC-USR-ID | X(8) | 1/8 | sec_usr_id | VARCHAR(8) | secUsrId / String | PK |
| SEC-USR-FNAME | X(20) | 9/20 | sec_usr_fname | VARCHAR(20) | secUsrFname / String | |
| SEC-USR-LNAME | X(20) | 29/20 | sec_usr_lname | VARCHAR(20) | secUsrLname / String | |
| SEC-USR-PWD | X(8) | 49/8 | sec_usr_pwd | VARCHAR(8) | secUsrPwd / String | plaintext parity |
| SEC-USR-TYPE | X(1) | 57/1 | sec_usr_type | VARCHAR(1) | secUsrType / String | |
| SEC-USR-FILLER | X(23) | 58/23 | — | — | — | dropped |

`CSUSR01Y` has no `COMP`, `COMP-3`, or `BINARY` fields.  The batch reader
decodes its complete 80-byte record with IBM037 display-character decoding;
it does not claim packed-decimal decoding.  The reusable packed-decimal
decoder is for later layouts.  In this repository the actual packed/binary
uses are in `CVEXPORT.cpy` (including `COMP-3` FICO, balances, and
transaction amount, and `COMP` integer fields) and date utility work areas
in `CSUTLDWY.cpy`; the core migration copybooks above do not use them.

Passwords remain plaintext to preserve `COSGN00C` parity.  Hashing is
explicitly deferred to a later migration phase.

## Foreign keys and schema ownership

`card.acct_id` references `account.acct_id`; `card_xref.card_num`,
`card_xref.cust_id`, and `card_xref.acct_id` reference card, customer, and
account respectively; `transaction.card_num` references card; and
`tran_cat_bal.acct_id` references account.  Flyway owns the SQL schema.
JPA entities use plain FK columns for the xref/card relationships and an
embedded ID for `tran_cat_bal`.
