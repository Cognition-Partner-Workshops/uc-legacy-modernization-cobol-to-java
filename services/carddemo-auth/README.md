# CardDemo Auth Service

Identity & Access Management microservice for the CardDemo system, migrated from legacy COBOL programs to Spring Boot 3.x.

## Legacy COBOL Mapping

| COBOL Program | Function | Java Equivalent |
|---|---|---|
| `COSGN00C` | User sign-on (authentication) | `POST /auth/login` — validates credentials, returns JWT |
| `COUSR00C` | List users | `GET /users` — returns all users (admin) or own profile (user) |
| `COUSR01C` | Add user | `POST /users` — creates a new user (admin only) |
| `COUSR02C` | Update user | `PUT /users/{id}` — updates user fields |
| `COUSR03C` | Delete user | `DELETE /users/{id}` — removes a user (admin only) |

### Data Model Mapping (Copybook `CSUSR01Y`)

| COBOL Field | PIC | Java Field | Type | Notes |
|---|---|---|---|---|
| `SEC-USR-ID` | `X(08)` | `userId` | `String(8)` | Primary key |
| `SEC-USR-FNAME` | `X(20)` | `firstName` | `String(20)` | |
| `SEC-USR-LNAME` | `X(20)` | `lastName` | `String(20)` | |
| `SEC-USR-PWD` | `X(08)` | `password` | `String` | BCrypt hash (replaces plaintext COBOL storage) |
| `SEC-USR-TYPE` | `X(01)` | `userType` | `Enum(ADMIN,USER)` | 'A' → ADMIN, 'U' → USER |
| `SEC-USR-FILLER` | `X(23)` | — | — | Not migrated (padding) |

## Architecture

- **Framework**: Spring Boot 3.2.x
- **Security**: Spring Security + JWT (stateless)
- **Persistence**: Spring Data JPA with PostgreSQL (H2 for tests)
- **Password Storage**: BCrypt (replacing COBOL plaintext `PIC X(08)`)

## API Endpoints

### Authentication
```
POST /auth/login
Body: { "userId": "ADMIN01", "password": "secret" }
Response: { "token": "eyJ...", "userId": "ADMIN01", "userType": "ADMIN" }
```

### User Management (requires Bearer token)
```
GET    /users          — List users (admin: all, user: own profile only)
POST   /users          — Create user (admin only)
PUT    /users/{id}     — Update user (admin: any, user: own profile only)
DELETE /users/{id}     — Delete user (admin only)
```

## Role-Based Access Control

| Role | GET /users | POST /users | PUT /users/{id} | DELETE /users/{id} |
|---|---|---|---|---|
| ADMIN | All users | Yes | Any user | Yes |
| USER | Own profile only | No | Own profile only (cannot change role) | No |

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or use the H2 test profile)

### Build & Test
```bash
cd services/carddemo-auth
mvn clean package
```

### Run
```bash
# Set environment variables for PostgreSQL connection
export DB_USERNAME=carddemo
export DB_PASSWORD=carddemo
export JWT_SECRET=your-256-bit-secret-key-here

mvn spring-boot:run
```

## Security Improvements Over Legacy

1. **Password Hashing**: BCrypt replaces plaintext COBOL `PIC X(08)` passwords
2. **Stateless Auth**: JWT tokens replace CICS session-based security
3. **Role Enforcement**: Programmatic RBAC replaces RACF group checks
4. **Input Validation**: Bean Validation replaces manual COBOL field checks
5. **Audit-Ready**: Structured JSON responses replace BMS screen output
