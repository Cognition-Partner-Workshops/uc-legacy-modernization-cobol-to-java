#!/usr/bin/env python3
"""Generate the CardDemo modernization backlog (CSV + JSON) from the AWS Transform
analysis artifacts.

    python3 backlog/generate.py --analysis-dir /path/to/aws-transform-output

Everything mechanical (copybooks, datasets, DD names, access modes, callers/callees,
JCL, code issues, LOC/complexity) is read from the analysis output. Functional
grouping, program purpose, labels and story-point calibration are hand-authored
below because the vendor run produced no decomposition or business-rule output.
"""
from __future__ import annotations

import argparse
import csv
import glob
import json
import os
import re
from collections import defaultdict

CSV_COLUMNS = [
    "Issue Type", "Summary", "Description", "Priority", "Labels",
    "Epic Name", "Parent", "Story Points", "Components",
]

# --------------------------------------------------------------------------------------
# Epics (functional / bounded areas derived from the dependency graph + COBOL)
# --------------------------------------------------------------------------------------
EPICS = [
    ("E-PLAT", "Platform Foundation and Shared Utilities",
     "Spring Boot 3.5 / Java 21 monorepo skeleton, Spring Batch runtime, common CICS-replacement "
     "abstractions (COMMAREA -> session/DTO, BMS map -> web view or REST), shared utility programs "
     "(CSUTLDTC, COBSWAIT, COBDATFT/MVSWAIT assembler), CI/CD and observability. Everything else "
     "depends on this epic.", "Platform"),
    ("E-DATA", "Shared Data Foundation and VSAM/Db2/IMS Migration",
     "One story per VSAM cluster / Db2 table / IMS database: copybook record layout -> JPA entity + "
     "Flyway DDL, EBCDIC unload/convert/load pipeline (COMP-3, COMP, signed DISPLAY), AIX -> secondary "
     "index, GDG -> versioned object store, plus the export/import bridge programs (CBEXPORT/CBIMPORT). "
     "Derived from data_dictionary_cbl.csv, dsn_to_file.csv and the IDCAMS define/load JCL.", "Data"),
    ("E-AUTH", "Sign-on, Menus and Navigation",
     "COSGN00C (CC00) sign-on against USRSEC, COMEN01C (CM00) user menu and COADM01C (CA00) admin menu, "
     "COCOM01Y commarea contract, COMEN02Y/COADM02Y menu option tables (dynamic XCTL targets the vendor "
     "flagged as unresolved). Gateway for every online function.", "Online"),
    ("E-USER", "User Administration",
     "COUSR00C/01C/02C/03C (CU00-CU03): list, add, update, delete users in USRSEC (CSUSR01Y). "
     "Admin-only, reached via COADM01C.", "Online"),
    ("E-ACCT", "Account and Customer Management",
     "COACTVWC (CAVW) account view, COACTUPC (CAUP) account+customer update, batch readers "
     "CBACT01C/CBCUS01C. Touches ACCTDATA, CUSTDATA and the CARDXREF AIX path (CXACAIX).", "Online"),
    ("E-CARD", "Card Management",
     "COCRDLIC (CCLI) card list with browse, COCRDSLC (CCDL) card detail, COCRDUPC (CCUP) card update, "
     "batch readers CBACT02C/CBACT03C. Touches CARDDATA (+CARDAIX) and CARDXREF.", "Online"),
    ("E-TRAN", "Transaction Inquiry, Entry and Bill Payment",
     "COTRN00C (CT00) transaction list, COTRN01C (CT01) detail, COTRN02C (CT02) add transaction, "
     "COBIL00C (CB00) bill payment. All read/write TRANSACT and use CXACAIX/CCXREF lookups; CT02 and "
     "CR00 call CSUTLDTC for date validation.", "Online"),
    ("E-POST", "Transaction Posting and Interest Batch",
     "Daily posting chain POSTTRAN (CBTRN02C), legacy poster CBTRN01C, monthly INTCALC (CBACT04C) and "
     "COMBTRAN, category balances TCATBALF, DISCGRP, DALYREJS rejects, SYSTRAN. Control-M "
     "MONTHLY-InterestCalculation and DAILY-TransactionBackup flows.", "Batch"),
    ("E-RPT", "Reporting and Statements",
     "CORPT00C (CR00) online report request that submits TRANREPT via the internal reader, CBTRN03C "
     "transaction report, CBSTM03A/CBSTM03B statement generation (text + HTML), TXT2PDF1, PRTCATBL, "
     "REPTFILE.", "Batch"),
    ("E-TTYP", "Transaction Type Reference Data (Db2 sub-application)",
     "app-transaction-type-db2: COTRTLIC (CTLI) list with forward/backward cursors, COTRTUPC (CTTU) "
     "maintenance, COBTUPDT batch update via MNTTRDB2, CREADB21 DDL bootstrap, TRANEXTR unload. "
     "Tables CARDDEMO.TRANSACTION_TYPE and TRANSACTION_TYPE_CATEGORY (DCLTRTYP/DCLTRCAT).", "Db2"),
    ("E-PAUT", "Card Pre-authorization (IMS DB / Db2 / MQ sub-application)",
     "app-authorization-ims-db2-mq: MQ-driven authorizer COPAUA0C, online viewers COPAUS0C (CPVS) / "
     "COPAUS1C (CPVD), fraud marking COPAUS2C into Db2 AUTHFRDS, IMS DB PAUTDB (DBPAUTP0/DBPAUTX0 DBDs, "
     "PSBPAUTB/PSBPAUTL PSBs), batch purge CBPAUP0C (EXEC DLI) and unload/load utilities "
     "PAUDBUNL/PAUDBLOD/DBUNLDGS.", "IMS"),
    ("E-MQ", "MQ Request/Reply Services (VSAM-MQ sub-application)",
     "app-vsam-mq: COACCT01 (CDRA) account inquiry and CODATE01 (CDRD) date service, triggered from MQ "
     "queues via CICS; re-platform as message listeners (JMS/AMQP) with the same request/reply "
     "contracts.", "Integration"),
    ("E-CUT", "Parity Testing, Cutover and Decommission",
     "Cross-cutting tasks: golden-file harness, EBCDIC test-data fixtures, screen/REST parity suites, "
     "batch run-book parity, scheduler migration, dual-run and cutover, mainframe decommission.", "Platform"),
]
EPIC_BY_ID = {e[0]: e for e in EPICS}

