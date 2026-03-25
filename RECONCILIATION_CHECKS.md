# CardDemo Reconciliation Checks

> **Generated**: 2026-03-25 | **Scope**: Per-job validation specifications for VSAM ↔ PostgreSQL data consistency

## Overview

This document specifies reconciliation checks for every batch job and data entity in the CardDemo migration. Each check runs automatically during the dual-run period to validate data consistency between the legacy VSAM files and the new PostgreSQL database.

### Automated Runner

```bash
# Run all reconciliation checks
python3 test-harness/reconciliation_runner.py \
  --vsam-dir app/data/ASCII/ \
  --golden-dir golden-files/ \
  --report-dir reports/recon/
```

### Baseline Results (Golden File Self-Check)

| Check Category | Total | Passed | Failed |
|---|---|---|---|
| Record Count | 9 | 9 | 0 |
| Balance Summation | 8 | 8 | 0 |
| Duplicate Key Detection | 18 | 18 | 0 |
| Field Checksum | 9 | 9 | 0 |
| Referential Integrity | 5 | 5 | 0 |
| **Total** | **49** | **49** | **0** |

---

## Entity-Level Reconciliation Specifications

### RC-01: Account Master (ACCTDATA)

| Attribute | Value |
|---|---|
| **VSAM File** | `ACCTDATA` (KSDS, key = ACCT-ID) |
| **Copybook** | CVACT01Y |
| **Record Length** | 300 bytes |
| **Baseline Records** | 50 |
| **PostgreSQL Table** | `accounts` |
| **Primary Key** | `ACCT-ID` → `acct_id` (BIGINT) |

#### Checks

| # | Check | VSAM Field(s) | PostgreSQL Column(s) | Tolerance | Frequency |
|---|---|---|---|---|---|
| RC-01.1 | Record count | COUNT(*) | COUNT(*) | Zero | Every sync cycle |
| RC-01.2 | Current balance sum | SUM(ACCT-CURR-BAL) | SUM(curr_bal) | $0.00 | Nightly |
| RC-01.3 | Credit limit sum | SUM(ACCT-CREDIT-LIMIT) | SUM(credit_limit) | $0.00 | Nightly |
| RC-01.4 | Cash credit limit sum | SUM(ACCT-CASH-CREDIT-LIMIT) | SUM(cash_credit_limit) | $0.00 | Nightly |
| RC-01.5 | Cycle credit sum | SUM(ACCT-CURR-CYC-CREDIT) | SUM(curr_cyc_credit) | $0.00 | Nightly |
| RC-01.6 | Cycle debit sum | SUM(ACCT-CURR-CYC-DEBIT) | SUM(curr_cyc_debit) | $0.00 | Nightly |
| RC-01.7 | No duplicate ACCT-IDs | Unique key check | Unique constraint | Zero | Every sync |
| RC-01.8 | Field checksum per record | MD5(all fields) | MD5(all columns) | Exact match | Nightly |

#### Baseline Balance Totals

| Field | Baseline Sum | Notes |
|---|---|---|
| ACCT-CURR-BAL | $12,269.00 | Sum of all account current balances |
| ACCT-CREDIT-LIMIT | $233,711.00 | Sum of all credit limits |
| ACCT-CASH-CREDIT-LIMIT | $122,148.00 | Sum of all cash credit limits |
| ACCT-CURR-CYC-CREDIT | $0.00 | No cycle credits in baseline data |
| ACCT-CURR-CYC-DEBIT | $0.00 | No cycle debits in baseline data |

#### SQL Validation Query

```sql
-- Account balance reconciliation
SELECT
  'VSAM' AS source,
  COUNT(*) AS record_count,
  SUM(curr_bal) AS total_bal,
  SUM(credit_limit) AS total_credit_limit,
  SUM(cash_credit_limit) AS total_cash_limit
FROM accounts
UNION ALL
SELECT
  'EXPECTED' AS source,
  50 AS record_count,
  12269.00 AS total_bal,
  233711.00 AS total_credit_limit,
  122148.00 AS total_cash_limit;
```

---

### RC-02: Card Master (CARDDATA)

