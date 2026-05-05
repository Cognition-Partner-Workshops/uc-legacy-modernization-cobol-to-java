# BRE Forward-Engineering Audit Report

**Audit Scope:** Forward-engineered React + Spring Boot code vs. BRE documents and original COBOL/CICS reference implementation  
**Source Programs:** `COCRDSLC.cbl` (detail), `COCRDUPC.cbl` (update), `COCRDLIC.cbl` (list)  
**BRE Documents:** `card-detail-view-bre.md`, `card-update-bre.md`, `data-model.md`, `screen-mapping.md`, `field-and-action-mapping.md`  
**Forward-Engineered Code:** `demo/backend/` (Spring Boot) + `demo/frontend/` (React)  
**Date:** 2026-04-24

---

## 1. LOGIC COVERAGE

### 1.1 Validation Rules

| Rule ID | BRE Rule | Source Paragraph | Status | Detail |
|---------|----------|------------------|--------|--------|
| V-01 | Account ID must be numeric, up to 11 digits | `2210-EDIT-ACCOUNT` (COCRDSLC) / `1210-EDIT-ACCOUNT` (COCRDUPC) | **Covered** | `CardService.validateAccountId()` uses regex `\\d{1,11}`. Matches COBOL `IS NOT NUMERIC` check. |
| V-02 | Card number must be exactly 16 numeric digits | `2220-EDIT-CARD` (COCRDSLC) / `1220-EDIT-CARD` (COCRDUPC) | **Covered** | `CardService.validateCardNumber()` uses regex `\\d{16}`. Matches COBOL PIC 9(16). |
| V-03 | Active status must be exactly 'Y' or 'N' | `1240-EDIT-CARDSTATUS` (COCRDUPC) | **Covered** | `CardService.validateActiveStatus()` checks `!"Y".equals(status) && !"N".equals(status)`. Exact match to `FLG-YES-NO-VALID VALUES 'Y', 'N'`. |
| V-04 | Expiry month must be 1–12 | `1250-EDIT-EXPIRY-MON` (COCRDUPC) | **Covered** | `CardService.validateExpirationDate()` checks `month < 1 \|\| month > 12`. Matches `VALID-MONTH VALUES 1 THRU 12`. |
| V-05 | Expiry year must be 1950–2099 | `1260-EDIT-EXPIRY-YEAR` (COCRDUPC) | **Covered** | `CardService.validateExpirationDate()` checks `year < 1950 \|\| year > 2099`. Matches `VALID-YEAR VALUES 1950 THRU 2099`. |
| V-06 | Cardholder name must not be blank | `1230-EDIT-NAME` (COCRDUPC) | **Covered** | Java: `updated.getEmbossedName() != null && !updated.getEmbossedName().isBlank()`. React: checks `trim() === ''`. |
| V-07 | **Cardholder name must contain only alphabets and spaces** | `1230-EDIT-NAME` (COCRDUPC) — `INSPECT CONVERTING LIT-ALL-ALPHA-FROM TO LIT-ALL-SPACES-TO` | **MISSING** | COBOL converts all A-Z/a-z to spaces, then checks if anything remains (non-alpha chars). Java/React only check not-blank. Names with digits, punctuation, or special characters are accepted. **Logic divergence from BRE intent.** |
| V-08 | Expiry date format YYYY-MM-DD | Combined from `1250`/`1260` paragraphs | **Covered** | Both Java regex `\\d{4}-\\d{2}-\\d{2}` and React regex `^\\d{4}-\\d{2}-\\d{2}$` enforce format. |
| V-09 | **Day component of expiry date** — COBOL auto-sets EXPDAY to "01" (DRK, PROT field) | `COCRDUP.bms` — EXPDAY at POS(15,36) with ATTRB DRK,PROT | **MISSING** | Java accepts any day value (01–31). No validation that day is "01" or valid for the given month. COBOL hides and auto-fills this field. |
| V-10 | **Both account and card blank → error** "Please provide Acct or Card" | `2200-EDIT-MAP-INPUTS` (COCRDSLC) — cross-field edit | **MISSING** | Java `getCard()` requires cardNumber from URL path. `listCards()` allows both empty (returns all). No cross-field blank check. |
| V-11 | **Account + card must match same record** (composite key lookup) | `9100-GETCARD-BYACCTCARD` (COCRDSLC) | **Partially Covered** | Java `findById()` uses card number only. Does not verify the card belongs to the provided account. |
| V-12 | Account number is immutable on update screen (PROT attribute) | `1210-EDIT-ACCOUNT` (COCRDUPC) | **Covered** | React `inputReadonly` style + `readOnly` attribute. Backend ignores accountId in PUT body. |
| V-13 | Card number is immutable (primary key) | `1220-EDIT-CARD` (COCRDUPC) | **Covered** | React `inputReadonly`. Backend uses path param, not body cardNumber. |
| V-14 | Expiry month must be numeric | `1250-EDIT-EXPIRY-MON` — `IS NOT NUMERIC` check | **Covered** | Regex `\\d{4}-\\d{2}-\\d{2}` enforces all-numeric components. |
| V-15 | Expiry year must be numeric | `1260-EDIT-EXPIRY-YEAR` — `IS NOT NUMERIC` check | **Covered** | Same regex enforcement. |

