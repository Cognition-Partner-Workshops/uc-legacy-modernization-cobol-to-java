# User Security Service

Spring Boot REST modernization of the CardDemo CICS `COSGN00C`, `COUSR00C`, `COUSR01C`, `COUSR02C`, and `COUSR03C` programs over the `USRSEC` user-security record.

## Run

Requires JDK 17 and Maven. If several JDKs are installed, point `JAVA_HOME` at the 17 install first.

```bash
mvn spring-boot:run
```

The service listens on http://localhost:8080. Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/signon` | Validate user ID and password |
| GET | `/api/users?page=0&size=10` | Paginated user list |
| GET | `/api/users/{id}` | Find a user |
| POST | `/api/users` | Create a user (duplicate ID returns 409) |
| PUT | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Delete a user |

User types mirror `COCOM01Y`: `ADMIN` is persisted as `A`, and `USER` as `U`. Signon not-found responses are 404, invalid passwords are 401, and validation errors are 400.

## Default credentials

| User ID | Password | Type |
|---|---|---|
| `ADMIN001` | `PASSWORDA` | ADMIN |
| `USER0001` | `PASSWORDU` | USER |

The credentials above intentionally show the complete 9-character values from the JCL demo data. In the original `CSUSR01Y.cpy` layout, `SEC-USR-PWD` is `PIC X(08)` followed by the one-character `SEC-USR-TYPE`; the trailing `A` or `U` in the fixed-width JCL record is therefore the type field. This REST service retains the explicitly specified `PASSWORDA` and `PASSWORDU` values as seeded passwords.

The remaining eight users are seeded from `app/jcl/ESDSRRDS.jcl`.

## Demo recording

The completed demo recording is available at [`docs/demo.mp4`](docs/demo.mp4).

The demonstrated flow is:

1. Admin signon with `ADMIN001` / `PASSWORDA` → `200`, `userType: ADMIN`.
2. `GET /api/users` → the 10 seeded users.
3. `POST /api/users` creating `DEMO0001` → `201`.
4. `PUT /api/users/DEMO0001` → `200`, updated fields, and type flipped to `ADMIN`.
5. `DELETE /api/users/DEMO0001` → `204`.
6. Follow-up `GET /api/users/DEMO0001` → `404`, `User ID NOT found...`.