| Attribute | Value |
|---|---|
| **VSAM File** | `CARDDATA` (KSDS, key = CARD-NUM) |
| **Copybook** | CVACT02Y |
| **Record Length** | 150 bytes |
| **Baseline Records** | 50 |
| **PostgreSQL Table** | `cards` |
| **Primary Key** | `CARD-NUM` → `card_num` (VARCHAR(16)) |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-02.1 | Record count match | Zero | Every sync cycle |
| RC-02.2 | No duplicate CARD-NUMs | Zero | Every sync |
| RC-02.3 | Every CARD-ACCT-ID references a valid ACCT-ID in accounts | Zero orphans | Nightly |
| RC-02.4 | Field checksum per record | Exact match | Nightly |
| RC-02.5 | Active status distribution (count of Y vs N) | Exact match | Nightly |

#### Referential Integrity

```sql
-- Cards referencing non-existent accounts
SELECT c.card_num, c.acct_id
FROM cards c
LEFT JOIN accounts a ON c.acct_id = a.acct_id
WHERE a.acct_id IS NULL;
-- Expected: 0 rows
```

---

### RC-03: Card Cross-Reference (CARDXREF)

| Attribute | Value |
|---|---|
| **VSAM File** | `CARDXREF` (KSDS, key = XREF-CARD-NUM) |
| **Copybook** | CVACT03Y |
| **Record Length** | 50 bytes |
| **Baseline Records** | 50 |
| **PostgreSQL Table** | `card_xref` |
| **Primary Key** | `XREF-CARD-NUM` → `card_num` (VARCHAR(16)) |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-03.1 | Record count match | Zero | Every sync cycle |
| RC-03.2 | No duplicate XREF-CARD-NUMs | Zero | Every sync |
| RC-03.3 | Every XREF-ACCT-ID → valid ACCT-ID | Zero orphans | Nightly |
| RC-03.4 | Every XREF-CARD-NUM → valid CARD-NUM in cards table | Zero orphans | Nightly |
| RC-03.5 | Every XREF-CUST-ID → valid CUST-ID in customers table | Zero orphans | Nightly |
| RC-03.6 | Field checksum per record | Exact match | Nightly |

#### Three-Way Referential Integrity

```sql
-- Cross-reference must link to valid account, card, AND customer
SELECT x.card_num,
       CASE WHEN a.acct_id IS NULL THEN 'MISSING_ACCT' END AS acct_check,
       CASE WHEN c.card_num IS NULL THEN 'MISSING_CARD' END AS card_check,
       CASE WHEN cu.cust_id IS NULL THEN 'MISSING_CUST' END AS cust_check
FROM card_xref x
LEFT JOIN accounts a ON x.acct_id = a.acct_id
LEFT JOIN cards c ON x.card_num = c.card_num
LEFT JOIN customers cu ON x.cust_id = cu.cust_id
WHERE a.acct_id IS NULL OR c.card_num IS NULL OR cu.cust_id IS NULL;
-- Expected: 0 rows
```

---

### RC-04: Customer Master (CUSTDATA)

| Attribute | Value |
|---|---|
| **VSAM File** | `CUSTDATA` (KSDS, key = CUST-ID) |
| **Copybook** | CVCUS01Y |
| **Record Length** | 500 bytes |
| **Baseline Records** | 50 |
| **PostgreSQL Table** | `customers` |
| **Primary Key** | `CUST-ID` → `cust_id` (BIGINT) |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-04.1 | Record count match | Zero | Every sync cycle |
| RC-04.2 | No duplicate CUST-IDs | Zero | Every sync |
| RC-04.3 | Field checksum per record | Exact match | Nightly |
| RC-04.4 | SSN format validation (9 digits) | All valid | Weekly |
| RC-04.5 | State code valid (2-char US state) | All valid | Weekly |

---

### RC-05: Daily Transactions (DAILYTRAN)

| Attribute | Value |
|---|---|
| **VSAM File** | `DALYTRAN` (KSDS, key = DALYTRAN-ID) |
| **Copybook** | CVTRA06Y |
| **Record Length** | 350 bytes |
| **Baseline Records** | 300 |
| **PostgreSQL Table** | `daily_transactions` |
| **Primary Key** | `DALYTRAN-ID` → `tran_id` (VARCHAR(16)) |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-05.1 | Record count match | Zero | Every sync cycle |
| RC-05.2 | Transaction amount sum | $0.00 | Nightly |
| RC-05.3 | No duplicate DALYTRAN-IDs | Zero | Every sync |
| RC-05.4 | Field checksum per record | Exact match | Nightly |
| RC-05.5 | Transaction type distribution (count per DALYTRAN-TYPE-CD) | Exact match | Nightly |
| RC-05.6 | Every DALYTRAN-CARD-NUM → valid card in cards table | Zero orphans | Nightly |