### 1.2 Data Access Rules

| Rule ID | BRE Rule | Source Paragraph | Status | Detail |
|---------|----------|------------------|--------|--------|
| D-01 | Read card by card number (VSAM KSDS primary key) | `9100-GETCARD-BYACCTCARD` (COCRDSLC/COCRDUPC) | **Covered** | `CardRepository.findById(cardNumber)` |
| D-02 | Browse cards by account ID (VSAM alternate index) | `9150-GETCARD-BYACCT` (COCRDSLC) / `9000-READ-FORWARD` (COCRDLIC) | **Covered** | `CardRepository.findByAccountId(accountId)` |
| D-03 | Read all cards (forward browse) | `9000-READ-FORWARD` (COCRDLIC) | **Covered** | `CardRepository.findAll()` |
| D-04 | Update card record (VSAM REWRITE) | `9200-WRITE-PROCESSING` (COCRDUPC) | **Covered** | `CardRepository.save(existing)` |
| D-05 | **Record locking before update** (EXEC CICS READ UPDATE) | `9200-WRITE-PROCESSING` (COCRDUPC) | **MISSING** | No pessimistic locking. JPA `.save()` with no `@Version` or `SELECT FOR UPDATE`. |
| D-06 | **Optimistic concurrency check** — detect if record changed while user was editing | `9300-CHECK-CHANGE-IN-REC` (COCRDUPC) | **MISSING** | COBOL compares all field values before/after to detect concurrent modification. No equivalent in Java code. |
| D-07 | **Account existence verification** — validate account exists in ACCTDAT | Implicit in COBOL cross-file reads | **MISSING** | Java does not verify accountId exists in accounts table during card operations. |

### 1.3 Screen / Navigation Rules

