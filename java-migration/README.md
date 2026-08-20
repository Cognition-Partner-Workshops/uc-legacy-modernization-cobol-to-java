# CardDemo Java migration foundation

This directory is the foundation for incrementally replacing the AWS
CardDemo COBOL/CICS/VSAM application with Java while preserving mainframe
behavior.  It is deliberately small: the current vertical slice is signon
plus an EBCDIC `USRSEC` load.

## Layout and prerequisites

```text
java-migration/
├── domain/   Flyway schema, JPA entities, Spring Data repositories
├── batch/    Spring Batch jobs and IBM037 fixed-record readers
├── web/      Spring Boot web app, Spring Security, signon API
└── docs/     source inventory and data-model notes
```

Prerequisites:

- JDK 17 or newer (the project compiler release is 17).
- Maven.
- Verification was performed with JDK 21 in this environment.

## Build and quickstart

From this directory:

```bash
mvn -q clean verify
mvn -q compile
```

The signon quickstart uses a persistent file-based H2 database.  Run the
steps in order so the batch loader creates the schema and loads `USRSEC`
before the web application starts:

1. Load the EBCDIC seed into persistent H2:

   ```bash
   mvn -pl batch -am spring-boot:run \
     -Dspring-boot.run.arguments="--spring.batch.job.name=usrsecLoadJob --carddemo.usrsec.input-file=/path/to/uc-legacy-modernization-cobol-to-java/app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS --spring.datasource.url=jdbc:h2:file:/path/to/carddemo-batch-db;MODE=PostgreSQL"
   ```

2. Start the web application against the same database:

   ```bash
   mvn -pl web -am spring-boot:run \
     -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:h2:file:/path/to/carddemo-batch-db;MODE=PostgreSQL"
   ```

3. Sign on with a credential from the seed file:

   ```bash
   curl -i -c /tmp/carddemo.cookies \
     -H 'Content-Type: application/json' \
     -d '{"userId":"ADMIN001","password":"PASSWORD"}' \
     http://localhost:8080/api/signon
   ```

4. With the returned session cookie, call the gated admin probe:

   ```bash
   curl -i -b /tmp/carddemo.cookies http://localhost:8080/api/admin/menu
   ```

`ADMIN001`/`PASSWORD` and `USER0001`/`PASSWORD` are real credentials in the
repository's EBCDIC seed file.  The default web profile instead uses an
in-memory H2 database with an empty `usrsec` table; signon returns `401 User
not found. Try again ...` there until the batch load is run against that
same database.  The application has no BMS UI or Java replacement for the
other screens yet.

## USRSEC batch load

The input path is required.  Supply either the
`carddemo.usrsec.input-file` property, as in the quickstart above, or the
`inputFile` Spring Batch job parameter:

```text
--job.name=usrsecLoadJob inputFile=/absolute/path/AWS.M2.CARDDEMO.USRSEC.PS
```

If neither is supplied, the job fails before opening the reader with a
message naming both accepted inputs.  The source is ten fixed 80-byte
records in IBM037/CP037 with no line delimiters.  The load is an upsert by
`sec_usr_id`, so rerunning it does not duplicate rows.  USRSEC contains
display-character fields only; no packed decimal is decoded by this job.

The batch application exits with code `0` for a completed job and a non-zero
code for a failed job.  For scheduler-visible exit codes, package and run the
jar directly; `mvn spring-boot:run` may mask the application exit code:

```bash
mvn -q -pl batch -am package -DskipTests
java -jar batch/target/batch-0.1.0-SNAPSHOT.jar \
  --spring.batch.job.name=usrsecLoadJob \
  --carddemo.usrsec.input-file=/absolute/path/AWS.M2.CARDDEMO.USRSEC.PS \
  "--spring.datasource.url=jdbc:h2:file:/absolute/path/carddemo-batch-db;MODE=PostgreSQL"
```

The default in-memory H2 database disappears when the application exits, so a
CLI load is meaningful only when the datasource points at a persistent
database.  For file-based H2, use a URL such as
`jdbc:h2:file:/path/to/carddemo-batch-db;MODE=PostgreSQL`; PostgreSQL can be
used by supplying its datasource URL, username, and password instead.