#### Baseline Transaction Totals

| Metric | Value |
|---|---|
| Total transactions | 300 |
| Total amount sum | $104,801.54 |
| Unique card numbers referenced | Varies per batch cycle |

#### SQL Validation Query

```sql
-- Daily transaction reconciliation
SELECT
  COUNT(*) AS record_count,
  SUM(tran_amt) AS total_amount,
  COUNT(DISTINCT tran_type_cd) AS distinct_types,
  COUNT(DISTINCT card_num) AS distinct_cards
FROM daily_transactions;
```

---

### RC-06: Disclosure Group (DISCGRP)

| Attribute | Value |
|---|---|
| **VSAM File** | `DISCGRP` (KSDS, composite key) |
| **Copybook** | CVTRA02Y |
| **Record Length** | 50 bytes |
| **Baseline Records** | 51 |
| **PostgreSQL Table** | `disclosure_groups` |
| **Primary Key** | (`DIS-ACCT-GROUP-ID`, `DIS-TRAN-TYPE-CD`, `DIS-TRAN-CAT-CD`) → composite PK |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-06.1 | Record count match | Zero | Every sync cycle |
| RC-06.2 | Interest rate sum | $0.00 | Nightly |
| RC-06.3 | No duplicate composite keys | Zero | Every sync |
| RC-06.4 | Field checksum per record | Exact match | Nightly |
| RC-06.5 | Interest rate range (0.00-99.99) | All valid | Weekly |

#### Baseline Interest Rate Totals

| Metric | Value |
|---|---|
| Total records | 51 |
| Total interest rate sum | 375.00 |

---

### RC-07: Transaction Category Balance (TCATBAL)

| Attribute | Value |
|---|---|
| **VSAM File** | `TCATBALF` (KSDS, composite key) |
| **Copybook** | CVTRA01Y |
| **Record Length** | 50 bytes |
| **Baseline Records** | 50 |
| **PostgreSQL Table** | `tran_cat_balances` |
| **Primary Key** | (`TRANCAT-ACCT-ID`, `TRANCAT-TYPE-CD`, `TRANCAT-CD`) → composite PK |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-07.1 | Record count match | Zero | Every sync cycle |
| RC-07.2 | Category balance sum | $0.00 | Nightly |
| RC-07.3 | No duplicate composite keys | Zero | Every sync |
| RC-07.4 | Every TRANCAT-ACCT-ID → valid ACCT-ID | Zero orphans | Nightly |
| RC-07.5 | Field checksum per record | Exact match | Nightly |

#### Post-Batch Validation

After the nightly batch cycle (POSTTRAN → INTCALC → COMBTRAN), category balances must be recalculated:

```sql
-- Verify category balances match posted transaction sums
SELECT
  b.acct_id, b.type_cd, b.cat_cd,
  b.balance AS stored_balance,
  COALESCE(SUM(t.tran_amt), 0) AS calculated_balance,
  b.balance - COALESCE(SUM(t.tran_amt), 0) AS delta
FROM tran_cat_balances b
LEFT JOIN transactions t
  ON t.acct_id = b.acct_id
  AND t.type_cd = b.type_cd
  AND t.cat_cd = b.cat_cd
GROUP BY b.acct_id, b.type_cd, b.cat_cd, b.balance
HAVING ABS(b.balance - COALESCE(SUM(t.tran_amt), 0)) > 0.005;
-- Expected: 0 rows (tolerance: half-cent for rounding)
```

---

### RC-08: Transaction Types (TRANTYPE)

| Attribute | Value |
|---|---|
| **VSAM File** | `TRANTYPE` (KSDS, key = TRAN-TYPE) |
| **Copybook** | CVTRA03Y |
| **Record Length** | 60 bytes |
| **Baseline Records** | 7 |
| **PostgreSQL Table** | `transaction_types` |
| **Primary Key** | `TRAN-TYPE` → `type_cd` (VARCHAR(2)) |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-08.1 | Record count match | Zero | Every sync cycle |
| RC-08.2 | No duplicate TRAN-TYPEs | Zero | Every sync |
| RC-08.3 | Field checksum per record | Exact match | Nightly |
| RC-08.4 | All type codes used in transactions exist in types table | Zero orphans | Nightly |

#### Reference Data Baseline