# --------------------------------------------------------------------------------------
# Program catalogue: epic, one-line purpose, extra labels, notes inferred from the COBOL
# (things the vendor graph did not say), optional point override.
# --------------------------------------------------------------------------------------
P = {
    # --- platform / utilities
    "CSUTLDTC": dict(epic="E-PLAT", kind="util", purpose="date validation utility (CEEDAYS wrapper) called by COTRN02C and CORPT00C",
                     labels=["cobol", "utility"], notes="Statically CALLed (not CICS LINK); becomes a shared DateValidationService bean."),
    "COBSWAIT": dict(epic="E-PLAT", kind="batch", purpose="batch wait step calling MVSWAIT assembler, used by WAITSTEP.jcl between scheduler steps",
                     labels=["cobol", "batch", "asm"], notes="Replace with scheduler-native delay/dependency; MVSWAIT.asm has no Java equivalent and should be retired."),
    # --- sign-on / navigation
    "COSGN00C": dict(epic="E-AUTH", kind="cics", purpose="sign-on screen; validates user id/password against USRSEC and XCTLs to admin or user menu",
                     labels=["cobol", "cics", "vsam", "security"], notes="Plaintext password compare in COBOL; target uses Spring Security + BCrypt with a one-off hash migration of USRSEC."),
    "COMEN01C": dict(epic="E-AUTH", kind="cics", purpose="main user menu; dispatches to option programs from the COMEN02Y table",
                     labels=["cobol", "cics"], notes="Vendor flagged CDEMO-MENU-OPT-PGMNAME as an unresolved dynamic call; the target list is the static table in COMEN02Y.cpy (CAVW, CAUP, CCLI, CCDL, CCUP, CT00, CT01, CT02, CR00, CB00)."),
    "COADM01C": dict(epic="E-AUTH", kind="cics", purpose="admin menu; dispatches to user-admin and Db2 transaction-type programs from COADM02Y",
                     labels=["cobol", "cics"], notes="Vendor flagged CDEMO-ADMIN-OPT-PGMNAME as unresolved; targets are in COADM02Y.cpy (COUSR00C-03C, COTRTLIC/COTRTUPC)."),
    # --- user admin
    "COUSR00C": dict(epic="E-USER", kind="cics", purpose="user list with STARTBR/READNEXT/READPREV paging over USRSEC", labels=["cobol", "cics", "vsam"]),
    "COUSR01C": dict(epic="E-USER", kind="cics", purpose="add user (WRITE USRSEC)", labels=["cobol", "cics", "vsam"]),
    "COUSR02C": dict(epic="E-USER", kind="cics", purpose="update user (READ UPDATE / REWRITE USRSEC)", labels=["cobol", "cics", "vsam"]),
    "COUSR03C": dict(epic="E-USER", kind="cics", purpose="delete user (READ / DELETE USRSEC)", labels=["cobol", "cics", "vsam"]),
    # --- account / customer
    "COACTVWC": dict(epic="E-ACCT", kind="cics", purpose="account view: card xref via CXACAIX path, then ACCTDAT and CUSTDAT reads", labels=["cobol", "cics", "vsam"]),
    "COACTUPC": dict(epic="E-ACCT", kind="cics", purpose="account and customer update with full field-level edit rules (largest online program, CC 373)",
                     labels=["cobol", "cics", "vsam"], notes="Contains the bulk of the business validation rules (state/zip via CSLKPCDY, phone, SSN, FICO range, date edits via CSUTLDPY/CSUTLDWY); rules must be extracted into Bean Validation + service-level checks. Two-file update (ACCTDAT + CUSTDAT) needs a single transaction.", points=21),
    "CBACT01C": dict(epic="E-ACCT", kind="batch", purpose="account file reader/dumper; writes three sequential variants (PS, VB, array) and calls COBDATFT assembler",
                     labels=["cobol", "batch", "vsam", "asm"], notes="COBDATFT.asm date-format call has no lineage entry; replace with java.time formatting."),
    "CBCUS01C": dict(epic="E-ACCT", kind="batch", purpose="customer file sequential reader (READCUST)", labels=["cobol", "batch", "vsam"]),
    # --- card
    "COCRDLIC": dict(epic="E-CARD", kind="cics", purpose="card list with browse, filter by account/card and selection to detail/update", labels=["cobol", "cics", "vsam"]),
    "COCRDSLC": dict(epic="E-CARD", kind="cics", purpose="card detail view via CARDDAT primary key or CARDAIX alternate index", labels=["cobol", "cics", "vsam"]),
    "COCRDUPC": dict(epic="E-CARD", kind="cics", purpose="card update (name, status, expiry) with READ UPDATE / REWRITE on CARDDAT", labels=["cobol", "cics", "vsam"]),
    "CBACT02C": dict(epic="E-CARD", kind="batch", purpose="card file sequential reader (READCARD)", labels=["cobol", "batch", "vsam"]),
    "CBACT03C": dict(epic="E-CARD", kind="batch", purpose="card cross-reference sequential reader (READXREF)", labels=["cobol", "batch", "vsam"]),
    # --- transactions / bill pay
    "COTRN00C": dict(epic="E-TRAN", kind="cics", purpose="transaction list with paging over TRANSACT", labels=["cobol", "cics", "vsam"]),
    "COTRN01C": dict(epic="E-TRAN", kind="cics", purpose="transaction detail view", labels=["cobol", "cics", "vsam"]),
    "COTRN02C": dict(epic="E-TRAN", kind="cics", purpose="add transaction: xref lookup, date validation via CSUTLDTC, next-id via READPREV, WRITE TRANSACT", labels=["cobol", "cics", "vsam"]),
    "COBIL00C": dict(epic="E-TRAN", kind="cics", purpose="bill payment: pays full balance, writes a TRANSACT record and REWRITEs ACCTDAT balance",
                     labels=["cobol", "cics", "vsam"], notes="Two-file update (TRANSACT write + ACCTDAT rewrite) requires one Spring transaction; confirmation flow (Y/N) is a two-step screen conversation."),
    # --- posting / interest
    "CBTRN01C": dict(epic="E-POST", kind="batch", purpose="legacy daily transaction poster (xref/account/card/customer lookups for each DALYTRAN record)",
                     labels=["cobol", "batch", "vsam"], notes="Vendor produced NO data lineage and NO JCL for this program: no job in app/jcl executes it (superseded by CBTRN02C). Migrate as a verification-only reader or retire after confirming with the business."),
    "CBTRN02C": dict(epic="E-POST", kind="batch", purpose="daily transaction posting: validates DALYTRAN, writes TRANSACT, updates ACCTFILE balances and TCATBALF, rejects to DALYREJS (POSTTRAN)",
                     labels=["cobol", "batch", "vsam"], notes="Core business rules: card-not-found, account-not-found, over-limit (credit limit vs balance+amount), expired card; reject codes 100-103 are the parity contract.", points=8),
    "CBACT04C": dict(epic="E-POST", kind="batch", purpose="monthly interest calculation per account/category using DISCGRP rates, writes SYSTRAN interest transactions and updates ACCTFILE (INTCALC)",
                     labels=["cobol", "batch", "vsam"], notes="Default-group fallback when DISCGRP key is missing; interest = balance * rate / 1200 with COMP-3 truncation semantics that must be reproduced with BigDecimal scale/rounding.", points=8),
    # --- reporting / statements
    "CORPT00C": dict(epic="E-RPT", kind="cics", purpose="report request screen (monthly/yearly/custom range) that builds TRANREPT JCL and submits it via the CICS internal reader (TDQ JOBS)",
                     labels=["cobol", "cics", "batch"], notes="Dynamic JCL submission has no lineage entry; replace with an async job trigger (Spring Batch JobLauncher / queue)."),
    "CBTRN03C": dict(epic="E-RPT", kind="batch", purpose="transaction detail report by date range with page/account/grand totals (TRANREPT)", labels=["cobol", "batch", "vsam"]),
    "CBSTM03A": dict(epic="E-RPT", kind="batch", purpose="statement generator: text and HTML statements per account, calls CBSTM03B for file access (CREASTMT)",
                     labels=["cobol", "batch", "vsam"], notes="Uses ALTER/GO TO-style control flow and a 2-D OCCURS array of transactions; restructure rather than transliterate."),
    "CBSTM03B": dict(epic="E-RPT", kind="batch", purpose="file-access subroutine for CBSTM03A (open/read/close TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE)",
                     labels=["cobol", "batch", "vsam"], notes="Vendor lists no copybooks for this program: record layouts are declared inline, so data_dictionary_cbl.csv is the only source for its record structures."),
    # --- Db2 transaction types
    "COTRTLIC": dict(epic="E-TTYP", kind="cics", purpose="transaction type list with forward/backward Db2 cursors and inline delete/update (CTLI)",
                     labels=["cobol", "cics", "db2"], notes="Vendor flagged DSNTIAC (Db2 error-message formatter) as unsupported; replace with SQLException mapping."),
    "COTRTUPC": dict(epic="E-TTYP", kind="cics", purpose="transaction type add/update/delete screen (CTTU)", labels=["cobol", "cics", "db2"]),
    "COBTUPDT": dict(epic="E-TTYP", kind="batch", purpose="batch insert/update/delete of TRANSACTION_TYPE from a control file (MNTTRDB2)",
                     labels=["cobol", "batch", "db2"], notes="INPFILE DD resolves to the literal DSN 'INPFILE' in MNTTRDB2.jcl (vendor reports it as missing); the real input is supplied at run time."),
    # --- pre-authorization
    "COPAUA0C": dict(epic="E-PAUT", kind="cics", purpose="MQ-triggered authorization engine: reads request from queue, checks card/account/customer, applies rules, stores decision in IMS PAUTDB and replies",
                     labels=["cobol", "cics", "mq", "ims", "vsam"], notes="No CICS transaction linked by the vendor; CRDDEMO2.csd defines it under CP00 (MQ trigger). IMS EXEC DLI calls to PAUTDB are NOT in the vendor lineage.", points=13),
    "COPAUS0C": dict(epic="E-PAUT", kind="cics", purpose="pending authorizations summary by account (CPVS)", labels=["cobol", "cics", "ims", "vsam"],
                     notes="IMS DB reads (EXEC DLI) not captured by the vendor lineage; only the VSAM lookups are."),
    "COPAUS1C": dict(epic="E-PAUT", kind="cics", purpose="authorization detail view (CPVD) with option to mark fraud via COPAUS2C", labels=["cobol", "cics", "ims"]),
    "COPAUS2C": dict(epic="E-PAUT", kind="cics", purpose="mark authorization as fraud: INSERT/UPDATE CARDDEMO.AUTHFRDS", labels=["cobol", "cics", "db2"]),
    "CBPAUP0C": dict(epic="E-PAUT", kind="batch", purpose="delete expired pending authorizations from IMS PAUTDB via EXEC DLI GN/GNP/DLET (CBPAUP0J)",
                     labels=["cobol", "batch", "ims"], notes="Vendor lineage lists only IMS system libraries; the actual DB access (EXEC DLI on PCB 2 of PSBPAUTB) is not represented."),
    "PAUDBUNL": dict(epic="E-PAUT", kind="batch", purpose="unload PAUTDB root/child segments to two sequential files (UNLDPADB)", labels=["cobol", "batch", "ims"]),
    "PAUDBLOD": dict(epic="E-PAUT", kind="batch", purpose="load PAUTDB from root/child sequential files (LOADPADB)", labels=["cobol", "batch", "ims"]),
    "DBUNLDGS": dict(epic="E-PAUT", kind="batch", purpose="unload PAUTDB to GSAM datasets via CBLTDLI (UNLDGSAM)", labels=["cobol", "batch", "ims"],
                     notes="Vendor lineage has empty access modes for the GSAM outputs; they are WRITE."),
    # --- MQ services
    "COACCT01": dict(epic="E-MQ", kind="cics", purpose="MQ request/reply account inquiry: MQGET request, READ ACCTDAT, MQPUT reply (CDRA)", labels=["cobol", "cics", "mq", "vsam"]),
    "CODATE01": dict(epic="E-MQ", kind="cics", purpose="MQ request/reply current-date service (CDRD)", labels=["cobol", "cics", "mq"],
                     notes="No file or DB access; vendor graph shows MQ API calls only."),
    # --- export / import
    "CBEXPORT": dict(epic="E-DATA", kind="batch", purpose="export accounts, cards, xrefs, customers and transactions into the 500-byte multi-record EXPORT.DATA file (CVEXPORT)", labels=["cobol", "batch", "vsam", "data-migration"]),
    "CBIMPORT": dict(epic="E-DATA", kind="batch", purpose="split EXPORT.DATA back into per-entity import files with an error file (CBIMPORT)", labels=["cobol", "batch", "vsam", "data-migration"]),
}

