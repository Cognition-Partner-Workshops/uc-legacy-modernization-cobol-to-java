# CardDemo Java data model

The tables below are derived from the authoritative `docs/INVENTORY.md` and the
record copybooks in `app/cpy`. Offsets are zero-based byte offsets in the
fixed-width record. COBOL `PIC X(n)` remains text, including date fields; this
preserves blanks and low-values found in legacy seed data. `FILLER` is retained
in this mapping ledger for traceability but is not a database column.

## Security user (`CSUSR01Y`, 80 bytes)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CSUSR01Y | SEC-USR-ID | X(08) | 0/8 | usrsec.sec_usr_id | VARCHAR(8) | PK |
| CSUSR01Y | SEC-USR-FNAME | X(20) | 8/20 | usrsec.sec_usr_fname | VARCHAR(20) | |
| CSUSR01Y | SEC-USR-LNAME | X(20) | 28/20 | usrsec.sec_usr_lname | VARCHAR(20) | |
| CSUSR01Y | SEC-USR-PWD | X(08) | 48/8 | usrsec.sec_usr_pwd | VARCHAR(8) | |
| CSUSR01Y | SEC-USR-TYPE | X(01) | 56/1 | usrsec.sec_usr_type | VARCHAR(1) | |
| CSUSR01Y | SEC-USR-FILLER | X(23) | 57/23 | — | — | FILLER dropped |

## Customer (`CVCUS01Y`, 500 bytes; duplicate `CUSTREC` uses the same layout)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVCUS01Y | CUST-ID | 9(09) | 0/9 | customer.cust_id | INTEGER | PK |
| CVCUS01Y | CUST-FIRST-NAME | X(25) | 9/25 | customer.first_name | VARCHAR(25) | prefix stripped |
| CVCUS01Y | CUST-MIDDLE-NAME | X(25) | 34/25 | customer.middle_name | VARCHAR(25) | |
| CVCUS01Y | CUST-LAST-NAME | X(25) | 59/25 | customer.last_name | VARCHAR(25) | |
| CVCUS01Y | CUST-ADDR-LINE-1 | X(50) | 84/50 | customer.addr_line_1 | VARCHAR(50) | |
| CVCUS01Y | CUST-ADDR-LINE-2 | X(50) | 134/50 | customer.addr_line_2 | VARCHAR(50) | |
| CVCUS01Y | CUST-ADDR-LINE-3 | X(50) | 184/50 | customer.addr_line_3 | VARCHAR(50) | |
| CVCUS01Y | CUST-ADDR-STATE-CD | X(02) | 234/2 | customer.addr_state_cd | VARCHAR(2) | |
| CVCUS01Y | CUST-ADDR-COUNTRY-CD | X(03) | 236/3 | customer.addr_country_cd | VARCHAR(3) | |
| CVCUS01Y | CUST-ADDR-ZIP | X(10) | 239/10 | customer.addr_zip | VARCHAR(10) | |
| CVCUS01Y | CUST-PHONE-NUM-1 | X(15) | 249/15 | customer.phone_num_1 | VARCHAR(15) | |
| CVCUS01Y | CUST-PHONE-NUM-2 | X(15) | 264/15 | customer.phone_num_2 | VARCHAR(15) | |
| CVCUS01Y | CUST-SSN | 9(09) | 279/9 | customer.ssn | INTEGER | |
| CVCUS01Y | CUST-GOVT-ISSUED-ID | X(20) | 288/20 | customer.govt_issued_id | VARCHAR(20) | |
| CVCUS01Y | CUST-DOB-YYYY-MM-DD | X(10) | 308/10 | customer.dob_yyyy_mm_dd | VARCHAR(10) | date-as-text |
| CVCUS01Y | CUST-EFT-ACCOUNT-ID | X(10) | 318/10 | customer.eft_account_id | VARCHAR(10) | |
| CVCUS01Y | CUST-PRI-CARD-HOLDER-IND | X(01) | 328/1 | customer.pri_card_holder_ind | VARCHAR(1) | |
| CVCUS01Y | CUST-FICO-CREDIT-SCORE | 9(03) | 329/3 | customer.fico_credit_score | INTEGER | |
| CVCUS01Y | FILLER | X(168) | 332/168 | — | — | FILLER dropped |

