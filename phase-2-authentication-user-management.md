# Phase 2: Authentication & User Management (Low-Medium Risk)

**Risk Level:** Low-Medium  
**Objective:** Convert the security subsystem — sign-on, user CRUD operations, and integrate with Spring Security. This phase establishes the authentication foundation that gates access to all subsequent online functionality.

---

## Wave 2.1: Data Migration — User Security

### Objective
Migrate user security data from VSAM to the relational database and integrate with Spring Security.

### Data Source

| VSAM File | Dataset | Target Table |
|---|---|---|
| `USRSEC` | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | `users` |

- **CSD Definition:** See `app/csd/CARDDEMO.CSD` lines 88-99 for the USRSEC file definition
- **Copybook:** `app/cpy/CSUSR01Y.cpy` defines the user record layout
- **EBCDIC Data:** `app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS`

### Spring Security Integration

| COBOL Concept | Spring Security Equivalent |
|---|---|
| USRSEC VSAM file lookup | `UserDetailsService` backed by `UserRepository` |
| Password field in user record | BCrypt-hashed password in `users.password_hash` |
| User type field (admin/user) | Spring Security `GrantedAuthority` / roles |
| CICS ASSIGN USERID | `SecurityContextHolder.getContext().getAuthentication()` |
| BMS sign-on screen | `/api/auth/login` REST endpoint + web login form |

### Deliverables
- `UserRepository` (Spring Data JPA)
- `UserDetailsServiceImpl` implementing `UserDetailsService`
- Spring Security configuration (`SecurityConfig.java`)
- Password migration strategy (hash existing plaintext passwords)
- ETL script for `USRSEC` -> `users` table

### Acceptance Criteria
- [ ] All users from USRSEC loaded into `users` table
- [ ] Passwords hashed with BCrypt
- [ ] Spring Security authenticates users against the database
- [ ] Role-based access control (admin vs regular user) functional

---

## Wave 2.2: Authentication — Sign-On

### Objective
Convert the COBOL sign-on program to a Spring Boot authentication flow.

### Source Program

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COSGN00C.cbl` |
| **BMS Screen** | `app/bms/COSGN00.bms` |
| **BMS Copybook** | `app/cpy-bms/COSGN00.CPY` |
| **CICS Transaction** | `CC00` (defined in `app/csd/CARDDEMO.CSD`) |
| **Estimated CICS Calls** | ~20 (SEND MAP, RECEIVE MAP, READ file, RETURN, XCTL, etc.) |
| **Java Target** | `AuthController` + `AuthService` |

### CICS Call Replacement Map

| CICS Command | Java Replacement |
|---|---|
| `EXEC CICS SEND MAP('COSGN00')` | Return JSON response / render login template |
| `EXEC CICS RECEIVE MAP('COSGN00')` | `@RequestBody LoginRequest` / form POST |
| `EXEC CICS READ FILE('USRSEC')` | `userRepository.findByUserId()` |
| `EXEC CICS RETURN TRANSID('CA00')` | Redirect to `/api/admin/menu` |
| `EXEC CICS XCTL PROGRAM('COADM01C')` | `return "redirect:/admin"` |

### REST API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user, return JWT token |
| `POST` | `/api/auth/logout` | Invalidate session/token |
| `GET` | `/api/auth/me` | Get current user info |

### Deliverables
- `AuthController.java` with login/logout endpoints
- `AuthService.java` with authentication logic
- JWT token generation and validation (or session-based auth)
- Login web page replacing `COSGN00.bms`

### Acceptance Criteria
- [ ] Users can log in with valid credentials
- [ ] Invalid credentials return appropriate error messages
- [ ] Successful login establishes a session/JWT
- [ ] All 20 CICS calls replaced with Spring equivalents

---

## Wave 2.3: User CRUD Operations

### Objective
Convert the four user management programs (list, create, update, delete) to REST API endpoints.

### Source Programs

#### COUSR00C — User List

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COUSR00C.cbl` |
| **BMS Screen** | `app/bms/COUSR00.bms` |
| **BMS Copybook** | `app/cpy-bms/COUSR00.CPY` |
| **Estimated CICS Calls** | ~22 |
| **Java Target** | `UserController.list()` + `UserService.findAll()` |
| **Key VSAM Operations** | STARTBR, READNEXT, ENDBR on USRSEC |