# CICS transaction ids from the dependency graph are picked up automatically; these are the
# ones the vendor did not link (from the CSD files).
CSD_TRANSACTIONS = {"COPAUA0C": "CP00 (MQ trigger)", "COACCT01": "CDRA", "CODATE01": "CDRD"}

# --------------------------------------------------------------------------------------
# Data-migration stories: dataset family -> copybook(s), consumers are read from lineage
# --------------------------------------------------------------------------------------
DATASETS = [
    dict(key="ACCTDATA", dsn="AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS", cpy=["CVACT01Y.cpy"], kind="VSAM KSDS", load_jcl=["ACCTFILE.jcl"],
         entity="Account (ACCT-ID key, balances, credit limits, cycle dates, group id; COMP-3 amounts)", points=8, labels=["vsam", "data-migration"]),
    dict(key="CUSTDATA", dsn="AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS", cpy=["CVCUS01Y.cpy", "CUSTREC.cpy"], kind="VSAM KSDS", load_jcl=["CUSTFILE.jcl", "DEFCUST.jcl"],
         entity="Customer (CUST-ID key, PII, address, SSN, FICO, DOB)", points=8, labels=["vsam", "data-migration", "pii"],
         notes="CUSTREC.cpy (used by CBSTM03A) and CVCUS01Y.cpy (everything else) describe the same 500-byte record; reconcile into one entity. DEFCUST.jcl defines an alternate cluster name AWS.CUSTDATA.CLUSTER that no program uses."),
    dict(key="CARDDATA", dsn="AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS", cpy=["CVACT02Y.cpy"], kind="VSAM KSDS + AIX (CARDDATA.VSAM.AIX on account id -> CARDAIX path)", load_jcl=["CARDFILE.jcl"],
         entity="Card (CARD-NUM key, ACCT-ID alternate key, CVV, embossed name, expiry, status)", points=8, labels=["vsam", "data-migration", "pii"]),
    dict(key="CARDXREF", dsn="AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS", cpy=["CVACT03Y.cpy"], kind="VSAM KSDS + AIX (CARDXREF.VSAM.AIX on account id -> CXACAIX/CCXREF paths)", load_jcl=["XREFFILE.jcl"],
         entity="Card/Customer/Account cross reference (50 bytes)", points=5, labels=["vsam", "data-migration"],
         notes="Most-read dataset in the estate (12 readers). Candidate to collapse into foreign keys on Card once Account/Customer/Card are relational."),
    dict(key="TRANSACT", dsn="AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS", cpy=["CVTRA05Y.cpy"], kind="VSAM KSDS + AIX (TRANSACT.VSAM.AIX, defined by TRANFILE/TRANIDX)", load_jcl=["TRANFILE.jcl", "TRANIDX.jcl", "TRANBKP.jcl"],
         entity="Posted transaction (TRAN-ID key, type/category codes, amount COMP-3, merchant, card, orig/proc timestamps)", points=8, labels=["vsam", "data-migration"],
         notes="TRANBKP/COMBTRAN back up and rebuild the cluster daily; GDG TRANSACT.BKUP/COMBINED become versioned snapshots."),
    dict(key="TCATBALF", dsn="AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS", cpy=["CVTRA01Y.cpy"], kind="VSAM KSDS", load_jcl=["TCATBALF.jcl", "PRTCATBL.jcl"],
         entity="Transaction category balance per account/type/category", points=5, labels=["vsam", "data-migration"]),
    dict(key="DISCGRP", dsn="AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS", cpy=["CVTRA02Y.cpy"], kind="VSAM KSDS", load_jcl=["DISCGRP.jcl", "DEFGDGD.jcl"],
         entity="Disclosure group interest rates (group id + type + category key)", points=3, labels=["vsam", "data-migration"]),
    dict(key="TRANTYPE", dsn="AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS", cpy=["CVTRA03Y.cpy"], kind="VSAM KSDS", load_jcl=["TRANTYPE.jcl"],
         entity="Transaction type reference (VSAM copy; the Db2 sub-app holds the same data in CARDDEMO.TRANSACTION_TYPE)", points=3, labels=["vsam", "data-migration"],
         notes="Duplicated in Db2 (TRANEXTR unloads Db2 back to TRANTYPE.PS). Target keeps a single reference table; decide system of record."),
    dict(key="TRANCATG", dsn="AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS", cpy=["CVTRA04Y.cpy"], kind="VSAM KSDS", load_jcl=["TRANCATG.jcl"],
         entity="Transaction category reference (type + category key)", points=3, labels=["vsam", "data-migration"],
         notes="Also duplicated in Db2 as CARDDEMO.TRANSACTION_TYPE_CATEGORY."),
    dict(key="USRSEC", dsn="AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS", cpy=["CSUSR01Y.cpy"], kind="VSAM KSDS (ESDS/RRDS variants defined by ESDSRRDS.jcl are unused by programs)", load_jcl=["DUSRSECJ.jcl", "ESDSRRDS.jcl"],
         entity="Security user (user id key, plaintext password, first/last name, type A/U)", points=5, labels=["vsam", "data-migration", "security"],
         notes="Passwords are stored in clear text; migration must hash them (BCrypt) rather than copy."),
    dict(key="TRXFL", dsn="AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS", cpy=[], kind="VSAM KSDS (temporary, rebuilt by CREASTMT from a SORT of TRANSACT)", load_jcl=["CREASTMT.JCL"],
         entity="Statement work file keyed by card number (layout inline in CBSTM03B, see data_dictionary_cbl.csv)", points=2, labels=["vsam", "batch"],
         notes="Not a system-of-record dataset; in the target it disappears in favour of a query on transactions by card."),
    dict(key="EXPORT", dsn="AWS.M2.CARDDEMO.EXPORT.DATA", cpy=["CVEXPORT.cpy"], kind="VSAM KSDS (500-byte polymorphic multi-record export)", load_jcl=["CBEXPORT.jcl", "CBIMPORT.jcl"],
         entity="Export envelope: record type + one of account/card/xref/customer/transaction payloads", points=5, labels=["vsam", "data-migration"]),
    dict(key="DALYTRAN", dsn="AWS.M2.CARDDEMO.DALYTRAN.PS", cpy=["CVTRA06Y.cpy"], kind="Sequential (PS) input + DALYREJS GDG output", load_jcl=["TRANFILE.jcl", "DALYREJS.jcl"],
         entity="Daily transaction feed and reject record (CVTRA06Y payload + reject reason)", points=5, labels=["batch", "data-migration"],
         notes="External feed contract: keep fixed-width EBCDIC ingestion adapter until upstream is modernized."),
    dict(key="TRANREPT", dsn="AWS.M2.CARDDEMO.TRANREPT", cpy=["CVTRA07Y.cpy"], kind="GDG report output (+ SYSTRAN GDG written by CBACT04C, STATEMNT.PS/HTML written by CBSTM03A)", load_jcl=["DEFGDGB.jcl", "REPTFILE.jcl"],
         entity="Report/statement output layouts", points=3, labels=["batch"],
         notes="GDG generations map to versioned files in object storage with retention matching the LIMIT in DEFGDGB."),
    dict(key="DB2TRTYP", dsn="CARDDEMO.TRANSACTION_TYPE / CARDDEMO.TRANSACTION_TYPE_CATEGORY", cpy=["DCLTRTYP.dcl", "DCLTRCAT.dcl"], kind="Db2 tables (DDL TRNTYPE.ddl, TRNTYCAT.ddl, indexes XTRNTYPE/XTRNTYCT, CREADB21.jcl bootstrap)", load_jcl=["CREADB21.jcl", "TRANEXTR.jcl"],
         entity="Transaction type and category reference tables", points=3, labels=["db2", "data-migration"]),
    dict(key="DB2AUTHFRDS", dsn="CARDDEMO.AUTHFRDS", cpy=["AUTHFRDS.dcl"], kind="Db2 table (AUTHFRDS.ddl, index XAUTHFRD on CARD_NUM + AUTH_TS DESC)", load_jcl=[],
         entity="Authorization fraud markers", points=2, labels=["db2", "data-migration"]),
    dict(key="IMSPAUTDB", dsn="OEM.IMS.IMSP.PAUTHDB / PAUTHDBX (DBDs DBPAUTP0, DBPAUTX0; GSAM DBDs PASFLDBD, PADFLDBD)", cpy=["CIPAUDTY.cpy", "CIPAUSMY.cpy", "PAUTBPCB.CPY", "PASFLPCB.CPY", "PADFLPCB.CPY"], kind="IMS DB HIDAM root (account summary) + child (authorization detail) with secondary index; PSBs PSBPAUTB, PSBPAUTL, PAUTBUNL, DLIGSAMP", load_jcl=["DBPAUTP0.jcl", "LOADPADB.JCL", "UNLDPADB.JCL", "UNLDGSAM.JCL"],
         entity="Pending authorization summary (root) and authorization messages (child)", points=13, labels=["ims", "data-migration"],
         notes="Hierarchical -> relational: root becomes authorization_summary(account_id), child becomes authorization(account_id, auth_ts). DFSVSMDB control card is missing from the analysis input (vendor issue)."),
]

