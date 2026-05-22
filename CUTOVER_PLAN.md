# CardDemo Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk** functional areas. Each phase includes entry criteria, migration activities, validation gates, and rollback procedures.

**Estimated Total Duration**: 18–24 months across 6 phases

---

## Phase Dependency Map

```
Phase 0 ──> Phase 1 ──> Phase 2 ──> Phase 3 ──> Phase 4 ──> Phase 5
Foundation   Low-Risk     Medium      Core        Batch       Optional
             Leaf Nodes   Services    Services    Financial   Modules

[Weeks 1-4]  [Months 1-3] [Months 3-6] [Months 6-12] [Months 12-18] [Months 18-24]
```

---

## Phase 0: Foundation & Infrastructure (Weeks 1–4)

**Risk Level**: Very Low
**Objective**: Establish the target platform, CI/CD pipeline, database schema, and anti-corruption layer.

### 0.1 Target Platform Setup

| Activity | Detail |
|:---------|:-------|
| Provision cloud infrastructure | PostgreSQL RDS, Spring Boot runtime (ECS/EKS), API Gateway |
| Database schema creation | Translate VSAM copybook layouts to DDL (see mapping below) |
| CI/CD pipeline | GitHub Actions / Jenkins for Java builds, automated testing, deployment |
| Observability | Logging (CloudWatch/ELK), metrics (Prometheus/Grafana), tracing (Jaeger) |
| Message broker | Kafka/SQS cluster for event-driven patterns |

### 0.2 VSAM-to-RDBMS Schema Mapping

| VSAM Dataset | Copybook | Table Name | Primary Key | Record Size |
|:-------------|:---------|:-----------|:------------|------------:|
| ACCTDATA | `CVACT01Y` | `accounts` | `acct_id` (BIGINT) | 300 → ~15 columns |
| CARDDATA | `CVACT02Y` | `cards` | `card_num` (VARCHAR 16) | 150 → ~6 columns |
| CARDXREF | `CVACT03Y` | `card_xref` | `xref_card_num` (VARCHAR 16) | 50 → 3 columns |
| CUSTDATA | `CVCUS01Y` | `customers` | `cust_id` (BIGINT) | 500 → ~18 columns |
| TRANSACT | `CVTRA05Y` | `transactions` | `tran_id` (VARCHAR 16) | 350 → ~13 columns |
| USRSEC | `CSUSR01Y` | `users` | `usr_id` (VARCHAR 8) | 80 → 5 columns |
| TRANTYPE | `CVTRA03Y` | `transaction_types` | `tran_type_cd` (VARCHAR 2) | 60 → ~3 columns |
| TRANCATG | `CVTRA04Y` | `transaction_categories` | `tran_cat_cd` (INTEGER) | 60 → ~3 columns |
| DISCGRP | `CVTRA02Y` | `disclosure_groups` | `disc_grp_key` (composite) | 50 → ~3 columns |
| TCATBALF | `CVTRA01Y` | `tran_cat_balances` | `(acct_id, tran_cat_cd)` | 50 → ~3 columns |

### 0.3 Data Migration (Initial Load)

1. Convert EBCDIC data files to UTF-8 (ASCII files already exist in `app/data/ASCII/`)
2. Parse fixed-width records using copybook definitions
3. Load into PostgreSQL tables using Spring Batch or `COPY` command
4. Validate record counts and checksums against source

### 0.4 Anti-Corruption Layer (ACL)

Deploy bidirectional ACL for the transition period:
- **VSAM Proxy**: Java service that translates REST calls to VSAM file operations (for unconverted programs)
- **Change Data Capture**: Sync VSAM changes to RDBMS during dual-operation period

### 0.5 Retire Batch Infrastructure

| Component | Action | Risk |
|:----------|:-------|:-----|
| CLOSEFIL / OPENFIL JCL | Retire — no equivalent needed | None |
| WAITSTEP / COBSWAIT | Retire — replaced by scheduler wait steps | None |
| DEFGDGB / DEFGDGD | Retire — GDGs replaced by timestamped storage | None |
| ESDSRRDS | Retire — ESDS/RRDS replaced by RDBMS tables | None |

**Exit Criteria**: Target platform operational, schema deployed, initial data loaded, ACL functional, batch infrastructure retired.

---

## Phase 1: Low-Risk Leaf Nodes (Months 1–3)

