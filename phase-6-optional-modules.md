# Phase 6: Optional Modules (Medium Risk, Can Defer)

**Risk Level:** Medium  
**Objective:** Convert DB2, IMS, and MQ integration modules. These are extensions to the core CardDemo application and can be deferred if they are not in scope for the initial migration wave.

> **Note:** These modules are located in separate subdirectories under `app/` and represent integrations with IBM middleware beyond the core COBOL/CICS/VSAM stack.

---

## Wave 6.1: DB2 Transaction Types

### Objective
Convert the DB2-backed transaction type management programs to JPA repositories with Spring Data.

### Source Location
All programs in `app/app-transaction-type-db2/`:

```
app/app-transaction-type-db2/
  cbl/         # COBOL programs
  bms/         # BMS screens
  cpy/         # Copybooks
  cpy-bms/     # BMS-generated copybooks
  csd/         # CICS definitions
  ctl/         # Control files
  dcl/         # DB2 DCLGEN (declare) files
  ddl/         # DB2 DDL (table definitions)
  jcl/         # JCL jobs
```

### Programs to Convert

#### COTRTUPC — Transaction Type Update (Online)

| Attribute | Value |
|---|---|
| **Source** | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` |
| **Technology** | COBOL + CICS + DB2 |
| **DB2 Operations** | SELECT, UPDATE, INSERT on transaction type table |
| **Java Target** | `TransactionTypeController.update()` + `TransactionTypeService` |

#### COTRTLIC — Transaction Type List (Online)

| Attribute | Value |
|---|---|
| **Source** | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` |
| **Technology** | COBOL + CICS + DB2 |
| **DB2 Operations** | SELECT with cursor for list/browse |
| **Java Target** | `TransactionTypeController.list()` + `TransactionTypeService.findAll()` |

#### COBTUPDT — Batch Transaction Type Update

| Attribute | Value |
|---|---|
| **Source** | `app/app-transaction-type-db2/cbl/COBTUPDT.cbl` |
| **Technology** | COBOL + DB2 (batch) |
| **DB2 Operations** | Bulk INSERT/UPDATE |
| **Java Target** | `TransactionTypeBatchUpdateJob` (Spring Batch) |

### DB2-to-JPA Migration

| DB2 Concept | JPA Equivalent |
|---|---|
| DCLGEN declarations (`dcl/`) | JPA `@Entity` classes |
| DDL table definitions (`ddl/`) | Flyway/Liquibase migrations |
| `EXEC SQL SELECT` | `@Query` / Spring Data derived queries |
| `EXEC SQL INSERT` | `repository.save()` |
| `EXEC SQL UPDATE` | `repository.save()` (merge) |
| `EXEC SQL DECLARE CURSOR` | Spring Data `Pageable` |
| `EXEC SQL FETCH` | `repository.findAll(pageable)` |
| `EXEC SQL COMMIT` | `@Transactional` |
| **~64 SQL statements total** | JPA repository methods |

### REST API Endpoints

| Method | Path | Description | Source |
|---|---|---|---|
| `GET` | `/api/transaction-types` | List transaction types | `COTRTLIC` |
| `GET` | `/api/transaction-types/{code}` | Get transaction type by code | `COTRTLIC` |
| `PUT` | `/api/transaction-types/{code}` | Update transaction type | `COTRTUPC` |
| `POST` | `/api/transaction-types` | Create transaction type | `COTRTUPC` |

### Acceptance Criteria
- [ ] All ~64 SQL statements mapped to JPA repository methods
- [ ] DCLGEN declarations converted to JPA entities
- [ ] DDL converted to Flyway migrations
- [ ] Online CRUD operations functional
- [ ] Batch update job processes correctly

---

## Wave 6.2: MQ Integration

### Objective
Convert the MQ-based message processing programs to Spring JMS or Spring Cloud Stream.

### Source Location
Programs in `app/app-vsam-mq/cbl/`:

#### CODATE01 — Date Service (MQ)

| Attribute | Value |
|---|---|
| **Source** | `app/app-vsam-mq/cbl/CODATE01.cbl` |
| **Technology** | COBOL + CICS + MQ |
| **MQ Operations** | GET/PUT messages from/to queues |
| **Function** | Receives date conversion requests via MQ, processes, returns result |
| **Java Target** | `DateMessageService` + Spring JMS `@JmsListener` |

#### COACCT01 — Account Service (MQ)

| Attribute | Value |
|---|---|
| **Source** | `app/app-vsam-mq/cbl/COACCT01.cbl` |
| **Technology** | COBOL + CICS + VSAM + MQ |
| **MQ Operations** | GET/PUT messages from/to queues |
| **Function** | Receives account lookup requests via MQ, reads VSAM, returns account data |
| **Java Target** | `AccountMessageService` + Spring JMS `@JmsListener` |

### MQ-to-Spring JMS Migration

| MQ Concept | Spring Equivalent |
|---|---|
| `MQGET` | `@JmsListener(destination = "queue.name")` |
| `MQPUT` | `JmsTemplate.convertAndSend()` |
| MQ Connection Factory | `ConnectionFactory` bean (ActiveMQ/RabbitMQ/MSK) |
| MQ Queue Manager | Spring Boot auto-configuration |
| Message correlation | `JMSCorrelationID` |
| Dead letter queue | Spring `DefaultJmsListenerContainerFactory` error handling |

