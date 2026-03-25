# CardDemo Data Model & Entity Mapping (VSAM to Relational DB)

## 1. Overview

The CardDemo application uses 8 VSAM KSDS (Keyed Sequential Data Set) files as its primary data store. This document maps each VSAM file to its proposed relational database table, including field-level mappings, key structures, relationships, and data type conversions.

---

## 2. VSAM File Inventory

| VSAM File | Copybook | Record Length | Key Field | Key Length | Description |
|-----------|----------|---------------|-----------|------------|-------------|
| USRSEC | CSUSR01Y | 80 bytes | SEC-USR-ID | 8 bytes | User security/authentication |
| ACCTDAT | CVACT01Y | 300 bytes | ACCT-ID | 11 bytes | Account master |
| CARDDAT | CVACT02Y | 150 bytes | CARD-NUM | 16 bytes | Card master |
| CUSTDAT | CVCUS01Y | 500 bytes | CUST-ID | 9 bytes | Customer master |
| TRANSACT | CVTRA05Y | 350 bytes | TRAN-ID | 16 bytes | Transaction master |
| CCXREF | CVACT03Y | 50 bytes | XREF-CARD-NUM | 16 bytes | Card-to-customer/account cross-reference |
| TCATBAL | CVTRA01Y | 50 bytes | Composite (ACCT+TYPE+CAT) | 17 bytes | Transaction category balances |
| DALYTRAN | CVTRA06Y | 350 bytes | DALYTRAN-ID | 16 bytes | Daily transaction staging (batch input) |

### Alternate Indexes

| Base File | AIX Name | AIX Key | Purpose |
|-----------|----------|---------|---------|
| CARDDAT | CARDAIX | CARD-ACCT-ID | Look up cards by account ID |
| CCXREF | CXACAIX | XREF-ACCT-ID | Look up cross-references by account ID |

---

## 3. Entity-Relationship Diagram

```
+-------------------+         +-------------------+
|      users        |         |    customers      |
+-------------------+         +-------------------+
| PK user_id        |         | PK customer_id    |
|    first_name     |    +----+    first_name      |
|    last_name      |    |    |    middle_name     |
|    password_hash   |    |    |    last_name       |
|    user_type      |    |    |    addr_line_1     |
+-------------------+    |    |    addr_line_2     |
                         |    |    addr_line_3     |
                         |    |    state_code      |
                         |    |    country_code    |
+-------------------+    |    |    zip_code        |
|    accounts       |    |    |    phone_1         |
+-------------------+    |    |    phone_2         |
| PK account_id     |    |    |    ssn             |
|    active_status   |    |    |    govt_id         |
|    current_balance |    |    |    date_of_birth   |
|    credit_limit   |    |    |    eft_account_id  |
|    cash_credit_lmt|    |    |    primary_holder  |
|    open_date      |    |    |    fico_score      |
|    expiration_date |    |    +-------------------+
|    reissue_date   |    |
|    cycle_credit   |    |
|    cycle_debit    |    |
|    zip_code       |    |
|    group_id       |    |
+--------+----------+    |
         |               |
         | 1:N           |
         |               |
+--------+----------+    |
|      cards        |    |
+-------------------+    |
| PK card_number    |    |
| FK account_id     +----+
| FK customer_id    +----+  (via card_xref)
|    cvv_code       |
|    embossed_name  |
|    expiration_date |
|    active_status   |
+--------+----------+
         |
         | 1:N
         |
+--------+----------+
|   transactions    |
+-------------------+
| PK transaction_id |
| FK card_number    |
|    type_code      |
|    category_code  |
|    source         |
|    description    |
|    amount         |
|    merchant_id    |
|    merchant_name  |
|    merchant_city  |
|    merchant_zip   |
|    originated_ts  |
|    processed_ts   |
+-------------------+
         |
         | Aggregated in
         |
+--------+----------+
| tran_cat_balances |
+-------------------+
| FK account_id     |
|    type_code      |
|    category_code  |
|    balance        |
+-------------------+
```

---

## 4. Detailed Field Mappings