**Risk Level**: Low
**Objective**: Migrate contexts with no downstream consumers and minimal business logic.

### 1.1 Identity & Access Management (BC-1)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Implement Spring Security + JWT authentication service | 1 week |
| 2 | Migrate `USRSEC` data to `users` table with bcrypt password hashing | 2 days |
| 3 | Build user admin CRUD API (replaces `COUSR00C`–`COUSR03C`) | 1 week |
| 4 | Deploy Token Translator ACL: intercept CICS COMMAREA, inject JWT claims as `CDEMO-USER-ID` / `CDEMO-USER-TYPE` | 1 week |
| 5 | Validate: all CICS programs still receive correct user context | 3 days |

**Programs Retired**: `COSGN00C`, `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C` (5 programs, 2,027 LOC)
**VSAM Datasets Retired**: USRSEC
**JCL Retired**: DUSRSECJ

**Rollback**: Reactivate USRSEC VSAM and COSGN00C. Token Translator ACL has fallback to VSAM-based auth.

### 1.2 Reference Data (BC-8)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Migrate reference VSAM data to PostgreSQL tables | 2 days |
| 2 | Build Reference Data Service API (GET endpoints + optional admin CRUD) | 1 week |
| 3 | If DB2 optional module is installed: redirect DB2 queries to PostgreSQL | 3 days |
| 4 | Update ACL to redirect VSAM reads from `TRANTYPE`, `TRANCATG`, `DISCGRP` to API | 1 week |
| 5 | Retire weekly Control-M refresh jobs (`WEEKLY-TransactionTypesDBRefresh`, `WEEKLY-DisclosureGroupsRefresh`) | 1 day |

**Programs Retired**: IDCAMS utility jobs (no COBOL programs)
**VSAM Datasets Retired**: TRANTYPE, TRANCATG, DISCGRP
**JCL Retired**: TRANTYPE, TRANCATG, DISCGRP, TCATBALF refresh jobs, MNTTRDB2
**Scheduler Retired**: 2 Control-M folders

**Rollback**: Restore weekly VSAM refresh cycle from DB2 extracts.

### 1.3 Menu Navigation & Routing (BC-1/BC-2 UI)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build SPA shell with router (React/Angular) | 1 week |
| 2 | Map `COMEN02Y` menu options to SPA routes | 2 days |
| 3 | Implement role-based menu visibility (Admin vs. User) | 2 days |
| 4 | Connect SPA to JWT auth service | 2 days |

**Programs Retired**: `COMEN01C`, `COADM01C` (2 programs, 596 LOC)
**BMS Maps Retired**: COMEN01, COADM01

**Rollback**: Restore 3270 menu screens.

**Phase 1 Exit Criteria**:
- Identity service handling all authentication; JWT tokens in use
- Reference data served from PostgreSQL
- SPA shell with routing operational
- 7 programs retired (2,623 LOC)
- Mainframe still fully operational for all non-migrated functions via ACL

---

## Phase 2: Medium-Risk Read-Only Services (Months 3–6)

**Risk Level**: Medium
**Objective**: Migrate read-heavy contexts and reporting. No financial write operations yet.

### 2.1 Customer Management (BC-4)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Customer Service API with JPA entity from `CVCUS01Y` | 1 week |
| 2 | Migrate `CUSTDATA` to `customers` table | 2 days |
| 3 | Implement PII encryption for SSN, DOB, government ID | 3 days |
| 4 | Update ACL: redirect CUSTDATA VSAM reads to Customer API | 1 week |
| 5 | Validate: `COACTVWC` (Account View) still displays customer info correctly | 2 days |

**Programs Retired**: `CBCUS01C` (1 program, 178 LOC)
**VSAM Datasets Retired**: CUSTDATA
**JCL Retired**: CUSTFILE

### 2.2 Card Management (BC-3) — XREF Service

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Card Service API with entities from `CVACT02Y` and `CVACT03Y` | 2 weeks |
| 2 | Migrate `CARDDATA` and `CARDXREF` to PostgreSQL | 3 days |
| 3 | Create XREF lookup API: `GET /cards/xref?cardNum=X` and `GET /cards/xref?acctId=Y` | 1 week |
| 4 | Build SPA screens for card list, view, and update | 2 weeks |
| 5 | Update ACL: redirect all XREF VSAM reads to XREF API | 1 week |
| 6 | Validate: all 6+ XREF consumers still function correctly | 1 week |

