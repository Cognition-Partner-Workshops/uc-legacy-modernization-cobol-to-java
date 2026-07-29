# User Security Service

Spring Boot REST modernization of the CardDemo CICS `COSGN00C`, `COUSR00C`, `COUSR01C`, `COUSR02C`, and `COUSR03C` programs over the `USRSEC` user-security record.

## Run

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

The remaining eight users are seeded from `app/jcl/ESDSRRDS.jcl`.

## Demo recording

The demo recording will be available at `docs/demo.mp4`.
