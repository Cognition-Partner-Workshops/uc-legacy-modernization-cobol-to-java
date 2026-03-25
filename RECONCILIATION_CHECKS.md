# Reconciliation Checks

Per-job validation specifications for the CardDemo COBOL-to-Java migration. Each check verifies a specific data integrity invariant that must hold after migration.

---

## 1. Account Data (`acctdata.txt` / `CVACT01Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total account records | 50 | Count of non-empty lines in `acctdata.txt` |

### Primary Key Uniqueness
| Check | Field | Constraint |
|---|---|---|
| Account IDs unique | `acct_id` | No duplicate values across all 50 records |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Active status valid | `acct_active_status` | Must be `Y` or `N` |
| Open date format | `acct_open_date` | Must match `YYYY-MM-DD` |
| Expiration date format | `acct_expiration_date` | Must match `YYYY-MM-DD` |
| Reissue date format | `acct_reissue_date` | Must match `YYYY-MM-DD` |
| Credit limit non-negative | `acct_credit_limit` | `>= 0` after sign decode |
| Cash credit limit non-negative | `acct_cash_credit_limit` | `>= 0` after sign decode |

### Financial Reconciliation
| Check | Description | Formula |
|---|---|---|
| Balance within limit | Current balance <= credit limit | `acct_curr_bal <= acct_credit_limit` for each active account |
| Cycle credits non-negative | Current cycle credits >= 0 | `acct_curr_cyc_credit >= 0` |
| Cycle debits non-negative | Current cycle debits >= 0 | `acct_curr_cyc_debit >= 0` |

---

## 2. Card Data (`carddata.txt` / `CVACT02Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total card records | 50 | Count of non-empty lines in `carddata.txt` |

### Primary Key Uniqueness
| Check | Field | Constraint |
|---|---|---|
| Card numbers unique | `card_num` | No duplicate 16-character card numbers |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Card number length | `card_num` | Exactly 16 characters |
| CVV code range | `card_cvv_cd` | `0 <= cvv <= 999` |
| Active status valid | `card_active_status` | Must be `Y` or `N` |
| Expiration date format | `card_expiration_date` | Must match `YYYY-MM-DD` |
| Embossed name non-empty | `card_embossed_name` | Must not be blank |

### Referential Integrity
| Check | Foreign Key | Parent Table | Parent Key |
|---|---|---|---|
| Card -> Account | `card_acct_id` | `acctdata` | `acct_id` |

---

## 3. Card Cross-Reference (`cardxref.txt` / `CVACT03Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total xref records | 50 | Count of non-empty lines in `cardxref.txt` |

### Primary Key Uniqueness
| Check | Field | Constraint |
|---|---|---|
| Xref card numbers unique | `xref_card_num` | No duplicate card numbers in xref |

### Referential Integrity
| Check | Foreign Key | Parent Table | Parent Key |
|---|---|---|---|
| Xref -> Account | `xref_acct_id` | `acctdata` | `acct_id` |
| Xref -> Customer | `xref_cust_id` | `custdata` | `cust_id` |
| Xref -> Card | `xref_card_num` | `carddata` | `card_num` |

### Cross-File Consistency
| Check | Description | Rule |
|---|---|---|
| Xref count matches card count | 1:1 card-to-xref mapping | `count(cardxref) == count(carddata)` |

---

## 4. Customer Data (`custdata.txt` / `CVCUS01Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total customer records | 50 | Count of non-empty lines in `custdata.txt` |

### Primary Key Uniqueness
| Check | Field | Constraint |
|---|---|---|
| Customer IDs unique | `cust_id` | No duplicate values across all 50 records |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| First name non-empty | `cust_first_name` | Must not be blank |
| Last name non-empty | `cust_last_name` | Must not be blank |
| DOB format | `cust_dob_yyyy_mm_dd` | Must match `YYYY-MM-DD` |
| FICO score range | `cust_fico_credit_score` | `0 <= score <= 999` |
| State code length | `cust_addr_state_cd` | Exactly 2 characters |
| Country code length | `cust_addr_country_cd` | Exactly 3 characters |
| Primary cardholder indicator | `cust_pri_card_holder_ind` | Must be `Y` or `N` |

### Cross-File Consistency
| Check | Description | Rule |
|---|---|---|
| Customer count matches account count | 1:1 customer-to-account mapping | `count(custdata) == count(acctdata)` |

---

## 5. Daily Transactions (`dailytran.txt` / `CVTRA06Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total transaction records | 300 | Count of non-empty lines in `dailytran.txt` |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Transaction ID non-empty | `tran_id` | Must not be blank |
| Type code valid | `tran_type_cd` | Must exist in `trantype.txt` type codes |
| Category code valid | `tran_cat_cd` | Must exist in `trancatg.txt` category codes |
| Amount parseable | `tran_amt` | Must be a valid signed decimal |
| Timestamp format | `tran_orig_ts` | Must start with `YYYY-MM-DD` |
| Card number non-empty | `tran_card_num` | Must not be blank |