# Cross-cutting tasks
TASKS = [
    dict(epic="E-PLAT", summary="Bootstrap Spring Boot 3.5 / Java 21 multi-module project and CI pipeline",
         desc="Maven multi-module (core, online, batch, integration), spring-boot-starter-parent 3.5.x, Java 21, Flyway, JPA, Spring Batch, Actuator, Spotless + JaCoCo gates, GitHub Actions running ./mvnw verify. Acceptance: empty app boots, /actuator/health is UP, CI green.", points=5, labels=["platform", "ci"]),
    dict(epic="E-PLAT", summary="Build the CICS-replacement abstractions: COMMAREA session model, BMS map to view/REST mapping, pseudo-conversational flow",
         desc="Model COCOM01Y (CARDDEMO-COMMAREA: user id/type, from/to program and transaction, selected account/card/customer, PFK handling) as a typed session DTO; map each BMS map in app/bms and app/cpy-bms (21 maps: COSGN00, COMEN01, COADM01, COACTVW, COACTUP, COCRDLI, COCRDSL, COCRDUP, COTRN00-02, COBIL00, CORPT00, COUSR00-03, COTRTLI, COTRTUP, COPAU00, COPAU01) to a Thymeleaf view or REST resource; shared header/title (COTTL01Y), date (CSDAT01Y) and message (CSMSG01Y/CSMSG02Y) copybooks become a layout fragment. Acceptance: one reference screen (COSGN00) round-trips through the abstraction.", points=8, labels=["cics", "platform"]),
    dict(epic="E-PLAT", summary="Build the Spring Batch job framework replacing JCL step semantics",
         desc="Job/step skeleton with return-code mapping (COBOL RETURN-CODE / CEE3ABD abend -> ExitStatus), restartability, GDG-style versioned output writer, IDCAMS/IEBGENER/SORT step equivalents (delete-define, copy, sort/merge as tasklets), CLOSEFIL/OPENFIL replaced by a maintenance-mode toggle on the online API. Covers utility JCL: CLOSEFIL, OPENFIL, WAITSTEP, DEFGDGB, DEFGDGD, DEFCUST, ESDSRRDS, DUSRSECJ, DALYREJS, REPTFILE, CBADMCDJ, FTPJCL, INTRDRJ1, INTRDRJ2, TXT2PDF1. Acceptance: a no-op job runs under Control-M-equivalent scheduler with the same exit codes.", points=8, labels=["batch", "platform"]),
    dict(epic="E-PLAT", summary="Retire assembler dependencies (COBDATFT.asm, MVSWAIT.asm, CSUTLDTC maclib) with Java equivalents",
         desc="COBDATFT (date formatting, called by CBACT01C) -> java.time DateTimeFormatter; MVSWAIT (STIMER wait, called by COBSWAIT) -> scheduler dependency; document CEEDAYS/CEE3ABD LE calls. Acceptance: no program story depends on an assembler artifact.", points=3, labels=["asm", "platform"]),
    dict(epic="E-DATA", summary="Generate the copybook-to-entity mapping catalogue from data_dictionary_cbl.csv",
         desc="For every record-layout copybook in app/cpy (CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CUSTREC, CVTRA01Y-CVTRA07Y, CSUSR01Y, CVEXPORT, CIPAUDTY, CIPAUSMY, DCLTRTYP, DCLTRCAT, AUTHFRDS) produce field -> Java type mapping (PIC X -> String, S9 COMP-3 -> BigDecimal(scale), COMP -> int/long, level-88 -> enum, REDEFINES -> discriminated union, OCCURS -> list) using the level/PIC/usage/offset columns of the vendor data dictionary. Output: docs/data-mapping.md + JSON consumed by the converter. Acceptance: every field of every copybook has a mapping row; offsets sum to the record length.", points=8, labels=["data-migration", "cobol"]),
    dict(epic="E-DATA", summary="Build the EBCDIC fixed-width to relational conversion pipeline",
         desc="Generic converter (Spring Batch) driven by the mapping catalogue: reads app/data/EBCDIC/*.PS (code page 037), decodes COMP-3/COMP/zoned, validates against the copybook length, loads Postgres via JDBC batch, emits reject file and row counts. Reused by every dataset story. Acceptance: round-trip test - convert EBCDIC -> DB -> re-encode EBCDIC yields byte-identical records for ACCTDATA sample.", points=8, labels=["data-migration", "vsam"]),
    dict(epic="E-CUT", summary="Build the golden-file batch parity harness",
         desc="Runs a legacy batch output (captured from the mainframe or the EBCDIC samples in app/data) and the Java job on identical input, compares record-by-record after EBCDIC->ASCII normalisation, reports diffs. Used by all batch parity tests (POSTTRAN, INTCALC, TRANREPT, CREASTMT, CBEXPORT/CBIMPORT, READ* dumps). Acceptance: harness detects a deliberately mutated amount field.", points=5, labels=["testing", "batch"]),
    dict(epic="E-CUT", summary="Build the online screen/API parity suite",
         desc="For each CICS transaction (CC00, CM00, CA00, CU00-CU03, CAVW, CAUP, CCLI, CCDL, CCUP, CT00-CT02, CB00, CR00, CTLI, CTTU, CPVS, CPVD) a scripted scenario set (happy path, each validation error, PF-key navigation) with expected field values derived from the COBOL edit rules; executed against the Java app via HTTP/Playwright. Acceptance: suite runs in CI; every story in the online epics links its scenarios here.", points=8, labels=["testing", "cics"]),
    dict(epic="E-CUT", summary="Curate EBCDIC and ASCII test-data fixtures from app/data",
         desc="Version the 13 EBCDIC PS files and the ASCII equivalents under test resources, add synthetic edge cases (negative COMP-3, max credit limit, expired card, missing DISCGRP group, duplicate user id), document PII masking. Acceptance: fixtures load through the conversion pipeline with zero rejects.", points=3, labels=["testing", "data-migration"]),
    dict(epic="E-CUT", summary="Migrate the Control-M / CA7 schedules to the target scheduler",
         desc="Recreate the 15 Control-M jobs found by the vendor (DAILY-TransactionBackup: CLOSEFIL, TRANBKP, WAITSTEP, OPENFIL; WEEKLY-DisclosureGroupsRefresh: CLOSEFIL, DISCGRP, WAITSTEP, OPENFIL; MONTHLY-InterestCalculation: CLOSEFIL, INTCALC, COMBTRAN, WAITSTEP, OPENFIL; WEEKLY-TransactionTypesDBRefresh: MNTTRDB2, TRANEXTR) plus the CA7 definitions in app/scheduler/CardDemo.ca7 as scheduler DAGs calling Spring Batch jobs. Acceptance: calendar, dependency order and failure handling match the XML.", points=5, labels=["batch", "scheduler"]),
    dict(epic="E-CUT", summary="Plan and execute dual-run, cutover and mainframe decommission",
         desc="Wave-by-wave dual run (see backlog/README.md migration waves): freeze window, final EBCDIC unload + convert, reconciliation report (row counts, balance totals per account, TCATBALF totals), DNS/queue switch for MQ services, rollback plan, CICS/IMS/Db2 resource decommission checklist (CSD groups CARDDEMO, CRDDEMO2, CRDDEMOM). Acceptance: signed-off reconciliation for every dataset story; legacy regions stopped.", points=8, labels=["cutover"]),
    dict(epic="E-PAUT", summary="Provision MQ/JMS infrastructure and queue definitions for authorization and inquiry services",
         desc="Request/reply queues used by COPAUA0C (authorization request/response), COACCT01 and CODATE01 (inquiry), dead-letter/error queues (WS-ERR-QUEUE in COACCT01), CMQ* copybook constants -> JMS headers. Provide a local broker in docker-compose for tests. Acceptance: a test client can put a request and receive a correlated reply through the Java listener skeleton.", points=5, labels=["mq", "platform"]),
]

