# Reconciliation Checks

Per-job validation specifications for the CardDemo COBOL-to-Java migration. Each check defines the invariant, the data files involved, and how to validate correctness after migration.

---

## 1. Referential Integrity Checks

### RI-001: Card Cross-Reference → Card Data

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-001                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `cardxref.txt` (CVACT03Y.cpy)                 |
| **Target File** | `carddata.txt` (CVACT02Y.cpy)                 |
| **Rule**       | Every `XREF-CARD-NUM` in cardxref must exist as `CARD-NUM` in carddata |
| **Join Key**   | `XREF-CARD-NUM` = `CARD-NUM` (PIC X(16))      |
| **Action on Fail** | Flag orphaned cross-reference records      |

### RI-002: Card Cross-Reference → Account Data

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-002                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `cardxref.txt` (CVACT03Y.cpy)                 |
| **Target File** | `acctdata.txt` (CVACT01Y.cpy)                 |
| **Rule**       | Every `XREF-ACCT-ID` in cardxref must exist as `ACCT-ID` in acctdata |
| **Join Key**   | `XREF-ACCT-ID` = `ACCT-ID` (PIC 9(11))        |
| **Action on Fail** | Flag orphaned cross-reference records      |

### RI-003: Card Cross-Reference → Customer Data

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-003                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `cardxref.txt` (CVACT03Y.cpy)                 |
| **Target File** | `custdata.txt` (CVCUS01Y.cpy)                 |
| **Rule**       | Every `XREF-CUST-ID` in cardxref must exist as `CUST-ID` in custdata |
| **Join Key**   | `XREF-CUST-ID` = `CUST-ID` (PIC 9(09))        |
| **Action on Fail** | Flag orphaned cross-reference records      |

### RI-004: Card Data → Account Data

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-004                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `carddata.txt` (CVACT02Y.cpy)                 |
| **Target File** | `acctdata.txt` (CVACT01Y.cpy)                 |
| **Rule**       | Every `CARD-ACCT-ID` in carddata must exist as `ACCT-ID` in acctdata |
| **Join Key**   | `CARD-ACCT-ID` = `ACCT-ID` (PIC 9(11))        |
| **Action on Fail** | Flag cards referencing non-existent accounts |

### RI-005: Daily Transactions → Card Cross-Reference

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-005                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `dailytran.txt` (CVTRA06Y.cpy)                |
| **Target File** | `cardxref.txt` (CVACT03Y.cpy)                 |
| **Rule**       | Every `DALYTRAN-CARD-NUM` in dailytran must exist as `XREF-CARD-NUM` in cardxref |
| **Join Key**   | `DALYTRAN-CARD-NUM` = `XREF-CARD-NUM` (PIC X(16)) |
| **Action on Fail** | These transactions would be rejected by POSTTRAN (CBTRN02C) |

### RI-006: Transaction Category Balance → Account Data

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | RI-006                                         |
| **Severity**   | ERROR                                          |
| **Source File** | `tcatbal.txt` (CVTRA01Y.cpy)                  |
| **Target File** | `acctdata.txt` (CVACT01Y.cpy)                 |
| **Rule**       | Every `TRANCAT-ACCT-ID` in tcatbal must exist as `ACCT-ID` in acctdata |
| **Join Key**   | `TRANCAT-ACCT-ID` = `ACCT-ID` (PIC 9(11))     |
| **Action on Fail** | Flag orphaned balance records              |

---

## 2. Aggregate Consistency Checks

### AG-001: Category Balance Reconciliation

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | AG-001                                         |
| **Severity**   | WARNING                                        |
| **Source Files** | `tcatbal.txt` (CVTRA01Y.cpy), `acctdata.txt` (CVACT01Y.cpy) |
| **Rule**       | Sum of `TRAN-CAT-BAL` grouped by `TRANCAT-ACCT-ID` must be reconcilable with `ACCT-CURR-BAL` for the corresponding account |
| **Tolerance**  | 0.01 (rounding differences from packed decimal) |
| **Notes**      | This is a soft check — the category balance may lag behind the current balance if transactions have been posted but interest has not yet been calculated |

**Validation Logic:**
```
For each ACCT-ID in acctdata:
    sum_cat_bal = SUM(TRAN-CAT-BAL) WHERE TRANCAT-ACCT-ID = ACCT-ID
    If |ACCT-CURR-BAL - sum_cat_bal| > tolerance:
        Report WARNING with both values
```

