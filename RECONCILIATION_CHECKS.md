# CardDemo Reconciliation Checks

Per-job validation specifications for the COBOL-to-Java migration of CardDemo.
Each check defines a data integrity invariant that must hold after migration.

---

## Entity Relationship Overview

```
custdata (50) ──┐
                ├── cardxref (50) ──┐
carddata (50) ──┘                   │
                                    ├── dailytran (300)
acctdata (50) ──────────────────────┘
    │
    ├── tcatbal (50)     (category balances per account)
    └── discgrp (51)     (interest rates per account group)
         │
         └── trantype (7) / trancatg (18)  (reference masters)
```

---

## Cross-Entity Integrity Checks

### R-01: Card-to-Account Reference

| Property | Value |
|----------|-------|
| **ID** | R-01 |
| **Invariant** | Every card record references an existing account |
| **Source entity** | `carddata` (`CARD-ACCT-ID`) |
| **Target entity** | `acctdata` (`ACCT-ID`) |
| **Check type** | Foreign key |
| **SQL equivalent** | `SELECT * FROM carddata c WHERE NOT EXISTS (SELECT 1 FROM acctdata a WHERE a.acct_id = c.card_acct_id)` |
| **Severity** | Critical - orphan cards cannot process transactions |
| **Batch jobs affected** | CARDFILE, POSTTRAN, INTCALC |

### R-02: Cross-Reference Integrity

| Property | Value |
|----------|-------|
| **ID** | R-02 |
| **Invariant** | Every cross-reference links a valid card, customer, and account |
| **Source entity** | `cardxref` (`XREF-CARD-NUM`, `XREF-CUST-ID`, `XREF-ACCT-ID`) |
| **Target entities** | `carddata` (`CARD-NUM`), `custdata` (`CUST-ID`), `acctdata` (`ACCT-ID`) |
| **Check type** | Composite foreign key (3-way join) |
| **SQL equivalent** | `SELECT * FROM cardxref x WHERE NOT EXISTS (SELECT 1 FROM carddata c WHERE c.card_num = x.xref_card_num) OR NOT EXISTS (SELECT 1 FROM custdata cu WHERE cu.cust_id = x.xref_cust_id) OR NOT EXISTS (SELECT 1 FROM acctdata a WHERE a.acct_id = x.xref_acct_id)` |
| **Severity** | Critical - broken xrefs break customer lookups |
| **Batch jobs affected** | XREFFILE, CUSTFILE, all online CICS lookups |

### R-03: Transaction-to-Card Reference

| Property | Value |
|----------|-------|
| **ID** | R-03 |
| **Invariant** | Every daily transaction references a valid card number |
| **Source entity** | `dailytran` (`DALYTRAN-CARD-NUM`) |
| **Target entity** | `carddata` (`CARD-NUM`) |
| **Check type** | Foreign key |
| **SQL equivalent** | `SELECT * FROM dailytran t WHERE NOT EXISTS (SELECT 1 FROM carddata c WHERE c.card_num = t.dalytran_card_num)` |
| **Severity** | Critical - orphan transactions cannot be posted |
| **Batch jobs affected** | POSTTRAN, TRANFILE, COMBTRAN, TRANBKP |

### R-04: Category Balance Account Coverage

| Property | Value |
|----------|-------|
| **ID** | R-04 |
| **Invariant** | Transaction category balance records reference valid accounts |
| **Source entity** | `tcatbal` (`TRANCAT-ACCT-ID`) |
| **Target entity** | `acctdata` (`ACCT-ID`) |
| **Check type** | Foreign key |
| **SQL equivalent** | `SELECT * FROM tcatbal t WHERE NOT EXISTS (SELECT 1 FROM acctdata a WHERE a.acct_id = t.trancat_acct_id)` |
| **Severity** | High - orphan balances indicate data corruption |
| **Batch jobs affected** | TCATBALF, POSTTRAN, INTCALC |

### R-05: Disclosure Group References

