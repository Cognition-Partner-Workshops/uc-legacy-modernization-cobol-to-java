# CardDemo - Spring Boot Migration

Spring Boot implementation of the CardDemo mainframe COBOL/CICS credit card management system.

## Prerequisites

- Java 17+
- Maven 3.6+

## Project Structure

```
com.carddemo
├── config/          # Security config, CORS, etc.
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities mapped from COBOL copybooks
├── exception/       # Global exception handler, custom exceptions
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic services
└── CarddemoApplication.java
```

## Entities (COBOL Copybook Mapping)

| Entity              | COBOL Copybook | VSAM File  | Description                          |
|---------------------|----------------|------------|--------------------------------------|
| User                | CSUSR01Y       | USRSEC     | System users and authentication      |
| Account             | CVACT01Y       | ACCTDAT    | Credit card accounts                 |
| Card                | CVACT02Y       | CARDDAT    | Physical card details                |
| Customer            | CVCUS01Y       | CUSTDATA   | Customer personal information        |
| CardCrossReference  | CVACT03Y       | CCXREF     | Links cards to customers & accounts  |
| Transaction         | CVTRA05Y       | TRANSACT   | Transaction history                  |

## Running the Application

### Development (H2 in-memory database)

```bash
cd carddemo-java
mvn spring-boot:run
```

The application starts on port 8080 with the `dev` profile by default.

### Production (PostgreSQL)

```bash
cd carddemo-java
mvn spring-boot:run -Dspring-boot.run.profiles=prod \
  -DDB_USERNAME=your_user -DDB_PASSWORD=your_password
```

## Available Endpoints

| Endpoint             | Description              |
|----------------------|--------------------------|
| `/api/health`        | Health check             |
| `/swagger-ui.html`   | Swagger UI               |
| `/api-docs`          | OpenAPI JSON spec        |
| `/h2-console`        | H2 database console (dev)|

## Profiles

- **dev** (default): H2 in-memory database, SQL logging enabled, H2 console available
- **prod**: PostgreSQL, SQL logging disabled

## Technology Stack

- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security
- Spring Validation
- H2 Database (dev)
- PostgreSQL (prod)
- Lombok
- SpringDoc OpenAPI (Swagger)