### 4.1 USRSEC --> `users` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| SEC-USR-ID | X(08) | user_id | VARCHAR(8) | PRIMARY KEY | |
| SEC-USR-FNAME | X(20) | first_name | VARCHAR(20) | NOT NULL | |
| SEC-USR-LNAME | X(20) | last_name | VARCHAR(20) | NOT NULL | |
| SEC-USR-PWD | X(08) | password_hash | VARCHAR(255) | NOT NULL | BCrypt hash in Java (not plaintext) |
| SEC-USR-TYPE | X(01) | user_type | VARCHAR(1) | NOT NULL, CHECK ('A','U') | A=Admin, U=User |
| SEC-USR-FILLER | X(23) | - | - | - | Dropped (padding) |

```sql
CREATE TABLE users (
    user_id         VARCHAR(8) PRIMARY KEY,
    first_name      VARCHAR(20) NOT NULL,
    last_name       VARCHAR(20) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    user_type       VARCHAR(1) NOT NULL CHECK (user_type IN ('A', 'U')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Java Entity**: `User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(length = 8)
    private String userId;

    @Column(length = 20, nullable = false)
    private String firstName;

    @Column(length = 20, nullable = false)
    private String lastName;

    @Column(length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 1, nullable = false)
    private String userType; // 'A' or 'U'
}
```

---

### 4.2 ACCTDAT --> `accounts` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| ACCT-ID | 9(11) | account_id | BIGINT | PRIMARY KEY | Numeric, 11 digits |
| ACCT-ACTIVE-STATUS | X(01) | active_status | VARCHAR(1) | NOT NULL | Y/N |
| ACCT-CURR-BAL | S9(10)V99 | current_balance | DECIMAL(12,2) | NOT NULL | Signed decimal |
| ACCT-CREDIT-LIMIT | S9(10)V99 | credit_limit | DECIMAL(12,2) | NOT NULL | |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | cash_credit_limit | DECIMAL(12,2) | NOT NULL | |
| ACCT-OPEN-DATE | X(10) | open_date | DATE | NOT NULL | YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | X(10) | expiration_date | DATE | | YYYY-MM-DD |
| ACCT-REISSUE-DATE | X(10) | reissue_date | DATE | | YYYY-MM-DD |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | cycle_credit | DECIMAL(12,2) | | Current cycle credits |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | cycle_debit | DECIMAL(12,2) | | Current cycle debits |
| ACCT-ADDR-ZIP | X(10) | zip_code | VARCHAR(10) | | |
| ACCT-GROUP-ID | X(10) | group_id | VARCHAR(10) | | |
| FILLER | X(178) | - | - | - | Dropped |

```sql
CREATE TABLE accounts (
    account_id        BIGINT PRIMARY KEY,
    active_status     VARCHAR(1) NOT NULL DEFAULT 'Y',
    current_balance   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    credit_limit      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    open_date         DATE NOT NULL,
    expiration_date   DATE,
    reissue_date      DATE,
    cycle_credit      DECIMAL(12,2) DEFAULT 0.00,
    cycle_debit       DECIMAL(12,2) DEFAULT 0.00,
    zip_code          VARCHAR(10),
    group_id          VARCHAR(10),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

### 4.3 CARDDAT --> `cards` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| CARD-NUM | X(16) | card_number | VARCHAR(16) | PRIMARY KEY | |
| CARD-ACCT-ID | 9(11) | account_id | BIGINT | FK -> accounts | |
| CARD-CVV-CD | 9(03) | cvv_code | VARCHAR(3) | NOT NULL | Store encrypted in production |
| CARD-EMBOSSED-NAME | X(50) | embossed_name | VARCHAR(50) | | |
| CARD-EXPIRAION-DATE | X(10) | expiration_date | DATE | | YYYY-MM-DD |
| CARD-ACTIVE-STATUS | X(01) | active_status | VARCHAR(1) | NOT NULL | Y/N |
| FILLER | X(59) | - | - | - | Dropped |

```sql
CREATE TABLE cards (
    card_number     VARCHAR(16) PRIMARY KEY,
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id),
    cvv_code        VARCHAR(3) NOT NULL,
    embossed_name   VARCHAR(50),
    expiration_date DATE,
    active_status   VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cards_account_id ON cards(account_id);
```

---

### 4.4 CUSTDAT --> `customers` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| CUST-ID | 9(09) | customer_id | BIGINT | PRIMARY KEY | |
| CUST-FIRST-NAME | X(25) | first_name | VARCHAR(25) | NOT NULL | |
| CUST-MIDDLE-NAME | X(25) | middle_name | VARCHAR(25) | | |
| CUST-LAST-NAME | X(25) | last_name | VARCHAR(25) | NOT NULL | |
| CUST-ADDR-LINE-1 | X(50) | addr_line_1 | VARCHAR(50) | | |
| CUST-ADDR-LINE-2 | X(50) | addr_line_2 | VARCHAR(50) | | |
| CUST-ADDR-LINE-3 | X(50) | addr_line_3 | VARCHAR(50) | | |
| CUST-ADDR-STATE-CD | X(02) | state_code | VARCHAR(2) | | |
| CUST-ADDR-COUNTRY-CD | X(03) | country_code | VARCHAR(3) | | |
| CUST-ADDR-ZIP | X(10) | zip_code | VARCHAR(10) | | |
| CUST-PHONE-NUM-1 | X(15) | phone_1 | VARCHAR(15) | | |
| CUST-PHONE-NUM-2 | X(15) | phone_2 | VARCHAR(15) | | |
| CUST-SSN | 9(09) | ssn | VARCHAR(9) | | Encrypted in production |
| CUST-GOVT-ISSUED-ID | X(20) | govt_id | VARCHAR(20) | | |
| CUST-DOB-YYYY-MM-DD | X(10) | date_of_birth | DATE | | |
| CUST-EFT-ACCOUNT-ID | X(10) | eft_account_id | VARCHAR(10) | | |
| CUST-PRI-CARD-HOLDER-IND | X(01) | primary_holder | VARCHAR(1) | | Y/N |
| CUST-FICO-CREDIT-SCORE | 9(03) | fico_score | INTEGER | | 0-999 |
| FILLER | X(168) | - | - | - | Dropped |

```sql
CREATE TABLE customers (
    customer_id     BIGINT PRIMARY KEY,
    first_name      VARCHAR(25) NOT NULL,
    middle_name     VARCHAR(25),
    last_name       VARCHAR(25) NOT NULL,
    addr_line_1     VARCHAR(50),
    addr_line_2     VARCHAR(50),
    addr_line_3     VARCHAR(50),
    state_code      VARCHAR(2),
    country_code    VARCHAR(3),
    zip_code        VARCHAR(10),
    phone_1         VARCHAR(15),
    phone_2         VARCHAR(15),
    ssn             VARCHAR(9),
    govt_id         VARCHAR(20),
    date_of_birth   DATE,
    eft_account_id  VARCHAR(10),
    primary_holder  VARCHAR(1),
    fico_score      INTEGER,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

### 4.5 TRANSACT --> `transactions` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| TRAN-ID | X(16) | transaction_id | VARCHAR(16) | PRIMARY KEY | |
| TRAN-TYPE-CD | X(02) | type_code | VARCHAR(2) | NOT NULL | |
| TRAN-CAT-CD | 9(04) | category_code | INTEGER | NOT NULL | |
| TRAN-SOURCE | X(10) | source | VARCHAR(10) | | |
| TRAN-DESC | X(100) | description | VARCHAR(100) | | |
| TRAN-AMT | S9(09)V99 | amount | DECIMAL(11,2) | NOT NULL | Signed (debits negative) |
| TRAN-MERCHANT-ID | 9(09) | merchant_id | BIGINT | | |
| TRAN-MERCHANT-NAME | X(50) | merchant_name | VARCHAR(50) | | |
| TRAN-MERCHANT-CITY | X(50) | merchant_city | VARCHAR(50) | | |
| TRAN-MERCHANT-ZIP | X(10) | merchant_zip | VARCHAR(10) | | |
| TRAN-CARD-NUM | X(16) | card_number | VARCHAR(16) | FK -> cards | |
| TRAN-ORIG-TS | X(26) | originated_ts | TIMESTAMP | | |
| TRAN-PROC-TS | X(26) | processed_ts | TIMESTAMP | | |
| FILLER | X(20) | - | - | - | Dropped |

```sql
CREATE TABLE transactions (
    transaction_id  VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2) NOT NULL,
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16) REFERENCES cards(card_number),
    originated_ts   TIMESTAMP,
    processed_ts    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_card ON transactions(card_number);