| Property | Value |
|----------|-------|
| **ID** | R-05 |
| **Invariant** | Disclosure group transaction types are valid and account groups reference existing disclosure groups |
| **Entities** | `discgrp` (`DIS-TRAN-TYPE-CD`), `trantype` (`TRAN-TYPE`), `acctdata` (`ACCT-GROUP-ID`) |
| **Check type** | Foreign key + reverse lookup |
| **Validation** | (1) Every `DIS-TRAN-TYPE-CD` in discgrp must exist in trantype. (2) Every non-empty `ACCT-GROUP-ID` in acctdata must match a `DIS-ACCT-GROUP-ID` in discgrp. |
| **Severity** | High - incorrect rates if group mapping is broken |
| **Batch jobs affected** | DISCGRP, INTCALC |

### R-06: Transaction Category Code Validity

| Property | Value |
|----------|-------|
| **ID** | R-06 |
| **Invariant** | Transaction category codes in daily transactions exist in the category master |
| **Source entity** | `dailytran` (`DALYTRAN-TYPE-CD`, `DALYTRAN-CAT-CD`) |
| **Target entity** | `trancatg` (`TRAN-TYPE-CD`, `TRAN-CAT-CD`) |
| **Check type** | Composite foreign key |
| **SQL equivalent** | `SELECT * FROM dailytran t WHERE NOT EXISTS (SELECT 1 FROM trancatg c WHERE c.tran_type_cd = t.dalytran_type_cd AND c.tran_cat_cd = t.dalytran_cat_cd)` |
| **Severity** | High - unknown categories break reporting and interest calc |
| **Batch jobs affected** | POSTTRAN, TRANREPT, INTCALC, TRANCATG |

### R-07: Transaction Type Code Validity

| Property | Value |
|----------|-------|
| **ID** | R-07 |
| **Invariant** | Transaction type codes in daily transactions exist in the type master |
| **Source entity** | `dailytran` (`DALYTRAN-TYPE-CD`) |
| **Target entity** | `trantype` (`TRAN-TYPE`) |
| **Check type** | Foreign key |
| **SQL equivalent** | `SELECT * FROM dailytran t WHERE NOT EXISTS (SELECT 1 FROM trantype tt WHERE tt.tran_type = t.dalytran_type_cd)` |
| **Severity** | High - unknown types break classification |
| **Batch jobs affected** | POSTTRAN, TRANREPT, TRANTYPE |

### R-08: Record Count Preservation

| Property | Value |
|----------|-------|
| **ID** | R-08 |
| **Invariant** | Record counts are identical between legacy and migrated data |
| **Entities** | All data files |
| **Expected counts** | acctdata: 50, carddata: 50, custdata: 50, cardxref: 50, dailytran: 300, trantype: 7, trancatg: 18, tcatbal: 50, discgrp: 51 |
| **Check type** | Count comparison |
| **Severity** | Critical - missing or extra records indicate data loss or duplication |
| **Batch jobs affected** | All data load jobs (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, etc.) |

### R-09: Account Status Consistency

| Property | Value |
|----------|-------|
| **ID** | R-09 |
| **Invariant** | Active accounts (status = 'Y') have valid open and expiration dates |
| **Entity** | `acctdata` (`ACCT-ACTIVE-STATUS`, `ACCT-OPEN-DATE`, `ACCT-EXPIRATION-DATE`) |
| **Check type** | Business rule |
| **Validation** | If `ACCT-ACTIVE-STATUS = 'Y'`, then `ACCT-OPEN-DATE` and `ACCT-EXPIRATION-DATE` must be non-empty and not `0000-00-00` |
| **Severity** | Medium - inconsistent status may cause business logic errors |
| **Batch jobs affected** | ACCTFILE, INTCALC, CREASTMT |

### R-10: Customer-Card Relationship Symmetry

| Property | Value |
|----------|-------|
| **ID** | R-10 |
| **Invariant** | Customer-card relationship is symmetric via cross-reference |
| **Entities** | `custdata` (`CUST-ID`), `carddata` (`CARD-NUM`), `cardxref` (`XREF-CUST-ID`, `XREF-CARD-NUM`) |
| **Check type** | Bidirectional completeness |
| **Validation** | (1) Every customer in custdata appears in at least one xref record. (2) Every card in carddata appears in at least one xref record. |
| **Severity** | High - orphan entities break lookups and reporting |
| **Batch jobs affected** | CUSTFILE, CARDFILE, XREFFILE, all online CICS lookups |