**Programs Retired**: `COCRDLIC`, `COCRDSLC`, `COCRDUPC` (3 programs, 3,906 LOC)
**VSAM Datasets Retired**: CARDDATA, CARDXREF (XREF read API replaces VSAM AIX)
**JCL Retired**: CARDFILE, XREFFILE

**Critical**: The XREF API is consumed by 6+ programs. This migration must be validated exhaustively.

### 2.3 Reporting & Statements (BC-9)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Reporting Service with template engine (JasperReports/Thymeleaf) | 2 weeks |
| 2 | Implement transaction report generation (replaces `CBTRN03C`) | 1 week |
| 3 | Implement statement generation in HTML + PDF (replaces `CBSTM03A`/`CBSTM03B`) | 2 weeks |
| 4 | Build SPA report request screen (replaces `CORPT00C`) | 1 week |
| 5 | Validate: compare new reports against mainframe-generated reports line-by-line | 1 week |

**Programs Retired**: `CORPT00C`, `CBTRN03C`, `CBSTM03A`, `CBSTM03B` (4 programs, 2,452 LOC)
**JCL Retired**: TRANREPT, CREASTMT

### 2.4 Data Migration Utility (BC-10)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build migration export API using service-to-service calls | 1 week |
| 2 | Build migration import API with JSON-based record format | 1 week |
| 3 | Validate: export → import round-trip produces identical data | 3 days |

**Programs Retired**: `CBEXPORT`, `CBIMPORT` (2 programs, 1,069 LOC)
**JCL Retired**: CBEXPORT, CBIMPORT

**Phase 2 Exit Criteria**:
- Customer, Card, and XREF services operational
- All XREF consumers validated
- Reporting service producing identical output to mainframe
- 10 additional programs retired (7,605 LOC)
- Running total: 17 programs retired (10,228 LOC)

---

## Phase 3: Core Online Services (Months 6–12)

**Risk Level**: High
**Objective**: Migrate the core account and transaction online CRUD with balance-affecting operations.

### 3.1 Account Management (BC-2)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Account Service API with JPA entity from `CVACT01Y` | 2 weeks |
| 2 | **Carefully migrate validation logic** from `COACTUPC` (4,236 LOC): phone, state code, yes/no, mandatory, signed number validations | 3 weeks |
| 3 | Implement balance-adjustment endpoint with optimistic locking | 1 week |
| 4 | Build SPA screens for account view and update | 2 weeks |
| 5 | Migrate `ACCTDATA` to `accounts` table (if not already done in Phase 0 initial load) | 3 days |
| 6 | **Dual-write validation**: run old CICS and new API in parallel for 2 weeks, compare results | 2 weeks |
| 7 | Cutover: switch primary to new Account Service | 1 day |

**Programs Retired**: `COACTVWC`, `COACTUPC` (2 programs, 5,177 LOC)
**BMS Maps Retired**: COACTVW, COACTUP

**Critical Validation**: The `COACTUPC` program is 4,236 LOC — the largest in the system. Its validation logic must be preserved exactly. Create a comprehensive test suite from the existing validation rules before retiring the COBOL program.

### 3.2 Transaction Online CRUD (BC-5 — Online Only)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Transaction Service API with JPA entity from `CVTRA05Y` | 2 weeks |
| 2 | Implement transaction list with cursor-based pagination | 1 week |
| 3 | Implement transaction add with XREF card-number validation (via Card Service API) | 1 week |
| 4 | Build SPA screens for transaction list, view, and add | 2 weeks |
| 5 | Migrate `TRANSACT` VSAM to `transactions` table | 3 days |
| 6 | Dual-write validation: 1 week | 1 week |
| 7 | Cutover | 1 day |

**Programs Retired**: `COTRN00C`, `COTRN01C`, `COTRN02C` (3 programs, 1,812 LOC)
**BMS Maps Retired**: COTRN00, COTRN01, COTRN02

### 3.3 Bill Payment (BC-7)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Payment Service orchestrating Account and Transaction APIs | 1 week |
| 2 | Implement two-step confirmation (initiate → confirm) with idempotency keys | 1 week |
| 3 | Build SPA payment screen | 1 week |
| 4 | Validate: payment creates correct transaction and updates correct account balance | 1 week |