### AG-002: Record Count Parity

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | AG-002                                         |
| **Severity**   | ERROR                                          |
| **Source**     | COBOL golden files                              |
| **Target**     | Java-migrated output files                      |
| **Rule**       | Record count for each entity type must be identical between COBOL and Java outputs |

**Expected Counts (baseline):**

| Entity                | File          | Count |
| :-------------------- | :------------ | ----: |
| Accounts              | acctdata.txt  |    50 |
| Customers             | custdata.txt  |    50 |
| Cards                 | carddata.txt  |    50 |
| Card Cross-References | cardxref.txt  |    50 |
| Daily Transactions    | dailytran.txt |   300 |
| Transaction Types     | trantype.txt  |     7 |
| Transaction Categories| trancatg.txt  |    18 |
| Category Balances     | tcatbal.txt   |    50 |
| Disclosure Groups     | discgrp.txt   |    51 |

### AG-003: Transaction Amount Conservation

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | AG-003                                         |
| **Severity**   | WARNING                                        |
| **Source Files** | `dailytran.txt` (CVTRA06Y.cpy), `acctdata.txt` (CVACT01Y.cpy), `cardxref.txt` (CVACT03Y.cpy) |
| **Rule**       | Sum of `DALYTRAN-AMT` grouped by account (via card → xref → account join) must be consistent with cycle debit/credit totals in the account |
| **Tolerance**  | 0.01                                           |

**Validation Logic:**
```
For each DALYTRAN-CARD-NUM in dailytran:
    Look up XREF-ACCT-ID via cardxref
    Accumulate DALYTRAN-AMT by account:
        Positive amounts → cycle_credits
        Negative amounts → cycle_debits
    Compare with ACCT-CURR-CYC-CREDIT and ACCT-CURR-CYC-DEBIT
```

---

## 3. Business Rule Checks

### BR-001: Active Account Expiration

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-001                                         |
| **Severity**   | WARNING                                        |
| **Source File** | `acctdata.txt` (CVACT01Y.cpy)                 |
| **Rule**       | Active accounts (`ACCT-ACTIVE-STATUS = 'Y'`) must have a non-past expiration date or a populated reissue date |
| **Fields**     | `ACCT-ACTIVE-STATUS`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE` |

**Validation Logic:**
```
For each account WHERE ACCT-ACTIVE-STATUS = 'Y':
    If ACCT-EXPIRAION-DATE < current_date AND ACCT-REISSUE-DATE is blank:
        Report WARNING
```

### BR-002: Credit Limit Compliance

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-002                                         |
| **Severity**   | WARNING                                        |
| **Source File** | `acctdata.txt` (CVACT01Y.cpy)                 |
| **Rule**       | `ACCT-CURR-BAL` should not exceed `ACCT-CREDIT-LIMIT` for active accounts |
| **Tolerance**  | 0.01 (soft check — overlimit may be temporarily valid) |

**Validation Logic:**
```
For each account WHERE ACCT-ACTIVE-STATUS = 'Y':
    If ACCT-CURR-BAL > ACCT-CREDIT-LIMIT + tolerance:
        Report WARNING with both values