| TRAN-TYPE | Description |
|---|---|
| 01 | Purchase |
| 02 | Payment |
| 03 | Credit |
| 04 | Authorization |
| 05 | Refund |
| 06 | Balance Transfer |
| 07 | Cash Advance |

---

### RC-09: Transaction Categories (TRANCATG)

| Attribute | Value |
|---|---|
| **VSAM File** | `TRANCATG` (KSDS, composite key) |
| **Copybook** | CVTRA04Y |
| **Record Length** | 60 bytes |
| **Baseline Records** | 18 |
| **PostgreSQL Table** | `transaction_categories` |
| **Primary Key** | (`TRAN-TYPE-CD`, `TRAN-CAT-CD`) → composite PK |

#### Checks

| # | Check | Tolerance | Frequency |
|---|---|---|---|
| RC-09.1 | Record count match | Zero | Every sync cycle |
| RC-09.2 | No duplicate composite keys | Zero | Every sync |
| RC-09.3 | Field checksum per record | Exact match | Nightly |
| RC-09.4 | Every TRAN-TYPE-CD → valid type in transaction_types | Zero orphans | Nightly |

---

## Batch Job Reconciliation Specifications

The nightly batch cycle modifies data across multiple entities. Each job requires specific pre- and post-condition checks.

### BJ-01: POSTTRAN (Transaction Posting)

| Attribute | Value |
|---|---|
| **JCL Job** | `POSTTRAN` |
| **Program** | CBTRN02C |
| **Input** | DALYTRAN (daily transactions) |
| **Output** | TRANSACT (system transactions), TCATBALF (category balances) |

#### Pre-Conditions

| # | Check | Query/Command |
|---|---|---|
| BJ-01.PRE.1 | Daily transactions loaded | `SELECT COUNT(*) FROM daily_transactions` > 0 |
| BJ-01.PRE.2 | All daily transaction cards exist | No orphan card references |
| BJ-01.PRE.3 | Snapshot of pre-posting category balances | `CREATE TABLE tcatbal_pre_posting AS SELECT * FROM tran_cat_balances` |

#### Post-Conditions

| # | Check | Tolerance |
|---|---|---|
| BJ-01.POST.1 | All daily transactions posted to system transactions | Zero missing |
| BJ-01.POST.2 | Category balance deltas = SUM(posted transaction amounts) per category | $0.00 |
| BJ-01.POST.3 | No duplicate transaction IDs in system transactions | Zero |
| BJ-01.POST.4 | Posted transaction amounts match daily transaction amounts exactly | $0.00 |

#### Validation Query

```sql
-- Verify all daily transactions were posted
SELECT d.tran_id, d.tran_amt
FROM daily_transactions d
LEFT JOIN transactions t ON d.tran_id = t.tran_id
WHERE t.tran_id IS NULL;
-- Expected: 0 rows
```

---

### BJ-02: INTCALC (Interest Calculation)

| Attribute | Value |
|---|---|
| **JCL Job** | `INTCALC` |
| **Program** | CBACT04C |
| **Input** | ACCTDATA (accounts), DISCGRP (interest rates), TCATBALF (category balances) |
| **Output** | ACCTDATA (updated balances), DALYTRAN (interest transactions) |

#### Pre-Conditions

| # | Check |
|---|---|
| BJ-02.PRE.1 | POSTTRAN completed successfully |
| BJ-02.PRE.2 | Snapshot of pre-interest account balances |
| BJ-02.PRE.3 | All disclosure groups have valid interest rates (0-99.99%) |

#### Post-Conditions

| # | Check | Tolerance |
|---|---|---|
| BJ-02.POST.1 | Interest = category_balance × (rate / 36500) × days, per account | $0.01 (rounding) |
| BJ-02.POST.2 | Account balance delta = SUM(interest transactions) for that account | $0.00 |
| BJ-02.POST.3 | Interest transactions have type code matching interest category | Exact |
| BJ-02.POST.4 | No negative interest on positive balances (and vice versa) | Zero violations |

#### Validation Query

```sql
-- Verify interest calculation correctness per account
WITH interest_txns AS (
  SELECT acct_id, SUM(tran_amt) AS total_interest
  FROM daily_transactions
  WHERE tran_cat_cd = 5  -- Interest category
  GROUP BY acct_id
),
balance_delta AS (
  SELECT a.acct_id,
         a.curr_bal - pre.curr_bal AS bal_change
  FROM accounts a
  JOIN acct_pre_interest pre ON a.acct_id = pre.acct_id
)
SELECT b.acct_id, b.bal_change, i.total_interest,
       ABS(b.bal_change - i.total_interest) AS discrepancy
FROM balance_delta b
JOIN interest_txns i ON b.acct_id = i.acct_id
WHERE ABS(b.bal_change - i.total_interest) > 0.01;
-- Expected: 0 rows
```