# --------------------------------------------------------------------------------------
# Loading of vendor artifacts
# --------------------------------------------------------------------------------------

def one(pattern: str) -> str:
    matches = glob.glob(pattern)
    if len(matches) != 1:
        raise SystemExit(f"expected exactly one file for {pattern}, found {matches}")
    return matches[0]


def load_analysis(root: str) -> dict:
    code = os.path.join(root, "analyze_code")
    data = os.path.join(root, "analyze_data")
    a = {}
    a["classification"] = json.load(open(one(f"{code}/classification_*.json")))
    a["dependencies"] = json.load(open(one(f"{code}/dependencies_*.json")))
    a["assets"] = {r["Name"].upper(): r for r in csv.DictReader(open(one(f"{code}/assets_*.csv")))}
    issues = defaultdict(list)
    for r in csv.DictReader(open(one(f"{code}/code_issues_*.csv"))):
        issues[r["File"].upper()].append(f"{r['Name']}: {r['File Details']}")
    a["issues"] = issues
    a["missing"] = list(csv.DictReader(open(one(f"{code}/missing_*.csv"))))
    lineage = defaultdict(list)
    for r in csv.DictReader(open(f"{data}/data_lineage_output/program_to_dsn.csv")):
        lineage[r["File Name"].upper()].append(r)
    a["lineage"] = lineage
    jcl_by_prog = defaultdict(set)
    jcl_steps = defaultdict(list)
    for r in csv.DictReader(open(f"{data}/data_lineage_output/jcl_to_dsn.csv")):
        jcl_by_prog[r["Program Name"].split("/")[-1].upper()].add(r["JCL File"])
        jcl_steps[r["JCL File"].upper()].append(r)
    a["jcl_by_prog"] = jcl_by_prog
    a["jcl_steps"] = jcl_steps
    dsn_users = defaultdict(set)
    for r in csv.DictReader(open(f"{data}/data_lineage_output/dsn_to_file.csv")):
        dsn_users[r["Data Source Name"]].add((r["File Name"], r["Program Name"], r["Access Type"]))
    a["dsn_users"] = dsn_users
    dd_fields = defaultdict(int)
    for fn in ("data_dictionary_cbl.csv", "data_dictionary_cpy.csv"):
        for r in csv.DictReader(open(f"{data}/data_dictionary_output/{fn}")):
            dd_fields[os.path.basename(r["source_file"]).upper()] += 1
    a["dd_fields"] = dd_fields
    return a


def program_files(a) -> list[dict]:
    return [c for c in a["classification"] if c["fileType"] == "cob"]


def stem(name: str) -> str:
    return re.sub(r"\.(cbl|cob)$", "", name, flags=re.I).upper()


# --------------------------------------------------------------------------------------
# Story points: Fibonacci from effective LOC + cyclomatic complexity, adjusted per kind
# --------------------------------------------------------------------------------------
FIB = [1, 2, 3, 5, 8, 13, 21]


def fib_points(eff_lines: int, cc: int, kind: str) -> int:
    score = eff_lines / 250 + cc / 40
    if kind == "cics":
        score += 0.5  # screen conversion + pseudo-conversational state
    for threshold, pts in ((1.0, 1), (1.8, 2), (3.5, 3), (5.0, 5), (8.0, 8), (14.0, 13)):
        if score < threshold:
            return pts
    return 21


# --------------------------------------------------------------------------------------
# Description builders
# --------------------------------------------------------------------------------------

def dep_groups(node: dict) -> dict:
    g = defaultdict(list)
    for d in node["dependencies"]:
        g[d["type"]].append((d["name"], d["dependencyType"]))
    return g


