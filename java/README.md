# CardDemo - Java Target State

This is the Java target state for the **CardDemo mainframe modernization** project. It replaces the original COBOL/CICS/VSAM application with a modern Java-based architecture using Spring Boot 3.2, Spring Data JPA, Spring Batch, and Spring Security.

## Module Structure

| Module | Description |
|--------|-------------|
| `carddemo-common` | Shared JPA entities, DTOs, exceptions, and utilities. Maps COBOL copybooks to Java classes. |
| `carddemo-api` | REST API replacing CICS online transactions. Spring Boot web application with controllers, services, and repositories. |
| `carddemo-batch` | Spring Batch jobs replacing JCL/COBOL batch programs. Handles transaction posting, interest calculation, reporting, etc. |
| `carddemo-integration-tests` | Integration tests spanning the API and batch modules. |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (for production) or H2 (for development, included)

## How to Build

```bash
cd java
mvn clean install
```

## How to Run

### API Module (Online CICS Replacement)

```bash
cd carddemo-api
mvn spring-boot:run
```

The API starts on port **8080** by default with an H2 in-memory database.  
H2 Console is available at: `http://localhost:8080/h2-console`

For production (PostgreSQL):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Batch Module (JCL/Batch Replacement)

```bash
cd carddemo-batch
mvn spring-boot:run
```

Individual batch jobs can be triggered by passing job parameters.

## API Endpoints

| Endpoint | Method | Description | Replaces COBOL |
|----------|--------|-------------|----------------|
| `/api/auth/login` | POST | User authentication | COSGN00C (CC00) |
| `/api/auth/logout` | POST | User sign-off | COSGN00C (CC00) |
| `/api/accounts/{id}` | GET | View account | COACTVWC (CAVW) |
| `/api/accounts/{id}` | PUT | Update account | COACTUPC (CAUP) |
| `/api/cards` | GET | List cards | COCRDLIC (CCLI) |
| `/api/cards/{cardNumber}` | GET | View card | COCRDSLC (CCDL) |
| `/api/cards/{cardNumber}` | PUT | Update card | COCRDUPC (CCUP) |
| `/api/transactions` | GET | List transactions (paginated) | COTRN00C (CT00) |
| `/api/transactions/{id}` | GET | View transaction | COTRN01C (CT01) |
| `/api/transactions` | POST | Create transaction | COTRN02C (CT02) |
| `/api/reports/transactions` | POST | Generate report | CORPT00C (CR00) |
| `/api/bill-payments` | POST | Process bill payment | COBIL00C (CB00) |
| `/api/admin/users` | CRUD | User management | COUSR00C-03C (CU00-CU03) |
| `/api/admin/transaction-types` | CRUD | Transaction type mgmt | COTRTLIC/COTRTUPC |

## COBOL Program to Java Class Mapping

### Online Programs (CICS) → REST Controllers/Services

| COBOL Program | Transaction | Java Controller | Java Service |
|---------------|-------------|-----------------|--------------|
| COSGN00C | CC00 | `AuthController` | `AuthenticationService` |
| COACTVWC | CAVW | `AccountController` | `AccountService` |
| COACTUPC | CAUP | `AccountController` | `AccountService` |
| COCRDLIC | CCLI | `CardController` | `CardService` |
| COCRDSLC | CCDL | `CardController` | `CardService` |
| COCRDUPC | CCUP | `CardController` | `CardService` |
| COTRN00C | CT00 | `TransactionController` | `TransactionService` |
| COTRN01C | CT01 | `TransactionController` | `TransactionService` |
| COTRN02C | CT02 | `TransactionController` | `TransactionService` |
| CORPT00C | CR00 | `ReportController` | `ReportService` |
| COBIL00C | CB00 | `BillPaymentController` | `BillPaymentService` |
| COUSR00C | CU00 | `UserManagementController` | `UserManagementService` |
| COUSR01C | CU01 | `UserManagementController` | `UserManagementService` |
| COUSR02C | CU02 | `UserManagementController` | `UserManagementService` |
| COUSR03C | CU03 | `UserManagementController` | `UserManagementService` |

### Batch Programs (JCL) → Spring Batch Jobs

| COBOL Program | JCL Job | Java Class |
|---------------|---------|------------|
| CBTRN02C | POSTTRAN | `TransactionPostingJob` |
| CBACT04C | INTCALC | `InterestCalculationJob` |
| CBSTM03A | CREASTMT | `StatementGenerationJob` |
| CBTRN03C | TRANREPT | `TransactionReportJob` |
| CBEXPORT | CBEXPORT | `DataExportJob` |
| CBIMPORT | CBIMPORT | `DataImportJob` |
| COMBTRAN | COMBTRAN | `CombineTransactionsJob` |

## VSAM Files to Database Tables Mapping

| VSAM File | Copybook | Database Table | JPA Entity |
|-----------|----------|---------------|------------|
| ACCTDAT | CVACT01Y | `accounts` | `Account` |
| CARDDAT | CVACT02Y | `cards` | `Card` |
| CUSTDAT | CVCUS01Y | `customers` | `Customer` |
| CARDXREF | CVACT03Y | `card_cross_references` | `CardCrossReference` |
| TRANSACT | CVTRA05Y | `transactions` | `Transaction` |
| DALYTRAN | CVTRA06Y | `daily_transactions` | `DailyTransaction` |
| TCATBAL | CVTRA01Y | `transaction_category_balances` | `TransactionCategoryBalance` |
| DISCGRP | CVTRA02Y | `disclosure_groups` | `DisclosureGroup` |
| TRANTYPE | CVTRA03Y | `transaction_types` | `TransactionType` |
| TRANCAT | CVTRA04Y | `transaction_categories` | `TransactionCategory` |
| USRSEC | CSUSR01Y | `user_security` | `UserSecurity` |

## Database Schema

The database schema is managed by Flyway migrations located in:
```
carddemo-common/src/main/resources/db/migration/
```

The initial migration (`V1__init_schema.sql`) creates all tables matching the COBOL record layouts.

## Security

Two roles are defined, matching the COBOL `CDEMO-USRTYP` pattern from `COCOM01Y.cpy`:
- **ADMIN** (`'A'`) — Full access including user management and transaction type administration
- **USER** (`'U'`) — Standard access to account, card, and transaction operations