---

### BJ-03: TRANBKP (Transaction Backup)

| Attribute | Value |
|---|---|
| **JCL Job** | `TRANBKP` |
| **Program** | IDCAMS REPRO |
| **Input** | TRANSACT (system transactions) |
| **Output** | TRANSACT.BACKUP (GDG) |

#### Post-Conditions

| # | Check | Tolerance |
|---|---|---|
| BJ-03.POST.1 | Backup record count = source record count | Zero |
| BJ-03.POST.2 | Backup file checksum = source file checksum | Exact |
| BJ-03.POST.3 | Backup timestamp recorded in audit log | Exists |

#### PostgreSQL Equivalent

```sql
-- Database snapshot before combine step
SELECT pg_export_snapshot();
-- Or: CREATE TABLE transactions_backup AS SELECT * FROM transactions;
```

---

### BJ-04: COMBTRAN (Combine Transactions)

| Attribute | Value |
|---|---|
| **JCL Job** | `COMBTRAN` |
| **Program** | SORT + IDCAMS |
| **Input** | DALYTRAN (daily) + TRANSACT (system) |
| **Output** | TRANSACT (combined, sorted) |

#### Pre-Conditions

| # | Check |
|---|---|
| BJ-04.PRE.1 | TRANBKP completed successfully |
| BJ-04.PRE.2 | Record counts: daily_count + system_count = expected_combined_count |

#### Post-Conditions

| # | Check | Tolerance |
|---|---|---|
| BJ-04.POST.1 | Combined count = daily_count + system_count (no records lost) | Zero |
| BJ-04.POST.2 | Combined file is sorted by transaction key | Strictly ordered |
| BJ-04.POST.3 | Sum of amounts in combined = SUM(daily) + SUM(system) | $0.00 |
| BJ-04.POST.4 | No duplicate transaction IDs after combine | Zero |

#### Validation Query

```sql
-- Post-combine transaction reconciliation
SELECT
  (SELECT COUNT(*) FROM daily_transactions) AS daily_count,
  (SELECT COUNT(*) FROM transactions WHERE is_system = true) AS system_count,
  (SELECT COUNT(*) FROM transactions) AS combined_count;
-- combined_count should equal daily_count + system_count
```

---

### BJ-05: CREASTMT (Statement Generation)

| Attribute | Value |
|---|---|
| **JCL Job** | `CREASTMT` |
| **Program** | CBSTM03A/B |
| **Input** | TRANSACT (combined), ACCTDATA, CUSTDATA |
| **Output** | Statement spool (print), STMTDATA |

#### Post-Conditions

| # | Check | Tolerance |
|---|---|---|
| BJ-05.POST.1 | One statement generated per active account | Zero missing |
| BJ-05.POST.2 | Statement opening balance matches previous closing balance | $0.00 |
| BJ-05.POST.3 | Statement closing balance = opening + credits - debits | $0.00 |
| BJ-05.POST.4 | Transaction count on statement matches transactions for that account | Zero |
| BJ-05.POST.5 | Statement grand totals match SUM of all account statements | $0.00 |

#### Validation Query

```sql
-- Statement balance accuracy
SELECT s.acct_id,
       s.opening_bal,
       s.closing_bal,
       s.total_credits,
       s.total_debits,
       s.opening_bal + s.total_credits - s.total_debits AS expected_closing,
       ABS(s.closing_bal - (s.opening_bal + s.total_credits - s.total_debits)) AS delta
FROM statements s
WHERE ABS(s.closing_bal - (s.opening_bal + s.total_credits - s.total_debits)) > 0.005;
-- Expected: 0 rows
```

---

### BJ-06: Full Batch Cycle End-to-End

The complete nightly cycle must be validated as a whole after all individual jobs complete.

#### Batch Cycle Order