def build_program_story(prog: dict, a: dict, nodes: dict, callers: dict, data_story_for_dsn: dict) -> dict:
    name = stem(prog["name"])
    meta = P[name]
    node = nodes[prog["name"].upper()]
    asset = a["assets"].get(prog["name"].upper(), {})
    eff = int(asset.get("Effective Lines", 0) or 0)
    cc = int(asset.get("Cyclomatic Complexity", 0) or 0)
    g = dep_groups(node)

    copybooks = sorted({n for n, _ in g.get("CPY", [])} | {n for n, _ in g.get("SQL", [])})
    bms = sorted({n for n, _ in g.get("BMS", [])})
    trans = sorted({n for n, _ in g.get("TRANSACTION", [])})
    if name in CSD_TRANSACTIONS:
        trans.append(CSD_TRANSACTIONS[name])
    callees = sorted({stem(n) for n, _ in g.get("COB", [])})
    cics_files = sorted({f"{n} ({t.replace('; [Dynamic] Call', '')})" for n, t in g.get("CICS_FILE", [])})
    sql_tables = sorted({f"{n} ({t})" for n, t in g.get("[SQL]TABLE", [])})
    sys_calls = sorted({n for n, t in g.get("System", []) if "Call" in t})
    asm = sorted({n for n, _ in g.get("ASM", [])})

    lineage_rows = a["lineage"].get(prog["name"].upper(), [])
    seen = set()
    lineage_lines = []
    for r in lineage_rows:
        key = (r["DD Name"], r["Data Source Name"])
        if key in seen:
            continue
        seen.add(key)
        dd = f"{r['DD Name']} -> " if r["DD Name"] else ""
        mode = r["Access Type"] or "(mode not captured by vendor)"
        lineage_lines.append(f"{dd}{r['Data Source Name']} [{r['Data Source Type']}; {mode}]")

    jcls = sorted(a["jcl_by_prog"].get(name, set()))
    issues = a["issues"].get(prog["name"].upper(), [])
    callers_of = sorted(callers.get(prog["name"].upper(), set()))

    kind_word = {"cics": "CICS online program", "batch": "batch program", "util": "utility subprogram"}[meta["kind"]]
    lines = [
        f"Migrate {name} ({prog['path']}), {kind_word}: {meta['purpose']}.",
        "",
        f"Metrics (assets csv): {asset.get('Total lines', '?')} total / {eff} effective lines, cyclomatic complexity {cc}, "
        f"{a['dd_fields'].get(prog['name'].upper(), 0)} data items in the vendor data dictionary.",
    ]
    if trans:
        lines.append(f"CICS transaction(s): {', '.join(trans)}.")
    if bms:
        lines.append(f"BMS map(s): {', '.join(bms)}.")
    lines.append(f"Copybooks: {', '.join(copybooks) if copybooks else 'none (record layouts declared inline)'}.")
    if cics_files:
        lines.append(f"CICS files: {'; '.join(cics_files)}.")
    if sql_tables:
        lines.append(f"Db2 tables: {'; '.join(sql_tables)}.")
    if lineage_lines:
        lines.append("Datasets / DD names (program_to_dsn): " + "; ".join(lineage_lines) + ".")
    elif meta["kind"] != "util":
        lines.append("Datasets: none captured in program_to_dsn.")
    if jcls:
        lines.append(f"Executed by JCL: {', '.join(jcls)}.")
    if callees:
        lines.append(f"Calls / transfers to: {', '.join(callees)}.")
    if callers_of:
        lines.append(f"Called / transferred from: {', '.join(callers_of)}.")
    if sys_calls or asm:
        lines.append(f"System / assembler calls: {', '.join(sys_calls + asm)}.")
    if issues:
        lines.append("Vendor code_issues: " + " | ".join(sorted(set(issues))) + ".")
    if meta.get("notes"):
        lines.append(f"Notes from reading the COBOL: {meta['notes']}")
    lines += [
        "",
        "Acceptance criteria:",
    ]
    if meta["kind"] == "cics":
        lines += [
            "- Every field, edit rule and message text of the BMS map is reproduced in the Java view/REST DTO with Bean Validation.",
            "- PF-key navigation (PF3 back, PF7/PF8 paging where present, ENTER) and COMMAREA hand-offs behave as in the COBOL.",
            "- All file/DB accesses above go through Spring Data repositories on the migrated entities inside one transaction per user action.",
            "- Unit coverage >= 80% on the service layer; scenarios registered in the online parity suite.",
        ]
    elif meta["kind"] == "batch":
        lines += [
            "- Implemented as a Spring Batch job with the same DD-name-to-input/output contract and exit codes (0 ok, 4 warnings, 8/12 abend via CEE3ABD).",
            "- Every dataset above is read/written through the migrated entity or a fixed-width adapter with identical record layout.",
            "- Restartable; reject/report outputs written as versioned objects where the JCL used GDG (+1).",
            "- Wired into the migrated scheduler flow where a Control-M / CA7 entry references the JCL.",
        ]
    else:
        lines += [
            "- Exposed as a Spring bean with the same input/output contract as the COBOL CALL interface.",
            "- Property-based tests cover the full input domain (leap years, invalid formats, boundary dates).",
        ]
    if meta["kind"] == "cics":
        parity = ("Parity test: online parity suite replays the transaction scenarios against the legacy screen recordings "
                  "(or the CICS region) and the Java endpoint; field values, error messages and resulting record images in the "
                  "touched datasets must be identical.")
    elif meta["kind"] == "batch":
        parity = ("Parity test: golden-file harness runs the legacy job and the Spring Batch job on the same EBCDIC fixtures; "
                  "all output datasets/reports and post-run record images of updated datasets must match byte-for-byte after "
                  "code-page normalisation; row counts and control totals are reconciled.")
    else:
        parity = "Parity test: table-driven test with inputs/expected outputs captured from the COBOL routine on the mainframe."
    lines.append(parity)

    labels = list(dict.fromkeys(meta["labels"]))
    if lineage_rows and any("GDG" in r["Data Source Type"] for r in lineage_rows):
        labels.append("gdg")
    points = meta.get("points") or fib_points(eff, cc, meta["kind"])

    # dependencies
    deps = ["Bootstrap Spring Boot 3.5 / Java 21 multi-module project and CI pipeline"]
    if meta["kind"] == "cics":
        deps.append("Build the CICS-replacement abstractions: COMMAREA session model, BMS map to view/REST mapping, pseudo-conversational flow")
    if meta["kind"] == "batch":
        deps.append("Build the Spring Batch job framework replacing JCL step semantics")
    for r in lineage_rows:
        s = data_story_for_dsn.get(r["Data Source Name"])
        if s:
            deps.append(s)
    for n, _ in g.get("CICS_FILE", []):
        s = data_story_for_dsn.get(n)
        if s:
            deps.append(s)
    for n, _ in g.get("[SQL]TABLE", []):
        s = data_story_for_dsn.get(n)
        if s:
            deps.append(s)
    # Navigation: menus/lists hand off to their option/detail screens and those XCTL back.
    # Order the work parent-first: the option/detail screen depends on the screen that
    # dispatches to it, and "return" transfers to a dispatcher are not dependencies.
    for c in callees:
        if c in P and c not in NAV_DISPATCHERS and not (name in NAV_DISPATCHERS and P[c]["kind"] == "cics"):
            deps.append(summary_for_program(c))
    for c in callers_of:
        if c in NAV_DISPATCHERS and c != name and name not in NAV_ROOTS:
            deps.append(summary_for_program(c))
    if "mq" in labels:
        deps.append("Provision MQ/JMS infrastructure and queue definitions for authorization and inquiry services")
    if "ims" in labels and name != "COPAUS1C":
        deps.append(summary_for_dataset("IMSPAUTDB"))
    if asm:
        deps.append("Retire assembler dependencies (COBDATFT.asm, MVSWAIT.asm, CSUTLDTC maclib) with Java equivalents")
    deps = [d for d in dict.fromkeys(deps) if d != summary_for_program(name)]

    epic = EPIC_BY_ID[meta["epic"]]
    return dict(
        issue_type="Story",
        summary=summary_for_program(name),
        description="\n".join(lines),
        priority=priority_for(meta["epic"], points),
        labels=labels,
        epic_name="",
        parent=epic[1],
        points=points,
        components=epic[3],
        depends_on=deps,
        programs=[prog["path"]],
    )


NAV_DISPATCHERS = {"COSGN00C", "COMEN01C", "COADM01C", "COTRN00C", "COPAUS0C", "COCRDLIC", "COUSR00C"}
NAV_ROOTS = {"COSGN00C"}


def summary_for_program(name: str) -> str:
    meta = P[name]
    short = meta["purpose"].split(":")[0].split(" (")[0]
    short = short[0].upper() + short[1:]
    if len(short) > 70:
        short = short[:67].rstrip() + "..."
    return f"{name} - {short}"


def summary_for_dataset(key: str) -> str:
    d = next(x for x in DATASETS if x["key"] == key)
    return f"Data migration: {d['key']} - {d['entity'].split(' (')[0]}"


PRIORITY_BY_EPIC = {
    "E-PLAT": "Highest", "E-DATA": "Highest", "E-AUTH": "High", "E-POST": "High", "E-ACCT": "High",
    "E-CARD": "High", "E-TRAN": "High", "E-USER": "Medium", "E-RPT": "Medium", "E-TTYP": "Medium",
    "E-PAUT": "Medium", "E-MQ": "Low", "E-CUT": "High",
}


def priority_for(epic_id: str, points: int) -> str:
    return PRIORITY_BY_EPIC[epic_id]