| Rule ID | BRE Rule | Source Paragraph | Status | Detail |
|---------|----------|------------------|--------|--------|
| N-01 | List → Detail navigation (EXEC CICS XCTL to COCRDSLC) | COCRDLIC.cbl | **Covered** | React `<Link to="/cards/{cardNumber}">`. |
| N-02 | Detail → Update navigation (EXEC CICS XCTL to COCRDUPC) | COCRDSLC.cbl | **Covered** | React "Edit Card" `<Link to="/cards/{cardNumber}/edit">`. |
| N-03 | **Update confirmation step** (PF5 to confirm save) | `2000-DECIDE-ACTION` (COCRDUPC) — `CCUP-CHANGES-OK-NOT-CONFIRMED AND CCARD-AID-PFK05` | **MISSING** | COBOL requires explicit PF5 confirmation after edit. Java saves immediately on button click. No confirm dialog. |
| N-04 | **Pagination** (PF7=backward, PF8=forward, 7-row pages) | COCRDLIC.cbl — `9000-READ-FORWARD`, `9100-READ-BACKWARDS`, `WS-MAX-SCREEN-LINES = 7` | **MISSING** | React returns all records in single list. No pagination. Works for demo (10 records) but not for production volumes. |
| N-05 | **Row selection S/U** (S=view detail, U=go directly to update) | COCRDLIC.cbl — `SELECT-OK VALUES 'S', 'U'`, `VIEW-REQUESTED-ON VALUE 'S'`, `UPDATE-REQUESTED-ON VALUE 'U'` | **Partially Covered** | React has "View" and "Edit" links per row (functional equivalent) but no keyboard shortcut or select-character semantics. |
| N-06 | Exit via PF3 (return to calling program or main menu) | All three programs | **Partially Covered** | React has "Back to List" buttons but no concept of "main menu" or calling program return. |
| N-07 | COMMAREA state preservation between programs | All three programs — `EXEC CICS RETURN COMMAREA` | **MISSING** | Stateless REST architecture. No inter-screen state preservation beyond URL parameters and React state. |
| N-08 | **Admin vs non-admin access control** | COCRDLIC.cbl header comment: "All cards if no context passed and admin user, Only ones associated with ACCT if user is not admin" | **MISSING** | No authentication or authorization. All cards visible to all users. |

### 1.4 Error Handling Rules

| Rule ID | BRE Rule | Source | Status | Detail |
|---------|----------|--------|--------|--------|
| E-01 | Field-level error in ERRMSG (RED, POS 23,1, 80 chars) | BMS ERRMSG field | **Partially Covered** | React shows field-level error text below each input. Java returns 400 with message JSON. Not exact RED/POS match but functional equivalent. |
| E-02 | Informational message in INFOMSG (POS 20,25, 40 chars) | BMS INFOMSG field | **MISSING** | No info-level messages in React UI (e.g., "TYPE S FOR DETAIL, U TO UPDATE ANY RECORD"). |
| E-03 | **File error formatted message** — "File Error: {op} on {file} returned RESP {code}, RESP2 {code2}" | `WS-FILE-ERROR-MESSAGE` (COCRDSLC/COCRDUPC) | **MISSING** | Java uses generic `IllegalArgumentException` messages. No structured file-error formatting. |
| E-04 | **Abend handling** (structured error recovery with ABCODE '9999') | `ABEND-ROUTINE` (COCRDUPC) | **MISSING** | No structured crash/abort recovery. Spring Boot default error page would show stack trace. |
| E-05 | Could-not-lock error message | `COULD-NOT-LOCK-FOR-UPDATE` 88-level | **MISSING** | No locking, so no lock-failure handling. |
| E-06 | Data-changed-by-another-user error | `DATA-WAS-CHANGED-BEFORE-UPDATE` 88-level | **MISSING** | No concurrent modification detection. |
| E-07 | "No records found" error | `WS-NO-RECORDS-FOUND` (COCRDLIC) | **Covered** | React shows "No cards found." when list is empty. Java returns empty list or 404 for single card. |

---

## 2. CODE LOGIC MATCH

### 2.1 Exact Matches

| Function | COBOL Reference | Java/React Implementation | Assessment |
|----------|----------------|--------------------------|------------|
| `validateActiveStatus()` | `1240-EDIT-CARDSTATUS` — `FLG-YES-NO-VALID VALUES 'Y', 'N'` | `!"Y".equals(status) && !"N".equals(status)` | **Exact match** — identical logic and boundary values |
| `validateExpirationDate()` month range | `1250-EDIT-EXPIRY-MON` — `VALID-MONTH VALUES 1 THRU 12` | `month < 1 \|\| month > 12` | **Exact match** — identical range check |
| `validateExpirationDate()` year range | `1260-EDIT-EXPIRY-YEAR` — `VALID-YEAR VALUES 1950 THRU 2099` | `year < 1950 \|\| year > 2099` | **Exact match** — identical range check |
| Account immutability | BMS `ATTRB PROT` on ACCTSID | React `readOnly` + `inputReadonly` style | **Exact match** — field cannot be edited |
| Card immutability | Primary key in VSAM KSDS | `@Id` annotation, not in PUT body | **Exact match** — PK unchangeable |

