# CardDemo Migration Strategy & Recommendations

## 1. Executive Summary

This document presents a phased migration strategy for converting the CardDemo mainframe application from COBOL/CICS/VSAM to Java/Spring Boot/PostgreSQL. The strategy prioritizes risk mitigation through incremental migration, starting with the lowest-risk components and progressively tackling more complex areas.

**Key Metrics:**
- Total COBOL programs to migrate: 25 (17 online + 8 batch)
- Optional module programs: 13 (IMS DB/DB2/MQ integration)
- Estimated total COBOL lines: ~17,000
- Estimated duplicate/boilerplate lines to eliminate: ~1,750 (13% reduction)
- VSAM files to convert to relational tables: 8
- BMS screens to replace with web UI: 17
- JCL batch jobs to convert: 38

---

## 2. Migration Approach: Strangler Fig Pattern

We recommend the **Strangler Fig** pattern - incrementally replacing mainframe functionality with Java services while keeping the existing system running until full migration is complete.

```
Phase 0        Phase 1          Phase 2          Phase 3          Phase 4
(Prep)         (Foundation)     (Core Online)    (Batch + Admin)  (Cutover)
  |               |                |                |                |
  v               v                v                v                v
+----------+  +----------+  +----------+  +----------+  +----------+
|Data Model|  |Auth +    |  |Account + |  |Batch     |  |Retire    |
|+ DB Setup|  |Navigation|  |Card +    |  |Processing|  |Mainframe |
|+ Infra   |  |+ User    |  |Transaction| |+ Reports |  |Components|
|          |  |Mgmt      |  |+ Bill Pay|  |+ Optional|  |          |
+----------+  +----------+  +----------+  +----------+  +----------+
  2 weeks       3 weeks        5 weeks       4 weeks       2 weeks
                                                       Total: ~16 weeks
```

---

## 3. Phase 0: Preparation & Foundation (Weeks 1-2)

### 3.1 Objectives
- Set up Java project structure and CI/CD pipeline
- Create database schema and seed with test data
- Establish coding standards and shared libraries

### 3.2 Tasks

| Task | Details | Effort |
|------|---------|--------|
| Project scaffolding | Create multi-module Maven/Gradle project (api, service, domain, batch, common) | 2 days |
| Database setup | Create PostgreSQL schema from entity mappings (see 03-data-model-entity-mapping.md) | 2 days |
| Data migration scripts | Parse ASCII fixed-width files from `app/data/ASCII/`, load into database | 2 days |
| Shared utilities | Create `DateUtils`, `ValidationUtils`, `HeaderService` (consolidating duplicate COBOL patterns) | 1 day |
| CI/CD pipeline | Configure build, test, lint, deploy pipeline | 1 day |
| Testing framework | Set up JUnit 5, Mockito, integration test infrastructure | 1 day |

### 3.3 Key Deliverables
- Running Spring Boot application with empty endpoints
- Populated PostgreSQL database with test data
- Shared utility classes (eliminating duplicated COBOL logic)
- CI/CD pipeline producing deployable artifacts

### 3.4 Consolidation Opportunity - Shared Utilities
Extract these duplicated patterns from COBOL into reusable Java components:

| COBOL Pattern | Java Component | Programs Consolidated |
|--------------|---------------|----------------------|
| POPULATE-HEADER-INFO (17 programs) | `BaseController.populateHeader()` or Spring interceptor | All controllers |
| WS-ERR-FLG error handling (17 programs) | `@ControllerAdvice` + custom exceptions | All controllers |
| COMMAREA init/navigation (17 programs) | Spring request routing + `NavigationService` | All controllers |
| Date validation/formatting (3+ programs) | `DateUtils` + Bean Validation annotations | All services |
| VSAM read with error handling (7 programs) | Spring Data JPA repositories | All repositories |

---

## 4. Phase 1: Authentication, Navigation & User Management (Weeks 3-5)

### 4.1 Objectives
- Implement authentication (replacing COSGN00C)
- Implement menu routing (replacing COMEN01C, COADM01C)
- Implement User CRUD (replacing COUSR00C-03C)