```

### BR-003: Active Card → Active Account

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-003                                         |
| **Severity**   | ERROR                                          |
| **Source Files** | `carddata.txt` (CVACT02Y.cpy), `acctdata.txt` (CVACT01Y.cpy) |
| **Rule**       | Every card with `CARD-ACTIVE-STATUS = 'Y'` must reference an account with `ACCT-ACTIVE-STATUS = 'Y'` |
| **Join Key**   | `CARD-ACCT-ID` = `ACCT-ID`                     |

### BR-004: Valid Transaction Type Codes

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-004                                         |
| **Severity**   | ERROR                                          |
| **Source Files** | `dailytran.txt` (CVTRA06Y.cpy), `trantype.txt` (CVTRA03Y.cpy) |
| **Rule**       | Every `DALYTRAN-TYPE-CD` in daily transactions must exist as `TRAN-TYPE` in the transaction type reference file |
| **Join Key**   | `DALYTRAN-TYPE-CD` = `TRAN-TYPE` (PIC X(02))   |
| **Notes**      | Invalid type codes would cause POSTTRAN to reject the transaction |

### BR-005: Valid Transaction Category Codes

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-005                                         |
| **Severity**   | ERROR                                          |
| **Source Files** | `dailytran.txt` (CVTRA06Y.cpy), `trancatg.txt` (CVTRA04Y.cpy) |
| **Rule**       | Every (`DALYTRAN-TYPE-CD`, `DALYTRAN-CAT-CD`) combination in daily transactions must exist as (`TRAN-TYPE-CD`, `TRAN-CAT-CD`) in the transaction category reference |
| **Join Key**   | Composite: `(DALYTRAN-TYPE-CD, DALYTRAN-CAT-CD)` = `(TRAN-TYPE-CD, TRAN-CAT-CD)` |

### BR-006: Disclosure Group Coverage

| Property       | Value                                          |
| :------------- | :--------------------------------------------- |
| **Check ID**   | BR-006                                         |
| **Severity**   | WARNING                                        |
| **Source Files** | `discgrp.txt` (CVTRA02Y.cpy), `acctdata.txt` (CVACT01Y.cpy), `trancatg.txt` (CVTRA04Y.cpy) |
| **Rule**       | For each active account group (`ACCT-GROUP-ID`), disclosure group entries must exist for all transaction type/category combinations used |
| **Notes**      | Missing disclosure entries would cause INTCALC (CBACT04C) to use a zero interest rate |

---

## 4. Per-Job Reconciliation Specifications

### Job: POSTTRAN (CBTRN02C)

**Purpose:** Process daily transaction file, update transaction master, update category balances, update account balances.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| PT-001 | Input record count | Count records in `DALYTRAN` input | `accepted + rejected = input_count` |
| PT-002 | Reject file integrity | N/A | Every record in `DALYREJS` has a valid rejection reason code |
| PT-003 | Transaction master growth | Count records in `TRANFILE` before | `TRANFILE_after = TRANFILE_before + accepted_count` |
| PT-004 | Category balance update | Snapshot `TCATBALF` before | For each accepted transaction, `TRAN-CAT-BAL` for the matching `(ACCT-ID, TYPE-CD, CAT-CD)` increases/decreases by `TRAN-AMT` |
| PT-005 | Account balance update | Snapshot `ACCTFILE` before | For each accepted transaction, `ACCT-CURR-BAL` changes by `TRAN-AMT`; `ACCT-CURR-CYC-CREDIT` or `ACCT-CURR-CYC-DEBIT` updated accordingly |
| PT-006 | Cross-reference validation | N/A | Every accepted transaction's `DALYTRAN-CARD-NUM` exists in `XREFFILE` |

### Job: TRANREPT (CBTRN03C)

**Purpose:** Generate formatted daily transaction report filtered by date range.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| TR-001 | Date filter accuracy | Transactions outside `[PARM-START-DATE, PARM-END-DATE]` | No transactions outside range appear in report |
| TR-002 | Sort order | N/A | Report transactions sorted by `TRAN-CARD-NUM` ascending |
| TR-003 | Amount formatting | N/A | All amounts in report match `PIC -ZZZ,ZZZ,ZZZ.ZZ` format |
| TR-004 | Page totals | N/A | Sum of transaction amounts on each page = displayed page total |
| TR-005 | Account totals | N/A | Sum of transaction amounts per account = displayed account total |
| TR-006 | Type/Category descriptions | N/A | `TRAN-REPORT-TYPE-DESC` matches `TRAN-TYPE-DESC` from trantype; `TRAN-REPORT-CAT-DESC` matches `TRAN-CAT-TYPE-DESC` from trancatg |

### Job: INTCALC (CBACT04C)

**Purpose:** Calculate monthly interest and fees based on category balances and disclosure group rates.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| IC-001 | Interest calculation | Snapshot `TCATBALF`, `DISCGRP` | For each `(ACCT-ID, TYPE-CD, CAT-CD)`: `interest = TRAN-CAT-BAL * DIS-INT-RATE / 1200` (monthly rate) |
| IC-002 | System transaction generation | N/A | One interest transaction generated per non-zero category balance |
| IC-003 | Account balance update | Snapshot `ACCTFILE` before | `ACCT-CURR-BAL` updated by sum of interest charges |
| IC-004 | Disclosure group lookup | N/A | Interest rate fetched using `ACCT-GROUP-ID` + `TYPE-CD` + `CAT-CD` as composite key into `DISCGRP` |
| IC-005 | Output file format | N/A | System-generated transactions in `TRANSACT` output match CVTRA05Y.cpy layout (LRECL=350) |

### Job: COMBTRAN (Sort + IDCAMS REPRO)

**Purpose:** Merge current transaction backup with system-generated transactions and reload into transaction master.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| CT-001 | Record conservation | Count `TRANSACT.BKUP` + `SYSTRAN` | `TRANSACT.COMBINED = BKUP_count + SYSTRAN_count` |
| CT-002 | Sort order | N/A | Combined file sorted by `TRAN-ID` ascending |
| CT-003 | VSAM reload | N/A | `TRANSACT.VSAM.KSDS` record count = `TRANSACT.COMBINED` count |
| CT-004 | No data corruption | Sample records from combined file | Field values match original source records exactly |

### Job: CREASTMT (CBSTM03A/CBSTM03B)

**Purpose:** Generate per-card statements in text and HTML format.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| CS-001 | Card coverage | Count distinct cards in `XREFFILE` | One statement section generated per card with transactions |
| CS-002 | Customer data accuracy | N/A | Statement header customer name matches `CUSTFILE` via `XREFFILE` lookup |
| CS-003 | Account data accuracy | N/A | Statement account details match `ACCTFILE` via `XREFFILE` lookup |
| CS-004 | Transaction completeness | N/A | All transactions for each card in `TRNXFILE` appear in the statement |
| CS-005 | HTML well-formedness | N/A | HTML output is valid (all tags properly closed) |
| CS-006 | Statement balance | N/A | Statement ending balance = opening balance + sum of transactions |

### Job: CBEXPORT

**Purpose:** Export all entity data into a multi-record VSAM file for branch migration.

| Check | Description | Pre-condition | Post-condition |
| :---- | :---------- | :------------ | :------------- |
| EX-001 | Record type markers | N/A | `EXPORT-REC-TYPE` correctly identifies record type ('C'=Customer, 'A'=Account, 'T'=Transaction, 'X'=Xref, 'D'=Card) |
| EX-002 | Record count conservation | Count all source files | Total export records = customers + accounts + transactions + xrefs + cards |
| EX-003 | COMP/COMP-3 encoding | N/A | Packed decimal and binary fields in CVEXPORT.cpy correctly encoded (EXP-ACCT-CURR-BAL COMP-3, EXP-TRAN-AMT COMP-3, etc.) |
| EX-004 | REDEFINES alignment | N/A | Each record type's REDEFINES overlay correctly maps to the 460-byte `EXPORT-RECORD-DATA` area |
| EX-005 | Timestamp population | N/A | `EXPORT-TIMESTAMP` populated for every record |
| EX-006 | Branch/region codes | N/A | `EXPORT-BRANCH-ID` and `EXPORT-REGION-CODE` populated from configuration |

---

## 5. Running Reconciliation Checks

```bash
cd test-harness

