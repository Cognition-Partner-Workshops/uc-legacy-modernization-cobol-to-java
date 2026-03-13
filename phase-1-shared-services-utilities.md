# Phase 1: Shared Services & Utilities (Low Risk)

**Risk Level:** Low  
**Objective:** Convert standalone utilities and copybook data structures that have no CICS or VSAM dependencies. These become the foundation that all subsequent phases build upon.

---

## Wave 1.1: Convert Copybooks to Java POJOs / JPA Entities

### Objective
Transform all 14 core copybooks in `app/cpy/` into Java record classes, POJOs, or JPA entity classes that will be used throughout the application.

### Copybook-to-Java Mapping

| Copybook | Source Path | Java Target | Description |
|---|---|---|---|
| `COCOM01Y.cpy` | `app/cpy/COCOM01Y.cpy` | `CommonArea.java` | Common communication area (COMMAREA) shared across programs |
| `CVEXPORT.cpy` | `app/cpy/CVEXPORT.cpy` | `ExportRecord.java` | Multi-record export layout used by CBEXPORT/CBIMPORT |
| `CVCRD01Y.cpy` | `app/cpy/CVCRD01Y.cpy` | `Card.java` (JPA Entity) | Card record — maps to `cards` table |
| `CVACT01Y.cpy` | `app/cpy/CVACT01Y.cpy` | `Account.java` (JPA Entity) | Account record — maps to `accounts` table |
| `CVACT02Y.cpy` | `app/cpy/CVACT02Y.cpy` | `AccountDetail.java` | Account detail fields |
| `CVACT03Y.cpy` | `app/cpy/CVACT03Y.cpy` | `AccountSummary.java` | Account summary fields |
| `CVCUS01Y.cpy` | `app/cpy/CVCUS01Y.cpy` | `Customer.java` (JPA Entity) | Customer record — maps to `customers` table |
| `CVTRA05Y.cpy` | `app/cpy/CVTRA05Y.cpy` | `Transaction.java` (JPA Entity) | Transaction record — maps to `transactions` table |
| `CSUSR01Y.cpy` | `app/cpy/CSUSR01Y.cpy` | `User.java` (JPA Entity) | User security record — maps to `users` table |
| `CSDAT01Y.cpy` | `app/cpy/CSDAT01Y.cpy` | `DateWorkArea.java` | Date conversion work area |
| `COADM02Y.cpy` | `app/cpy/COADM02Y.cpy` | `AdminData.java` | Admin screen data area |
| `COMEN02Y.cpy` | `app/cpy/COMEN02Y.cpy` | `MenuData.java` | Main menu data area |
| `COTTL01Y.cpy` | `app/cpy/COTTL01Y.cpy` | `TitleData.java` | Title/header data |
| `CSMSG01Y.cpy` | `app/cpy/CSMSG01Y.cpy` | `MessageArea.java` | Message handling area |
| `CSMSG02Y.cpy` | `app/cpy/CSMSG02Y.cpy` | `ErrorMessage.java` | Error message area |
| `CSSETATY.cpy` | `app/cpy/CSSETATY.cpy` | `ScreenAttribute.java` | Screen attribute settings |
| `CSSTRPFY.cpy` | `app/cpy/CSSTRPFY.cpy` | `StringPrefix.java` | String prefix utility |
| `CSUTLDPY.cpy` | `app/cpy/CSUTLDPY.cpy` | `DateUtilParams.java` | Date utility parameters |
| `CSUTLDWY.cpy` | `app/cpy/CSUTLDWY.cpy` | `DateUtilWorkArea.java` | Date utility work area |
| `CSLKPCDY.cpy` | `app/cpy/CSLKPCDY.cpy` | `LookupCode.java` | Lookup code definitions |
| `CODATECN.cpy` | `app/cpy/CODATECN.cpy` | `DateCondition.java` | Date condition names |
| `COSTM01.CPY` | `app/cpy/COSTM01.CPY` | `StatementRecord.java` | Statement record layout |
| `CUSTREC.cpy` | `app/cpy/CUSTREC.cpy` | `CustomerRecord.java` | Alternate customer record |
| `CVTRA01Y.cpy` | `app/cpy/CVTRA01Y.cpy` | `TransactionRecord.java` | Transaction record (variant 1) |
| `CVTRA02Y.cpy` | `app/cpy/CVTRA02Y.cpy` | `TransactionDetail.java` | Transaction detail |
| `CVTRA03Y.cpy` | `app/cpy/CVTRA03Y.cpy` | `TransactionSummary.java` | Transaction summary |
| `CVTRA04Y.cpy` | `app/cpy/CVTRA04Y.cpy` | `TransactionType.java` (JPA Entity) | Transaction type — maps to `transaction_types` table |
| `CVTRA06Y.cpy` | `app/cpy/CVTRA06Y.cpy` | `TransactionCategory.java` | Transaction category |
| `CVTRA07Y.cpy` | `app/cpy/CVTRA07Y.cpy` | `TransactionBalance.java` | Transaction balance |
| `UNUSED1Y.cpy` | `app/cpy/UNUSED1Y.cpy` | _(skip)_ | Unused — do not convert |

