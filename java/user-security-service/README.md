# CardDemo User Security Service

Spring Boot modernization of the CardDemo CICS user-security module — the COBOL programs that
maintain the `USRSEC` VSAM file.

| Legacy program | Function | Modern endpoint |
| --- | --- | --- |
| `COSGN00C` | Signon / authentication | `POST /api/signon` (alias `POST /api/auth/login`) |
| `COUSR00C` | List users, 10 per screen | `GET /api/users?page=0&size=10` |
| `COUSR02C` (lookup) | Read one user by ID | `GET /api/users/{userId}` |
| `COUSR01C` | Add user | `POST /api/users` |
| `COUSR02C` | Update user | `PUT /api/users/{userId}` |
| `COUSR03C` | Delete user | `DELETE /api/users/{userId}` |

## Running

```bash
cd java/user-security-service
mvn spring-boot:run
```

The service listens on `http://localhost:8080` with an H2 in-memory database
(`jdbc:h2:mem:usrsec`, console at `/h2-console`). A small operator screen that exercises every
endpoint — modeled on the BMS maps of the legacy transactions — is served at
`http://localhost:8080/`.

Build and test:

```bash
mvn package     # compiles, runs tests, builds the executable jar
mvn test
```

## Data model

`User` maps `SEC-USER-DATA` from copybook `app/cpy/CSUSR01Y.cpy`:

| Field | COBOL | Java |
| --- | --- | --- |
| `SEC-USR-ID` | `PIC X(08)` | `userId` (primary key, max 8) |
| `SEC-USR-FNAME` | `PIC X(20)` | `firstName` (max 20) |
| `SEC-USR-LNAME` | `PIC X(20)` | `lastName` (max 20) |
| `SEC-USR-PWD` | `PIC X(08)` | `password` (max 8, never returned by the API) |
| `SEC-USR-TYPE` | `PIC X(01)` | `userType` — `A` admin, `U` regular |

User IDs, passwords and user types are trimmed and upper-cased on the way in, matching the
uppercase, space-padded fields the BMS maps handed to the COBOL programs.

## Seed data

`src/main/resources/usrsec-seed.txt` holds the in-stream `SYSUT1` records of
`app/jcl/ESDSRRDS.jcl` verbatim; `UsrsecSeeder` parses them with the `CSUSR01Y` layout
(ID 1-8, first name 9-28, last name 29-48, password 49-56, user type 57) and loads them on
startup when the table is empty.

Note that column 57 of those records is `SEC-USR-TYPE`, not the last character of the password:
the seeded password is `PASSWORD` for every user (as documented in the repository root
`README.md`), while the trailing `A`/`U` is the user type.

| User ID | Name | Password | Type |
| --- | --- | --- | --- |
| `ADMIN001` | MARGARET GOLD | `PASSWORD` | A |
| `ADMIN002` | RUSSELL RUSSELL | `PASSWORD` | A |
| `ADMIN003` | RAYMOND WHITMORE | `PASSWORD` | A |
| `ADMIN004` | EMMANUEL CASGRAIN | `PASSWORD` | A |
| `ADMIN005` | GRANVILLE LACHAPELLE | `PASSWORD` | A |
| `USER0001` | LAWRENCE THOMAS | `PASSWORD` | U |
| `USER0002` | AJITH KUMAR | `PASSWORD` | U |
| `USER0003` | LAURITZ ALME | `PASSWORD` | U |
| `USER0004` | AVERARDO MAZZI | `PASSWORD` | U |
| `USER0005` | LEE TING | `PASSWORD` | U |

## Error semantics

The HTTP status codes carry the message text of the original screens:

| Condition | Legacy message | Status |
| --- | --- | --- |
| Unknown user ID | `User ID NOT found...` | 404 |
| Wrong password on signon | `Wrong Password. Try again ...` | 401 |
| Duplicate user on add | `User ID already exist...` | 409 |
| Empty / oversized field | `First Name can NOT be empty...` etc. | 400 |

## Examples

```bash
# Signon as an admin (COSGN00C -> COADM01C)
curl -sX POST localhost:8080/api/signon -H 'Content-Type: application/json' \
  -d '{"userId":"ADMIN001","password":"PASSWORD"}'
# {"user":{"userId":"ADMIN001",...,"userType":"A"},"admin":true,"nextProgram":"COADM01C"}

# Wrong password -> 401, unknown user -> 404
curl -sX POST localhost:8080/api/signon -H 'Content-Type: application/json' \
  -d '{"userId":"ADMIN001","password":"NOPE"}'

# List page 1 (COUSR00C)
curl -s 'localhost:8080/api/users?page=0&size=10'

# Add, update and delete (COUSR01C / COUSR02C / COUSR03C)
curl -sX POST localhost:8080/api/users -H 'Content-Type: application/json' \
  -d '{"userId":"NEWUSR1","firstName":"NEW","lastName":"USER","password":"PWD00001","userType":"U"}'
curl -sX PUT localhost:8080/api/users/NEWUSR1 -H 'Content-Type: application/json' \
  -d '{"firstName":"CHANGED","lastName":"USER","password":"PWD00002","userType":"A"}'
curl -sX DELETE localhost:8080/api/users/NEWUSR1
```

## Tests

* `UsrsecSeederTest` — fixed-width `ESDSRRDS` record parsing.
* `UserServiceTest` — signon success/failure, pagination, CRUD and the not-found/duplicate paths.
* `UserSecurityApiTest` — MockMvc coverage of every endpoint, including status codes and
  validation messages.

```bash
mvn test
```