CREATE INDEX idx_transactions_type ON transactions(type_code, category_code);
CREATE INDEX idx_transactions_orig_ts ON transactions(originated_ts);
```

---

### 4.6 CCXREF --> `card_xref` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| XREF-CARD-NUM | X(16) | card_number | VARCHAR(16) | PK, FK -> cards | |
| XREF-CUST-ID | 9(09) | customer_id | BIGINT | FK -> customers | |
| XREF-ACCT-ID | 9(11) | account_id | BIGINT | FK -> accounts | |
| FILLER | X(14) | - | - | - | Dropped |

```sql
CREATE TABLE card_xref (
    card_number     VARCHAR(16) PRIMARY KEY REFERENCES cards(card_number),
    customer_id     BIGINT NOT NULL REFERENCES customers(customer_id),
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id)
);

CREATE INDEX idx_card_xref_customer ON card_xref(customer_id);
CREATE INDEX idx_card_xref_account ON card_xref(account_id);
```

> **Migration Note**: In the target schema, the card_xref table may be denormalized into the `cards` table by adding `customer_id` directly to `cards`. This eliminates one join in most queries. The separate VSAM file exists because VSAM doesn't support multi-key access without alternate indexes.

---

### 4.7 TCATBAL --> `tran_cat_balances` Table

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| TRANCAT-ACCT-ID | 9(11) | account_id | BIGINT | PK part, FK -> accounts | |
| TRANCAT-TYPE-CD | X(02) | type_code | VARCHAR(2) | PK part | |
| TRANCAT-CD | 9(04) | category_code | INTEGER | PK part | |
| TRAN-CAT-BAL | S9(09)V99 | balance | DECIMAL(11,2) | NOT NULL | |
| FILLER | X(22) | - | - | - | Dropped |

```sql
CREATE TABLE tran_cat_balances (
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id),
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    balance         DECIMAL(11,2) NOT NULL DEFAULT 0.00,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, type_code, category_code)
);
```

---

### 4.8 DALYTRAN --> `daily_transactions` Table (Staging)

| COBOL Field | PIC Clause | DB Column | SQL Type | Constraints | Notes |
|-------------|-----------|-----------|----------|-------------|-------|
| DALYTRAN-ID | X(16) | transaction_id | VARCHAR(16) | PRIMARY KEY | |
| DALYTRAN-TYPE-CD | X(02) | type_code | VARCHAR(2) | NOT NULL | |
| DALYTRAN-CAT-CD | 9(04) | category_code | INTEGER | NOT NULL | |
| DALYTRAN-SOURCE | X(10) | source | VARCHAR(10) | | |
| DALYTRAN-DESC | X(100) | description | VARCHAR(100) | | |
| DALYTRAN-AMT | S9(09)V99 | amount | DECIMAL(11,2) | NOT NULL | |
| DALYTRAN-MERCHANT-ID | 9(09) | merchant_id | BIGINT | | |
| DALYTRAN-MERCHANT-NAME | X(50) | merchant_name | VARCHAR(50) | | |
| DALYTRAN-MERCHANT-CITY | X(50) | merchant_city | VARCHAR(50) | | |
| DALYTRAN-MERCHANT-ZIP | X(10) | merchant_zip | VARCHAR(10) | | |
| DALYTRAN-CARD-NUM | X(16) | card_number | VARCHAR(16) | | |
| DALYTRAN-ORIG-TS | X(26) | originated_ts | TIMESTAMP | | |
| DALYTRAN-PROC-TS | X(26) | processed_ts | TIMESTAMP | | |
| FILLER | X(20) | - | - | - | Dropped |

```sql
CREATE TABLE daily_transactions (
    transaction_id  VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2) NOT NULL,
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16),
    originated_ts   TIMESTAMP,
    processed_ts    TIMESTAMP,
    status          VARCHAR(10) DEFAULT 'PENDING',
    rejection_reason VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> **Migration Note**: The `daily_transactions` table is a staging table used by the batch transaction posting job. Records are validated and moved to the `transactions` table. Additional columns (`status`, `rejection_reason`) replace the DALYREJS reject file pattern from the COBOL batch.