### Conversion Guidelines

1. **COBOL `PIC X(n)`** -> `String` (with `@Column(length = n)` for JPA entities)
2. **COBOL `PIC 9(n)`** -> `int` or `long` depending on size
3. **COBOL `PIC 9(n)V9(m)`** -> `BigDecimal` with `@Column(precision = n+m, scale = m)`
4. **COBOL `PIC S9(n) COMP-3`** -> `BigDecimal` (packed decimal)
5. **COBOL group items** -> Nested Java objects or `@Embeddable` classes
6. **COBOL `REDEFINES`** -> Java inheritance or composition pattern
7. **COBOL `88-level` condition names** -> Java `enum` or boolean helper methods

### Acceptance Criteria
- [ ] All 28 copybooks converted (excluding `UNUSED1Y.cpy`)
- [ ] JPA entities annotated with proper `@Entity`, `@Table`, `@Column` annotations
- [ ] Unit tests validate field mappings against copybook layouts
- [ ] No CICS or VSAM dependencies in any converted class

---

## Wave 1.2: Convert Utility Programs

### Objective
Convert standalone utility programs that have no CICS transaction or VSAM file dependencies.

### Programs to Convert

#### 1. CSUTLDTC — Date Conversion Service

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/CSUTLDTC.cbl` |
| **Java Target** | `DateConversionService.java` |
| **Related Copybooks** | `CSUTLDPY.cpy`, `CSUTLDWY.cpy`, `CSDAT01Y.cpy`, `CODATECN.cpy` |
| **Key Function** | Converts dates between formats: YYYYMMDD, MMDDYYYY, Julian, etc. |
| **Java Replacement** | Use `java.time.LocalDate`, `DateTimeFormatter`, and `java.time.temporal.JulianFields` |

**Conversion notes:**
- Replace all COBOL date manipulation with `java.time` API
- Map COBOL date condition names (`CODATECN.cpy`) to enum constants
- Preserve all date format conversion paths for backward compatibility

#### 2. COBSWAIT — Wait/Sleep Utility

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COBSWAIT.cbl` |
| **Java Target** | **Remove** — not needed in Java |
| **Related ASM** | `app/asm/MVSWAIT.asm` |
| **Key Function** | Pauses execution for a specified interval |
| **Java Replacement** | `Thread.sleep()` or Spring `@Scheduled` / `TaskScheduler` |

**Conversion notes:**
- This program exists because COBOL has no native sleep mechanism
- In Java, use `Thread.sleep(millis)` for simple waits or `ScheduledExecutorService` for timed operations
- No dedicated service class needed; inline the replacement where callers exist

#### 3. COBDATFT.asm — Assembler Date Formatting

| Attribute | Value |
|---|---|
| **Source** | `app/asm/COBDATFT.asm` |
| **Java Target** | **Remove** — covered by `java.time` |
| **Key Function** | Low-level date formatting at the assembler level |
| **Java Replacement** | `java.time.format.DateTimeFormatter` |

**Conversion notes:**
- This assembler routine provides date formatting that COBOL calls via `CALL`
- In Java, `DateTimeFormatter` handles all date formatting natively
- Ensure all date format patterns used in the assembler are replicated in the `DateConversionService`

### Acceptance Criteria
- [ ] `DateConversionService` passes all date conversion test cases
- [ ] All date formats from `CODATECN.cpy` are supported
- [ ] `COBSWAIT` and `COBDATFT.asm` are documented as removed with Java replacements noted
- [ ] No assembler or COBOL runtime dependencies remain

---

## Programs in Scope

| Program | Source Path | Action | Java Target |
|---|---|---|---|
| `CSUTLDTC.cbl` | `app/cbl/CSUTLDTC.cbl` | Convert | `DateConversionService.java` |
| `COBSWAIT.cbl` | `app/cbl/COBSWAIT.cbl` | Remove | `Thread.sleep()` / Scheduler |
| `COBDATFT.asm` | `app/asm/COBDATFT.asm` | Remove | `java.time.DateTimeFormatter` |
| `MVSWAIT.asm` | `app/asm/MVSWAIT.asm` | Remove | Not needed in Java |

---

## Dependencies
- **Phase 0** must be complete (project structure, database schema, test framework)

## Next Phase
Proceed to [Phase 2: Authentication & User Management](phase-2-authentication-user-management.md) once all Wave 1.x deliverables are accepted.