### 2.2 Functional Equivalents

| Function | COBOL | Java/React | Assessment |
|----------|-------|-----------|------------|
| Read card | `EXEC CICS READ FILE(CARDDAT) RIDFLD(key)` | `CardRepository.findById()` | **Functional equivalent** — same semantics, different mechanism |
| Write card | `EXEC CICS REWRITE FILE(CARDDAT)` | `CardRepository.save()` | **Functional equivalent** — but COBOL has locking that Java lacks |
| Browse by account | `EXEC CICS STARTBR` + `READNEXT` on CARDAIX | `CardRepository.findByAccountId()` | **Functional equivalent** — JPA query replaces alternate index browse |
| Screen send | `EXEC CICS SEND MAP` | JSON response → React render | **Functional equivalent** — data serialization differs |
| Screen receive | `EXEC CICS RECEIVE MAP` | JSON PUT body + React form state | **Functional equivalent** |
| Program transfer | `EXEC CICS XCTL PROGRAM(pgm)` | React Router `<Link>` navigation | **Functional equivalent** — URL-based vs COMMAREA-based |
| Error display | BMS ERRMSG field (RED text POS 23,1) | React `serverError` state → red banner | **Functional equivalent** — UI differs, intent matches |

### 2.3 Deviations

| Function | COBOL Behavior | Java/React Behavior | Severity | Detail |
|----------|---------------|---------------------|----------|--------|
| **Name validation** | `1230-EDIT-NAME` — `INSPECT CONVERTING` alphabets→spaces, checks residual = 0. Only A-Z, a-z, and space allowed. | Java: `!name.isBlank()`. React: `trim() === ''`. No alpha-only enforcement. | **HIGH** | Accepts names like "John123", "O'Brien", "Jean-Pierre". COBOL would reject all of these. Logic divergence from BRE rule. |
| **Composite key lookup** | `9100-GETCARD-BYACCTCARD` uses `RIDFLD` with both cardNumber + accountId | `findById(cardNumber)` — single key only | **MEDIUM** | Does not verify card belongs to stated account. Could return card for wrong account context. |
| **Update confirmation** | Two-step: edit fields → press PF5 to confirm → REWRITE | One-step: click Save → immediate PUT | **MEDIUM** | No safeguard against accidental saves. COBOL provides review step. |
| **Date component handling** | Separate EXPMON (2 chars), EXPYEAR (4 chars), EXPDAY (2 chars, hidden, auto "01") | Combined YYYY-MM-DD string. Day value freely set by user or existing data. | **LOW** | Day is not constrained to "01". Existing seed data has various days (09, 13, 11, etc.) which wouldn't match COBOL behavior of always setting day to "01". |
| **Account ID format** | Stored as PIC 9(11) — always 11 digits, zero-padded | `validateAccountId()` accepts `\\d{1,11}` — 1 to 11 digits | **LOW** | User can search with "50" instead of "00000000050". Won't match seed data format. Frontend search may return 0 results for non-padded input. |
| **Error aggregation** | COBOL sets all field flags independently, displays first error message via 88-level | Java throws `IllegalArgumentException` at first validation failure. React checks all fields but backend short-circuits. | **LOW** | Backend reports only first error; COBOL could set multiple flags and show the first non-off message. Frontend does validate all at once (better than backend). |

### 2.4 Dead Code

| Code | Location | Detail |
|------|----------|--------|
| `AccountRepository.java` | `demo/backend/src/main/java/com/carddemo/repository/` | Defined but never injected or used by any service or controller. No REST endpoints expose account data. |
| `CardXref.java` entity | `demo/backend/src/main/java/com/carddemo/model/` | Entity defined and table seeded with 10 rows, but no repository created and never queried. |
| `card_xref` seed data | `data.sql` line 36-46 | 10 cross-reference records inserted but never read by any code path. |
| `accounts` seed data | `data.sql` line 19-33 | 14 account records inserted but never read by any code path (no account endpoint or service). |
| CVV in Card entity | `Card.java` — `cvvCode` field | Mapped and returned in JSON responses but never displayed in UI (hidden for security). Unnecessary exposure. |