### Referential Integrity
| Check | Foreign Key | Parent Table | Parent Key |
|---|---|---|---|
| Tran card -> Cards | `tran_card_num` | `carddata` | `card_num` |
| Tran type -> Types | `tran_type_cd` | `trantype` | `tran_type` |

### Financial Reconciliation
| Check | Description | Rule |
|---|---|---|
| Net transaction total | Sum of all transaction amounts | Must be computable (no parse errors) |
| No zero-amount purchases | Purchase transactions (type 01) | Amount > 0 for all purchases |

---

## 6. Disclosure Groups (`discgrp.txt` / `CVTRA02Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total disclosure group records | 51 | Count of non-empty lines in `discgrp.txt` |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Group ID non-empty | `dis_acct_group_id` | Must not be blank |
| Transaction type code valid | `dis_tran_type_cd` | Must be 2 characters |
| Interest rate parseable | `dis_int_rate` | Must be a valid signed decimal |

### Cross-File Consistency
| Check | Description | Rule |
|---|---|---|
| Group IDs match account groups | Disclosure group IDs should align with account group IDs or known group names | `dis_acct_group_id` in (`account.acct_group_id` values + `DEFAULT` + `ZEROAPR`) |

---

## 7. Transaction Category Balances (`tcatbal.txt` / `CVTRA01Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total tcat balance records | 50 | Count of non-empty lines in `tcatbal.txt` |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Account ID valid | `trancat_acct_id` | Must exist in `acctdata` account IDs |
| Type code valid | `trancat_type_cd` | Must be 2 characters |
| Category code valid | `trancat_cd` | Must be a 4-digit number |
| Balance parseable | `tran_cat_bal` | Must be a valid signed decimal |

### Referential Integrity
| Check | Foreign Key | Parent Table | Parent Key |
|---|---|---|---|
| TCatBal -> Account | `trancat_acct_id` | `acctdata` | `acct_id` |

---

## 8. Transaction Categories (`trancatg.txt` / `CVTRA04Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total transaction category records | 18 | Count of non-empty lines in `trancatg.txt` |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Type code valid | `tran_type_cd` | Must exist in `trantype.txt` |
| Category code positive | `tran_cat_cd` | Must be > 0 |
| Description non-empty | `tran_cat_type_desc` | Must not be blank |

### Cross-File Consistency
| Check | Description | Rule |
|---|---|---|
| All type codes covered | Categories reference valid type codes | All `tran_type_cd` values exist in `trantype.txt` |

---

## 9. Transaction Types (`trantype.txt` / `CVTRA03Y.cpy`)

### Row Count
| Check | Expected | Source |
|---|---|---|
| Total transaction type records | 7 | Count of non-empty lines in `trantype.txt` |

### Field Validation
| Check | Field | Rule |
|---|---|---|
| Type codes complete | `tran_type` | Must include codes `01` through `07` |
| Description non-empty | `tran_type_desc` | Must not be blank |

### Completeness
| Check | Description | Rule |
|---|---|---|
| Type code coverage | All 7 standard types present | Codes `{01, 02, 03, 04, 05, 06, 07}` all present |

---

## Batch Job Reconciliation

These checks validate the outputs of migrated batch jobs against their legacy equivalents.

### POSTTRAN (Transaction Posting)

| Check | Description | Validation |
|---|---|---|
| Input record count | All daily transactions processed | Input count == processed count + rejected count |
| Account balance delta | Net balance change matches transaction sum | `SUM(post_amounts) == SUM(acct_balance_after) - SUM(acct_balance_before)` |
| No orphan transactions | All posted transactions reference valid accounts | Every `tran_card_num` maps to a valid `acct_id` via `cardxref` |

### INTCALC (Interest Calculation)

| Check | Description | Validation |
|---|---|---|
| Interest applied to all active accounts | Every active account receives interest calculation | `count(interest_records) == count(active_accounts)` |
| Rate source matches disclosure | Interest rate used matches disclosure group rate | Rate from `discgrp` for account's group matches applied rate |
| Balance update consistency | New balance = old balance + interest amount | `acct_curr_bal_after == acct_curr_bal_before + interest_amt` |

### CREASTMT (Statement Creation)

| Check | Description | Validation |
|---|---|---|
| Statement count | One statement per active account | `count(statements) == count(active_accounts)` |
| Statement balance | Statement closing balance matches current account balance | `stmt_closing_bal == acct_curr_bal` |
| Customer data correct | Statement customer name matches customer file | `stmt_name == custdata.first_name + custdata.last_name` |

### COMBTRAN (Combine Transactions)

| Check | Description | Validation |
|---|---|---|
| Record count preserved | Combined file has same record count as inputs | `count(combined) == count(input_transactions)` |
| No data loss | Every input transaction appears in output | All `tran_id` values from input present in output |
| Sort order | Output is sorted by required key | Records ordered by `tran_id` or `tran_orig_ts` |

---

## Execution

Run all reconciliation checks:

```bash
python test-harness/reconciliation.py
```

Or as part of the full test suite:

```bash
python test-harness/run_all_tests.py --recon-only
```
