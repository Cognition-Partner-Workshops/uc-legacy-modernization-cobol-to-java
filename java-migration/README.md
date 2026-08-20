# CardDemo COBOL-to-Java migration

This directory contains the Java 17/Spring Boot replacement for the AWS
CardDemo application. The Java modules are a Maven reactor; the React
application in `frontend/` is intentionally a separate Vite project and is
not a Maven module.

## Quick start

### PostgreSQL

The local development database is PostgreSQL 16:

```text
host: localhost
port: 5432
database: carddemo
user: carddemo
password: carddemo
```

```bash
cd java-migration
docker compose up -d postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/carddemo
export SPRING_DATASOURCE_USERNAME=carddemo
export SPRING_DATASOURCE_PASSWORD=carddemo
mvn -pl dataload spring-boot:run -Dspring-boot.run.arguments="--dataset=all"
```

Flyway creates the schema when an application starts. The loader reads the
ASCII and cp037 EBCDIC fixed-width seed files and performs a dependency-safe
reload.

### Backend

```bash
mvn clean install
mvn spotless:check
mvn -pl web spring-boot:run
```

The REST application listens on `http://localhost:8080`; health is available
at `/actuator/health`. Set `CARDDEMO_JWT_SECRET` to replace the development
JWT signing-key default.

### Frontend

Vite proxies browser `/api` requests to `http://localhost:8080`:

```bash
cd frontend
npm ci
npm run dev
```

The dev server is `http://localhost:5173`. Run `npm run lint`, `npm run build`,
and `npm test` for production checks.

The active JWT is kept in memory. On page reload it is rehydrated from
`sessionStorage`, preserving the current tab across refreshes without the
long-lived cross-session persistence of `localStorage`. It is cleared on
logout and on an authenticated API response of 401. This remains a
browser-token tradeoff; production deployment should use its chosen hardened
token storage and transport policy.

## Running batch jobs

Spring Batch metadata tables are owned by Flyway. Select a job with
`spring.batch.job.name`:

```bash
mvn -pl batch spring-boot:run \
  -Dspring-boot.run.arguments="--spring.batch.job.name=postTransactionsJob,--runDate=20220718"
```

The current job registry is:

```text
postTransactionsJob
interestCalculationJob
dailyTransactionValidateJob
transactionReportJob
createStatementsJob
accountFilePrintJob
cardFilePrintJob
xrefFilePrintJob
customerFilePrintJob
exportJob
importJob
combtranJob
transactionBackupJob
categoryBalancePrintJob
```

Common job parameters include `runDate`, `startDate`, `endDate`, `outputPath`,
`reportPath`, `statementPath`, `statementHtmlPath`, `exportPath`,
`importPath`, `backupPath`, and the COMBTRAN input paths. Output defaults
below `target/output` and parent directories are created automatically.

## Architecture

```text
COBOL copybooks / fixed-width files
              │
              ▼
common ───► dataload ───► domain (JPA + Flyway/PostgreSQL)
                              │
                              ├──► batch (Spring Batch jobs and CLI)
                              └──► web (stateless REST + JWT security)
                                               │
                                               ▼
                                  frontend (React/Vite/TypeScript)
```

- `common` ports decimal, date, lookup, and other COBOL-compatible utilities.
- `domain` owns entities, repositories, schema migrations, and Spring Batch
  metadata migrations.
- `dataload` reads the ASCII and EBCDIC fixed-width seed estate.
- `batch` maps the JCL/program estate to tasklet or chunk-oriented jobs.
  Tasklets remain where sequential mutable state or control-break behavior is
  part of the COBOL contract; file-print jobs use
  `ItemReader` → `ItemProcessor` → `ItemWriter`.
- `web` maps all 17 core CICS online programs to stateless REST controllers
  and services. CICS COMMAREA values are explicit `Context` fields.
- `frontend` provides one 3270-inspired view for each core BMS map, typed
  hand-written API calls, PF3/PF7/PF8 controls, and role guards.

## Legacy-to-Java mapping

- [`../docs/INVENTORY.md`](../docs/INVENTORY.md): source inventory and
  coverage.
- [`../docs/DATA-MODEL.md`](../docs/DATA-MODEL.md): copybook/record and
  PostgreSQL domain mapping.
- [`../docs/BATCH-MAPPING.md`](../docs/BATCH-MAPPING.md): JCL/proc to Spring
  Batch mapping and deliberate N/A entries.
- [`../docs/WEB-MAPPING.md`](../docs/WEB-MAPPING.md): CICS transactions, BMS
  programs, REST routes, navigation context, and security parity.

## Status

| Area | Status |
| --- | --- |
| `common` | Complete |
| `domain` | Complete |
| `dataload` | Complete |
| `batch` | Complete for the current mapped estate |
| `web` | Complete for all 17 core online programs |
| `frontend` | Complete for all 17 core BMS views |
| STEP 7 extensions | Not yet done |
| STEP 8 end-to-end reconciliation suite | Not yet done |

The final two rows are intentionally open and are the next migration
handoffs.