def build_data_story(d: dict, a: dict) -> dict:
    users = set()
    for dsn, rows in a["dsn_users"].items():
        if d["dsn"].split(" ")[0] in dsn or (d["key"] == "DB2TRTYP" and "TRANSACTION_TYPE" in dsn) \
                or (d["key"] == "IMSPAUTDB" and ("PAUT" in dsn or "IMSDATA" in dsn)) \
                or (d["key"] == "TRANREPT" and dsn in ("AWS.M2.CARDDEMO.SYSTRAN", "AWS.M2.CARDDEMO.STATEMNT.PS", "AWS.M2.CARDDEMO.STATEMNT.HTML")) \
                or (d["key"] == "DALYTRAN" and dsn == "AWS.M2.CARDDEMO.DALYREJS"):
            users |= rows
    prog_users = sorted({f"{f.split('.')[0]} ({m or 'mode not captured'})" for f, p, m in users if f.lower().endswith((".cbl", ".cob")) and not p})
    jcl_users = sorted({f"{f} step {p} ({m})" for f, p, m in users if f.lower().endswith(".jcl") and p})
    fields = sum(a["dd_fields"].get(c.upper(), 0) for c in d["cpy"])
    lines = [
        f"Migrate {d['dsn']} ({d['kind']}) to the relational target.",
        f"Record layout: {', '.join(d['cpy']) if d['cpy'] else 'inline in consuming program'}; entity: {d['entity']}; {fields} data-dictionary items.",
        f"Program consumers (dsn_to_file): {'; '.join(prog_users) if prog_users else 'none in vendor lineage'}.",
        f"JCL define/load/backup steps: {'; '.join(jcl_users[:14]) if jcl_users else 'none'}.",
        f"Related JCL: {', '.join(d['load_jcl']) if d['load_jcl'] else 'none'}.",
    ]
    if d.get("notes"):
        lines.append(f"Notes: {d['notes']}")
    lines += [
        "",
        "Acceptance criteria:",
        "- JPA entity + Flyway migration generated from the copybook mapping catalogue (types, scale, nullability, key, secondary indexes for every AIX/path).",
        "- Conversion pipeline loads the EBCDIC/DDL source with zero unexplained rejects; row count and control totals (sum of every COMP-3 amount field) match the source.",
        "- Repository API covers every access mode used by the consumers listed above (READ, browse forward/backward, UPDATE, WRITE, DELETE).",
        "- Reverse mapping (entity -> fixed-width record) available for batch outputs and parity tests.",
        "Parity test: unload legacy dataset -> convert -> load -> re-encode; compare byte-for-byte with the original after code-page normalisation, and reconcile per-key checksums.",
    ]
    deps = ["Generate the copybook-to-entity mapping catalogue from data_dictionary_cbl.csv",
            "Build the EBCDIC fixed-width to relational conversion pipeline"]
    if d["key"] in ("CARDXREF",):
        deps += [summary_for_dataset("ACCTDATA"), summary_for_dataset("CUSTDATA"), summary_for_dataset("CARDDATA")]
    if d["key"] == "CARDDATA":
        deps += [summary_for_dataset("ACCTDATA")]
    if d["key"] in ("TRANSACT", "TCATBALF"):
        deps += [summary_for_dataset("CARDXREF"), summary_for_dataset("TRANTYPE"), summary_for_dataset("TRANCATG")]
    if d["key"] == "DISCGRP":
        deps += [summary_for_dataset("TRANTYPE"), summary_for_dataset("TRANCATG")]
    if d["key"] == "TRANCATG":
        deps += [summary_for_dataset("TRANTYPE")]
    if d["key"] == "DB2TRTYP":
        deps += [summary_for_dataset("TRANTYPE"), summary_for_dataset("TRANCATG")]
    if d["key"] == "DB2AUTHFRDS":
        deps += [summary_for_dataset("CARDDATA")]
    if d["key"] == "IMSPAUTDB":
        deps += [summary_for_dataset("ACCTDATA"), summary_for_dataset("CARDDATA")]
    if d["key"] == "EXPORT":
        deps += [summary_for_dataset(k) for k in ("ACCTDATA", "CARDDATA", "CARDXREF", "CUSTDATA", "TRANSACT")]
    if d["key"] == "TRXFL":
        deps += [summary_for_dataset("TRANSACT")]
    if d["key"] == "DALYTRAN":
        deps += [summary_for_dataset("TRANSACT")]
    if d["key"] == "TRANREPT":
        deps += [summary_for_dataset("TRANSACT")]
    labels = ["cobol"] + d["labels"]
    if any("GDG" in u[2] or "BKUP" in u[0] for u in users):
        labels.append("gdg")
    epic = EPIC_BY_ID["E-DATA"]
    return dict(issue_type="Story", summary=summary_for_dataset(d["key"]), description="\n".join(lines),
                priority="Highest", labels=list(dict.fromkeys(labels)), epic_name="", parent=epic[1],
                points=d["points"], components=epic[3], depends_on=list(dict.fromkeys(deps)), programs=[])


# JCL-only job stories (jobs without a COBOL program of their own but with business meaning)
JCL_STORIES = [
    dict(epic="E-POST", jcl="POSTTRAN.jcl", summary="Batch job POSTTRAN - daily posting flow (CBTRN02C)",
         desc="Single-step job STEP15 EXEC PGM=CBTRN02C. Wrap the CBTRN02C job with the DD contract from jcl_to_dsn, DALYREJS (+1) GDG increment, and the CLOSEFIL/OPENFIL bracket from the daily schedule.",
         points=3, labels=["batch", "vsam", "gdg"], deps=["CBTRN02C"]),
    dict(epic="E-POST", jcl="INTCALC.jcl", summary="Batch job INTCALC - monthly interest flow (CBACT04C)",
         desc="Runs CBACT04C with ACCTFILE/XREFFILE/DISCGRP/TCATBALF/TRANSACT DDs, SYSTRAN GDG output. Sits in the MONTHLY-InterestCalculation Control-M flow between CLOSEFIL and COMBTRAN.",
         points=3, labels=["batch", "vsam", "gdg"], deps=["CBACT04C"]),
    dict(epic="E-POST", jcl="COMBTRAN.jcl", summary="Batch job COMBTRAN - merge SYSTRAN interest transactions into TRANSACT",
         desc="SORT merge of TRANSACT.BKUP + SYSTRAN into TRANSACT.COMBINED then IDCAMS REPRO into TRANSACT.VSAM.KSDS. Becomes a Spring Batch merge step or a set-based SQL insert; no COBOL.",
         points=3, labels=["batch", "vsam", "gdg", "sort"], deps=[]),
    dict(epic="E-POST", jcl="TRANBKP.jcl", summary="Batch job TRANBKP - daily TRANSACT backup and cluster rebuild (with TRANIDX)",
         desc="IDCAMS REPRO of TRANSACT to TRANSACT.BKUP GDG, delete/define of the KSDS and AIX (TRANIDX.jcl), reload. DAILY-TransactionBackup flow. In the target this is a DB backup/snapshot policy plus index maintenance; keep the versioned snapshot for parity.",
         points=2, labels=["batch", "vsam", "gdg"], deps=[]),
    dict(epic="E-RPT", jcl="TRANREPT.jcl", summary="Batch job TRANREPT - transaction report flow (SORT + CBTRN03C)",
         desc="Steps: IDCAMS REPRO TRANSACT -> TRANSACT.BKUP, SORT by date range into TRANSACT.DALY GDG, CBTRN03C with DATEPARM (start/end date card) producing TRANREPT GDG. Submitted on demand by CORPT00C via the internal reader and by schedule.",
         points=3, labels=["batch", "vsam", "gdg", "sort"], deps=["CBTRN03C"]),
    dict(epic="E-RPT", jcl="CREASTMT.JCL", summary="Batch job CREASTMT - statement generation flow (SORT + CBSTM03A/CBSTM03B)",
         desc="SORT TRANSACT into TRXFL.SEQ, IDCAMS define/load TRXFL.VSAM.KSDS, CBSTM03A produces STATEMNT.PS and STATEMNT.HTML; TXT2PDF1.JCL converts text statements to PDF (missing PDS AWS.M2.LBD.TXT2PDF.* per vendor). Target: statement job writing text/HTML/PDF via a template engine.",
         points=3, labels=["batch", "vsam"], deps=["CBSTM03A", "CBSTM03B"]),
    dict(epic="E-RPT", jcl="PRTCATBL.jcl", summary="Batch job PRTCATBL - transaction category balance report",
         desc="IDCAMS REPRO TCATBALF to TCATBALF.BKUP GDG then SORT report to TCATBALF.REPT. No COBOL; becomes a SQL report job.",
         points=2, labels=["batch", "vsam", "gdg", "sort"], deps=[]),
    dict(epic="E-DATA", jcl="CBEXPORT.jcl", summary="Batch jobs CBEXPORT / CBIMPORT - export-import bridge flow",
         desc="CBEXPORT.jcl deletes/defines EXPORT.DATA and runs CBEXPORT; CBIMPORT.jcl runs CBIMPORT producing *.IMPORT files and IMPORT.ERRORS (all reported as missing datasets by the vendor because they are created at run time). Used as the bulk data bridge during dual-run.",
         points=2, labels=["batch", "vsam", "data-migration"], deps=["CBEXPORT", "CBIMPORT"]),
    dict(epic="E-ACCT", jcl="READACCT.jcl", summary="Batch jobs READACCT / READCUST / READCARD / READXREF - verification dumps",
         desc="Each runs a reader program (CBACT01C, CBCUS01C, CBACT02C, CBACT03C) against the KSDS and writes sequential dumps (ACCTDATA.PSCOMP/VBPS/ARRYPS reported missing by the vendor: created by the job). Keep as data-verification jobs whose output feeds the parity harness.",
         points=2, labels=["batch", "vsam"], deps=["CBACT01C", "CBCUS01C", "CBACT02C", "CBACT03C"]),
    dict(epic="E-TTYP", jcl="MNTTRDB2.jcl", summary="Batch jobs MNTTRDB2 / TRANEXTR / CREADB21 - transaction type Db2 maintenance flow",
         desc="CREADB21 runs DSNTIAD/DSNTEP4 with control cards DB2CREAT, DB2FREE, DB2TIAD1, DB2LTTYP, DB2LTCAT, DB2TEP41 (schema + load); MNTTRDB2 runs COBTUPDT under IKJEFT01; TRANEXTR unloads via DSNTIAUL to TRANTYPE.PS / TRANCATG.PS with GDG backups. WEEKLY-TransactionTypesDBRefresh flow. Target: Flyway migrations + a reference-data import job.",
         points=3, labels=["batch", "db2", "gdg"], deps=["COBTUPDT"]),
    dict(epic="E-PAUT", jcl="CBPAUP0J.jcl", summary="Batch jobs CBPAUP0J / DBPAUTP0 / LOADPADB / UNLDPADB / UNLDGSAM - IMS PAUTDB maintenance flow",
         desc="DFSRRC00 BMP/DLI regions running CBPAUP0C (purge), PAUDBLOD (load), PAUDBUNL (unload), DBUNLDGS (GSAM unload) and DFSURGU0 image copy (DBPAUTP0). Vendor flags missing control card DFSVSMDB and missing IMS/Db2 system libraries (OEM.IMS.*, OEMA.*). Target: SQL purge job + backup policy; unload/load become the conversion pipeline.",
         points=3, labels=["batch", "ims"], deps=["CBPAUP0C", "PAUDBLOD", "PAUDBUNL", "DBUNLDGS"]),
    dict(epic="E-PLAT", jcl="CLOSEFIL.jcl", summary="Batch jobs CLOSEFIL / OPENFIL / WAITSTEP - CICS file quiesce bracket",
         desc="CEMT SET FILE CLOSED/OPEN via the batch CICS interface and a timed wait (COBSWAIT). Every scheduled flow wraps its business steps with these. Target: online maintenance-mode toggle exposed by the API and awaited by the scheduler.",
         points=2, labels=["batch", "cics"], deps=["COBSWAIT"]),
]