---

## Per-Batch-Job Validation Matrix

The table below maps each batch job to the reconciliation checks that should
pass before and after it runs.

| JCL Job | Program | Pre-Checks | Post-Checks | Description |
|---------|---------|------------|-------------|-------------|
| `ACCTFILE.jcl` | Data load | R-08 | R-01, R-04, R-08, R-09 | Load/refresh account master |
| `CARDFILE.jcl` | Data load | R-08 | R-01, R-02, R-08, R-10 | Load/refresh card master |
| `CUSTFILE.jcl` | Data load | R-08 | R-02, R-08, R-10 | Load/refresh customer master |
| `XREFFILE.jcl` | Data load | R-08 | R-02, R-08, R-10 | Load card cross-reference |
| `TRANFILE.jcl` | Data load | R-08 | R-03, R-06, R-07, R-08 | Load transaction master |
| `TCATBALF.jcl` | Data load | R-08 | R-04, R-08 | Load category balances |
| `DISCGRP.jcl` | Data load | R-08 | R-05, R-08 | Load disclosure groups |
| `TRANTYPE.jcl` | Data load | R-08 | R-05, R-07, R-08 | Load transaction types |
| `TRANCATG.jcl` | Data load | R-08 | R-05, R-06, R-08 | Load transaction categories |
| `POSTTRAN.jcl` | `CBTRN02C` | R-01 to R-08 | R-01, R-03, R-04, R-06, R-07, R-08 | Post daily transactions to accounts |
| `INTCALC.jcl` | `CBACT04C` | R-01, R-04, R-05 | R-04, R-09 | Calculate interest per disclosure group |
| `COMBTRAN.jcl` | Combine | R-03, R-06, R-07 | R-03, R-06, R-07, R-08 | Merge daily transactions into master |
| `CREASTMT.JCL` | `CBSTM03A/B` | R-01, R-02, R-04 | R-08 | Generate account statements |
| `TRANREPT.jcl` | `CBTRN03C` | R-03, R-06, R-07 | R-08 | Produce transaction report |
| `TRANBKP.jcl` | Backup | R-08 | R-08 | Backup transaction files |
| `DUSRSECJ.jcl` | Data load | R-08 | R-08 | Load user security VSAM |

---

## Full Batch Cycle Validation

The CardDemo batch cycle runs jobs in this order:

```
CLOSEFIL -> Data refresh jobs -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL
```

### Validation checkpoints during the batch cycle:

| Checkpoint | After Job(s) | Required Checks | Purpose |
|------------|-------------|-----------------|---------|
| CP-1 | Data refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE) | R-01 to R-10 | Full integrity after data load |
| CP-2 | POSTTRAN | R-01, R-03, R-04, R-06, R-07, R-08 | Balances updated correctly |
| CP-3 | INTCALC | R-04, R-05, R-09 | Interest accrued correctly |
| CP-4 | COMBTRAN | R-03, R-06, R-07, R-08 | Daily merged into master |
| CP-5 | CREASTMT | R-08 | Statements generated without data loss |
| CP-6 | OPENFIL (end of cycle) | R-01 to R-10 | Full integrity after complete cycle |

---

## Running the Checks

```bash
# Run all reconciliation checks against golden files
cd test-harness
python -m pytest test_reconciliation.py -v

# Run a specific check
python -m pytest test_reconciliation.py::TestReconciliationChecks::test_r01_card_references_account -v

# Run against migrated Java output (provide path to Java-generated golden files)
python -m pytest test_reconciliation.py -v --golden-dir /path/to/java/output
```

## Adding New Checks

1. Define the invariant in this document with ID, entities, check type, and severity.
2. Implement the check as a method in `test-harness/reconciliation.py` (`ReconciliationRunner`).
3. Add it to `run_all()` and create a test in `test_reconciliation.py`.
4. Update the per-job validation matrix above.