## Account (`CVACT01Y`, 300 bytes)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVACT01Y | ACCT-ID | 9(11) | 0/11 | account.acct_id | BIGINT | PK |
| CVACT01Y | ACCT-ACTIVE-STATUS | X(01) | 11/1 | account.active_status | VARCHAR(1) | |
| CVACT01Y | ACCT-CURR-BAL | S9(10)V99 | 12/12 | account.curr_bal | NUMERIC(12,2) | signed zoned decimal |
| CVACT01Y | ACCT-CREDIT-LIMIT | S9(10)V99 | 24/12 | account.credit_limit | NUMERIC(12,2) | signed zoned decimal |
| CVACT01Y | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36/12 | account.cash_credit_limit | NUMERIC(12,2) | signed zoned decimal |
| CVACT01Y | ACCT-OPEN-DATE | X(10) | 48/10 | account.open_date | VARCHAR(10) | date-as-text |
| CVACT01Y | ACCT-EXPIRAION-DATE | X(10) | 58/10 | account.expiraion_date | VARCHAR(10) | source spelling preserved |
| CVACT01Y | ACCT-REISSUE-DATE | X(10) | 68/10 | account.reissue_date | VARCHAR(10) | date-as-text |
| CVACT01Y | ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78/12 | account.curr_cyc_credit | NUMERIC(12,2) | signed zoned decimal |
| CVACT01Y | ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90/12 | account.curr_cyc_debit | NUMERIC(12,2) | signed zoned decimal |
| CVACT01Y | ACCT-ADDR-ZIP | X(10) | 102/10 | account.addr_zip | VARCHAR(10) | |
| CVACT01Y | ACCT-GROUP-ID | X(10) | 112/10 | account.group_id | VARCHAR(10) | |
| CVACT01Y | FILLER | X(178) | 122/178 | — | — | FILLER dropped |

## Card and card cross-reference (`CVACT02Y`, 150; `CVACT03Y`, 50)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVACT02Y | CARD-NUM | X(16) | 0/16 | card.card_num | VARCHAR(16) | PK |
| CVACT02Y | CARD-ACCT-ID | 9(11) | 16/11 | card.acct_id | BIGINT | FK account |
| CVACT02Y | CARD-CVV-CD | 9(03) | 27/3 | card.cvv_cd | INTEGER | |
| CVACT02Y | CARD-EMBOSSED-NAME | X(50) | 30/50 | card.embossed_name | VARCHAR(50) | |
| CVACT02Y | CARD-EXPIRAION-DATE | X(10) | 80/10 | card.expiraion_date | VARCHAR(10) | date-as-text; source spelling preserved |
| CVACT02Y | CARD-ACTIVE-STATUS | X(01) | 90/1 | card.active_status | VARCHAR(1) | |
| CVACT02Y | FILLER | X(59) | 91/59 | — | — | FILLER dropped |
| CVACT03Y | XREF-CARD-NUM | X(16) | 0/16 | card_xref.card_num | VARCHAR(16) | PK, FK card |
| CVACT03Y | XREF-CUST-ID | 9(09) | 16/9 | card_xref.cust_id | INTEGER | FK customer |
| CVACT03Y | XREF-ACCT-ID | 9(11) | 25/11 | card_xref.acct_id | BIGINT | FK account |
| CVACT03Y | FILLER | X(14) | 36/14 | — | — | FILLER dropped |

