# User Management Service

A Java Spring Boot 3.x application that replaces the four COBOL Admin User Management programs from the CardDemo CICS application.

## COBOL-to-Java Mapping

| COBOL Program | Function | Java Replacement | HTTP Endpoint |
|---|---|---|---|
| `COUSR00C.cbl` | List all users (paginated browse) | `UserController.listUsers()` | `GET /api/users?page=0&size=10` |
| `COUSR01C.cbl` | Add a new user | `UserController.createUser()` | `POST /api/users` |
| `COUSR02C.cbl` | Update an existing user | `UserController.updateUser()` | `PUT /api/users/{userId}` |
| `COUSR03C.cbl` | Delete a user | `UserController.deleteUser()` | `DELETE /api/users/{userId}` |

## Data Model Mapping

The JPA `User` entity maps the USRSEC VSAM record defined in the COBOL copybook `CSUSR01Y.cpy`:

| COBOL Field | PIC Clause | Java Field | DB Column | Type |
|---|---|---|---|---|
| `SEC-USR-ID` | `PIC X(08)` | `userId` | `user_id` | `VARCHAR(8)` PK |
| `SEC-USR-FNAME` | `PIC X(20)` | `firstName` | `first_name` | `VARCHAR(20)` |
| `SEC-USR-LNAME` | `PIC X(20)` | `lastName` | `last_name` | `VARCHAR(20)` |
| `SEC-USR-PWD` | `PIC X(08)` | `password` | `password` | `VARCHAR(8)` |
| `SEC-USR-TYPE` | `PIC X(01)` | `userType` | `user_type` | `VARCHAR(10)` enum |
| `SEC-USR-FILLER` | `PIC X(23)` | *(not migrated)* | — | — |

**User Types:** `R` = Regular, `A` = Admin (stored as `REGULAR`/`ADMIN` enum in Java).

## Prerequisites

- **Java 21** or later
- **Maven 3.6+**

## Build and Run

```bash
# Build the project
mvn clean package

# Run the application (uses H2 in-memory database by default)
mvn spring-boot:run

# Or run the JAR directly
java -jar target/user-management-service-1.0.0.jar
```

The service starts on **http://localhost:8080**. The H2 console is available at **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:usrsec`).

## Run Tests

```bash
mvn test
```

## API Endpoints

### List Users (replaces COUSR00C)

```
GET /api/users?page=0&size=10
```

Returns a paginated list of users sorted by `userId`. The default page size of 10 matches the COBOL program's 10-row screen display.

**Response:** Spring Data `Page<UserResponse>` with `content`, `totalElements`, `totalPages`, etc.

### Get User by ID

```
GET /api/users/{userId}
```

**Response (200):**
```json
{
  "userId": "USER0001",
  "firstName": "John",
  "lastName": "Smith",
  "userType": "Regular"
}
```

**Error (404):** `"User ID NOT found: {userId}"`

### Create User (replaces COUSR01C)

```
POST /api/users
Content-Type: application/json

{
  "userId": "NEWUSR01",
  "firstName": "Alice",
  "lastName": "Wonder",
  "password": "PASS1234",
  "userType": "R"
}
```

**Response (201):**
```json
{
  "message": "User NEWUSR01 has been added ...",
  "user": { ... }
}
```

**Errors:**
- `409 Conflict` — `"User ID already exists: {userId}"` (mirrors COBOL DFHRESP(DUPREC))
- `400 Bad Request` — Validation errors (e.g., `"First Name can NOT be empty..."`)

### Update User (replaces COUSR02C)

```
PUT /api/users/{userId}
Content-Type: application/json

{
  "firstName": "Johnny",
  "lastName": "Smith",
  "password": "NEWPASS1",
  "userType": "R"
}
```

**Response (200):**
```json
{
  "message": "User USER0001 has been updated ...",
  "user": { ... }
}
```

### Delete User (replaces COUSR03C)

```
DELETE /api/users/{userId}
```

**Response (200):**
```json
{
  "message": "User USER0001 has been deleted ..."
}
```

## Validation Rules

Validation replicates the exact checks from COUSR01C (lines 117-151) and COUSR02C (lines 179-213):

| Field | Rule | Error Message |
|---|---|---|
| First Name | Required, max 20 chars | `"First Name can NOT be empty..."` |
| Last Name | Required, max 20 chars | `"Last Name can NOT be empty..."` |
| User ID | Required, max 8 chars | `"User ID can NOT be empty..."` |
| Password | Required, max 8 chars | `"Password can NOT be empty..."` |
| User Type | Required, must be `R` or `A` | `"User Type can NOT be empty..."` |

## Production Configuration

To use PostgreSQL in production, activate the `prod` profile:

```bash
java -jar target/user-management-service-1.0.0.jar \
  --spring.profiles.active=prod \
  --DB_USERNAME=myuser \
  --DB_PASSWORD=mypassword
```

The production profile expects a PostgreSQL database at `jdbc:postgresql://localhost:5432/usermanagement`. Configure with the `DB_USERNAME` and `DB_PASSWORD` environment variables.

## Project Structure

```
user-management-service/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/carddemo/usermanagement/
    │   │   ├── UserManagementApplication.java
    │   │   ├── controller/
    │   │   │   └── UserController.java
    │   │   ├── dto/
    │   │   │   ├── CreateUserRequest.java
    │   │   │   ├── UpdateUserRequest.java
    │   │   │   └── UserResponse.java
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   └── UserType.java
    │   │   ├── exception/
    │   │   │   ├── DuplicateUserException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── UserNotFoundException.java
    │   │   ├── repository/
    │   │   │   └── UserRepository.java
    │   │   └── service/
    │   │       └── UserService.java
    │   └── resources/
    │       ├── application.yml
    │       ├── data.sql
    │       └── schema.sql
    └── test/
        └── java/com/carddemo/usermanagement/
            ├── controller/
            │   └── UserControllerTest.java
            └── service/
                └── UserServiceTest.java
```