```
CLOSEFIL → ACCTFILE → CARDFILE → CUSTFILE → XREFFILE → TRANFILE
  → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

#### End-to-End Checks

| # | Check | Description | Tolerance |
|---|---|---|---|
| BJ-06.E2E.1 | Global balance sheet | SUM(account balances) before cycle + SUM(daily transactions) + SUM(interest) = SUM(account balances) after cycle | $0.01 per account |
| BJ-06.E2E.2 | Zero data loss | Pre-cycle record count + new records = post-cycle record count for every entity | Zero |
| BJ-06.E2E.3 | Cycle idempotency | Running the cycle twice with no new daily transactions produces identical output | Exact match |
| BJ-06.E2E.4 | Timing | Total cycle duration within 120% of COBOL baseline | Alert if exceeded |
| BJ-06.E2E.5 | Restart recovery | Kill batch mid-cycle, restart from last checkpoint, verify final state is correct | Exact match |

#### Global Balance Sheet Validation

```sql
-- The accounting equation must hold across the full cycle
WITH pre AS (
  SELECT SUM(curr_bal) AS total_bal FROM acct_snapshot_pre_batch
),
post AS (
  SELECT SUM(curr_bal) AS total_bal FROM accounts
),
txn AS (
  SELECT SUM(tran_amt) AS total_posted FROM daily_transactions
),
interest AS (
  SELECT SUM(tran_amt) AS total_interest
  FROM daily_transactions WHERE tran_cat_cd = 5
)
SELECT
  pre.total_bal AS pre_balance,
  txn.total_posted AS posted_amount,
  interest.total_interest AS interest_amount,
  post.total_bal AS post_balance,
  ABS(post.total_bal - (pre.total_bal + txn.total_posted + interest.total_interest)) AS discrepancy
FROM pre, post, txn, interest;
-- discrepancy must be < $0.01 * number_of_accounts
```

---

## Cross-Entity Reconciliation

### XR-01: Card-to-Account Integrity

Every card must belong to a valid account.

```
CARDDATA.CARD-ACCT-ID → ACCTDATA.ACCT-ID
```

| Check | Description | Tolerance |
|---|---|---|
| Orphan cards | Cards referencing non-existent accounts | 0 |
| Orphan accounts | Accounts with no cards (allowed but flagged) | WARN only |

### XR-02: Cross-Reference Completeness

Every card cross-reference must link to valid card, account, AND customer.

```
CARDXREF.XREF-CARD-NUM → CARDDATA.CARD-NUM
CARDXREF.XREF-ACCT-ID  → ACCTDATA.ACCT-ID
CARDXREF.XREF-CUST-ID  → CUSTDATA.CUST-ID
```

| Check | Description | Tolerance |
|---|---|---|
| Missing card link | XREF card not in CARDDATA | 0 |
| Missing account link | XREF account not in ACCTDATA | 0 |
| Missing customer link | XREF customer not in CUSTDATA | 0 |
| Bidirectional consistency | Every CARDDATA record has a matching XREF | 0 |

### XR-03: Transaction-to-Reference Integrity

Every transaction must reference valid type and category codes.

```
DAILYTRAN.DALYTRAN-TYPE-CD  → TRANTYPE.TRAN-TYPE
DAILYTRAN.DALYTRAN-CAT-CD   → TRANCATG.TRAN-CAT-CD (for that TYPE-CD)
TCATBALF.TRANCAT-ACCT-ID   → ACCTDATA.ACCT-ID
```

| Check | Description | Tolerance |
|---|---|---|
| Invalid type code | Transaction type not in TRANTYPE | 0 |
| Invalid category code | Transaction category not in TRANCATG | 0 |
| Orphan category balances | TCATBAL account not in ACCTDATA | 0 |

---

## Reconciliation Schedule

| Frequency | Checks | Trigger |
|---|---|---|
| **Every sync cycle** (< 5 min) | Record counts for all entities | CDC event |
| **Nightly** (after batch) | Balance sums, checksums, referential integrity, batch job post-conditions | Batch completion |
| **Weekly** | Data format validation (dates, SSN, state codes), interest rate ranges | Scheduled |
| **Pre-cutover** (once) | Full end-to-end cycle comparison: COBOL vs Java batch output | Manual |

---

## Alert and Escalation

| Severity | Trigger | Response | SLA |
|---|---|---|---|
| **CRITICAL** | Any balance mismatch > $0.00 | Halt sync, block cutover, page on-call | 1 hour |
| **CRITICAL** | Record count mismatch | Halt sync, investigate source | 1 hour |
| **HIGH** | Referential integrity violation | Block affected module cutover | 4 hours |
| **MEDIUM** | Checksum mismatch on non-financial fields | Investigate, fix in next sync | 1 business day |
| **LOW** | Data format warning | Log for review | Next sprint |
