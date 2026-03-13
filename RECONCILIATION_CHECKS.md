# Reconciliation Checks: CardDemo Batch Jobs

This document describes each batch job in `app/jcl/`, what it reads, what it writes, what reconciliation checks should pass after execution, and what business rules it enforces.

---

## 1. ACCTFILE.jcl -- Account Data Load

**Purpose**: Delete and redefine the Account VSAM KSDS file, then load account data from a flat file.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.PS` (flat file, RECLN 300, copybook `CVACT01Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (VSAM KSDS, key=ACCT-ID at offset 0, length 11) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM file record count must equal flat file record count (50 records) |
| Key uniqueness | Every `ACCT-ID` must be unique in the output VSAM |
| Field integrity | All `ACCT-ACTIVE-STATUS` values must be 'Y' or 'N' |
| Date validity | `ACCT-OPEN-DATE`, `ACCT-EXPIRAION-DATE`, `ACCT-REISSUE-DATE` must be valid YYYY-MM-DD dates |
| Balance consistency | `ACCT-CURR-BAL` must not exceed `ACCT-CREDIT-LIMIT` for any account |
| Sum preservation | Sum of `ACCT-CURR-BAL` in VSAM must equal sum in flat file |

### Business Rules

- Account IDs are 11-digit zero-padded numbers starting from 1
- Active status must be 'Y' (active) or 'N' (inactive)
- Credit limit must be greater than or equal to cash credit limit
- Expiration date must be after open date

---

## 2. CARDFILE.jcl -- Card Data Load

