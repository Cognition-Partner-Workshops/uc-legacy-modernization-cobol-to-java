# Data Model Mapping: COBOL Copybooks → Java Entities

## CVACT02Y.cpy → Card.java

| COBOL Field | PIC | Java Field | Java Type | DB Column |
|-------------|-----|------------|-----------|-----------|
| `CARD-NUM` | X(16) | `cardNumber` | String | `card_number VARCHAR(16) PK` |
| `CARD-ACCT-ID` | 9(11) | `accountId` | String | `account_id VARCHAR(11)` |
| `CARD-CVV-CD` | 9(03) | `cvvCode` | String | `cvv_code VARCHAR(3)` |
| `CARD-EMBOSSED-NAME` | X(50) | `embossedName` | String | `embossed_name VARCHAR(50)` |
| `CARD-EXPIRAION-DATE` | X(10) | `expirationDate` | String | `expiration_date VARCHAR(10)` |
| `CARD-ACTIVE-STATUS` | X(01) | `activeStatus` | String | `active_status VARCHAR(1)` |
| `FILLER` | X(59) | — | — | — (padding, not mapped) |

**Record Length**: 150 bytes (COBOL) → variable (JPA)

## CVACT01Y.cpy → Account.java

| COBOL Field | PIC | Java Field | Java Type | DB Column |
|-------------|-----|------------|-----------|-----------|
| `ACCT-ID` | 9(11) | `accountId` | String | `account_id VARCHAR(11) PK` |
| `ACCT-ACTIVE-STATUS` | X(01) | `activeStatus` | String | `active_status VARCHAR(1)` |
| `ACCT-CURR-BAL` | S9(10)V99 | `currentBalance` | BigDecimal | `current_balance DECIMAL(12,2)` |
| `ACCT-CREDIT-LIMIT` | S9(10)V99 | `creditLimit` | BigDecimal | `credit_limit DECIMAL(12,2)` |
| `ACCT-CASH-CREDIT-LIMIT` | S9(10)V99 | `cashCreditLimit` | BigDecimal | `cash_credit_limit DECIMAL(12,2)` |
| `ACCT-OPEN-DATE` | X(10) | `openDate` | String | `open_date VARCHAR(10)` |
| `ACCT-EXPIRAION-DATE` | X(10) | `expirationDate` | String | `expiration_date VARCHAR(10)` |
| `ACCT-REISSUE-DATE` | X(10) | `reissueDate` | String | `reissue_date VARCHAR(10)` |
| `ACCT-CURR-CYC-CREDIT` | S9(10)V99 | — | — | — (not mapped in demo) |
| `ACCT-CURR-CYC-DEBIT` | S9(10)V99 | — | — | — (not mapped in demo) |
| `ACCT-ADDR-ZIP` | X(10) | — | — | — (not mapped in demo) |
| `ACCT-GROUP-ID` | X(10) | — | — | — (not mapped in demo) |
| `FILLER` | X(178) | — | — | — (padding, not mapped) |

**Record Length**: 300 bytes (COBOL) → variable (JPA)

## CVACT03Y.cpy → CardXref.java

| COBOL Field | PIC | Java Field | Java Type | DB Column |
|-------------|-----|------------|-----------|-----------|
| `XREF-CARD-NUM` | X(16) | `cardNumber` | String | `card_number VARCHAR(16) PK` |
| `XREF-CUST-ID` | 9(09) | `customerId` | String | `customer_id VARCHAR(9)` |
| `XREF-ACCT-ID` | 9(11) | `accountId` | String | `account_id VARCHAR(11)` |
| `FILLER` | X(14) | — | — | — (padding, not mapped) |

**Record Length**: 50 bytes (COBOL) → variable (JPA)

## Key Design Decisions

1. **String types for IDs**: Account and card numbers are stored as strings (not longs) to preserve leading zeros, matching COBOL PIC X/9 semantics.
2. **BigDecimal for currency**: COBOL S9(10)V99 (signed with 2 implied decimal places) maps to Java BigDecimal for exact arithmetic.
3. **Date as String**: COBOL date fields are X(10) containing formatted date strings. We preserve this as VARCHAR(10) rather than converting to java.time types to maintain exact fidelity with the original data format.
4. **FILLER fields dropped**: COBOL FILLER fields are padding for fixed-length records and have no equivalent in a relational model.
