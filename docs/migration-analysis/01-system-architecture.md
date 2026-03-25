# CardDemo System Architecture Document

## 1. Executive Summary

CardDemo is a mainframe-based credit card management application built with COBOL/CICS/VSAM technologies. It simulates core credit card operations including account management, card management, transaction processing, bill payments, and reporting. The application is designed around a two-tier architecture: CICS online transaction processing for real-time user interactions via 3270 terminal screens, and JCL batch processing for scheduled back-office operations.

This document describes the current-state architecture and proposes a target Java/Spring Boot architecture for migration.

---

## 2. Current-State Architecture (COBOL/CICS/VSAM)

### 2.1 Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Presentation | BMS Maps (3270 Terminal) | Screen definitions for terminal UI |
| Transaction Processing | CICS | Online transaction monitor |
| Business Logic | COBOL | Application programs (online + batch) |
| Data Storage | VSAM KSDS | Keyed Sequential Data Sets (primary storage) |
| Batch Processing | JCL | Job Control Language for scheduled jobs |
| Job Scheduling | CA7 / Control-M | Batch job scheduling |
| Optional Integration | DB2, IMS DB, MQ | Database, hierarchical DB, messaging |

### 2.2 High-Level Architecture Diagram

```
+------------------------------------------------------------------+
|                        3270 Terminal Users                         |
+------------------------------------------------------------------+
                              |
                    +---------+---------+
                    |     CICS Region   |
                    |  (Transaction     |
                    |   Processing)     |
                    +---------+---------+
                              |
          +-------------------+-------------------+
          |                   |                   |
  +-------+-------+  +-------+-------+  +-------+-------+
  |   Online      |  |   Admin       |  |   Batch       |
  |   Programs    |  |   Programs    |  |   Programs    |
  |   (CO*)       |  |   (CO*)       |  |   (CB*)       |
  +-------+-------+  +-------+-------+  +-------+-------+
          |                   |                   |
          +-------------------+-------------------+
                              |
                    +---------+---------+
                    |   VSAM KSDS       |
                    |   Data Files      |
                    +-------------------+
                    | USRSEC  | ACCTDAT |
                    | CARDDAT | CUSTDAT |
                    | TRANSACT| CCXREF  |
                    | TCATBAL | CXACAIX |
                    +-------------------+
```

### 2.3 Application Modules

The application is organized into three primary modules:

#### 2.3.1 Authentication & Navigation
- **COSGN00C** - Sign-on screen (transaction CC00) - Entry point
- **COMEN01C** - Main menu for regular users (transaction CM00)
- **COADM01C** - Admin menu for admin users (transaction CA00)

#### 2.3.2 Online Transaction Processing (User Functions)
- **Account Management**: COACTVWC (view), COACTUPC (update)
- **Card Management**: COCRDLIC (list), COCRDSLC (view), COCRDUPC (update)
- **Transaction Management**: COTRN00C (list), COTRN01C (view), COTRN02C (add)
- **Reporting**: CORPT00C (transaction reports)
- **Bill Payment**: COBIL00C (pay balance)

#### 2.3.3 Admin Functions
- **User Management**: COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete)

#### 2.3.4 Batch Processing
- **CBTRN02C** - Transaction posting (daily transaction file to master)
- **CBACT04C** - Interest calculation
- **CBSTM03A/B** - Statement generation
- **CBTRN03C** - Transaction reporting
- **CBEXPORT/CBIMPORT** - Data export/import utilities

#### 2.3.5 Optional Modules
- **Authorization (IMS DB + DB2 + MQ)**: COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C
- **Transaction Type Management (DB2)**: COTRTLIC, COTRTUPC, COBTUPDT
- **VSAM-MQ Integration**: CODATE01, COACCT01

### 2.4 Inter-Program Communication

Programs communicate via:
1. **COMMAREA** (Communication Area) - Shared data structure (COCOM01Y copybook) passed between programs via CICS XCTL
2. **XCTL** (Transfer Control) - Program-to-program navigation
3. **CICS RETURN with TRANSID** - Pseudo-conversational return to same program
4. **TDQ** (Transient Data Queue) - Used by CORPT00C to submit batch jobs from online

### 2.5 Data Flow

```
User Input (3270) --> CICS RECEIVE MAP --> COBOL Program Logic
    --> CICS READ/WRITE/REWRITE/DELETE (VSAM) --> CICS SEND MAP --> Screen Output

Batch Input (Sequential Files) --> COBOL Batch Program --> VSAM I/O --> Reports/Output Files
```

---

## 3. Target-State Architecture (Java/Spring Boot)

### 3.1 Proposed Technology Stack

