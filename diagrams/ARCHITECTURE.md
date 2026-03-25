# CardDemo Application - Mermaid Architecture Diagrams

This document provides visual architecture diagrams for the CardDemo mainframe application using Mermaid syntax. These diagrams are intended to support modernization planning by making the system's structure, data flows, and dependencies explicit.

---

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "3270 Terminal Layer"
        T[3270 Terminal Users]
    end

    subgraph "CICS Online Region"
        direction TB
        CC00[COSGN00C<br/>Signon - CC00]
        CM00[COMEN01C<br/>Main Menu - CM00]
        CA00[COADM01C<br/>Admin Menu - CA00]

        subgraph "User Functions"
            CAVW[COACTVWC<br/>Account View]
            CAUP[COACTUPC<br/>Account Update]
            CCLI[COCRDLIC<br/>Card List]
            CCDL[COCRDSLC<br/>Card View]
            CCUP[COCRDUPC<br/>Card Update]
            CT00[COTRN00C<br/>Transaction List]
            CT01[COTRN01C<br/>Transaction View]
            CT02[COTRN02C<br/>Transaction Add]
            CR00[CORPT00C<br/>Reports]
            CB00[COBIL00C<br/>Bill Payment]
        end

        subgraph "Admin Functions"
            CU00[COUSR00C<br/>User List]
            CU01[COUSR01C<br/>User Add]
            CU02[COUSR02C<br/>User Update]
            CU03[COUSR03C<br/>User Delete]
        end

        subgraph "Shared Utilities"
            CSUTL[CSUTLDTC<br/>Date Utility]
        end
    end

    subgraph "Batch Region (JCL)"
        CBTRN02[CBTRN02C<br/>Transaction Posting]
        CBACT04[CBACT04C<br/>Interest Calc]
        CBSTM03[CBSTM03A/B<br/>Statement Gen]
        CBTRN03[CBTRN03C<br/>Transaction Report]
        CBTRN01[CBTRN01C<br/>Daily Tran Process]
        CBEXP[CBEXPORT<br/>Data Export]
        CBIMP[CBIMPORT<br/>Data Import]
    end

    subgraph "VSAM Data Stores"
        USRSEC[(USRSEC<br/>User Security)]
        ACCTDAT[(ACCTDAT<br/>Account Master)]
        CARDDAT[(CARDDAT<br/>Card Master)]
        CUSTDAT[(CUSTDAT<br/>Customer Master)]
        CARDXREF[(CARDXREF<br/>Card Cross-Ref)]
        TRANSACT[(TRANSACT<br/>Transactions)]
        DALYTRAN[(DALYTRAN<br/>Daily Trans)]
        TCATBALF[(TCATBALF<br/>Category Balance)]
        DISCGRP[(DISCGRP<br/>Disclosure Groups)]
        TRANCATG[(TRANCATG<br/>Tran Categories)]
        TRANTYPE[(TRANTYPE<br/>Tran Types)]
    end

    T --> CC00
    CC00 --> CM00
    CC00 --> CA00
    CM00 --> CAVW & CAUP & CCLI & CT00 & CT01 & CT02 & CR00 & CB00
    CCLI --> CCDL & CCUP
    CA00 --> CU00 & CU01 & CU02 & CU03

    CC00 -.-> USRSEC
    CAVW -.-> ACCTDAT & CARDXREF & CUSTDAT
    CAUP -.-> ACCTDAT & CARDXREF & CUSTDAT
    CCLI -.-> CARDDAT
    CCDL -.-> CARDDAT & CUSTDAT
    CCUP -.-> CARDDAT & CUSTDAT
    CT00 -.-> TRANSACT
    CT01 -.-> TRANSACT
    CT02 -.-> TRANSACT & ACCTDAT & CARDXREF
    CB00 -.-> ACCTDAT & CARDXREF & TRANSACT
    CU00 -.-> USRSEC
    CU01 -.-> USRSEC
    CU02 -.-> USRSEC
    CU03 -.-> USRSEC

    CBTRN02 -.-> DALYTRAN & TRANSACT & CARDXREF & ACCTDAT & TCATBALF
    CBACT04 -.-> TRANSACT & CARDXREF & ACCTDAT
    CBSTM03 -.-> TRANSACT & CARDXREF & CUSTDAT & ACCTDAT
    CBTRN03 -.-> TRANSACT & CARDXREF & TRANTYPE & TRANCATG

    style CC00 fill:#f9f,stroke:#333
    style CM00 fill:#bbf,stroke:#333
    style CA00 fill:#fbb,stroke:#333