### Alternative: Spring Cloud Stream
For a more modern approach, consider using Spring Cloud Stream with a message broker (Kafka, RabbitMQ):

```java
@Bean
public Consumer<DateRequest> dateConversion() {
    return request -> dateConversionService.convert(request);
}
```

### Acceptance Criteria
- [ ] Date service processes messages from queue and returns results
- [ ] Account service looks up account data and returns via message
- [ ] Message correlation preserved (request-reply pattern)
- [ ] Error handling and dead letter queue configured

---

## Wave 6.3: IMS-DB2-MQ Authorization

### Objective
Convert the IMS/DB2/MQ-based authorization subsystem to Spring services with JPA and JMS.

### Source Location
Programs in `app/app-authorization-ims-db2-mq/`:

```
app/app-authorization-ims-db2-mq/
  bms/         # BMS screens
  cbl/         # COBOL programs
  cpy/         # Copybooks
  cpy-bms/     # BMS-generated copybooks
  csd/         # CICS definitions
  data/        # Test/seed data
  dcl/         # DB2 declarations
  ddl/         # DB2 DDL
  ims/         # IMS DBD/PSB definitions
  jcl/         # JCL jobs
```

### Programs to Convert

| Program | Source Path | Type | Java Target |
|---|---|---|---|
| `COPAUS0C.cbl` | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | Online (CICS) | `AuthorizationController` (screen 0) |
| `COPAUS1C.cbl` | `app/app-authorization-ims-db2-mq/cbl/COPAUS1C.cbl` | Online (CICS) | `AuthorizationController` (screen 1) |
| `COPAUA0C.cbl` | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | Online (CICS) | `AuthorizationAdminController` |
| `COPAUS2C.cbl` | `app/app-authorization-ims-db2-mq/cbl/COPAUS2C.cbl` | Online (CICS) | `AuthorizationController` (screen 2) |
| `CBPAUP0C.cbl` | `app/app-authorization-ims-db2-mq/cbl/CBPAUP0C.cbl` | Batch | `AuthorizationBatchJob` |
| `DBUNLDGS.CBL` | `app/app-authorization-ims-db2-mq/cbl/DBUNLDGS.CBL` | Batch utility | `DbUnloadService` |
| `PAUDBLOD.CBL` | `app/app-authorization-ims-db2-mq/cbl/PAUDBLOD.CBL` | Batch utility | `AuthorizationDbLoadJob` |
| `PAUDBUNL.CBL` | `app/app-authorization-ims-db2-mq/cbl/PAUDBUNL.CBL` | Batch utility | `AuthorizationDbUnloadJob` |

### IMS-to-JPA Migration

| IMS Concept | JPA Equivalent |
|---|---|
| IMS DBD (Database Description) | JPA entity model |
| IMS PSB (Program Specification Block) | JPA repository methods |
| IMS `GU` (Get Unique) | `repository.findById()` |
| IMS `GN` (Get Next) | `repository.findAll()` with cursor |
| IMS `ISRT` (Insert) | `repository.save()` |
| IMS `DLET` (Delete) | `repository.deleteById()` |
| IMS `REPL` (Replace) | `repository.save()` (merge) |
| IMS segment hierarchy | JPA `@OneToMany` / `@ManyToOne` relationships |

### REST API Endpoints

| Method | Path | Description | Source |
|---|---|---|---|
| `POST` | `/api/authorizations` | Create authorization | `COPAUS0C` |
| `GET` | `/api/authorizations` | List authorizations | `COPAUS1C` |
| `GET` | `/api/authorizations/{id}` | Get authorization detail | `COPAUS2C` |
| `PUT` | `/api/authorizations/{id}` | Update authorization | `COPAUA0C` |

### Acceptance Criteria
- [ ] All 8 programs converted to Spring services / batch jobs
- [ ] IMS hierarchical data model mapped to relational JPA entities
- [ ] DB2 operations converted to JPA repository methods
- [ ] MQ messaging converted to Spring JMS
- [ ] Authorization business logic preserved
- [ ] DB load/unload utilities converted to Spring Batch jobs

---

## Deferral Considerations

These modules can be deferred if:

1. **DB2 Transaction Types (Wave 6.1):** The core system uses VSAM for transaction types. DB2 is an optional enhancement. Can defer until after cutover.
2. **MQ Integration (Wave 6.2):** If the migration target does not require asynchronous messaging initially, these can be replaced with synchronous REST calls.
3. **IMS Authorization (Wave 6.3):** If the authorization subsystem is not in production use, it can be deferred indefinitely.

---

## Dependencies
- **Phase 0:** Database schema, test framework
- **Phase 1:** JPA entities, utilities
- **Phase 2:** Authentication (authorization extends security)
- **Phase 5:** Batch infrastructure (for batch components)

## Next Phase
Proceed to [Phase 7: Migration Tools & Cutover](phase-7-migration-tools-cutover.md).