**Programs Retired**: `COBIL00C` (1 program, 572 LOC)
**BMS Maps Retired**: COBIL00

**Phase 3 Exit Criteria**:
- All online CICS transactions replaced by SPA + REST APIs
- Balance adjustments go through Account Service API with optimistic locking
- Dual-write validation passed for account and transaction operations
- 6 additional programs retired (7,561 LOC)
- Running total: 23 programs retired (17,789 LOC)
- **All BMS maps and CICS transactions can be decommissioned**

---

## Phase 4: Batch Financial Processing (Months 12–18)

**Risk Level**: Very High
**Objective**: Migrate the core batch financial processing — transaction posting and interest calculation. These are the highest-risk components due to direct monetary impact.

### 4.1 Transaction Posting (BC-5 — Batch)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Implement Spring Batch job replicating `CBTRN02C` logic | 3 weeks |
| 2 | Map daily transaction file ingestion to file upload or message queue | 1 week |
| 3 | Implement card-number validation against XREF API | 1 week |
| 4 | Implement account balance update via Account Service API | 1 week |
| 5 | Implement category balance tracking in `tran_cat_balances` table | 1 week |
| 6 | Implement rejection handling (dead-letter queue / error table replacing GDG) | 1 week |
| 7 | **Reconciliation testing**: run old and new batch in parallel for 1 full month | 4 weeks |
| 8 | Compare: transaction master, account balances, category balances, rejection counts | 1 week |
| 9 | Cutover to new batch processor | 1 day |

**Programs Retired**: `CBTRN02C`, `CBTRN01C` (2 programs, 1,225 LOC)
**JCL Retired**: POSTTRAN, TRANBKP, COMBTRAN, TRANIDX, DALYREJS
**Scheduler Retired**: Control-M `DAILY-TransactionBackup` folder
**VSAM Datasets Retired**: DALYTRAN, TCATBALF

**Critical**: Financial reconciliation must show **zero discrepancies** before cutover. Run parallel for a full business cycle.

### 4.2 Interest Calculation (BC-6)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Implement Spring Batch job replicating `CBACT04C` logic | 3 weeks |
| 2 | Map COMP-3 packed decimal arithmetic to Java `BigDecimal` with explicit rounding modes | 2 weeks |
| 3 | Implement disclosure group rate lookup via Reference Data API | 1 week |
| 4 | Implement account balance update via Account Service API | 1 week |
| 5 | Implement system transaction generation for computed interest | 1 week |
| 6 | **Reconciliation testing**: run old and new interest calculation on same month's data | 2 weeks |
| 7 | Compare: computed interest amounts to the **cent** for every account | 1 week |
| 8 | Cutover | 1 day |

**Programs Retired**: `CBACT04C` (1 program, 652 LOC)
**JCL Retired**: INTCALC
**Scheduler Retired**: Control-M `MONTHLY-InterestCalculation` folder