#### COUSR01C — User Create

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COUSR01C.cbl` |
| **BMS Screen** | `app/bms/COUSR01.bms` |
| **BMS Copybook** | `app/cpy-bms/COUSR01.CPY` |
| **Estimated CICS Calls** | ~10 |
| **Java Target** | `UserController.create()` + `UserService.create()` |
| **Key VSAM Operations** | WRITE to USRSEC |

#### COUSR02C — User Update

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COUSR02C.cbl` |
| **BMS Screen** | `app/bms/COUSR02.bms` |
| **BMS Copybook** | `app/cpy-bms/COUSR02.CPY` |
| **Estimated CICS Calls** | ~12 |
| **Java Target** | `UserController.update()` + `UserService.update()` |
| **Key VSAM Operations** | READ, REWRITE on USRSEC |

#### COUSR03C — User Delete

| Attribute | Value |
|---|---|
| **Source** | `app/cbl/COUSR03C.cbl` |
| **BMS Screen** | `app/bms/COUSR03.bms` |
| **BMS Copybook** | `app/cpy-bms/COUSR03.CPY` |
| **Estimated CICS Calls** | ~12 |
| **Java Target** | `UserController.delete()` + `UserService.delete()` |
| **Key VSAM Operations** | DELETE from USRSEC |

### REST API Endpoints

| Method | Path | Description | Source Program |
|---|---|---|---|
| `GET` | `/api/users` | List users (paginated) | `COUSR00C` |
| `GET` | `/api/users/{id}` | Get user by ID | `COUSR00C` |
| `POST` | `/api/users` | Create new user | `COUSR01C` |
| `PUT` | `/api/users/{id}` | Update user | `COUSR02C` |
| `DELETE` | `/api/users/{id}` | Delete user | `COUSR03C` |

### BMS Screens to Convert

| BMS Map | Source Path | Web UI Replacement |
|---|---|---|
| `COSGN00.bms` | `app/bms/COSGN00.bms` | Login page |
| `COUSR00.bms` | `app/bms/COUSR00.bms` | User list page |
| `COUSR01.bms` | `app/bms/COUSR01.bms` | User create form |
| `COUSR02.bms` | `app/bms/COUSR02.bms` | User edit form |
| `COUSR03.bms` | `app/bms/COUSR03.bms` | User delete confirmation |

### Acceptance Criteria
- [ ] All four user CRUD operations functional via REST API
- [ ] Pagination working for user list
- [ ] Input validation matches COBOL program validation rules
- [ ] Admin-only access enforced via Spring Security
- [ ] All ~56 CICS calls across 4 programs replaced
- [ ] Web UI pages replace all 5 BMS screens

---

## CICS Call Summary

| Program | CICS Calls | Status |
|---|---|---|
| `COSGN00C` | ~20 | Wave 2.2 |
| `COUSR00C` | ~22 | Wave 2.3 |
| `COUSR01C` | ~10 | Wave 2.3 |
| `COUSR02C` | ~12 | Wave 2.3 |
| `COUSR03C` | ~12 | Wave 2.3 |
| **Total** | **~76** | |

---

## Dependencies
- **Phase 0:** Database schema (`users` table), test framework
- **Phase 1:** `User.java` entity from copybook conversion (`CSUSR01Y.cpy`)

## Next Phase
Proceed to [Phase 3: Read-Only Operations](phase-3-read-only-operations.md) once all Wave 2.x deliverables are accepted.