### 4.2 Program-to-Java Mapping

| COBOL Program | Java Component | Complexity | Notes |
|--------------|---------------|-----------|-------|
| COSGN00C (261 lines) | `AuthController` + `AuthenticationService` + Spring Security config | Low | Replace plaintext password check with BCrypt; add JWT tokens |
| COMEN01C (309 lines) | Frontend routing (React/Angular) | Low | No backend needed; menu is UI-only |
| COADM01C (289 lines) | Frontend routing with role guard | Low | No backend needed; admin menu is UI-only |
| COUSR00C (696 lines) | `UserController.listUsers()` + `UserService` | Medium | Replace STARTBR/READNEXT pagination with Spring Data `Pageable` |
| COUSR01C (300 lines) | `UserController.createUser()` | Low | Replace WRITE with `userRepository.save()` |
| COUSR02C (415 lines) | `UserController.updateUser()` | Low | Replace READ/REWRITE with find + save |
| COUSR03C (360 lines) | `UserController.deleteUser()` | Low | Replace READ/DELETE with find + delete |

### 4.3 API Endpoints

```
POST   /api/auth/login          # COSGN00C - authenticate user
POST   /api/auth/logout         # New (no COBOL equivalent)
GET    /api/users               # COUSR00C - list users (paginated)
POST   /api/users               # COUSR01C - create user
GET    /api/users/{userId}      # COUSR02C - get user for edit
PUT    /api/users/{userId}      # COUSR02C - update user
DELETE /api/users/{userId}      # COUSR03C - delete user
```

### 4.4 Security Implementation

```java
// Replace COBOL's plaintext password storage (USRSEC file)
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")  // Admin-only
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### 4.5 Consolidation Applied
- **Menu definitions** (COMEN02Y, COADM02Y copybooks): Replaced by frontend routing configuration - no backend table needed
- **Header population**: Handled by frontend layout component
- **Navigation XCTL pattern**: Replaced by SPA client-side routing

---

## 5. Phase 2: Core Online Functions (Weeks 6-10)

### 5.1 Objectives
- Implement Account management (view, update)
- Implement Card management (list, view, update)
- Implement Transaction management (list, view, add)
- Implement Bill Payment
- Implement Report submission

### 5.2 Program-to-Java Mapping

| COBOL Program | Java Component | Complexity | Migration Notes |
|--------------|---------------|-----------|-----------------|
| COACTVWC (942 lines) | `AccountController.viewAccount()` + `AccountService` | Medium | Multi-file read → single service call with JPA joins |
| **COACTUPC (4237 lines)** | `AccountController.updateAccount()` + `AccountService` + `AccountValidator` | **High** | Largest program; extract validation to Bean Validation; consolidate with COACTVWC shared logic |
| COCRDLIC (1460 lines) | `CardController.listCards()` + `CardService` | Medium | Replace STARTBR/READNEXT with Pageable; alternate index → DB index |
| COCRDSLC (888 lines) | `CardController.viewCard()` + `CardService` | Low-Med | Consolidate shared card-read logic with COCRDLIC and COCRDUPC |
| **COCRDUPC (1561 lines)** | `CardController.updateCard()` + `CardService` + `CardValidator` | **Med-High** | Extract validation; consolidate with COCRDSLC |
| COTRN00C (700 lines) | `TransactionController.listTransactions()` | Medium | Replace STARTBR/READNEXT with Pageable |
| COTRN01C (331 lines) | `TransactionController.viewTransaction()` | Low | Simple read |
| COTRN02C (784 lines) | `TransactionController.addTransaction()` + `TransactionService` | Medium | Multi-file validation; extract to service layer |
| CORPT00C (650 lines) | `ReportController.submitReport()` + async job trigger | Medium | Replace TDQ/INTRDR job submission with Spring Batch async trigger |
| COBIL00C (573 lines) | `BillPaymentController` + `BillPaymentService` | Medium | Multi-file ops; transaction creation + balance update in single DB transaction |

### 5.3 API Endpoints

```
# Account Management
GET    /api/accounts/{accountId}              # COACTVWC
PUT    /api/accounts/{accountId}              # COACTUPC

