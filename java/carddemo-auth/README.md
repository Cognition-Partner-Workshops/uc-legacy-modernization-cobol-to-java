# carddemo-auth — Identity & Access (Phase 1)

Spring Boot 3 service that modernizes the CardDemo sign-on and user-management
mainframe programs (bounded context **BC-1 Identity & Access**).

## Legacy programs replaced

| Legacy | Function | Replacement |
| --- | --- | --- |
| `COSGN00C` (txn `CC00`) | VSAM `USRSEC` sign-on, plaintext password check, COMMAREA hand-off | `POST /api/auth/login` → JWT |
| `COUSR00C` | Sequential browse of `USRSEC` | `GET /api/users` (paginated) |
| `COUSR01C` | Add user | `POST /api/users` |
| `COUSR02C` | Update user (read + rewrite) | `PUT /api/users/{id}` |
| `COUSR03C` | Delete user | `DELETE /api/users/{id}` |
| `COADM01C` | Admin menu dispatch | `ADMIN` role requirement on `/api/users/**` |

The 80-byte `USRSEC` record (copybook `CSUSR01Y`, keyed on `SEC-USR-ID`) becomes
the relational `users` table. The COMMAREA trust flags `CDEMO-USER-ID` /
`CDEMO-USER-TYPE` become JWT claims (`sub` / `userType`).

## Security uplift

The whole point of Phase 1: the legacy 8-character plaintext `SEC-USR-PWD` is
**gone**. Passwords are stored as BCrypt hashes and verified via Spring
Security's `PasswordEncoder`; identity downstream is a signed JWT, not implicit
shared memory.

## API

- `POST /api/auth/login` — `{ "userId", "password" }` → `{ token, userId, userType }`; `401` on bad credentials. Public.
- `GET /api/users` — paginated list. **ADMIN only.**
- `POST /api/users` — create (`409` on duplicate). **ADMIN only.**
- `GET /api/users/{id}` — fetch one (`404` if absent). **ADMIN only.**
- `PUT /api/users/{id}` — update (blank password leaves hash unchanged). **ADMIN only.**
- `DELETE /api/users/{id}` — delete (`404` if absent). **ADMIN only.**

All non-login endpoints require a valid `Authorization: Bearer <jwt>` header.

## Seeded users

Flyway migration `V2` seeds the legacy defaults with BCrypt-hashed passwords
(plaintext `PASSWORD`; rotate after first sign-on):

- `ADMIN001` — `ADMIN`
- `USER0001` — `USER`

## Build & test

```bash
# Requires JDK 17+
mvn test          # run the JUnit 5 / MockMvc suite
mvn spring-boot:run
```

H2 (in-memory) backs dev/test; Flyway manages the schema. Override the JWT
signing secret in real deployments via `CARDDEMO_JWT_SECRET`.