## Database configuration

H2 is the default for local development and tests:

```text
jdbc:h2:mem:carddemo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
```

For PostgreSQL, override the datasource properties and provide the PostgreSQL
driver already declared by the `domain` module, for example:

```bash
mvn -pl web -am spring-boot:run \
  -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/carddemo --spring.datasource.username=carddemo --spring.datasource.password=change-me"
```

Use environment/configuration management for real credentials.  Flyway
remains the schema owner in both H2 and PostgreSQL.

## Security posture (foundation)

- **Plaintext USRSEC credentials:** Passwords are stored and compared in
  plaintext to preserve `COSGN00C` parity, so the database contains usable
  credentials and the comparison is not constant-time.  Before any
  non-demo use, replace `LegacyPasswordEncoder` with a hashing encoder and
  perform a credential re-issue or migration step for existing rows.
- **Session-cookie authentication without CSRF:** CSRF protection is disabled
  while authentication state lives in a session cookie.  This is acceptable
  only while the API is read-only/demo.  Before the first state-changing
  endpoint ships, either enable CSRF tokens or move to a stateless bearer-token
  scheme.
- **Session fixation:** Successful signon rotates the session ID before the
  authenticated context is saved.

## Phased migration

The source inventory in `docs/INVENTORY.md` is the authority for the current
COBOL programs, JCL, files, and CICS transactions.

| Phase | Scope | Done means | Verification |
|---|---|---|---|
| Data layer | Flyway schema, JPA model, fixed-record/EBCDIC conventions, source-to-table mappings | Core tables and keys represent the copybooks without losing character dates or decimal scale | `mvn -q clean verify`, schema validation, record-level fixtures, row counts |
| Batch | Load/reconcile ACCTDAT, CARDDAT, CCXREF, CUSTDAT, TRANSACT, reference and report files | Each selected JCL flow has a repeatable Java job with idempotent writes and control totals | Replay seed extracts, compare counts/checksums, compare NUMERIC totals |
| Online services | REST equivalents of signon, account, card, transaction, billing, reporting, and user flows | A selected CICS transaction has behaviorally equivalent API responses and authorization | Unit/MockMvc tests plus parallel-run comparisons for the selected transaction |
| UI | Replace BMS mapsets with a web UI over the Java APIs | Users can complete the selected screen flow without the BMS map | Browser/API acceptance tests and role/error parity checks |
| Extensions | Export/import, optional integrations, date conversion, and password hashing | An extension has an explicit compatibility contract and a controlled cutover | Contract tests, migration rehearsal, security/data review |

Only the data layer's schema, the signon online slice, and the USRSEC load
are implemented today.  The other phases are plan and target state, not
claims of completed migration.

## Strangler and parallel-run strategy

The mainframe remains the system of record while Java runs alongside it.
Nightly EBCDIC extracts are loaded into Java.  Reconciliation is performed
per logical file using row counts and field-level checksums/control totals,
including account balances and transaction amounts.  NUMERIC totals have a
documented zero-tolerance requirement; a discrepancy is a stop condition,
not a rounding adjustment.

Traffic is cut over one transaction at a time: a CICS transaction ID such as
`CC00`, `CT00`, or `CU00` is mapped to its Java REST endpoint only after the
corresponding wave passes reconciliation and behavior checks.  One wave is
cut over at a time.  Rollback is routing that transaction back to CICS while
the mainframe remains authoritative.

## Current gaps and source caveats

- Only the signon slice and the idempotent `USRSEC` load exist.
- There is no Java BMS UI.
- The other logical files are not loaded by Java yet.
- Passwords are plaintext for mainframe parity; hashing is deferred.
- Phase-1 date/timestamp fields remain text.
- The packed-decimal decoder is reusable infrastructure for later
  `COMP`/`COMP-3` layouts; it is not used for `CSUSR01Y`.
- The relative batch default depends on the current working directory;
  use an absolute input path for reliable operation.
