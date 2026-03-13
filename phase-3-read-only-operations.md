# Phase 3: Read-Only Operations (Medium Risk)

**Risk Level:** Medium  
**Objective:** Convert view and list screens that perform read-only operations against VSAM files. These are lower risk because they do not modify data, but they involve significant CICS interaction and multiple VSAM file reads.

---

## Wave 3.1: Account View

### Objective
Convert the account view program that displays account details along with associated customer information.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COACTVWC.cbl` |
| **BMS Screen** | `app/bms/COACTVW.bms` |
| **BMS Copybook** | `app/cpy-bms/COACTVW.CPY` |
| **CICS Transaction** | `CAVW` (defined in `app/csd/CARDDEMO.CSD`) |
| **Estimated CICS Calls** | ~29 |
| **VSAM Files Read** | `ACCTDAT`, `CUSTDAT` |
| **Related Copybooks** | `CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy`, `CVCUS01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `AccountController.view()` + `AccountService.getAccountWithCustomer()` |

### CICS Call Replacement Summary

| CICS Command Pattern | Count (est.) | Java Replacement |
|---|---|---|
| `EXEC CICS SEND MAP` | 3-4 | Return JSON / render view template |
| `EXEC CICS RECEIVE MAP` | 2-3 | `@RequestParam` / `@PathVariable` |
| `EXEC CICS READ FILE('ACCTDAT')` | 3-4 | `accountRepository.findById()` |
| `EXEC CICS READ FILE('CUSTDAT')` | 2-3 | `customerRepository.findById()` |
| `EXEC CICS RETURN` | 3-4 | Return HTTP response |
| `EXEC CICS XCTL` | 2-3 | Redirect / forward |
| Other CICS commands | ~10 | Various Spring equivalents |

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/accounts/{id}` | Get account details with customer info |

### Acceptance Criteria
- [ ] Account detail view returns all fields from `ACCTDAT` + related `CUSTDAT`
- [ ] All ~29 CICS calls replaced
- [ ] Response matches COBOL program output for same input

---

## Wave 3.2: Card Operations (List & Search)

### Objective
Convert the card list and card search/detail programs.

### Source Programs

#### COCRDLIC — Card List

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COCRDLIC.cbl` |
| **BMS Screen** | `app/bms/COCRDLI.bms` |
| **BMS Copybook** | `app/cpy-bms/COCRDLI.CPY` |
| **CICS Transaction** | `CCLI` |
| **Estimated CICS Calls** | ~34 |
| **VSAM Files Read** | `CARDDAT`, `CCXREF`, `CARDAIX` |
| **Related Copybooks** | `CVCRD01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `CardController.list()` + `CardService.findAll()` |

**Key behavior:** Browse cards using STARTBR/READNEXT pattern on CARDDAT, with pagination via forward/backward scrolling.

#### COCRDSLC — Card Search / Detail

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COCRDSLC.cbl` |
| **BMS Screen** | `app/bms/COCRDSL.bms` |
| **BMS Copybook** | `app/cpy-bms/COCRDSL.CPY` |
| **CICS Transaction** | `CCDL` |
| **Estimated CICS Calls** | ~26 |
| **VSAM Files Read** | `CARDDAT`, `CCXREF`, `ACCTDAT` |
| **Related Copybooks** | `CVCRD01Y.cpy`, `CVACT01Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `CardController.search()` + `CardService.search()` |

**Key behavior:** Search by card number, account number, or partial match. Uses CARDAIX (alternate index) for account-based lookups.

### REST API Endpoints

| Method | Path | Description | Source |
|---|---|---|---|
| `GET` | `/api/cards` | List cards (paginated) | `COCRDLIC` |
| `GET` | `/api/cards/search` | Search cards by criteria | `COCRDSLC` |
| `GET` | `/api/cards/{cardNumber}` | Get card detail | `COCRDSLC` |

### Acceptance Criteria
- [ ] Card list supports pagination (replacing STARTBR/READNEXT/ENDBR)
- [ ] Card search supports card number, account number, and partial match
- [ ] Cross-reference lookups via `card_xref` table replace CCXREF/CXACAIX VSAM reads
- [ ] All ~60 CICS calls across both programs replaced

---

## Wave 3.3: Transaction Views

### Objective
Convert the transaction list and transaction detail view programs.

### Source Programs

#### COTRN00C — Transaction List

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COTRN00C.cbl` |
| **BMS Screen** | `app/bms/COTRN00.bms` |
| **BMS Copybook** | `app/cpy-bms/COTRN00.CPY` |
| **Estimated CICS Calls** | ~19 |
| **VSAM Files Read** | `TRANSACT` |
| **Related Copybooks** | `CVTRA01Y.cpy`, `CVTRA05Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `TransactionController.list()` + `TransactionService.findAll()` |

**Key behavior:** Browse transactions with STARTBR/READNEXT pattern, supports filtering by account and date range.

#### COTRN01C — Transaction View (Detail)

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COTRN01C.cbl` |
| **BMS Screen** | `app/bms/COTRN01.bms` |
| **BMS Copybook** | `app/cpy-bms/COTRN01.CPY` |
| **Estimated CICS Calls** | ~10 |
| **VSAM Files Read** | `TRANSACT` |
| **Related Copybooks** | `CVTRA01Y.cpy`, `CVTRA05Y.cpy`, `COCOM01Y.cpy` |
| **Java Target** | `TransactionController.view()` + `TransactionService.findById()` |

