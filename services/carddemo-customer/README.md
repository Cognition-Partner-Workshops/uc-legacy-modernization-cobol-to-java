# CardDemo Customer Service

Spring Boot 3.x microservice providing read-only access to customer data, modernized from the CardDemo mainframe COBOL system.

## Legacy COBOL Mapping

This service replaces direct VSAM KSDS file access that COBOL programs performed via the `CBCUS01C` batch loader and online XREF lookups.

### Customer Entity (`CVCUS01Y.cpy`)

| COBOL Field | PIC | Java Field | Java Type |
|---|---|---|---|
| `CUST-ID` | `9(09)` | `id` | `Long` |
| `CUST-FIRST-NAME` | `X(25)` | `firstName` | `String` |
| `CUST-MIDDLE-NAME` | `X(25)` | `middleName` | `String` |
| `CUST-LAST-NAME` | `X(25)` | `lastName` | `String` |
| `CUST-ADDR-LINE-1` | `X(50)` | `addressLine1` | `String` |
| `CUST-ADDR-LINE-2` | `X(50)` | `addressLine2` | `String` |
| `CUST-ADDR-LINE-3` | `X(50)` | `addressLine3` | `String` |
| `CUST-ADDR-STATE-CD` | `X(02)` | `stateCode` | `String` |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | `countryCode` | `String` |
| `CUST-ADDR-ZIP` | `X(10)` | `zip` | `String` |
| `CUST-PHONE-NUM-1` | `X(15)` | `phoneNumber1` | `String` |
| `CUST-PHONE-NUM-2` | `X(15)` | `phoneNumber2` | `String` |
| `CUST-SSN` | `9(09)` | `ssn` | `String` (masked in API) |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | `govtIssuedId` | `String` |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | `dateOfBirth` | `LocalDate` |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | `eftAccountId` | `String` |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | `primaryCardHolderInd` | `String` |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | `ficoCreditScore` | `Integer` |

### Card Cross-Reference Entity (`CVACT03Y.cpy`)

| COBOL Field | PIC | Java Field | Java Type |
|---|---|---|---|
| `XREF-CARD-NUM` | `X(16)` | `cardNumber` | `String` |
| `XREF-CUST-ID` | `9(09)` | `customerId` | `Long` |
| `XREF-ACCT-ID` | `9(11)` | `accountId` | `Long` |

## API Endpoints

All endpoints are read-only (GET).

| Method | Path | Description |
|---|---|---|
| GET | `/customers` | Paginated list of customers |
| GET | `/customers/{id}` | Full customer profile |
| GET | `/customers/{id}/accounts` | Linked accounts/cards via XREF |
| GET | `/customers/search?lastName=X&zip=Y` | Search by last name and/or zip |

### SSN Masking

SSN is never exposed in full via the API. Responses show only the last 4 digits: `***-**-6789`.

### Pagination

List and search endpoints support standard Spring Data pagination parameters:
- `page` — zero-based page index (default: 0)
- `size` — page size (default: 20)
- `sort` — sort expression (e.g., `lastName,asc`)

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- PostgreSQL (production)
- H2 (tests)

## Running

```bash
# Prerequisites: JDK 17+, Maven 3.8+, PostgreSQL running on localhost:5432

cd services/carddemo-customer
mvn spring-boot:run
```

## Testing

```bash
cd services/carddemo-customer
mvn test
```

26 tests covering:
- Unit tests for DTO mapping and SSN masking
- Repository tests (search queries, XREF lookups)
- Integration tests (full HTTP request/response cycle via MockMvc)

## Configuration

Application properties are in `src/main/resources/application.yml`. Key settings:

| Property | Default | Description |
|---|---|---|
| `server.port` | `8082` | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/carddemo` | Database URL |
| `spring.datasource.username` | `carddemo` | DB user |
| `spring.datasource.password` | `carddemo` | DB password |