---

## 5. COBOL Data Type Conversion Reference

| COBOL PIC Clause | Java Type | SQL Type | Notes |
|-----------------|-----------|----------|-------|
| PIC X(n) | String | VARCHAR(n) | Fixed-length in COBOL, variable in Java/SQL |
| PIC 9(n) | long / int | BIGINT / INTEGER | Unsigned numeric |
| PIC S9(n)V99 | BigDecimal | DECIMAL(n+2, 2) | Signed decimal with 2 fractional digits |
| PIC 9(n)V99 | BigDecimal | DECIMAL(n+2, 2) | Unsigned decimal |
| PIC X(10) (date) | LocalDate | DATE | COBOL stores as string 'YYYY-MM-DD' |
| PIC X(26) (timestamp) | LocalDateTime | TIMESTAMP | COBOL stores as string |
| FILLER | - | - | Padding bytes, dropped in migration |

---

## 6. VSAM Alternate Index to Database Index Mapping

| VSAM AIX | Purpose | Database Equivalent |
|----------|---------|-------------------|
| CARDAIX (CARDDAT by CARD-ACCT-ID) | Look up cards by account | `CREATE INDEX idx_cards_account_id ON cards(account_id)` |
| CXACAIX (CCXREF by XREF-ACCT-ID) | Look up card xrefs by account | `CREATE INDEX idx_card_xref_account ON card_xref(account_id)` |