## Category balance and disclosure group

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVTRA01Y | TRANCAT-ACCT-ID | 9(11) | 0/11 | tran_category_balance.acct_id | BIGINT | composite PK, FK account |
| CVTRA01Y | TRANCAT-TYPE-CD | X(02) | 11/2 | tran_category_balance.type_cd | VARCHAR(2) | composite PK |
| CVTRA01Y | TRANCAT-CD | 9(04) | 13/4 | tran_category_balance.cat_cd | INTEGER | composite PK |
| CVTRA01Y | TRAN-CAT-BAL | S9(09)V99 | 17/11 | tran_category_balance.tran_cat_bal | NUMERIC(11,2) | signed zoned decimal |
| CVTRA01Y | FILLER | X(22) | 28/22 | — | — | FILLER dropped |
| CVTRA02Y | DIS-ACCT-GROUP-ID | X(10) | 0/10 | disclosure_group.acct_group_id | VARCHAR(10) | composite PK |
| CVTRA02Y | DIS-TRAN-TYPE-CD | X(02) | 10/2 | disclosure_group.tran_type_cd | VARCHAR(2) | composite PK |
| CVTRA02Y | DIS-TRAN-CAT-CD | 9(04) | 12/4 | disclosure_group.tran_cat_cd | INTEGER | composite PK |
| CVTRA02Y | DIS-INT-RATE | S9(04)V99 | 16/6 | disclosure_group.int_rate | NUMERIC(6,2) | signed zoned decimal |
| CVTRA02Y | FILLER | X(28) | 22/28 | — | — | FILLER dropped |

## Transaction type/category (`CVTRA03Y`, 60; `CVTRA04Y`, 60)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVTRA03Y | TRAN-TYPE | X(02) | 0/2 | transaction_type.type_cd | VARCHAR(2) | PK |
| CVTRA03Y | TRAN-TYPE-DESC | X(50) | 2/50 | transaction_type.type_desc | VARCHAR(50) | |
| CVTRA03Y | FILLER | X(08) | 52/8 | — | — | FILLER dropped |
| CVTRA04Y | TRAN-TYPE-CD | X(02) | 0/2 | transaction_category.type_cd | VARCHAR(2) | composite PK, FK transaction_type |
| CVTRA04Y | TRAN-CAT-CD | 9(04) | 2/4 | transaction_category.cat_cd | INTEGER | composite PK |
| CVTRA04Y | TRAN-CAT-TYPE-DESC | X(50) | 6/50 | transaction_category.cat_type_desc | VARCHAR(50) | |
| CVTRA04Y | FILLER | X(04) | 56/4 | — | — | FILLER dropped |

## Transaction and daily transaction (`CVTRA05Y` and `CVTRA06Y`, 350 bytes each)

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CVTRA05Y/CVTRA06Y | TRAN-ID/DALYTRAN-ID | X(16) | 0/16 | transaction.tran_id / daily_transaction.tran_id | VARCHAR(16) | PK |
| CVTRA05Y/CVTRA06Y | TRAN-TYPE-CD/DALYTRAN-TYPE-CD | X(02) | 16/2 | transaction.type_cd / daily_transaction.type_cd | VARCHAR(2) | |
| CVTRA05Y/CVTRA06Y | TRAN-CAT-CD/DALYTRAN-CAT-CD | 9(04) | 18/4 | transaction.cat_cd / daily_transaction.cat_cd | INTEGER | |
| CVTRA05Y/CVTRA06Y | TRAN-SOURCE/DALYTRAN-SOURCE | X(10) | 22/10 | transaction.source / daily_transaction.source | VARCHAR(10) | |
| CVTRA05Y/CVTRA06Y | TRAN-DESC/DALYTRAN-DESC | X(100) | 32/100 | transaction.tran_desc / daily_transaction.tran_desc | VARCHAR(100) | |
| CVTRA05Y/CVTRA06Y | TRAN-AMT/DALYTRAN-AMT | S9(09)V99 | 132/11 | transaction.amt / daily_transaction.amt | NUMERIC(11,2) | signed zoned decimal |
| CVTRA05Y/CVTRA06Y | TRAN-MERCHANT-ID/DALYTRAN-MERCHANT-ID | 9(09) | 143/9 | transaction.merchant_id / daily_transaction.merchant_id | INTEGER | |
| CVTRA05Y/CVTRA06Y | TRAN-MERCHANT-NAME/DALYTRAN-MERCHANT-NAME | X(50) | 152/50 | transaction.merchant_name / daily_transaction.merchant_name | VARCHAR(50) | |
| CVTRA05Y/CVTRA06Y | TRAN-MERCHANT-CITY/DALYTRAN-MERCHANT-CITY | X(50) | 202/50 | transaction.merchant_city / daily_transaction.merchant_city | VARCHAR(50) | |
| CVTRA05Y/CVTRA06Y | TRAN-MERCHANT-ZIP/DALYTRAN-MERCHANT-ZIP | X(10) | 252/10 | transaction.merchant_zip / daily_transaction.merchant_zip | VARCHAR(10) | |
| CVTRA05Y/CVTRA06Y | TRAN-CARD-NUM/DALYTRAN-CARD-NUM | X(16) | 262/16 | transaction.card_num / daily_transaction.card_num | VARCHAR(16) | indexed; nullable-tolerant |
| CVTRA05Y/CVTRA06Y | TRAN-ORIG-TS/DALYTRAN-ORIG-TS | X(26) | 278/26 | transaction.orig_ts / daily_transaction.orig_ts | VARCHAR(26) | timestamp-as-text |
| CVTRA05Y/CVTRA06Y | TRAN-PROC-TS/DALYTRAN-PROC-TS | X(26) | 304/26 | transaction.proc_ts / daily_transaction.proc_ts | VARCHAR(26) | timestamp-as-text |
| CVTRA05Y/CVTRA06Y | FILLER | X(20) | 330/20 | — | — | FILLER dropped |