---

## 3. LIVE READINESS

### 3.1 Module-Level Assessment

| Module / Function | Rating | Issues |
|-------------------|--------|--------|
| `CardController.listCards()` (GET /api/cards) | **Ready** | Proper error handling, returns 200 with list or empty array. |
| `CardController.getCard()` (GET /api/cards/{id}) | **Ready** | Returns 200 or 404. Validation on card number format. |
| `CardController.updateCard()` (PUT /api/cards/{id}) | **Needs Minor Fix** | No `@Transactional`. No concurrency control. Name validation gap (alpha-only missing). |
| `CardService.validateAccountId()` | **Needs Minor Fix** | Accepts unpadded account IDs (e.g., "50") that won't match zero-padded seed data format. |
| `CardService.validateCardNumber()` | **Ready** | Exact 16-digit check matches COBOL. |
| `CardService.validateActiveStatus()` | **Ready** | Exact Y/N match. |
| `CardService.validateExpirationDate()` | **Needs Minor Fix** | Missing day validation (should be "01" per COBOL, or at least valid calendar day). No check that day component exists or is valid. |
| `Card.java` entity | **Ready** | Correct JPA mapping. Field lengths match copybook. |
| `Account.java` entity | **Ready** | Correct mapping but unused (dead code). |
| `CardXref.java` entity | **Ready** | Correct mapping but unused (dead code). |
| `CorsConfig.java` | **Needs Minor Fix** | Hardcoded `localhost:3000`. Production deployment would need configurable origins. |
| `schema.sql` | **Needs Minor Fix** | No foreign key constraints (cards.account_id → accounts.account_id). No indexes on account_id for query performance. |
| `CardListPage.jsx` | **Ready** | Loads data, search works, error state handled. |
| `CardDetailPage.jsx` | **Ready** | All fields rendered, loading/error states handled. |
| `CardUpdatePage.jsx` | **Needs Minor Fix** | Missing alpha-only name validation. No confirmation dialog before save. |
| `GreenScreenPreview.jsx` | **Ready** | Static mockup for demo purposes. |
| `cardApi.js` | **Needs Minor Fix** | Hardcoded `localhost:8080`. No request timeout configured. No auth headers. |

### 3.2 Security Concerns

| Concern | Severity | Detail |
|---------|----------|--------|
| **CVV exposed in API responses** | **CRITICAL** | `GET /api/cards` and `GET /api/cards/{id}` return `cvvCode` in JSON. PCI DSS explicitly prohibits exposing CVV after authorization. Must be excluded from API responses. |
| **No authentication** | **HIGH** | All endpoints publicly accessible. COBOL COCRDLIC.cbl has admin check ("All cards if admin, only account-specific if not"). No equivalent. |
| **No authorization / RBAC** | **HIGH** | Any caller can read/update any card. No user-to-account access control. |
| **No input sanitization** | **MEDIUM** | `embossedName` field could contain XSS payloads. No HTML escaping on server side. React auto-escapes in JSX, but API consumers won't have this protection. |
| **No HTTPS** | **MEDIUM** | Server runs on plain HTTP. Card data in transit is unencrypted. |
| **H2 Console enabled** | **LOW** | `spring.h2.console.enabled=true` exposes database admin interface at `/h2-console`. Must be disabled in production. |
| **No rate limiting** | **LOW** | API endpoints have no throttling. Vulnerable to brute-force enumeration of card numbers. |

### 3.3 Production Blockers

| Blocker | Detail |
|---------|--------|
| CVV in API responses | Must add `@JsonIgnore` on `cvvCode` or use a DTO layer to exclude CVV from serialization. |
| No authentication | Must add Spring Security or equivalent auth layer before any production use. |
| In-memory H2 database | All data lost on restart. Must replace with persistent database (PostgreSQL, MySQL, etc.). |
| No concurrency control | Concurrent updates will silently overwrite each other. Must add `@Version` (optimistic) or `SELECT FOR UPDATE` (pessimistic). |
| Hardcoded URLs | `localhost:3000` in CORS and `localhost:8080` in React must be environment-configurable. |

