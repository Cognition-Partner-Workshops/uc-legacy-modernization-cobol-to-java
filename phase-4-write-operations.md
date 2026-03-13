# Phase 4: Write Operations (Medium-High Risk)

**Risk Level:** Medium-High  
**Objective:** Convert data-modifying online transactions. These programs write to VSAM files and require careful transaction management, data integrity validation, and thorough regression testing.

> **Critical Note:** `COACTUPC` is the highest-complexity online program in the entire CardDemo system and should receive the most extensive test coverage.

---

## Wave 4.1: Account Update

### Objective
Convert the account update program — the most complex online program in CardDemo.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COACTUPC.cbl` |
| **BMS Screen** | `app/bms/COACTUP.bms` |
| **BMS Copybook** | `app/cpy-bms/COACTUP.CPY` |
| **CICS Transaction** | `CAUP` (defined in `app/csd/CARDDEMO.CSD`) |
| **Estimated CICS Calls** | ~34 |
| **Estimated COPY References** | ~113 (highest of any online program) |
| **VSAM Files** | `ACCTDAT` (read/write), `CUSTDAT` (read), `CCXREF` (read) |
| **Related Copybooks** | `CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy`, `CVCUS01Y.cpy`, `CVCRD01Y.cpy`, `COCOM01Y.cpy`, and many others |
| **Java Target** | `AccountController.update()` + `AccountService.updateAccount()` |

### Why This Is the Most Complex Program

1. **113 COPY references** — the highest of any online program, indicating extensive use of shared data structures
2. **34 CICS calls** — high CICS interaction count
3. **Multi-file writes** — updates `ACCTDAT` while reading `CUSTDAT` and `CCXREF` for validation
4. **Complex validation logic** — account status checks, balance validations, field-level edits
5. **Optimistic locking** — CICS READ FOR UPDATE / REWRITE pattern must be converted to JPA versioning or `@Version` annotation

### CICS Call Replacement Map

| CICS Command | Count (est.) | Java Replacement |
|---|---|---|
| `EXEC CICS SEND MAP` | 4-5 | Return JSON / render template |
| `EXEC CICS RECEIVE MAP` | 3-4 | `@RequestBody AccountUpdateRequest` |
| `EXEC CICS READ FILE('ACCTDAT') UPDATE` | 3-4 | `accountRepository.findById()` with `@Lock(PESSIMISTIC_WRITE)` |
| `EXEC CICS READ FILE('CUSTDAT')` | 2-3 | `customerRepository.findById()` |
| `EXEC CICS READ FILE('CCXREF')` | 1-2 | `cardXrefRepository.findByAccountId()` |
| `EXEC CICS REWRITE FILE('ACCTDAT')` | 2-3 | `accountRepository.save()` |
| `EXEC CICS RETURN` | 4-5 | Return HTTP response |
| `EXEC CICS XCTL` | 2-3 | Redirect / forward |
| Other CICS commands | ~10 | Various Spring equivalents |

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/accounts/{id}/edit` | Get account for editing (with current values) |
| `PUT` | `/api/accounts/{id}` | Update account |

### Test Strategy (Enhanced for COACTUPC)

Due to the complexity of this program, the following enhanced test coverage is required:

- **Unit tests:** Every validation rule, every field update path
- **Integration tests:** Full update flow with real database
- **Concurrent update tests:** Verify optimistic/pessimistic locking behavior
- **Boundary tests:** Min/max values for all numeric fields
- **Parallel-run tests:** Same inputs to COBOL and Java, compare outputs field-by-field
- **Regression suite:** At least 50 test cases covering all account states

### Acceptance Criteria
- [ ] Account update functional with all validation rules preserved
- [ ] Optimistic/pessimistic locking prevents concurrent update conflicts
- [ ] All ~34 CICS calls replaced
- [ ] All ~113 COPY references resolved to Java equivalents
- [ ] Enhanced test coverage (50+ test cases)
- [ ] Parallel-run validation passes for all test scenarios

---

## Wave 4.2: Card Update

### Objective
Convert the card update program.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COCRDUPC.cbl` |
| **BMS Screen** | `app/bms/COCRDUP.bms` |
| **BMS Copybook** | `app/cpy-bms/COCRDUP.CPY` |
| **CICS Transaction** | `CCUP` (defined in `app/csd/CARDDEMO.CSD`) |
| **Estimated CICS Calls** | ~21 |
| **VSAM Files** | `CARDDAT` (read/write), `CCXREF` (read) |
| **Related Copybooks** | `CVCRD01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `CardController.update()` + `CardService.updateCard()` |

### CICS Call Replacement Summary