### REST API Endpoints

| Method | Path | Description | Source |
|---|---|---|---|
| `GET` | `/api/transactions` | List transactions (paginated, filterable) | `COTRN00C` |
| `GET` | `/api/transactions/{id}` | View transaction detail | `COTRN01C` |

### Acceptance Criteria
- [ ] Transaction list supports pagination and filtering
- [ ] Transaction detail returns all fields from TRANSACT
- [ ] All ~29 CICS calls across both programs replaced

---

## Wave 3.4: Reporting

### Objective
Convert the report generation program.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/CORPT00C.cbl` |
| **BMS Screen** | `app/bms/CORPT00.bms` |
| **BMS Copybook** | `app/cpy-bms/CORPT00.CPY` |
| **Estimated CICS Calls** | ~14 |
| **VSAM Files Read** | `TRANSACT`, `ACCTDAT` (for report data) |
| **Related Copybooks** | `COCOM01Y.cpy` |
| **Java Target** | `ReportController.generate()` + `ReportService` |

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/reports/generate` | Generate a report based on parameters |
| `GET` | `/api/reports/{id}` | Retrieve generated report |

### Acceptance Criteria
- [ ] Report generation matches COBOL output format
- [ ] All ~14 CICS calls replaced
- [ ] Reports can be exported (PDF/CSV)

---

## BMS Screens to Convert (Phase 3)

| BMS Map | Source Path | Replaces | Web UI Page |
|---|---|---|---|
| `COACTVW.bms` | `app/bms/COACTVW.bms` | Account view screen | Account detail page |
| `COCRDLI.bms` | `app/bms/COCRDLI.bms` | Card list screen | Card list page |
| `COCRDSL.bms` | `app/bms/COCRDSL.bms` | Card search screen | Card search page |
| `COTRN00.bms` | `app/bms/COTRN00.bms` | Transaction list screen | Transaction list page |
| `COTRN01.bms` | `app/bms/COTRN01.bms` | Transaction detail screen | Transaction detail page |
| `CORPT00.bms` | `app/bms/CORPT00.bms` | Report screen | Report generation page |

---

## CICS Call Summary

| Program | CICS Calls | Wave |
|---|---|---|
| `COACTVWC` | ~29 | 3.1 |
| `COCRDLIC` | ~34 | 3.2 |
| `COCRDSLC` | ~26 | 3.2 |
| `COTRN00C` | ~19 | 3.3 |
| `COTRN01C` | ~10 | 3.3 |
| `CORPT00C` | ~14 | 3.4 |
| **Total** | **~132** | |

---

## Dependencies
- **Phase 0:** Database schema (all tables), test framework
- **Phase 1:** JPA entities (`Account`, `Card`, `Customer`, `Transaction`)
- **Phase 2:** Authentication (all endpoints require login)

## Next Phase
Proceed to [Phase 4: Write Operations](phase-4-write-operations.md) once all Wave 3.x deliverables are accepted.