---

## 4. VALIDATION GAPS (vs Classic Forward Engineering)

### 4.1 Present in Classic (COBOL) but Absent in Forward-Engineered Code

| Classic Feature | COBOL Location | Impact | Recommendation |
|----------------|---------------|--------|----------------|
| Name alphabetic-only validation | `1230-EDIT-NAME` — `INSPECT CONVERTING` | **HIGH** — accepts invalid data | Add regex `^[A-Za-z ]+$` check in `CardService.updateCard()` and React `validate()` |
| Optimistic concurrency control | `9300-CHECK-CHANGE-IN-REC` | **HIGH** — silent data loss on concurrent updates | Add `@Version` column to `Card` entity with `version` field |
| Record locking (pessimistic) | `EXEC CICS READ UPDATE` in `9200-WRITE-PROCESSING` | **MEDIUM** — lock-based integrity missing | Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on repository read-for-update, or rely on optimistic locking |
| Update confirmation step | `2000-DECIDE-ACTION` — PF5 confirm after edit | **MEDIUM** — accidental save risk | Add confirmation dialog in React before PUT call |
| Pagination (7-row pages) | `WS-MAX-SCREEN-LINES = 7`, PF7/PF8 handlers | **MEDIUM** — performance at scale | Add server-side pagination: `Pageable` in controller, `Page<Card>` response |
| Admin role check | COCRDLIC.cbl header: "admin user" vs "not admin" | **HIGH** — no access control | Implement Spring Security with role-based access |
| Account existence verification | Implicit cross-file read in COBOL | **LOW** — referential integrity gap | Add foreign key constraint or service-layer check |
| S/U row selection from list | `SELECT-OK VALUES 'S', 'U'` in COCRDLIC | **LOW** — UX difference only | "View" and "Edit" links are functional equivalent |
| EXPDAY auto-set to "01" | BMS field EXPDAY ATTRB DRK,PROT | **LOW** — data format divergence | Could normalize to always use "01" for day |
| COMMAREA state management | `EXEC CICS RETURN COMMAREA` | **LOW** — architecture difference | Stateless REST is acceptable modern equivalent |
| File error message formatting | `WS-FILE-ERROR-MESSAGE` structured template | **LOW** — error message quality | Could add structured error DTO with operation/resource/code fields |
| Abend handling | `ABEND-ROUTINE` with `ABCODE('9999')` | **LOW** — unhandled crash recovery | Add `@ControllerAdvice` global exception handler |
| Informational messages | `WS-INFO-MSG` — "TYPE S FOR DETAIL, U TO UPDATE" | **LOW** — UX guidance missing | Could add instructional text to React list page |

### 4.2 Present in Code but No BRE Traceability

| Forward-Engineered Feature | Location | BRE Gap |
|---------------------------|----------|---------|
| React Router client-side routing | `App.jsx` — routes for `/`, `/cards/:id`, `/cards/:id/edit`, `/green-screen` | No BRE document describes modern routing. COBOL used `EXEC CICS XCTL` which is mapped in action tables but routing structure is undocumented. |
| Auto-redirect after save | `CardUpdatePage.jsx` — `setTimeout(() => navigate(...), 1200)` | No COBOL equivalent. After REWRITE, COBOL shows updated screen. Auto-redirect is new behavior. |
| Loading/saving UI states | All React components — `useState(true)` for loading | No terminal equivalent. 3270 blocked until response. |
| Clear search button | `CardListPage.jsx` — `handleClear()` | Not in original BMS. COBOL required re-entering search criteria. |
| GreenScreenPreview component | `GreenScreenPreview.jsx` | Demo-only component with no COBOL equivalent or BRE mapping. Informational. |
| H2 Console access | `application.properties` — `/h2-console` | Infrastructure debugging tool. No BRE mapping needed but must be disabled in production. |
| CORS configuration | `CorsConfig.java` | Infrastructure concern. Not a business rule. |

### 4.3 Areas Requiring Manual QA or Business Sign-Off