| CICS Command Pattern | Java Replacement |
|---|---|
| `READ FILE('CARDDAT') UPDATE` | `cardRepository.findByCardNumber()` with lock |
| `REWRITE FILE('CARDDAT')` | `cardRepository.save()` |
| `READ FILE('CCXREF')` | `cardXrefRepository.findByCardNumber()` |
| `SEND MAP / RECEIVE MAP` | REST request/response |

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/cards/{cardNumber}/edit` | Get card for editing |
| `PUT` | `/api/cards/{cardNumber}` | Update card |

### Acceptance Criteria
- [ ] Card update functional with all validation rules preserved
- [ ] Cross-reference lookup validates card-account relationship
- [ ] All ~21 CICS calls replaced

---

## Wave 4.3: Transaction Add

### Objective
Convert the transaction add program, which writes new transactions and updates account balances.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COTRN02C.cbl` |
| **BMS Screen** | `app/bms/COTRN02.bms` |
| **BMS Copybook** | `app/cpy-bms/COTRN02.CPY` |
| **Estimated CICS Calls** | ~22 |
| **VSAM Files** | `TRANSACT` (write), `ACCTDAT` (read/write — balance update) |
| **Related Copybooks** | `CVTRA01Y.cpy`, `CVTRA05Y.cpy`, `CVACT01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `TransactionController.add()` + `TransactionService.createTransaction()` |

### Key Considerations
- **Dual-file write:** Adding a transaction also updates the account balance in `ACCTDAT`. This must be an atomic operation.
- **Transaction integrity:** Use `@Transactional` to ensure both the transaction record insert and account balance update succeed or fail together.
- **Validation:** Transaction amount, type, and account status must be validated before posting.

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/transactions` | Add new transaction |

### Acceptance Criteria
- [ ] Transaction creation writes to `transactions` table and updates `accounts` balance
- [ ] Atomic operation — both writes succeed or both fail
- [ ] All ~22 CICS calls replaced
- [ ] Validation rules match COBOL program logic

---

## Wave 4.4: Bill Payment

### Objective
Convert the bill payment program — a financial operation requiring high accuracy and careful validation.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COBIL00C.cbl` |
| **BMS Screen** | `app/bms/COBIL00.bms` |
| **BMS Copybook** | `app/cpy-bms/COBIL00.CPY` |
| **CICS Transaction** | `CB00` (defined in `app/csd/CARDDEMO.CSD`) |
| **Estimated CICS Calls** | ~26 |
| **VSAM Files** | `TRANSACT` (write), `ACCTDAT` (read/write), `CCXREF` (read) |
| **Related Copybooks** | `CVTRA05Y.cpy`, `CVACT01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `BillPaymentController.pay()` + `BillPaymentService.processPayment()` |

### Key Considerations
- **Financial operation:** This is the highest-risk online program from a business perspective
- **Payment processing:** Must handle payment amount validation, sufficient balance checks, and payment posting
- **Audit trail:** All payments should be logged for audit compliance
- **Idempotency:** Payment operations should be idempotent to prevent double-charging

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/payments` | Process a bill payment |
| `GET` | `/api/payments/{id}` | Get payment status |

### Acceptance Criteria
- [ ] Bill payment processes correctly with balance validation
- [ ] Payment amount precision matches COBOL (packed decimal / `BigDecimal`)
- [ ] Idempotency key prevents duplicate payments
- [ ] All ~26 CICS calls replaced
- [ ] Audit logging for all payment operations

---

## BMS Screens to Convert (Phase 4)

| BMS Map | Source Path | Web UI Page |
|---|---|---|
| `COACTUP.bms` | `app/bms/COACTUP.bms` | Account update form |
| `COCRDUP.bms` | `app/bms/COCRDUP.bms` | Card update form |
| `COTRN02.bms` | `app/bms/COTRN02.bms` | Transaction add form |
| `COBIL00.bms` | `app/bms/COBIL00.bms` | Bill payment form |

---

## CICS Call Summary

| Program | CICS Calls | COPY Refs | Wave | Risk |
|---|---|---|---|---|
| `COACTUPC` | ~34 | ~113 | 4.1 | **Highest complexity** |
| `COCRDUPC` | ~21 | — | 4.2 | Medium |
| `COTRN02C` | ~22 | — | 4.3 | Medium-High (dual write) |
| `COBIL00C` | ~26 | — | 4.4 | **High (financial)** |
| **Total** | **~103** | | | |

---

## Dependencies
- **Phase 0:** Database schema, test framework
- **Phase 1:** JPA entities, `DateConversionService`
- **Phase 2:** Authentication (all write operations require login)
- **Phase 3:** Read operations (update forms pre-populate with read data)

## Next Phase
Proceed to [Phase 5: Core Batch Processing](phase-5-batch-processing.md) once all Wave 4.x deliverables are accepted.