| Layer | Technology | Replaces |
|-------|-----------|----------|
| Presentation | React/Angular SPA or Thymeleaf | BMS Maps / 3270 Screens |
| API Layer | Spring Boot REST Controllers | CICS Transaction Processing |
| Business Logic | Spring Services (Java) | COBOL Programs |
| Data Access | Spring Data JPA / Hibernate | CICS File Control (VSAM) |
| Data Storage | PostgreSQL / MySQL | VSAM KSDS Files |
| Batch Processing | Spring Batch | JCL + COBOL Batch Programs |
| Job Scheduling | Spring Scheduler / Quartz | CA7 / Control-M |
| Messaging | Spring JMS / RabbitMQ / Kafka | MQ Series (optional module) |
| Security | Spring Security | CICS Sign-on (USRSEC file) |
| Build/Deploy | Maven/Gradle, Docker, Kubernetes | Mainframe compile/link |

### 3.2 Target Architecture Diagram

```
+------------------------------------------------------------------+
|                    Web Browser / Mobile App                        |
+------------------------------------------------------------------+
                              |
                    +---------+---------+
                    |   API Gateway /   |
                    |   Load Balancer   |
                    +---------+---------+
                              |
          +-------------------+-------------------+
          |                                       |
  +-------+-------+                     +---------+---------+
  | Spring Boot   |                     |  Spring Batch     |
  | REST API      |                     |  Jobs             |
  | (Online)      |                     |  (Batch)          |
  +-------+-------+                     +---------+---------+
          |                                       |
  +-------+-------+                     +---------+---------+
  | Spring        |                     | Spring Batch      |
  | Services      |                     | Step/Tasklet      |
  | (Business     |                     | Processors        |
  |  Logic)       |                     |                   |
  +-------+-------+                     +---------+---------+
          |                                       |
          +-------------------+-------------------+
                              |
                    +---------+---------+
                    | Spring Data JPA   |
                    | (Repository Layer)|
                    +---------+---------+
                              |
                    +---------+---------+
                    |   PostgreSQL /    |
                    |   MySQL DB       |
                    +-------------------+
                    | users      | accounts  |
                    | cards      | customers |
                    | transactions | card_xref|
                    | tran_cat_balance       |
                    +-------------------+
```

### 3.3 Proposed Java Module Structure

```
carddemo-java/
+-- carddemo-api/                     # REST API module
|   +-- controllers/
|   |   +-- AuthController.java       # Login/logout (replaces COSGN00C)
|   |   +-- AccountController.java    # Account CRUD (replaces COACTVWC, COACTUPC)
|   |   +-- CardController.java       # Card CRUD (replaces COCRDLIC, COCRDSLC, COCRDUPC)
|   |   +-- TransactionController.java # Transaction CRUD (replaces COTRN00C-02C)
|   |   +-- ReportController.java     # Reports (replaces CORPT00C)
|   |   +-- BillPaymentController.java # Bill payment (replaces COBIL00C)
|   |   +-- UserController.java       # Admin user CRUD (replaces COUSR00C-03C)
|   +-- dto/                          # Request/Response DTOs
|   +-- config/                       # Security, CORS, etc.
|
+-- carddemo-service/                 # Business logic module
|   +-- service/
|   |   +-- AuthenticationService.java
|   |   +-- AccountService.java
|   |   +-- CardService.java
|   |   +-- TransactionService.java
|   |   +-- ReportService.java
|   |   +-- BillPaymentService.java
|   |   +-- UserManagementService.java
|   +-- validation/                   # Input validation (extracted from COBOL programs)
|
+-- carddemo-domain/                  # Domain model module
|   +-- entity/
|   |   +-- Account.java             # Maps to CVACT01Y (ACCTDAT)
|   |   +-- Card.java                # Maps to CVACT02Y (CARDDAT)
|   |   +-- Customer.java            # Maps to CVCUS01Y (CUSTDAT)
|   |   +-- Transaction.java         # Maps to CVTRA05Y (TRANSACT)
|   |   +-- CardXref.java            # Maps to CVACT03Y (CCXREF)
|   |   +-- User.java                # Maps to CSUSR01Y (USRSEC)
|   |   +-- TransactionCategoryBalance.java  # Maps to CVTRA01Y (TCATBAL)
|   +-- repository/                   # Spring Data JPA repositories
|
+-- carddemo-batch/                   # Batch processing module
|   +-- jobs/
|   |   +-- TransactionPostingJob.java     # Replaces CBTRN02C
|   |   +-- InterestCalculationJob.java    # Replaces CBACT04C
|   |   +-- StatementGenerationJob.java    # Replaces CBSTM03A/B
|   |   +-- TransactionReportJob.java      # Replaces CBTRN03C
|   |   +-- DataExportJob.java             # Replaces CBEXPORT
|   |   +-- DataImportJob.java             # Replaces CBIMPORT
|   +-- processors/
|   +-- readers/
|   +-- writers/
|
+-- carddemo-common/                  # Shared utilities
    +-- util/
    |   +-- DateUtils.java            # Replaces CSUTLDTC/CSUTLDWY
    +-- constants/
    +-- exception/
```