def build_jcl_story(j: dict) -> dict:
    epic = EPIC_BY_ID[j["epic"]]
    desc = j["desc"] + ("\n\nAcceptance criteria:\n- Job graph reproduced in Spring Batch with identical step order, DD contracts and exit codes.\n"
                        "- Scheduler entry migrated where the job appears in Control-M / CA7.\n"
                        "Parity test: golden-file harness compares every output dataset of the legacy job run with the Java job on the same fixtures.")
    return dict(issue_type="Story", summary=j["summary"], description=desc, priority=PRIORITY_BY_EPIC[j["epic"]],
                labels=["jcl"] + j["labels"], epic_name="", parent=epic[1], points=j["points"], components=epic[3],
                depends_on=["Build the Spring Batch job framework replacing JCL step semantics"] + [summary_for_program(p) for p in j["deps"]],
                programs=[])


def build_task(t: dict) -> dict:
    epic = EPIC_BY_ID[t["epic"]]
    deps = []
    if t["summary"] != "Bootstrap Spring Boot 3.5 / Java 21 multi-module project and CI pipeline":
        deps.append("Bootstrap Spring Boot 3.5 / Java 21 multi-module project and CI pipeline")
    if t["epic"] == "E-CUT" and "dual-run" in t["summary"]:
        deps += ["Build the golden-file batch parity harness", "Build the online screen/API parity suite",
                 "Migrate the Control-M / CA7 schedules to the target scheduler"]
    if "conversion pipeline" in t["summary"]:
        deps.append("Generate the copybook-to-entity mapping catalogue from data_dictionary_cbl.csv")
    desc = t["desc"].replace(" Acceptance: ", "\n\nAcceptance criteria:\n- ")
    desc += ("\nParity test: enabling task - verified by the program/data stories that depend on it passing their own parity tests; "
             "this task is done when its acceptance criteria are demonstrated in CI.")
    return dict(issue_type="Task", summary=t["summary"], description=desc, priority=PRIORITY_BY_EPIC[t["epic"]],
                labels=t["labels"], epic_name="", parent=epic[1], points=t["points"], components=epic[3],
                depends_on=deps, programs=[])


# --------------------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------------------

def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--analysis-dir", required=True, help="path to aws-transform-output/")
    ap.add_argument("--out-dir", default=os.path.dirname(os.path.abspath(__file__)))
    args = ap.parse_args()
    a = load_analysis(args.analysis_dir)

    nodes = {n["name"].upper(): n for n in a["dependencies"]}
    callers = defaultdict(set)
    for n in a["dependencies"]:
        if n["type"] != "COB":
            continue
        for d in n["dependencies"]:
            if d["type"] == "COB":
                callers[d["name"].upper()].add(stem(n["name"]))

    progs = program_files(a)
    unknown = [stem(p["name"]) for p in progs if stem(p["name"]) not in P]
    if unknown:
        raise SystemExit(f"programs in classification without catalogue entry: {unknown}")

    data_story_for_dsn = {}
    for d in DATASETS:
        s = summary_for_dataset(d["key"])
        for dsn in a["dsn_users"]:
            base = d["dsn"].split(" ")[0]
            if dsn.startswith(base) and "IMPORT" not in dsn:
                data_story_for_dsn[dsn] = s
        if d["key"] == "DB2TRTYP":
            data_story_for_dsn["CARDDEMO.TRANSACTION_TYPE"] = s
            data_story_for_dsn["CARDDEMO.TRANSACTION_TYPE_CATEGORY"] = s
        if d["key"] == "DB2AUTHFRDS":
            data_story_for_dsn["CARDDEMO.AUTHFRDS"] = s
        if d["key"] == "IMSPAUTDB":
            for dsn in ("OEM.IMS.IMSP.PAUTHDB", "OEM.IMS.IMSP.PAUTHDBX", "AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO",
                        "AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO", "AWS.M2.CARDDEMO.PAUTDB.ROOT.GSAM", "AWS.M2.CARDDEMO.PAUTDB.CHILD.GSAM"):
                data_story_for_dsn[dsn] = s
        if d["key"] == "DALYTRAN":
            data_story_for_dsn["AWS.M2.CARDDEMO.DALYREJS"] = s
        if d["key"] == "TRANREPT":
            for dsn in ("AWS.M2.CARDDEMO.SYSTRAN", "AWS.M2.CARDDEMO.STATEMNT.PS", "AWS.M2.CARDDEMO.STATEMNT.HTML", "AWS.M2.CARDDEMO.TRANREPT"):
                data_story_for_dsn[dsn] = s
    # CICS file names -> dataset stories
    data_story_for_dsn.update({
        "ACCTDAT": summary_for_dataset("ACCTDATA"), "CUSTDAT": summary_for_dataset("CUSTDATA"),
        "CARDDAT": summary_for_dataset("CARDDATA"), "CARDAIX": summary_for_dataset("CARDDATA"),
        "CCXREF": summary_for_dataset("CARDXREF"), "CXACAIX": summary_for_dataset("CARDXREF"),
        "TRANSACT": summary_for_dataset("TRANSACT"), "USRSEC": summary_for_dataset("USRSEC"),
    })

    items = []
    for eid, name, desc, comp in EPICS:
        items.append(dict(issue_type="Epic", summary=name, description=desc, priority=PRIORITY_BY_EPIC[eid],
                          labels=["modernization", comp.lower()], epic_name=name, parent="", points="", components=comp,
                          depends_on=[], programs=[]))
    for t in TASKS:
        items.append(build_task(t))
    for d in DATASETS:
        items.append(build_data_story(d, a))
    order = {k: i for i, k in enumerate(P)}
    for prog in sorted(progs, key=lambda p: order[stem(p["name"])]):
        items.append(build_program_story(prog, a, nodes, callers, data_story_for_dsn))
    for j in JCL_STORIES:
        items.append(build_jcl_story(j))

    # sanity: every depends_on resolves
    summaries = {i["summary"] for i in items}
    for i in items:
        for dep in i["depends_on"]:
            if dep not in summaries:
                raise SystemExit(f"unresolved dependency '{dep}' on '{i['summary']}'")

    csv_path = os.path.join(args.out_dir, "carddemo-modernization-backlog.csv")
    json_path = os.path.join(args.out_dir, "carddemo-modernization-backlog.json")
    with open(csv_path, "w", newline="") as f:
        w = csv.writer(f, quoting=csv.QUOTE_ALL, lineterminator="\n")
        w.writerow(CSV_COLUMNS)
        for i in items:
            w.writerow([i["issue_type"], i["summary"], i["description"], i["priority"], " ".join(i["labels"]),
                        i["epic_name"], i["parent"], i["points"], i["components"]])
    with open(json_path, "w") as f:
        json.dump([
            {"Issue Type": i["issue_type"], "Summary": i["summary"], "Description": i["description"],
             "Priority": i["priority"], "Labels": i["labels"], "Epic Name": i["epic_name"], "Parent": i["parent"],
             "Story Points": i["points"], "Components": i["components"], "depends_on": i["depends_on"],
             "programs": i["programs"]}
            for i in items], f, indent=2)
        f.write("\n")
    counts = defaultdict(int)
    for i in items:
        counts[i["issue_type"]] += 1
    print(dict(counts), "programs:", len(progs), "->", csv_path)


if __name__ == "__main__":
    main()