**Critical**: COMP-3 to `BigDecimal` conversion must use `RoundingMode.HALF_EVEN` (banker's rounding) to match mainframe behavior. Test with edge cases: very large balances, very small rates, zero balances, negative balances.

### 4.3 Replace Batch Scheduler

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Implement job orchestration in Spring Cloud Data Flow or Apache Airflow | 2 weeks |
| 2 | Replicate Control-M folder dependencies in new scheduler | 1 week |
| 3 | Implement alerting and retry policies (replace MAXRERUN=5) | 3 days |
| 4 | Parallel run both schedulers for 1 cycle | 1 week |
| 5 | Decommission Control-M jobs | 1 day |

**Phase 4 Exit Criteria**:
- All batch processing running on Spring Batch + new scheduler
- Full-month reconciliation passed with zero financial discrepancies
- Control-M fully decommissioned
- 3 additional programs retired (1,877 LOC)
- Running total: 26 programs retired (19,666 LOC)

---

## Phase 5: Optional Modules & Final Decommission (Months 18–24)

**Risk Level**: High (but isolated)
**Objective**: Migrate the IMS/DB2/MQ authorization module and complete mainframe decommission.

### 5.1 Authorization Processing (BC-11)

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Build Authorization microservice with Kafka/SQS event consumer | 3 weeks |
| 2 | Migrate IMS database to PostgreSQL authorization schema | 2 weeks |
| 3 | Migrate DB2 `AUTHFRDS` table to PostgreSQL | 1 week |
| 4 | Build pending authorization screens in SPA | 2 weeks |
| 5 | Implement expired authorization purge as scheduled job | 1 week |
| 6 | MQ-to-Kafka bridge: route auth requests to new service | 2 weeks |
| 7 | Validation: process sample authorization requests through both paths | 2 weeks |
| 8 | Cutover: switch MQ routing to new service | 1 day |

**Programs Retired**: `COPAUA0C`, `COPAUS0C`, `COPAUS1C`, `COPAUS2C`, `CBPAUP0C`, plus IMS utilities (8 programs, ~2,000 LOC)

### 5.2 Date Utility & Miscellaneous

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Replace `CSUTLDTC` date conversion utility with Java `DateTimeFormatter` | 2 days |
| 2 | Replace ASSEMBLER routines (`MVSWAIT`, `COBDATFT`) with Java equivalents | 1 week |

**Programs Retired**: `CSUTLDTC` (157 LOC), assembler routines

### 5.3 Final Decommission

| Step | Activity | Duration |
|:-----|:---------|:---------|
| 1 | Verify all CICS transactions deactivated | 1 day |
| 2 | Archive VSAM datasets to long-term storage | 1 week |
| 3 | Archive EBCDIC data files | 1 day |
| 4 | Decommission CICS region | 1 day |
| 5 | Decommission mainframe LPAR (if dedicated) | 1 day |
| 6 | Update disaster recovery procedures for new platform | 1 week |
| 7 | Knowledge transfer and documentation | 2 weeks |

**Phase 5 Exit Criteria**:
- All 31+ COBOL programs retired
- All VSAM datasets decommissioned or archived
- All BMS maps, JCL, and Control-M jobs removed
- Mainframe dependency fully eliminated
- Documentation complete

---

## Cutover Summary Timeline

| Phase | Months | Programs Retired | LOC Retired | Cumulative LOC | Risk |
|:------|:------:|:----------------:|:-----------:|:--------------:|:----:|
| Phase 0 | 0–1 | 1 (COBSWAIT) | 41 | 41 | Very Low |
| Phase 1 | 1–3 | 7 | 2,623 | 2,664 | Low |
| Phase 2 | 3–6 | 10 | 7,605 | 10,269 | Medium |
| Phase 3 | 6–12 | 6 | 7,561 | 17,830 | High |
| Phase 4 | 12–18 | 3 | 1,877 | 19,707 | Very High |
| Phase 5 | 18–24 | 9+ | ~2,157+ | ~20,650+ | High |

---

## Rollback Strategy

Each phase has an independent rollback plan:

| Phase | Rollback Trigger | Rollback Action | RTO |
|:------|:-----------------|:----------------|:----|
| Phase 0 | Schema deployment failure | Drop new tables, no mainframe impact | < 1 hour |
| Phase 1 | Auth service failure | Reactivate USRSEC VSAM auth, restore 3270 menus | < 2 hours |
| Phase 2 | XREF API failure | Reactivate VSAM XREF reads via ACL fallback | < 1 hour |
| Phase 3 | Balance discrepancy > $0.01 | Revert to CICS online screens, restore VSAM from backup | < 4 hours |
| Phase 4 | Reconciliation failure | Revert to COBOL batch + Control-M, restore VSAM from pre-batch backup | < 2 hours |
| Phase 5 | Authorization processing failure | Reroute MQ back to IMS/CICS path | < 1 hour |

**Golden Rule**: No phase cutover is irreversible until the next phase begins. VSAM backups are retained for the duration of the subsequent phase.

---

## Staffing Model

| Role | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5 |
|:-----|:-------:|:-------:|:-------:|:-------:|:-------:|:-------:|
| Java/Spring developers | 2 | 3 | 4 | 4 | 3 | 3 |
| Frontend (SPA) developers | 0 | 1 | 2 | 2 | 0 | 1 |
| COBOL/Mainframe SME | 1 | 1 | 1 | 2 | 2 | 1 |
| DBA (VSAM + RDBMS) | 1 | 1 | 1 | 1 | 1 | 1 |
| QA / Test automation | 1 | 1 | 2 | 3 | 3 | 2 |
| DevOps / Infrastructure | 2 | 1 | 1 | 1 | 1 | 1 |
| Project manager | 1 | 1 | 1 | 1 | 1 | 1 |
| **Total** | **8** | **9** | **12** | **14** | **11** | **10** |