**Purpose**: Delete and redefine the Card Data VSAM KSDS file, load card data, and build alternate index on account ID.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.CARDDATA.PS` (flat file, RECLN 150, copybook `CVACT02Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` (VSAM KSDS, key=CARD-NUM at offset 0, length 16) |
| **Also creates** | Alternate index on CARD-ACCT-ID (offset 16, length 11), non-unique key |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (50 records) |
| Key uniqueness | Every `CARD-NUM` must be unique |
| Alternate index | Every `CARD-ACCT-ID` must reference a valid account in ACCTDATA |
| Status values | All `CARD-ACTIVE-STATUS` values must be 'Y' or 'N' |
| CVV validity | `CARD-CVV-CD` must be a 3-digit number |
| Date validity | `CARD-EXPIRAION-DATE` must be a valid YYYY-MM-DD date |

### Business Rules

- Card numbers are 16-character identifiers
- Each card is linked to exactly one account via `CARD-ACCT-ID`
- Multiple cards can share the same account (non-unique alternate index)
- Embossed name cannot be blank for active cards

---

## 3. CUSTFILE.jcl -- Customer Data Load

**Purpose**: Delete and redefine the Customer VSAM KSDS file, then load customer data.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.CUSTDATA.PS` (flat file, RECLN 500, copybook `CVCUS01Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` (VSAM KSDS, key=CUST-ID at offset 0, length 9) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (50 records) |
| Key uniqueness | Every `CUST-ID` must be unique |
| SSN format | `CUST-SSN` must be a 9-digit number |
| State code validity | `CUST-ADDR-STATE-CD` must be a valid 2-character US state/territory code |
| Credit score range | `CUST-FICO-CREDIT-SCORE` must be between 0 and 999 |

### Business Rules

- Customer IDs are 9-digit zero-padded numbers
- Each customer has primary card holder indicator ('Y' or 'N')
- Phone numbers follow format `(NNN)NNN-NNNN` with padding
- Date of birth must be in YYYY-MM-DD format and represent a valid past date

---

## 4. XREFFILE.jcl -- Card Cross-Reference Load

**Purpose**: Delete and redefine the Card Cross-Reference VSAM file, load cross-reference data, and build alternate index on account ID.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.PS` (flat file, RECLN 50, copybook `CVACT03Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (VSAM KSDS, key=XREF-CARD-NUM at offset 0, length 16) |
| **Also creates** | Alternate index on XREF-ACCT-ID (offset 25, length 11), non-unique key |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (50 records) |
| Card reference | Every `XREF-CARD-NUM` must exist in CARDDATA |
| Account reference | Every `XREF-ACCT-ID` must exist in ACCTDATA |
| Customer reference | Every `XREF-CUST-ID` must exist in CUSTDATA |
| Bidirectional consistency | Every card in CARDDATA must have exactly one XREF entry |

### Business Rules

- The cross-reference file links cards to both customers and accounts
- Each card number appears exactly once in the cross-reference
- The account ID in XREF must match the account ID in CARDDATA for the same card
- Every customer referenced must exist in the customer master file

---

## 5. TRANFILE.jcl -- Transaction Data Load

**Purpose**: Delete and redefine the Transaction VSAM KSDS file and load transaction data.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.PS` (flat file, RECLN 350, copybook `CVTRA05Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (VSAM KSDS, key=TRAN-ID at offset 0, length 16) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count |
| Key uniqueness | Every `TRAN-ID` must be unique |
| Card reference | Every `TRAN-CARD-NUM` must exist in CARDDATA |
| Type code validity | Every `TRAN-TYPE-CD` must exist in TRANTYPE |
| Category validity | Every `TRAN-TYPE-CD` + `TRAN-CAT-CD` combination must exist in TRANCATG |
| Amount sum | Sum of `TRAN-AMT` in VSAM must equal sum in flat file |

### Business Rules

- Transaction IDs are 16-character unique identifiers
- Transaction amounts can be positive (debits/purchases) or negative (credits/returns)
- Transaction type codes must reference valid entries in the transaction type table
- Each transaction must reference a valid card number

---

## 6. POSTTRAN.jcl -- Post Daily Transactions

**Purpose**: Process the daily transaction file, update account balances and transaction category balances.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.DALYTRAN.PS` (daily transactions, RECLN 350, copybook `CVTRA06Y.cpy`) |
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (existing transactions) |
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (account data) |
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (card cross-reference) |
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (transaction category balances) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (updated transaction file) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (updated account balances) |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (updated category balances) |
| **Writes** | `AWS.M2.CARDDEMO.DALYREJS.PS` (rejected transactions) |
| **Writes** | `AWS.M2.CARDDEMO.DALYREPT.PS` (daily report) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Input count | Count of daily transactions read must equal `dailytran.txt` record count (300) |
| Posted + rejected = input | Number of posted transactions + rejected transactions must equal input count |
| Balance delta | For each account, the change in `ACCT-CURR-BAL` must equal the sum of posted transaction amounts for that account |
| Category balance delta | For each (account, type, category) triple, the change in `TRAN-CAT-BAL` must equal the sum of posted transactions for that category |
| Cycle credit/debit | `ACCT-CURR-CYC-CREDIT` and `ACCT-CURR-CYC-DEBIT` must be updated with cycle totals |
| Cross-reference valid | All daily transaction card numbers must resolve to valid accounts via CARDXREF |
| No orphan transactions | Every posted transaction must have a corresponding record in the transaction file |

### Business Rules

- Transactions with invalid card numbers are rejected and written to DALYREJS
- Transactions that would exceed the credit limit are rejected
- Cash advance transactions must not exceed the cash credit limit
- Account current balance is updated: purchases increase the balance, payments decrease it
- Transaction category balances track cumulative amounts by (account, type, category)
- The processing timestamp (`DALYTRAN-PROC-TS`) is set at the time of posting

---

## 7. TCATBALF.jcl -- Transaction Category Balance Load

**Purpose**: Delete and redefine the Transaction Category Balance VSAM file and load initial balances.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.PS` (flat file, RECLN 50, copybook `CVTRA01Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (VSAM KSDS, composite key: ACCT-ID + TYPE-CD + CAT-CD) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (50 records) |
| Key uniqueness | Each (ACCT-ID, TYPE-CD, CAT-CD) triple must be unique |
| Account reference | Every `TRANCAT-ACCT-ID` must exist in ACCTDATA |
| Type reference | Every `TRANCAT-TYPE-CD` must exist in TRANTYPE |
| Sum preservation | Sum of `TRAN-CAT-BAL` must be preserved from input to output |

### Business Rules

- Category balances track cumulative transaction amounts by type and category for each account
- Initial balances are zero for new accounts
- Balance values are signed decimals (PIC S9(09)V99) allowing negative balances

---

## 8. DISCGRP.jcl -- Disclosure Group Load

**Purpose**: Delete and redefine the Disclosure Group VSAM file and load disclosure/interest rate data.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.DISCGRP.PS` (flat file, RECLN 50, copybook `CVTRA02Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` (VSAM KSDS, composite key: GROUP-ID + TYPE-CD + CAT-CD) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (51 records) |
| Key uniqueness | Each (GROUP-ID, TYPE-CD, CAT-CD) triple must be unique |
| Rate validity | `DIS-INT-RATE` must be a non-negative decimal value |
| Group coverage | DEFAULT group must have entries for all standard type/category combinations |

### Business Rules

- Disclosure groups define interest rates by account group, transaction type, and category
- The DEFAULT group provides fallback rates for accounts without a specific group assignment
- The ZEROAPR group provides 0% rates (promotional)
- Group IDs beginning with 'A' followed by digits are account-specific groups
- Interest rates are in PIC S9(04)V99 format (e.g., 150 = 1.50%)

---

## 9. TRANCATG.jcl -- Transaction Category Type Load

**Purpose**: Delete and redefine the Transaction Category VSAM file and load category type definitions.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TRANCATG.PS` (flat file, RECLN 60, copybook `CVTRA04Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` (VSAM KSDS, composite key: TYPE-CD + CAT-CD) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (18 records) |
| Key uniqueness | Each (TYPE-CD, CAT-CD) pair must be unique |
| Type reference | Every `TRAN-TYPE-CD` must exist in TRANTYPE |
| Description populated | `TRAN-CAT-TYPE-DESC` must not be blank |

### Business Rules

- Transaction categories subdivide transaction types into more specific classifications
- Type 01 (Purchase): Regular Sales Draft, Cash Advance, Convenience Check, ATM, Interest
- Type 02 (Payment): Cash, Electronic, Check
- Type 03 (Credit): Account credit, Purchase balance credit, Cash balance credit
- Type 04 (Authorization): Zero dollar, Online purchase, Travel booking
- Type 05 (Refund): Refund credit
- Type 06 (Reversal): Fraud reversal, Non-fraud reversal
- Type 07 (Adjustment): Sales draft credit adjustment

---

## 10. TRANTYPE.jcl -- Transaction Type Load

**Purpose**: Delete and redefine the Transaction Type VSAM file and load type definitions.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TRANTYPE.PS` (flat file, RECLN 60, copybook `CVTRA03Y.cpy`) |
| **Writes** | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` (VSAM KSDS, key=TRAN-TYPE at offset 0, length 2) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count | VSAM record count must equal flat file record count (7 records) |
| Key uniqueness | Every `TRAN-TYPE` must be unique |
| Sequential codes | Type codes must be sequential: 01 through 07 |
| Description populated | `TRAN-TYPE-DESC` must not be blank |

### Business Rules

- Seven transaction types: Purchase (01), Payment (02), Credit (03), Authorization (04), Refund (05), Reversal (06), Adjustment (07)
- Type codes are 2-character strings ('01' through '07')
- Each type has a human-readable description

---

## 11. COMBTRAN.jcl -- Combine Transactions

**Purpose**: Combine daily transactions into the main transaction file.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.DALYTRAN.PS` (daily transactions) |
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (existing transactions) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (merged transaction file) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Output count | Output record count must equal pre-existing count + daily transaction count |
| No duplicates | No duplicate `TRAN-ID` values in output |
| Sum preservation | Sum of all `TRAN-AMT` in output equals sum of pre-existing + sum of daily |

### Business Rules

- Daily transactions are appended to the main transaction VSAM file
- Duplicate transaction IDs are rejected
- Transaction order within the file is maintained

---

## 12. DALYREJS.jcl -- Daily Rejection Processing

**Purpose**: Process rejected daily transactions and write a rejection report.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.DALYREJS.PS` (rejected transactions) |
| **Writes** | `AWS.M2.CARDDEMO.DALYREJS.REPORT` (rejection report) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Rejection count | Count of rejected records must match POSTTRAN rejection count |
| Rejection reasons | Each rejected transaction must have a documented reason |

### Business Rules

- Rejected transactions are those that failed validation during POSTTRAN processing
- Common rejection reasons: invalid card number, credit limit exceeded, invalid transaction type

---

## 13. INTCALC.jcl -- Interest Calculation

**Purpose**: Calculate interest charges on account balances using disclosure group rates.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (account data) |
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (category balances) |
| **Reads** | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` (interest rates) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (updated balances with interest) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (interest charge transactions) |
| **Writes** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (updated category balances) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Interest accuracy | For each account, interest = sum of (category_balance * applicable_rate / 12) |
| Rate lookup | Interest rate used must match the account's group in DISCGRP, falling back to DEFAULT |
| Balance update | Post-interest `ACCT-CURR-BAL` = pre-interest balance + total interest charged |
| Transaction creation | One interest transaction created per account per category with a balance |
| Category balance update | `TRAN-CAT-BAL` for interest category must be updated |

### Business Rules

- Interest is calculated monthly on outstanding category balances
- The applicable rate is determined by matching account group ID to disclosure group
- If no account-specific group exists, the DEFAULT group rates are used
- ZEROAPR groups result in zero interest charges
- Interest amounts are rounded to 2 decimal places
- Interest is only charged on accounts with active status ('Y')

---

## 14. TRANREPT.jcl -- Transaction Report

**Purpose**: Generate the daily transaction report.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (transactions) |
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (account data) |
| **Reads** | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` (transaction types for descriptions) |
| **Reads** | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` (categories for descriptions) |
| **Writes** | `AWS.M2.CARDDEMO.DALYREPT.PS` (report output) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Grand total | Report grand total must equal sum of all `TRAN-AMT` in the transaction file |
| Account totals | Each account subtotal must equal the sum of transactions for that account |
| Page totals | Each page total must equal the sum of transactions on that page |
| Record coverage | All transactions in the input must appear in the report |

### Business Rules

- Report is sorted by card number, then by transaction ID
- Report layout follows `CVTRA07Y.cpy` structure
- Each transaction line shows: transaction ID, account ID, type code+description, category code+description, source, and amount
- Account subtotals are printed after all transactions for an account
- Page totals are printed at the bottom of each page
- Grand total is printed at the end of the report

---

## 15. CREASTMT.JCL -- Create Statement

**Purpose**: Create account statements for each card in the cross-reference file.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (transactions, re-sorted by card+tran-id) |
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (card cross-reference) |
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (account data) |
| **Reads** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` (customer data) |
| **Writes** | `AWS.M2.CARDDEMO.STATEMNT.PS` (text statements) |
| **Writes** | `AWS.M2.CARDDEMO.STATEMNT.HTML` (HTML statements) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Card coverage | One statement section per card in CARDXREF |
| Transaction completeness | All transactions for each card must appear in the statement |
| Balance accuracy | Statement balance must match account current balance |
| Customer data | Statement header must contain correct customer name and address |

### Business Rules

- Transactions are re-sorted by card number + transaction ID for statement grouping
- Both text and HTML format statements are produced
- Statement includes customer info, account summary, and transaction details
- The CBSTM03A program produces the statements with called subroutines

---

## 16. CBEXPORT.jcl -- Data Export for Migration

**Purpose**: Export all VSAM data to a multi-record sequential file for branch migration.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (accounts) |
| **Reads** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` (customers) |
| **Reads** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` (cards) |
| **Reads** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (cross-references) |
| **Reads** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (transactions) |
| **Writes** | `AWS.M2.CARDDEMO.EXPORT.DATA.PS` (multi-record export file, RECLN 500, copybook `CVEXPORT.cpy`) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Total record count | Export record count must equal sum of all input file record counts |
| Record type counts | Count of each `EXPORT-REC-TYPE` must match source file counts |
| Field preservation | Key fields in export must match source VSAM values exactly |
| Sequence numbers | `EXPORT-SEQUENCE-NUM` must be sequential within each record type |

### Business Rules

- Export uses multi-record format with `EXPORT-REC-TYPE` discriminator
- Record types use REDEFINES to overlay different entity structures on the same 500-byte record
- COMP-3 and COMP fields are used for storage optimization in the export format
- Branch ID and region code are populated for migration routing
- Timestamp records the export date/time

---

## 17. CBIMPORT.jcl -- Data Import

**Purpose**: Import data from the multi-record export file back into VSAM files.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.EXPORT.DATA.PS` (multi-record export file) |
| **Writes** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` (accounts) |
| **Writes** | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` (customers) |
| **Writes** | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` (cards) |
| **Writes** | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (cross-references) |
| **Writes** | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` (transactions) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Record count per type | Each output VSAM must contain the correct number of records from the export |
| Round-trip integrity | Export followed by import must produce identical VSAM contents |
| Key preservation | All primary keys must be preserved through the export/import cycle |
| Data integrity | All field values must round-trip without loss or corruption |

### Business Rules

- Import is the inverse of export, reading the multi-record file and distributing records to the appropriate VSAM files
- Record type field determines which VSAM file receives the record
- COMP-3 and COMP fields must be correctly decoded back to display format

---

## 18. PRTCATBL.jcl -- Print Category Balances

**Purpose**: Print transaction category balances for review.

| Attribute | Value |
|---|---|
| **Reads** | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` (category balances) |
| **Writes** | SYSOUT (printed report) |

### Reconciliation Checks

| Check | Description |
|---|---|
| Coverage | All records in TCATBALF must appear in the report |
| Grand total | Report total must equal sum of all `TRAN-CAT-BAL` values |

### Business Rules

- Report lists all category balance records for audit purposes
- Used as a verification step after POSTTRAN or INTCALC processing

---

## 19. Utility/Infrastructure Jobs

The following JCL jobs provide infrastructure support and do not process business data directly:

| Job | Purpose | Notes |
|---|---|---|
| `CBADMCDJ.jcl` | CICS resource definitions | Defines CICS programs, mapsets, transactions for CardDemo |
| `CLOSEFIL.jcl` | Close VSAM files in CICS | Closes files before batch update operations |
| `OPENFIL.jcl` | Open VSAM files in CICS | Opens files after batch updates complete |
| `DEFCUST.jcl` | Define customer VSAM cluster | Creates empty VSAM cluster for customer data |
| `DEFGDGB.jcl` | Define GDG base | Creates Generation Data Group for backup management |
| `DEFGDGD.jcl` | Define GDG dataset | Defines a generation within the GDG |
| `DUSRSECJ.jcl` | User security setup | Defines user security VSAM for login/authentication |
| `ESDSRRDS.jcl` | Define ESDS/RRDS datasets | Creates ESDS and RRDS VSAM clusters |
| `FTPJCL.JCL` | FTP file transfer | Transfers files to/from mainframe |
| `INTRDRJ1.JCL` | Internal reader job 1 | Triggers downstream JCL via internal reader |
| `INTRDRJ2.JCL` | Internal reader job 2 | Downstream processing triggered by INTRDRJ1 |
| `READACCT.jcl` | Read account data | Utility to read/verify account VSAM |
| `READCARD.jcl` | Read card data | Utility to read/verify card VSAM |
| `READCUST.jcl` | Read customer data | Utility to read/verify customer VSAM |
| `READXREF.jcl` | Read cross-reference data | Utility to read/verify XREF VSAM |
| `REPTFILE.jcl` | Report file setup | Defines report output datasets |
| `TRANIDX.jcl` | Transaction index build | Builds alternate indexes on transaction VSAM |
| `TRANBKP.jcl` | Transaction backup | Backs up transaction VSAM to sequential file |
| `WAITSTEP.jcl` | Wait step utility | Introduces delays between batch steps |
| `TXT2PDF1.JCL` | Convert text to PDF | Converts statement text files to PDF format |

---

## Summary: Batch Job Dependency Graph

```
                    +------------------+
                    |  Load Reference  |
                    |  Data (one-time) |
                    +--------+---------+
                             |
              +--------------+--------------+
              |              |              |
        TRANTYPE.jcl   TRANCATG.jcl   DISCGRP.jcl
              |              |              |
              +--------------+--------------+
                             |
              +--------------+--------------+--------------+
              |              |              |              |
        ACCTFILE.jcl   CUSTFILE.jcl   CARDFILE.jcl   TCATBALF.jcl
              |              |              |              |
              +--------------+--------------+              |
                             |                             |
                       XREFFILE.jcl                        |
                             |                             |
              +--------------+-----------------------------+
              |
        POSTTRAN.jcl  <--- dailytran.txt
              |
              +----> DALYREJS.jcl (rejected transactions)
              |
              +----> COMBTRAN.jcl (merge into main)
              |
              +----> INTCALC.jcl (interest calculation)
              |
              +----> TRANREPT.jcl (daily report)
              |
              +----> CREASTMT.JCL (statements)
              |
              +----> CBEXPORT.jcl (migration export)
                             |
                       CBIMPORT.jcl (migration import)
```