### 3.4 Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| Monolith-first approach | CardDemo is a tightly coupled application; starting with a modular monolith allows for incremental decomposition to microservices later |
| Spring Boot framework | Industry standard for Java enterprise apps; comprehensive ecosystem for REST, data access, batch, security |
| PostgreSQL database | Open-source, ACID-compliant, strong support for the relational model needed by VSAM-to-RDBMS migration |
| Spring Batch for batch jobs | Direct replacement for JCL batch processing with job/step/reader/processor/writer pattern |
| Spring Security for auth | Replaces CICS sign-on and USRSEC file-based authentication with standards-based security |
| REST API design | Replaces BMS/3270 terminal UI with modern API-first approach, enabling web/mobile frontends |

---

## 4. CICS-to-Spring Mapping

| CICS Concept | Spring Equivalent |
|-------------|-------------------|
| Transaction (e.g., CC00, CM00) | REST endpoint / Controller method |
| SEND MAP / RECEIVE MAP | HTTP Response / Request (JSON) |
| COMMAREA | Session state / Request context / DTO |
| XCTL (Transfer Control) | Service method call / Controller redirect |
| CICS RETURN with TRANSID | Stateless REST call (no pseudo-conversational needed) |
| CICS READ/WRITE/REWRITE/DELETE | JPA Repository methods (findById, save, delete) |
| CICS STARTBR/READNEXT/READPREV/ENDBR | JPA Pagination (Pageable, Page<T>) |
| HANDLE ABEND | @ControllerAdvice / @ExceptionHandler |
| TDQ (Transient Data Queue) | Message queue (RabbitMQ/Kafka) or async job trigger |
| CICS ASKTIME/FORMATTIME | java.time.LocalDateTime / DateTimeFormatter |

---

## 5. Security Architecture Comparison

### Current (COBOL/CICS)
- User credentials stored in USRSEC VSAM file (plaintext passwords)
- Two user types: Admin ('A') and Regular User ('U')
- COSGN00C validates credentials, routes to appropriate menu
- No session timeout, no password complexity rules visible

### Target (Java/Spring Security)
- User credentials in database with BCrypt password hashing
- Role-based access control (ROLE_ADMIN, ROLE_USER)
- JWT token-based authentication for stateless REST API
- Session management with configurable timeout
- Password complexity rules, account lockout policies
- CSRF protection, CORS configuration

---

## 6. Batch Processing Architecture Comparison

### Current (JCL/COBOL)
```
Batch Cycle Order:
CLOSEFIL -> Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
         -> POSTTRAN (Transaction Posting)
         -> INTCALC (Interest Calculation)
         -> TRANBKP (Transaction Backup)
         -> COMBTRAN (Combine Transactions)
         -> CREASTMT (Statement Generation)
         -> TRANIDX (Rebuild Alternate Indexes)
         -> OPENFIL
```

### Target (Spring Batch)
```
Scheduled Job Flow:
1. TransactionPostingJob (reads daily transactions, validates, posts to master)
   - ItemReader: File or queue reader for daily transactions
   - ItemProcessor: Validation (xref lookup, account lookup, balance check)
   - ItemWriter: Database writer + rejection handler
2. InterestCalculationJob (calculate and apply interest)
3. StatementGenerationJob (produce customer statements)
4. TransactionReportJob (generate reports by date range)
5. DataExportJob / DataImportJob (system integration)
```

No need for CLOSEFIL/OPENFIL equivalents - database handles concurrent access natively.

---

## 7. Non-Functional Considerations

| Aspect | Current (Mainframe) | Target (Java) |
|--------|-------------------|---------------|
| Availability | CICS region availability | Kubernetes pods with auto-restart |
| Scalability | Vertical (LPAR/CPU) | Horizontal (pod replicas) |
| Performance | Optimized for batch throughput | Connection pooling, caching, async processing |
| Monitoring | CICS monitoring, SMF records | Spring Actuator, Prometheus, Grafana |
| Logging | CICS log, DISPLAY statements | SLF4J/Logback, structured logging, ELK stack |
| Deployment | Mainframe compile/link/deploy | CI/CD pipeline, Docker containers |