```

---

## 2. CICS Transaction Navigation Flow

```mermaid
stateDiagram-v2
    [*] --> CC00_Signon

    CC00_Signon --> CM00_MainMenu : Regular User Login
    CC00_Signon --> CA00_AdminMenu : Admin User Login

    state "User Functions" as UserFuncs {
        CM00_MainMenu --> CAVW_AccountView : Option 1
        CM00_MainMenu --> CAUP_AccountUpdate : Option 2
        CM00_MainMenu --> CCLI_CardList : Option 3
        CM00_MainMenu --> CCDL_CardView : Option 4
        CM00_MainMenu --> CCUP_CardUpdate : Option 5
        CM00_MainMenu --> CT00_TransactionList : Option 6
        CM00_MainMenu --> CT01_TransactionView : Option 7
        CM00_MainMenu --> CT02_TransactionAdd : Option 8
        CM00_MainMenu --> CR00_Reports : Option 9
        CM00_MainMenu --> CB00_BillPayment : Option 10

        CCLI_CardList --> CCDL_CardView : Select Card
        CCLI_CardList --> CCUP_CardUpdate : Update Card
    }

    state "Admin Functions" as AdminFuncs {
        CA00_AdminMenu --> CU00_UserList : Option 1
        CA00_AdminMenu --> CU01_UserAdd : Option 2
        CA00_AdminMenu --> CU02_UserUpdate : Option 3
        CA00_AdminMenu --> CU03_UserDelete : Option 4

        CU00_UserList --> CU02_UserUpdate : Select User
        CU00_UserList --> CU03_UserDelete : Delete User
    }

    CAVW_AccountView --> CM00_MainMenu : PF3 Back
    CAUP_AccountUpdate --> CM00_MainMenu : PF3 Back
    CCLI_CardList --> CM00_MainMenu : PF3 Back
    CT00_TransactionList --> CM00_MainMenu : PF3 Back
    CR00_Reports --> CM00_MainMenu : PF3 Back
    CB00_BillPayment --> CM00_MainMenu : PF3 Back
    CU00_UserList --> CA00_AdminMenu : PF3 Back

    CM00_MainMenu --> CC00_Signon : PF3 Sign Off
    CA00_AdminMenu --> CC00_Signon : PF3 Sign Off
```

---

## 3. Batch Processing Pipeline

```mermaid
flowchart LR
    subgraph "Phase 1: File Closure"
        CLOSE[CLOSEFIL<br/>Close CICS Files]
    end

    subgraph "Phase 2: Data Refresh"
        ACCT[ACCTFILE<br/>Refresh Accounts]
        CARD[CARDFILE<br/>Refresh Cards]
        CUST[CUSTFILE<br/>Refresh Customers]
        XREF[XREFFILE<br/>Load Cross-Ref]
        USRSEC[DUSRSECJ<br/>Load User Security]
        TRANBKP1[TRANBKP<br/>Backup Transactions]
        TRANCATG[TRANCATG<br/>Load Tran Categories]
        TRANTYPE[TRANTYPE<br/>Load Tran Types]
        DISCGRP[DISCGRP<br/>Load Disclosure Groups]
        TCATBALF[TCATBALF<br/>Load Category Balance]
    end

    subgraph "Phase 3: Core Processing"
        POST[POSTTRAN<br/>CBTRN02C<br/>Transaction Posting]
        INT[INTCALC<br/>CBACT04C<br/>Interest Calculation]
    end

    subgraph "Phase 4: Consolidation"
        BKP2[TRANBKP<br/>Backup Transactions]
        COMB[COMBTRAN<br/>Combine Transactions]
    end

    subgraph "Phase 5: Reporting"
        STMT[CREASTMT<br/>CBSTM03A/B<br/>Generate Statements]
    end

    subgraph "Phase 6: Reopen"
        IDX[TRANIDX<br/>Define Alt Index]
        OPEN[OPENFIL<br/>Open CICS Files]
    end

    CLOSE --> ACCT & CARD & CUST & XREF & USRSEC
    ACCT & CARD & CUST & XREF --> TRANBKP1
    TRANBKP1 --> TRANCATG & TRANTYPE & DISCGRP & TCATBALF
    TRANCATG & TRANTYPE & DISCGRP & TCATBALF --> POST
    POST --> INT
    INT --> BKP2
    BKP2 --> COMB
    COMB --> STMT
    STMT --> IDX
    IDX --> OPEN

    style POST fill:#f96,stroke:#333,stroke-width:2px
    style INT fill:#f96,stroke:#333,stroke-width:2px
    style STMT fill:#f96,stroke:#333,stroke-width:2px