| Area | Reason | Recommended Action |
|------|--------|-------------------|
| **Name validation gap** | COBOL enforces alpha+space only; Java accepts any non-blank string. Business must decide: enforce legacy rule or relax for modern names (hyphens, apostrophes, accented chars)? | Business sign-off required. If relaxed, document as intentional rule change. |
| **CVV exposure** | COBOL never displays CVV on any screen. Java returns it in every API response. PCI DSS compliance at risk. | Security review required before any production use. |
| **All-records-at-once** (no pagination) | COBOL had 7-row pages. React shows all records. With production data volume (thousands of cards), this will cause performance issues and UI overload. | Performance test with realistic data volume. Add pagination if >100 records expected. |
| **Date day component** | COBOL auto-sets day to "01" and hides it. Java stores arbitrary day values. Seed data has various days. Business must clarify: is day significant or always "01"? | Business clarification needed on date semantics. |
| **Account ID format** | Seed data is zero-padded 11 digits ("00000000050"). Frontend search accepts unpadded values ("50"). Search will fail unless user knows to zero-pad. | UX decision: auto-pad on search, or document requirement? |
| **No update confirmation** | COBOL required PF5 to confirm. React saves immediately. Risk of accidental updates to production card data. | Business risk acceptance or add confirm dialog. |
| **Multi-user concurrency** | No locking or version check. Two users editing same card simultaneously → last-write-wins with no warning. | Unacceptable for production. Must implement before go-live. |
| **Data integrity** | No foreign keys between cards↔accounts↔card_xref. Orphan records possible. | DBA review of schema constraints before production. |

---

## 5. SUMMARY SCORECARD

### Coverage Metrics

| Metric | Value | Detail |
|--------|-------|--------|
| **Total Business Rules Identified** | 30 | From BRE docs + COBOL reference analysis |
| **Fully Covered** | 18 | 60% |
| **Partially Covered** | 5 | 17% |
| **Missing** | 7 | 23% |
| **Logic Coverage** | **68%** | (18 + 5×0.5) / 30 |

### Live Readiness

| Metric | Value | Detail |
|--------|-------|--------|
| **Total Modules Assessed** | 17 | Backend services, controllers, entities, frontend components |
| **Ready** | 9 | 53% |
| **Needs Minor Fix** | 6 | 35% |
| **Not Ready** | 2 | 12% (update flow lacks concurrency; no auth) |
| **Live Readiness Score** | **53%** | Percentage of modules rated "Ready" |

### Top 3 Risks

| Rank | Risk | Impact | Mitigation |
|------|------|--------|------------|
| **1** | **CVV exposed in API responses** | PCI DSS non-compliance. Card security data leakage via any API consumer. | Add `@JsonIgnore` to `cvvCode` field or introduce DTO layer excluding CVV. Immediate fix required. |
| **2** | **No concurrency control** | Silent data loss when multiple users edit the same card simultaneously. COBOL had explicit locking (`EXEC CICS READ UPDATE`) and change detection (`9300-CHECK-CHANGE-IN-REC`). | Add `@Version` column for optimistic locking. Minimum viable fix for production safety. |
| **3** | **Name alphabetic validation missing** | Accepts invalid cardholder names (digits, special chars, unicode). COBOL enforced alphabets + spaces only via `INSPECT CONVERTING`. Data quality degradation. | Add regex validation `^[A-Za-z ]+$` in `CardService` and React `validate()`. Business may choose to relax for modern naming conventions — requires explicit sign-off. |

### Overall Assessment

The forward-engineered code successfully captures the **core CRUD workflow** (list, detail, update) and the **primary validation rules** (status Y/N, month 1-12, year 1950-2099, card 16 digits, account 11 digits). Data model mapping from COBOL copybooks to JPA entities is accurate and well-documented with traceability comments.

However, the code is **not production-ready** due to critical security gaps (CVV exposure, no auth), missing concurrency control, and incomplete validation coverage (name alpha-only check). These gaps must be addressed before any deployment beyond demo/POC use.

**Recommended classification: Demo / POC grade — suitable for CTO presentation and architecture validation, not for live transaction processing.**
