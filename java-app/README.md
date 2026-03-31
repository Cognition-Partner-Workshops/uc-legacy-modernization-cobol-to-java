# CardDemo - Java Spring Boot Application

Modernized version of the CardDemo mainframe COBOL application, converted to Java using Spring Boot 3.x.

## Prerequisites

- Java 17+
- Maven 3.8+

## Build and Run

```bash
cd java-app
mvn clean package
java -jar target/carddemo-1.0.0.jar
```

Or run with Maven:

```bash
cd java-app
mvn spring-boot:run
```

The application starts at **http://localhost:8080**

## Default Credentials

| User ID   | Password | Role  |
|-----------|----------|-------|
| ADMIN001  | PASSWORD | Admin |
| USER0001  | PASSWORD | User  |

## Database

- **Development**: H2 in-memory database (auto-populated with seed data from `schema.sql` and `data.sql`)
- **Production**: PostgreSQL (activate `prod` profile)
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:carddemo`)

## API Endpoints

### Authentication
| Method | Path      | Description        |
|--------|-----------|--------------------|
| GET    | /login    | Login page         |
| POST   | /login    | Process login      |
| GET    | /logout   | Logout             |

### Main Menu
| Method | Path  | Description |
|--------|-------|-------------|
| GET    | /menu | Main menu   |

### Admin Menu (ROLE_ADMIN only)
| Method | Path        | Description |
|--------|-------------|-------------|
| GET    | /admin/menu | Admin menu  |

### Accounts
| Method | Path                   | Description          |
|--------|------------------------|----------------------|
| GET    | /accounts/view         | Account search form  |
| POST   | /accounts/view         | View account details |
| GET    | /accounts/update       | Account update form  |
| POST   | /accounts/update/search| Search account       |
| POST   | /accounts/update       | Update account       |

### Credit Cards
| Method | Path                     | Description       |
|--------|--------------------------|-------------------|
| GET    | /cards/list              | List all cards    |
| GET    | /cards/view/{cardNum}    | View card details |
| GET    | /cards/update/{cardNum}  | Card update form  |
| POST   | /cards/update            | Update card       |

### Transactions
| Method | Path                        | Description            |
|--------|-----------------------------|------------------------|
| GET    | /transactions               | List (paginated, 10/page) |
| GET    | /transactions/view/{tranId} | View transaction       |
| GET    | /transactions/add           | Add transaction form   |
| POST   | /transactions/add           | Create transaction     |

### Bill Payment
| Method | Path      | Description         |
|--------|-----------|---------------------|
| GET    | /payments | Payment form        |
| POST   | /payments | Process payment     |

### Reports
| Method | Path     | Description          |
|--------|----------|----------------------|
| GET    | /reports | Report options form  |
| POST   | /reports | Generate report      |

### User Management (ROLE_ADMIN only)
| Method | Path                           | Description  |
|--------|--------------------------------|--------------|
| GET    | /admin/users                   | List users   |
| GET    | /admin/users/add               | Add user form|
| POST   | /admin/users/add               | Create user  |
| GET    | /admin/users/update/{userId}   | Update form  |
| POST   | /admin/users/update/{userId}   | Update user  |
| POST   | /admin/users/delete/{userId}   | Delete user  |

### Batch Jobs
| Method | Path                          | Description                |
|--------|-------------------------------|----------------------------|
| POST   | /batch/post-transactions      | Post daily transactions    |
| POST   | /batch/calculate-interest     | Calculate interest         |
| POST   | /batch/generate-statements    | Generate statements        |

## Batch Jobs

Trigger batch jobs via REST endpoints:

```bash
# Post daily transactions (replaces POSTTRAN.jcl + CBTRN02C.cbl)
curl -X POST http://localhost:8080/batch/post-transactions

# Calculate interest (replaces INTCALC.jcl + CBACT04C.cbl)
curl -X POST http://localhost:8080/batch/calculate-interest

# Generate statements (replaces CREASTMT.jcl + CBSTM03A.CBL)
curl -X POST http://localhost:8080/batch/generate-statements
```

## Database Schema

| Table                          | Source Copybook | VSAM File   |
|--------------------------------|-----------------|-------------|
| user_security                  | CSUSR01Y        | USRSEC      |
| account                        | CVACT01Y        | ACCTDAT     |
| customer                       | CVCUS01Y        | CUSTDAT     |
| card_xref                      | CVACT03Y        | CARDXREF    |
| card_data                      | CVACT02Y        | CARDDAT     |
| transaction                    | CVTRA05Y        | TRANSACT    |
| daily_transaction              | CVTRA06Y        | DAILYTRAN   |
| transaction_category_balance   | CVTRA01Y        | TCATBALF    |
| disclosure_group               | CVTRA02Y        | DISCGRP     |
| transaction_type               | -               | TRANTYPE    |
| transaction_category           | -               | TRANCATG    |
| rejected_transaction           | -               | (new)       |

## COBOL-to-Java Mapping

### Online Programs
| COBOL Program | Java Controller              | Function              |
|---------------|------------------------------|-----------------------|
| COSGN00C      | LoginController              | Sign-on               |
| COMEN01C      | MainMenuController           | Main menu             |
| COADM01C      | AdminMenuController          | Admin menu            |
| COACTVWC      | AccountController            | Account view          |
| COACTUPC      | AccountController            | Account update        |
| COCRDLIC      | CreditCardController         | Card list             |
| COCRDSLC      | CreditCardController         | Card detail           |
| COCRDUPC      | CreditCardController         | Card update           |
| COTRN00C      | TransactionController        | Transaction list      |
| COTRN01C      | TransactionController        | Transaction view      |
| COTRN02C      | TransactionController        | Transaction add       |
| COBIL00C      | BillPaymentController        | Bill payment          |
| CORPT00C      | ReportController             | Reports               |
| COUSR00C      | UserManagementController     | User list             |
| COUSR01C      | UserManagementController     | User add              |
| COUSR02C      | UserManagementController     | User update           |
| COUSR03C      | UserManagementController     | User delete           |

### Batch Programs
| COBOL Program | Java Batch Job                     | JCL          |
|---------------|------------------------------------|--------------|
| CBTRN02C      | TransactionPostingTasklet          | POSTTRAN.jcl |
| CBACT04C      | InterestCalculationTasklet         | INTCALC.jcl  |
| CBSTM03A/B    | StatementGenerationTasklet         | CREASTMT.jcl |

### Services
| Service                  | Purpose                                    |
|--------------------------|--------------------------------------------|
| AuthenticationService    | User lookup and password validation        |
| AccountService           | Account CRUD and balance updates           |
| CustomerService          | Customer CRUD                              |
| CardService              | Card and cross-reference operations        |
| TransactionService       | Transaction CRUD, validation, pagination   |
| UserManagementService    | Admin user management                      |
| ReportService            | Transaction report generation              |

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Build**: Maven
- **Web**: Spring MVC + Thymeleaf
- **Persistence**: Spring Data JPA + H2 (dev) / PostgreSQL (prod)
- **Security**: Spring Security with form-based login
- **Batch**: Spring Batch with Tasklet-based jobs
- **Testing**: JUnit 5 + Mockito
- **CSS**: Custom terminal-style green-on-black theme

## Running Tests

```bash
cd java-app
mvn test
```