```

---

## 4. Data Model (VSAM Files and Copybook Record Layouts)

```mermaid
erDiagram
    USRSEC {
        PIC_X_8 SEC_USR_ID PK "User ID"
        PIC_X_1 SEC_USR_TYPE "U=User A=Admin"
        PIC_X_20 SEC_USR_FNAME "First Name"
        PIC_X_20 SEC_USR_LNAME "Last Name"
        PIC_X_8 SEC_USR_PWD "Password"
    }

    ACCTDAT {
        PIC_9_11 ACCT_ID PK "Account Number"
        PIC_9_1 ACCT_ACTIVE_STATUS "Active Status"
        PIC_X_26 ACCT_CURR_BAL "Current Balance"
        PIC_X_15 ACCT_CREDIT_LIMIT "Credit Limit"
        PIC_X_15 ACCT_CASH_CREDIT_LIMIT "Cash Credit Limit"
        PIC_X_10 ACCT_OPEN_DATE "Open Date"
        PIC_X_10 ACCT_EXPIRAION_DATE "Expiration Date"
        PIC_9_11 ACCT_CURR_CYC_CREDIT "Current Cycle Credit"
        PIC_9_11 ACCT_CURR_CYC_DEBIT "Current Cycle Debit"
        PIC_X_25 ACCT_GROUP_ID "Group ID"
    }

    CARDDAT {
        PIC_X_16 CARD_NUM PK "Card Number"
        PIC_9_11 CARD_ACCT_ID FK "Account ID"
        PIC_9_1 CARD_ACTIVE_STATUS "Active Status"
    }

    CUSTDAT {
        PIC_9_9 CUST_ID PK "Customer ID"
        PIC_X_25 CUST_FIRST_NAME "First Name"
        PIC_X_25 CUST_MIDDLE_NAME "Middle Name"
        PIC_X_25 CUST_LAST_NAME "Last Name"
        PIC_X_50 CUST_ADDR_LINE_1 "Address Line 1"
        PIC_X_50 CUST_ADDR_LINE_2 "Address Line 2"
        PIC_X_50 CUST_ADDR_LINE_3 "Address Line 3"
        PIC_X_10 CUST_ADDR_ZIP "Zip Code"
        PIC_X_15 CUST_PHONE_NUM_1 "Phone Number 1"
        PIC_X_15 CUST_PHONE_NUM_2 "Phone Number 2"
        PIC_9_1 CUST_EFT_ACCOUNT_ID "EFT Account"
        PIC_9_9 CUST_SSN "SSN"
        PIC_X_25 CUST_GOVT_ISSUED_ID "Government ID"
        PIC_X_10 CUST_DOB_YYYYMMDD "Date of Birth"
        PIC_9_1 CUST_FICO_CREDIT_SCORE "FICO Score"
    }

    CARDXREF {
        PIC_X_16 XREF_CARD_NUM PK "Card Number"
        PIC_9_11 XREF_ACCT_ID FK "Account ID"
        PIC_9_9 XREF_CUST_ID FK "Customer ID"
    }

    TRANSACT {
        PIC_X_16 TRAN_ID PK "Transaction ID"
        PIC_X_16 TRAN_CARD_NUM FK "Card Number"
        PIC_X_2 TRAN_TYPE_CD "Transaction Type"
        PIC_X_2 TRAN_CAT_CD "Category Code"
        PIC_X_10 TRAN_SOURCE "Source"
        PIC_X_100 TRAN_DESC "Description"
        PIC_S9_11V99 TRAN_AMT "Amount"
        PIC_X_8 TRAN_ORIG_TS "Timestamp"
        PIC_X_8 TRAN_PROC_TS "Processing Timestamp"
    }

    DALYTRAN {
        PIC_X_16 DALYTRAN_ID PK "Daily Transaction ID"
        PIC_X_16 DALYTRAN_CARD_NUM FK "Card Number"
        PIC_X_2 DALYTRAN_TYPE_CD "Type Code"
        PIC_S9_11V99 DALYTRAN_AMT "Amount"
    }

    TCATBALF {
        PIC_X_2 TCAT_TYPE_CD PK "Category Type Code"
        PIC_S9_11V99 TCAT_BAL "Balance"
    }

    TRANCATG {
        PIC_X_2 TRAN_CAT_CD PK "Category Code"
        PIC_X_50 TRAN_CAT_DESC "Category Description"
    }

    TRANTYPE {
        PIC_X_2 TRAN_TYPE_CD PK "Type Code"
        PIC_X_50 TRAN_TYPE_DESC "Type Description"
    }

    DISCGRP {
        PIC_X_10 DIS_GROUP_ID PK "Disclosure Group ID"
        PIC_X_40 DIS_GROUP_NAME "Group Name"
    }

    ACCTDAT ||--o{ CARDDAT : "has cards"
    CARDDAT ||--|| CARDXREF : "cross-referenced"
    CUSTDAT ||--o{ CARDXREF : "owns cards"
    ACCTDAT ||--|| CARDXREF : "linked"
    CARDDAT ||--o{ TRANSACT : "transactions"
    CARDDAT ||--o{ DALYTRAN : "daily transactions"
    TRANTYPE ||--o{ TRANSACT : "categorizes"
    TRANCATG ||--o{ TRANSACT : "classifies"
    USRSEC }|--|| ACCTDAT : "manages"
```

---

## 5. Program Call Graph and Inter-Program Dependencies

```mermaid
flowchart TB
    subgraph "Entry Points"
        COSGN00C["COSGN00C<br/>(Signon)"]
    end

    subgraph "Menu Dispatchers"
        COMEN01C["COMEN01C<br/>(Main Menu)"]
        COADM01C["COADM01C<br/>(Admin Menu)"]
    end

    subgraph "Online Programs - Account Domain"
        COACTVWC["COACTVWC<br/>(Account View)"]
        COACTUPC["COACTUPC<br/>(Account Update)"]
    end

    subgraph "Online Programs - Card Domain"
        COCRDLIC["COCRDLIC<br/>(Card List)"]
        COCRDSLC["COCRDSLC<br/>(Card View)"]
        COCRDUPC["COCRDUPC<br/>(Card Update)"]
    end

    subgraph "Online Programs - Transaction Domain"
        COTRN00C["COTRN00C<br/>(Transaction List)"]
        COTRN01C["COTRN01C<br/>(Transaction View)"]
        COTRN02C["COTRN02C<br/>(Transaction Add)"]
        CORPT00C["CORPT00C<br/>(Reports)"]
        COBIL00C["COBIL00C<br/>(Bill Payment)"]
    end

    subgraph "Online Programs - User Admin"
        COUSR00C["COUSR00C<br/>(User List)"]
        COUSR01C["COUSR01C<br/>(User Add)"]
        COUSR02C["COUSR02C<br/>(User Update)"]
        COUSR03C["COUSR03C<br/>(User Delete)"]
    end

    subgraph "Shared Utilities"
        CSUTLDTC["CSUTLDTC<br/>(Date Utility)"]
        COBDATFT["COBDATFT<br/>(Date Format - ASM)"]
        CEEDAYS["CEEDAYS<br/>(LE Date Routine)"]
    end

    subgraph "Batch Programs"
        CBTRN02C["CBTRN02C<br/>(Transaction Posting)"]
        CBACT04C["CBACT04C<br/>(Interest Calc)"]
        CBSTM03A["CBSTM03A<br/>(Statement Gen)"]
        CBSTM03B["CBSTM03B<br/>(Statement Detail)"]
        CBTRN01C["CBTRN01C<br/>(Daily Tran Process)"]
        CBTRN03C["CBTRN03C<br/>(Transaction Report)"]
        CBACT01C["CBACT01C<br/>(Account Read)"]
        CBEXPORT["CBEXPORT<br/>(Data Export)"]
        CBIMPORT["CBIMPORT<br/>(Data Import)"]
    end

    COSGN00C -->|XCTL| COMEN01C
    COSGN00C -->|XCTL| COADM01C

    COMEN01C -->|XCTL| COACTVWC
    COMEN01C -->|XCTL| COACTUPC
    COMEN01C -->|XCTL| COCRDLIC
    COMEN01C -->|XCTL| COCRDSLC
    COMEN01C -->|XCTL| COCRDUPC
    COMEN01C -->|XCTL| COTRN00C
    COMEN01C -->|XCTL| COTRN01C
    COMEN01C -->|XCTL| COTRN02C
    COMEN01C -->|XCTL| CORPT00C
    COMEN01C -->|XCTL| COBIL00C

    COADM01C -->|XCTL| COUSR00C
    COADM01C -->|XCTL| COUSR01C
    COADM01C -->|XCTL| COUSR02C
    COADM01C -->|XCTL| COUSR03C

    COCRDLIC -->|XCTL| COCRDSLC
    COCRDLIC -->|XCTL| COCRDUPC

    COTRN02C -->|CALL| CSUTLDTC
    CORPT00C -->|CALL| CSUTLDTC
    CSUTLDTC -->|CALL| CEEDAYS

    CBACT01C -->|CALL| COBDATFT
    CBSTM03A -->|CALL| CBSTM03B

    style COACTUPC fill:#ff6b6b,stroke:#333,stroke-width:3px
    style COCRDLIC fill:#ff9f43,stroke:#333,stroke-width:2px
    style COCRDUPC fill:#ff9f43,stroke:#333,stroke-width:2px
    style CBTRN02C fill:#ff9f43,stroke:#333,stroke-width:2px
```

---

## 6. Copybook Dependency Matrix

```mermaid
flowchart LR
    subgraph "Shared Infrastructure Copybooks"
        COCOM01Y["COCOM01Y<br/>(Common Area)<br/>Used by: 17 programs"]
        COTTL01Y["COTTL01Y<br/>(Title/Header)<br/>Used by: 17 programs"]
        CSDAT01Y["CSDAT01Y<br/>(Date Storage)<br/>Used by: 17 programs"]
        CSMSG01Y["CSMSG01Y<br/>(Messages)<br/>Used by: 17 programs"]
        CSUSR01Y["CSUSR01Y<br/>(User Security)<br/>Used by: 12 programs"]
        CSSETATY["CSSETATY<br/>(Set Attribute)<br/>Used by: 39 copies"]
    end

    subgraph "Data Record Copybooks"
        CVACT01Y["CVACT01Y<br/>(Account Data)<br/>Used by: 11 programs"]
        CVACT02Y["CVACT02Y<br/>(Card Data)<br/>Used by: 8 programs"]
        CVACT03Y["CVACT03Y<br/>(Cross-Ref)<br/>Used by: 12 programs"]
        CVCUS01Y["CVCUS01Y<br/>(Customer Data)<br/>Used by: 8 programs"]
        CVTRA05Y["CVTRA05Y<br/>(Online Tran)<br/>Used by: 11 programs"]
        CVCRD01Y["CVCRD01Y<br/>(Card Detail)<br/>Used by: 5 programs"]
    end

    subgraph "Domain-Specific Copybooks"
        CVTRA06Y["CVTRA06Y<br/>(Daily Tran)<br/>Used by: 2 programs"]
        CVTRA01Y["CVTRA01Y<br/>(Tran Cat Bal)<br/>Used by: 2 programs"]
        CVTRA02Y["CVTRA02Y<br/>(Disclosure)<br/>Used by: 1 program"]
        CVTRA03Y["CVTRA03Y<br/>(Tran Type)<br/>Used by: 1 program"]
        CVTRA04Y["CVTRA04Y<br/>(Tran Category)<br/>Used by: 1 program"]
        CVEXPORT["CVEXPORT<br/>(Export Layout)<br/>Used by: 2 programs"]
        COMEN02Y["COMEN02Y<br/>(Menu Options)<br/>Used by: 1 program"]
        COADM02Y["COADM02Y<br/>(Admin Options)<br/>Used by: 1 program"]
    end

    COCOM01Y -.-> CVACT01Y & CVACT02Y & CVACT03Y
    CVACT03Y -.-> CVCUS01Y
    CVACT03Y -.-> CVACT01Y
    CVTRA05Y -.-> CVACT03Y

    style COCOM01Y fill:#e74c3c,stroke:#333,stroke-width:2px
    style CSSETATY fill:#e74c3c,stroke:#333,stroke-width:2px
    style CVACT03Y fill:#f39c12,stroke:#333,stroke-width:2px
    style CVACT01Y fill:#f39c12,stroke:#333,stroke-width:2px
    style CVTRA05Y fill:#f39c12,stroke:#333,stroke-width:2px
```

---

## 7. Online vs Batch Data Access Heatmap

```mermaid
flowchart TB
    subgraph "VSAM Data Files"
        USRSEC[(USRSEC)]
        ACCTDAT[(ACCTDAT)]
        CARDDAT[(CARDDAT)]
        CUSTDAT[(CUSTDAT)]
        CARDXREF[(CARDXREF)]
        TRANSACT[(TRANSACT)]
        DALYTRAN[(DALYTRAN)]
        TCATBALF[(TCATBALF)]
    end

    subgraph "Online CICS Programs (Read/Write)"
        O1[COSGN00C] -->|R| USRSEC
        O2[COACTVWC] -->|R| ACCTDAT
        O2 -->|R| CARDXREF
        O2 -->|R| CUSTDAT
        O3[COACTUPC] -->|R/W| ACCTDAT
        O3 -->|R| CARDXREF
        O3 -->|R| CUSTDAT
        O4[COCRDLIC] -->|R| CARDDAT
        O5[COCRDSLC] -->|R| CARDDAT
        O5 -->|R| CUSTDAT
        O6[COCRDUPC] -->|R| CARDDAT
        O6 -->|R| CUSTDAT
        O7[COTRN00C] -->|R| TRANSACT
        O8[COTRN01C] -->|R| TRANSACT
        O9[COTRN02C] -->|R/W| TRANSACT
        O9 -->|R| ACCTDAT
        O9 -->|R| CARDXREF
        O10[COBIL00C] -->|R/W| TRANSACT
        O10 -->|R/W| ACCTDAT
        O10 -->|R| CARDXREF
        O11[COUSR00C] -->|R| USRSEC
        O12[COUSR01C] -->|W| USRSEC
        O13[COUSR02C] -->|R/W| USRSEC
        O14[COUSR03C] -->|R/D| USRSEC
    end

    subgraph "Batch Programs (Sequential I/O)"
        B1[CBTRN02C] -->|R/W| DALYTRAN
        B1 -->|R/W| TRANSACT
        B1 -->|R| CARDXREF
        B1 -->|R/W| ACCTDAT
        B1 -->|R/W| TCATBALF
        B2[CBACT04C] -->|R/W| TRANSACT
        B2 -->|R| CARDXREF
        B2 -->|R| ACCTDAT
        B3[CBSTM03A] -->|R| TRANSACT
        B3 -->|R| CARDXREF
        B3 -->|R| CUSTDAT
        B3 -->|R| ACCTDAT
        B4[CBTRN03C] -->|R| TRANSACT
        B4 -->|R| CARDXREF
    end

    style ACCTDAT fill:#e74c3c,stroke:#333,stroke-width:3px
    style CARDXREF fill:#e74c3c,stroke:#333,stroke-width:3px
    style TRANSACT fill:#e74c3c,stroke:#333,stroke-width:3px
    style CUSTDAT fill:#f39c12,stroke:#333,stroke-width:2px
    style CARDDAT fill:#f39c12,stroke:#333,stroke-width:2px
    style USRSEC fill:#3498db,stroke:#333,stroke-width:2px
```

---

## 8. Optional Modules - IMS/DB2/MQ Integration

```mermaid
flowchart TB
    subgraph "MQ Layer"
        MQ_REQ[MQ Request Queue]
        MQ_RESP[MQ Response Queue]
    end

    subgraph "Authorization Module (IMS-DB2-MQ)"
        COPAUA0C["COPAUA0C<br/>(MQ Trigger - CP00)<br/>Process Auth Requests"]
        COPAUS0C["COPAUS0C<br/>(Auth Summary - CPVS)<br/>Read IMS + VSAM"]
        COPAUS1C["COPAUS1C<br/>(Auth Details - CPVD)<br/>Update IMS + Insert DB2"]
        COPAUS2C["COPAUS2C<br/>(Fraud Marking)<br/>Write to DB2"]
        CBPAUP0C["CBPAUP0C<br/>(Batch Purge)<br/>Purge Expired Auths"]
    end

    subgraph "Transaction Type DB2 Module"
        COTRTLIC["COTRTLIC<br/>(Tran Type List - CTLI)<br/>DB2 Cursor + Delete"]
        COTRTUPC["COTRTUPC<br/>(Tran Type Maint - CTTU)<br/>DB2 Insert + Update"]
        COBTUPDT["COBTUPDT<br/>(Batch Update)<br/>DB2 Batch Maintenance"]
    end

    subgraph "VSAM-MQ Module"
        CODATE01["CODATE01<br/>(System Date - CDRD)<br/>MQ Request/Response"]
        COACCT01["COACCT01<br/>(Account Inquiry - CDRA)<br/>MQ Request/Response"]
    end

    subgraph "Data Stores"
        IMS_DB[(IMS DB<br/>Hierarchical)]
        DB2[(DB2<br/>Relational)]
        VSAM[(VSAM<br/>Key-Sequenced)]
    end

    MQ_REQ --> COPAUA0C
    COPAUA0C --> MQ_RESP
    COPAUA0C --> IMS_DB
    COPAUS0C --> IMS_DB & VSAM
    COPAUS1C --> IMS_DB & DB2
    COPAUS2C --> DB2
    CBPAUP0C --> IMS_DB & DB2

    COTRTLIC --> DB2
    COTRTUPC --> DB2
    COBTUPDT --> DB2

    CODATE01 --> MQ_REQ & MQ_RESP
    COACCT01 --> MQ_REQ & MQ_RESP & VSAM

    style DB2 fill:#3498db,stroke:#333
    style IMS_DB fill:#9b59b6,stroke:#333
    style VSAM fill:#2ecc71,stroke:#333
```

---

## 9. Modernization Target Architecture (Java/Spring)

```mermaid
flowchart TB
    subgraph "Frontend Layer"
        WEB[Web Browser / React UI]
    end

    subgraph "API Gateway"
        GW[Spring Cloud Gateway<br/>or API Gateway]
    end

    subgraph "Microservices (Spring Boot)"
        AUTH[Auth Service<br/>COSGN00C -> Spring Security]
        ACCT[Account Service<br/>COACTVWC + COACTUPC]
        CARD[Card Service<br/>COCRDLIC + COCRDSLC + COCRDUPC]
        TRAN[Transaction Service<br/>COTRN00C + COTRN01C + COTRN02C]
        BILL[Payment Service<br/>COBIL00C]
        RPT[Report Service<br/>CORPT00C]
        USR[User Admin Service<br/>COUSR00C-03C]
    end

    subgraph "Batch Layer (Spring Batch)"
        BATCH_POST[Transaction Posting Job<br/>CBTRN02C]
        BATCH_INT[Interest Calc Job<br/>CBACT04C]
        BATCH_STMT[Statement Gen Job<br/>CBSTM03A/B]
        BATCH_RPT[Transaction Report Job<br/>CBTRN03C]
        BATCH_EXP[Export/Import Jobs<br/>CBEXPORT + CBIMPORT]
    end

    subgraph "Data Layer"
        RDB[(PostgreSQL / MySQL<br/>Replacing VSAM)]
        MQ_NEW[RabbitMQ / SQS<br/>Replacing MQ]
    end

    WEB --> GW
    GW --> AUTH & ACCT & CARD & TRAN & BILL & RPT & USR
    ACCT --> RDB
    CARD --> RDB
    TRAN --> RDB
    BILL --> RDB
    USR --> RDB
    RPT --> RDB

    BATCH_POST --> RDB
    BATCH_INT --> RDB
    BATCH_STMT --> RDB
    BATCH_RPT --> RDB
    BATCH_EXP --> RDB

    TRAN -.-> MQ_NEW
    BILL -.-> MQ_NEW

    style AUTH fill:#e74c3c,stroke:#333
    style RDB fill:#3498db,stroke:#333
    style MQ_NEW fill:#9b59b6,stroke:#333
```