# Run all reconciliation checks
python -m pytest test_reconciliation.py -v

# Run a specific check category
python -m pytest test_reconciliation.py::TestReferentialIntegrity -v
python -m pytest test_reconciliation.py::TestBusinessRules -v

# Run the full suite (includes golden-file + reconciliation)
python -m pytest -v
```

## 6. Implementation Status

| Check ID | Implemented | Test File                  |
| :------- | :---------- | :------------------------- |
| RI-001   | Yes         | `test_reconciliation.py`   |
| RI-002   | Yes         | `test_reconciliation.py`   |
| RI-003   | Yes         | `test_reconciliation.py`   |
| RI-004   | Yes         | `test_reconciliation.py`   |
| RI-005   | Yes         | `test_reconciliation.py`   |
| RI-006   | Yes         | `test_reconciliation.py`   |
| AG-001   | Spec only   | —                          |
| AG-002   | Yes         | `reconciliation.py`        |
| AG-003   | Spec only   | —                          |
| BR-001   | Spec only   | —                          |
| BR-002   | Spec only   | —                          |
| BR-003   | Yes         | `test_reconciliation.py`   |
| BR-004   | Yes         | `test_reconciliation.py`   |
| BR-005   | Yes         | `test_reconciliation.py`   |
| BR-006   | Spec only   | —                          |
| PT-*     | Spec only   | — (requires Java outputs)  |
| TR-*     | Spec only   | — (requires Java outputs)  |
| IC-*     | Spec only   | — (requires Java outputs)  |
| CT-*     | Spec only   | — (requires Java outputs)  |
| CS-*     | Spec only   | — (requires Java outputs)  |
| EX-*     | Spec only   | — (requires Java outputs)  |