In COBOL, alternate indexes require separate VSAM path definitions and JCL (TRANIDX job rebuilds indexes). In PostgreSQL/MySQL, indexes are maintained automatically on insert/update/delete.

---

## 7. Sample Data Migration

The repository contains sample data in `app/data/ASCII/` format that can be used for initial database seeding:

| File | Target Table | Format |
|------|-------------|--------|
| app/data/ASCII/ACCTDATA.txt | accounts | Fixed-width, 300 bytes/record |
| app/data/ASCII/CARDDATA.txt | cards | Fixed-width, 150 bytes/record |
| app/data/ASCII/CUSTDATA.txt | customers | Fixed-width, 500 bytes/record |
| app/data/ASCII/TRANSACT.txt | transactions | Fixed-width, 350 bytes/record |
| app/data/ASCII/XREFDATA.txt | card_xref | Fixed-width, 50 bytes/record |
| app/data/ASCII/USRSEC.txt | users | Fixed-width, 80 bytes/record |

A data migration script should:
1. Parse fixed-width records using copybook field definitions
2. Trim trailing spaces from text fields
3. Convert COBOL numeric formats to standard numbers
4. Hash plaintext passwords before inserting into `users` table
5. Convert date strings to proper DATE types
6. Validate foreign key relationships before loading

---

## 8. Schema Optimization Recommendations

### 8.1 Denormalize card_xref into cards
Add `customer_id` to the `cards` table and eliminate the separate `card_xref` table. The VSAM cross-reference file exists because VSAM doesn't support multi-column keys natively - relational databases handle this through standard JOINs and foreign keys.

### 8.2 Replace Transaction ID Generation
COBOL generates transaction IDs by reading the last transaction ID and incrementing. In Java, use database sequences (`BIGSERIAL`) or UUIDs for simpler, concurrent-safe ID generation.

### 8.3 Add Audit Columns
Add `created_at`, `updated_at`, and optionally `created_by`, `updated_by` to all tables for audit trail (COBOL has no equivalent).

### 8.4 Consider tran_cat_balances as Materialized View
The `TCATBAL` file maintains running balances by category. In the Java system, this could be implemented as a materialized view or calculated on-demand via SQL aggregation rather than maintaining a separate table, depending on performance requirements.

### 8.5 Sensitive Data Handling
- **Passwords**: BCrypt hash (never plaintext as in current USRSEC file)
- **SSN**: Encrypt at rest, mask in API responses
- **CVV**: Encrypt at rest, never return in API responses (PCI-DSS compliance)
- **Card Numbers**: Consider tokenization for PCI-DSS compliance