## Daily reject record (`CBTRN02C`, 430 bytes)

The first 350 bytes are the `CVTRA06Y` record above. The reject trailer is:

| Copybook | COBOL field | PIC | Offset/length | Target table.column | SQL type | Notes |
|---|---|---|---:|---|---|---|
| CBTRN02C | VALIDATION-TRAILER | X(80) | 350/80 | daily_transaction_reject.validation_trailer | VARCHAR(80) | raw trailer retained |
| CBTRN02C | WS-VALIDATION-FAIL-REASON | 9(04) | 350/4 | daily_transaction_reject.validation_fail_reason | INTEGER | logical subfield |
| CBTRN02C | WS-VALIDATION-FAIL-REASON-DESC | X(76) | 354/76 | daily_transaction_reject.validation_fail_reason_desc | VARCHAR(76) | logical subfield |

## Relationship decisions

The VSAM `CCXREF` and its `CXACAIX` alternate index are represented by
`card_xref` foreign keys and `idx_card_xref_acct_id`; `CARDAIX` is
`idx_card_acct_id`. The xref card key is also a real FK to `card`, while
customer and account links are real FKs rather than application navigation.
Transaction `card_num` deliberately has **no FK**: `app/data/ASCII/dailytran.txt`
contains daily records whose card numbers are not guaranteed to have a matching
card seed row, and the inventory explicitly requires nullable-tolerant batch
loading. It remains a plain indexed column. Transaction category references are
enforced through `(type_cd, cat_cd)` and category-to-type through `type_cd`.

The fixed-width ASCII seed verification found no violations:

| Relationship | Referencing rows checked | Violations |
|---|---:|---:|
| `card_xref.card_num` → `card.card_num` | 50 | 0 |
| `card_xref.acct_id` → `account.acct_id` | 50 | 0 |
| `card_xref.cust_id` → `customer.cust_id` | 50 | 0 |
| `card.acct_id` → `account.acct_id` | 50 | 0 |

The check used the fixed-width offsets from this document over
`custdata.txt`, `acctdata.txt`, `carddata.txt`, and `cardxref.txt`; no
whitespace splitting or inferred relationships were used.

For dataload, blank and low-value numeric fields are represented as SQL
`NULL`; they are not coerced to zero. Text fields, including date fields,
retain their trimmed text value, while low-values remain distinguishable from
numeric zero.

All numeric money/rate fields use exact COBOL precision and scale. No date
field was converted to SQL `DATE`; callers can use `DateValidator` and
`DateConverter` from `common`.