# Card Management
GET    /api/accounts/{accountId}/cards        # COCRDLIC - list by account
GET    /api/cards/{cardNumber}                # COCRDSLC - view card detail
PUT    /api/cards/{cardNumber}                # COCRDUPC - update card

# Transaction Management
GET    /api/transactions                      # COTRN00C - list (with pagination)
GET    /api/transactions/{transactionId}      # COTRN01C - view detail
POST   /api/transactions                      # COTRN02C - add transaction

# Reports
POST   /api/reports/transactions              # CORPT00C - submit report job
GET    /api/reports/{reportId}/status          # New - check job status

# Bill Payment
POST   /api/accounts/{accountId}/payments     # COBIL00C - make payment
```

### 5.4 Key Consolidation Opportunities in This Phase

#### 5.4.1 Account View + Update Shared Logic
COACTVWC (view) and COACTUPC (update) both read from ACCTDAT, CUSTDAT, and CARDXREF. In Java:
```java
@Service
public class AccountService {
    // Shared method used by both view and update
    public AccountDetailDTO getAccountDetail(long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        Customer customer = cardXrefRepository.findCustomerByAccountId(accountId);
        List<Card> cards = cardRepository.findByAccountId(accountId);
        return new AccountDetailDTO(account, customer, cards);
    }
}
```

#### 5.4.2 Card List + View + Update Shared Logic
COCRDLIC, COCRDSLC, and COCRDUPC all share card lookup by number and by account. Consolidate into:
```java
@Service
public class CardService {
    public Card getCard(String cardNumber) { /* shared lookup */ }
    public Page<Card> listCardsByAccount(long accountId, Pageable pageable) { /* shared list */ }
    public Card updateCard(String cardNumber, CardUpdateDTO dto) { /* validate + update */ }
}
```

#### 5.4.3 Pagination Pattern (3 programs)
COTRN00C, COUSR00C, and COCRDLIC all implement identical STARTBR/READNEXT/READPREV/ENDBR patterns. In Java:
```java
// All list endpoints use the same pattern
@GetMapping
public Page<TransactionDTO> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    return transactionService.list(PageRequest.of(page, size));
}
```

#### 5.4.4 Input Validation Consolidation
COACTUPC (4237 lines) has ~2000 lines of field-by-field validation. Replace with:
```java
public class AccountUpdateDTO {
    @NotBlank @Size(max = 15) private String phone1;
    @Pattern(regexp = "\\d{9}") private String ssn;
    @PastOrPresent private LocalDate dateOfBirth;
    @Positive private BigDecimal creditLimit;
    // ... Bean Validation handles what took hundreds of COBOL lines
}
```

---

## 6. Phase 3: Batch Processing & Optional Modules (Weeks 11-14)

### 6.1 Objectives
- Migrate batch programs to Spring Batch
- Implement optional modules (DB2, IMS DB, MQ) if needed

### 6.2 Batch Program Migration

| COBOL Program | Spring Batch Job | Pattern | Notes |
|--------------|------------------|---------|-------|
| CBTRN02C (732 lines) | `TransactionPostingJob` | Chunk-oriented (read/process/write) | Most complex batch job; 6-file I/O → single DB transaction |
| CBACT04C (~500 lines) | `InterestCalculationJob` | Chunk-oriented | Read accounts, calculate interest, update balances |
| CBSTM03A/B (~1200 lines) | `StatementGenerationJob` | Two-step: sort + generate | Replace COBOL SORT with SQL ORDER BY; single job with two steps |
| CBTRN03C (~500 lines) | `TransactionReportJob` | Chunk-oriented | Read transactions by date range, generate report |
| CBEXPORT (~300 lines) | `DataExportJob` | Chunk-oriented | VSAM → file becomes DB → file |
| CBIMPORT (~300 lines) | `DataImportJob` | Chunk-oriented | File → VSAM becomes file → DB |

### 6.3 Spring Batch Architecture for Transaction Posting (CBTRN02C)

```java
@Configuration
public class TransactionPostingJobConfig {

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository,
                                      Step readAndPostStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
            .start(readAndPostStep)
            .build();
    }

    @Bean
    public Step readAndPostStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager) {
        return new StepBuilder("readAndPostStep", jobRepository)
            .<DailyTransaction, Transaction>chunk(100, txManager)
            .reader(dailyTransactionReader())
            .processor(transactionValidator())     // Validate card, account, amount
            .writer(transactionWriter())           // Write to transactions table
            .faultTolerant()
            .skipLimit(Integer.MAX_VALUE)
            .skip(ValidationException.class)       // Replaces DALYREJS reject file
            .listener(rejectionLogger())           // Log rejections
            .build();
    }
}
```

### 6.4 JCL Batch Cycle → Spring Batch Equivalent

| JCL Step | Purpose | Spring Batch Equivalent |
|----------|---------|----------------------|
| CLOSEFIL | Close CICS files for batch | Not needed (DB handles concurrent access) |
| ACCTFILE, CARDFILE, etc. | Refresh data files | Not needed (DB is always current) |
| POSTTRAN | Post daily transactions | `TransactionPostingJob` |
| INTCALC | Calculate interest | `InterestCalculationJob` |
| TRANBKP | Backup transactions | Database backup (pg_dump or scheduled snapshot) |
| COMBTRAN | Combine transactions | SQL query / view (no separate job needed) |
| CREASTMT | Generate statements | `StatementGenerationJob` |
| TRANIDX | Rebuild alternate indexes | Not needed (DB indexes auto-maintained) |
| OPENFIL | Re-open CICS files | Not needed |

**Eliminated JCL jobs**: CLOSEFIL, OPENFIL, data refresh jobs, TRANIDX, TRANBKP (replaced by DB backup), COMBTRAN (replaced by SQL). This reduces 38 JCL jobs to approximately 4-6 Spring Batch jobs.

### 6.5 Optional Modules

| Module | Priority | Migration Path |
|--------|----------|---------------|
| Authorization (IMS DB + DB2 + MQ) | Medium | Convert IMS DB reads to JPA; MQ trigger → Spring JMS listener; DB2 queries → JPA queries |
| Transaction Type (DB2) | Low | Simple CRUD already handled by standard JPA repository pattern |
| VSAM-MQ Integration | Low | Replace MQ request/response with REST API calls or Spring Integration |

---

## 7. Phase 4: Testing, Cutover & Decommission (Weeks 15-16)

### 7.1 Testing Strategy

| Test Type | Scope | Tools |
|-----------|-------|-------|
| Unit tests | All services, validators, utilities | JUnit 5, Mockito |
| Integration tests | Repository layer, API endpoints | Spring Boot Test, TestContainers |
| Data validation | Compare VSAM data with PostgreSQL | Custom comparison scripts |
| Functional tests | End-to-end business scenarios | Selenium/Cypress (UI), REST Assured (API) |
| Performance tests | Batch job throughput, API response times | JMeter, Gatling |
| Regression tests | All original COBOL test scenarios | Map to Java test cases |

### 7.2 Cutover Checklist

1. Final data migration from mainframe to PostgreSQL
2. DNS/load balancer switch from mainframe to Java service
3. Batch job scheduling configured in production
4. Monitoring and alerting configured
5. Rollback plan documented and tested
6. Mainframe kept running in read-only mode for 30 days

---

## 8. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Data loss during migration | Low | Critical | Validate row counts and checksums; run parallel for 30 days |
| Business logic differences | Medium | High | Comprehensive test suite comparing COBOL output vs Java output |
| COACTUPC complexity (4237 lines) | Medium | Medium | Dedicated spike for validation logic extraction; pair programming |
| Batch timing changes | Low | Medium | Performance testing; maintain batch windows |
| EBCDIC/ASCII encoding issues | Medium | Low | Test with actual EBCDIC data from `app/data/EBCDIC/` |
| Missing edge cases | Medium | Medium | Review all EVALUATE/IF branches in COBOL; map to test cases |

---

## 9. Effort Estimation Summary

| Phase | Duration | Team Size | Key Deliverables |
|-------|----------|-----------|-----------------|
| Phase 0: Preparation | 2 weeks | 2 developers | Project setup, DB schema, shared utilities |
| Phase 1: Auth + User Mgmt | 3 weeks | 2 developers | Login, user CRUD, Spring Security |
| Phase 2: Core Online | 5 weeks | 3-4 developers | Account, Card, Transaction, Bill Payment, Reports |
| Phase 3: Batch + Optional | 4 weeks | 2-3 developers | Spring Batch jobs, optional modules |
| Phase 4: Testing + Cutover | 2 weeks | Full team | Testing, data migration, go-live |
| **Total** | **16 weeks** | **2-4 developers** | **Full Java application** |

---

## 10. Recommended Tools & Libraries

| Category | Tool | Purpose |
|----------|------|---------|
| Framework | Spring Boot 3.x | Application framework |
| Security | Spring Security + JWT | Authentication/authorization |
| Data Access | Spring Data JPA + Hibernate | ORM and data access |
| Database | PostgreSQL 15+ | Primary data store |
| Batch | Spring Batch 5.x | Batch job processing |
| Validation | Jakarta Bean Validation (Hibernate Validator) | Input validation |
| API Documentation | SpringDoc OpenAPI (Swagger) | API documentation |
| Testing | JUnit 5 + Mockito + TestContainers | Unit and integration testing |
| Build | Maven or Gradle | Build management |
| Migration | Flyway or Liquibase | Database schema versioning |
| Monitoring | Spring Actuator + Micrometer | Application metrics |
| Logging | SLF4J + Logback | Structured logging |
| Containerization | Docker + Kubernetes | Deployment |

---

## 11. Success Criteria

| Criteria | Metric |
|----------|--------|
| Functional parity | All 17 online functions and 8 batch jobs reproduced |
| Data integrity | 100% of VSAM records migrated with zero data loss |
| Performance | API response time < 200ms (P95); batch jobs complete within same window |
| Code reduction | ~30-40% fewer lines of code than original COBOL through consolidation |
| Test coverage | > 80% line coverage for business logic |
| Security improvement | BCrypt passwords, JWT auth, PCI-DSS ready card handling |
| Zero downtime cutover | Parallel run capability with automated switchover |

---

## 12. Appendix: Quick Reference - COBOL-to-Java Mapping Cheat Sheet

| COBOL Construct | Java Equivalent |
|----------------|-----------------|
| `WORKING-STORAGE SECTION` | Class instance fields |
| `PROCEDURE DIVISION` | Methods |
| `PERFORM paragraph` | Method call |
| `PERFORM ... VARYING` | for loop |
| `EVALUATE ... WHEN` | switch/case or if-else chain |
| `IF ... ELSE ... END-IF` | if-else |
| `MOVE X TO Y` | `y = x;` (assignment) |
| `STRING ... DELIMITED BY` | `String.format()` or StringBuilder |
| `INSPECT ... TALLYING` | String character counting |
| `COMPUTE` | Arithmetic expression |
| `PIC X(n)` | `String` (length n) |
| `PIC 9(n)` | `int` or `long` |
| `PIC S9(n)V99` | `BigDecimal` |
| `88-level condition names` | `enum` or boolean methods |
| `COPY copybook` | `import` + shared class |
| `EXEC CICS READ` | `repository.findById()` |
| `EXEC CICS WRITE` | `repository.save()` |
| `EXEC CICS REWRITE` | `repository.save()` (JPA merge) |
| `EXEC CICS DELETE` | `repository.deleteById()` |
| `EXEC CICS STARTBR/READNEXT/ENDBR` | `repository.findAll(Pageable)` |
| `EXEC CICS XCTL` | Controller method call / redirect |
| `EXEC CICS RETURN TRANSID` | Stateless REST (no pseudo-conversational) |
| `EXEC CICS SEND MAP` | Return JSON response |
| `EXEC CICS RECEIVE MAP` | Accept JSON request body |
| `EXEC CICS HANDLE ABEND` | `@ExceptionHandler` / try-catch |
| `EXEC CICS WRITEQ TD` | Message queue publish / async job trigger |
