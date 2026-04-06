# SuiteCRM Migration Strategy Document

## 1. Executive Summary

This document outlines the comprehensive strategy for migrating SuiteCRM from its current PHP/MySQL monolithic architecture to a modern Spring Boot microservices backend with Angular microfrontends, backed by Amazon Aurora PostgreSQL, deployed on AWS with containerization and CI/CD via Jenkins.

## 2. Migration Approach: Strangler Fig Pattern

We adopt the **Strangler Fig Pattern** combined with **Domain-Driven Design (DDD)** decomposition:

1. **Phase 0 - Foundation** (Weeks 1-3): Set up target infrastructure, CI/CD, and shared libraries
2. **Phase 1 - Core Services** (Weeks 4-8): Migrate Authentication, Users, Accounts, Contacts
3. **Phase 2 - Sales Pipeline** (Weeks 9-12): Migrate Leads, Opportunities, Quotes, Invoices
4. **Phase 3 - Service & Marketing** (Weeks 13-16): Migrate Cases, Campaigns, Knowledge Base
5. **Phase 4 - Activities & Reporting** (Weeks 17-20): Migrate Calendar, Emails, Tasks, Reports
6. **Phase 5 - Cutover & Optimization** (Weeks 21-24): Data migration, testing, go-live

## 3. Source to Target Technology Mapping

| Component | Source (SuiteCRM) | Target |
|-----------|-------------------|--------|
| **Backend Language** | PHP 8.1 | Java 17 (Spring Boot 3.2) |
| **API Framework** | Slim Framework 3.x | Spring WebFlux / Spring MVC |
| **ORM** | SugarBean (Custom) | Spring Data JPA / Hibernate |
| **Database** | MySQL 5.7 / MariaDB 10.3 | Amazon Aurora PostgreSQL 15 |
| **Frontend** | Smarty + jQuery/YUI | Angular 17 + Module Federation |
| **Authentication** | Custom PHP + SAML | Spring Security + OAuth2/OIDC + Keycloak |
| **Authorization** | ACL Roles + Security Groups | Spring Security RBAC + ABAC |
| **Caching** | APCu / File cache | Redis (ElastiCache) |
| **Search** | Elasticsearch / Lucene | OpenSearch (AWS) |
| **File Storage** | Local filesystem | Amazon S3 |
| **Email** | PHPMailer | Spring Mail + Amazon SES |
| **PDF Generation** | TCPDF | JasperReports / iText |
| **Scheduling** | PHP Cron | Spring Batch + CloudWatch Events |
| **Message Queue** | None (synchronous) | Amazon SQS / SNS |
| **Service Discovery** | N/A (monolith) | Spring Cloud Eureka |
| **API Gateway** | N/A | Spring Cloud Gateway |
| **Config Management** | config.php / database | Spring Cloud Config Server |
| **Monitoring** | N/A | Prometheus + Grafana + CloudWatch |
| **Logging** | PHP error_log | ELK Stack (OpenSearch) |
| **Container Runtime** | N/A | Docker + Amazon EKS |
| **CI/CD** | N/A | Jenkins Unified Pipeline |
| **IaC** | N/A | Terraform + CloudFormation |

## 4. Database Migration Strategy

### 4.1 Schema Migration (MySQL → Aurora PostgreSQL)

| MySQL Feature | PostgreSQL Equivalent |
|--------------|----------------------|
| `AUTO_INCREMENT` | `SERIAL` / `GENERATED ALWAYS AS IDENTITY` |
| `TINYINT(1)` (boolean) | `BOOLEAN` |
| `DATETIME` | `TIMESTAMP` |
| `CHAR(36)` (UUID) | `UUID` native type |
| `TEXT` / `MEDIUMTEXT` | `TEXT` |
| `DOUBLE` | `DOUBLE PRECISION` |
| `ENUM(...)` | Custom `TYPE` or `VARCHAR` + CHECK |
| `utf8mb4` | `UTF-8` (native) |
| `InnoDB` | PostgreSQL MVCC (native) |
| `FULLTEXT INDEX` | `tsvector` + `GIN` index |
| Backtick quoting | Double-quote quoting |
| `LIMIT x, y` | `LIMIT y OFFSET x` |
| `IFNULL()` | `COALESCE()` |
| `GROUP_CONCAT()` | `STRING_AGG()` |
| `NOW()` | `NOW()` / `CURRENT_TIMESTAMP` |

### 4.2 Data Migration Steps

1. **Schema Export**: Extract MySQL schema DDL and convert to PostgreSQL
2. **Type Conversion**: Apply type mapping (CHAR(36) → UUID, TINYINT → BOOLEAN, etc.)
3. **Index Migration**: Convert MySQL indexes to PostgreSQL equivalents
4. **Constraint Migration**: Convert foreign keys, unique constraints
5. **Data Export**: Use `pg_loader` or custom ETL scripts for data transfer
6. **Validation**: Row count verification, checksum validation, referential integrity check
7. **Sequence Setup**: Initialize PostgreSQL sequences from max IDs

### 4.3 Data Migration Tools
- **AWS Database Migration Service (DMS)**: For continuous replication during migration
- **pgLoader**: Direct MySQL-to-PostgreSQL data migration
- **Flyway**: Schema version control and migration management
- **Custom ETL**: Spring Batch jobs for data transformation

## 5. Microservices Decomposition Strategy

### 5.1 Domain-Driven Design Bounded Contexts

Based on SuiteCRM's 123 modules, we identify **10 bounded contexts** mapped to microservices:

| Microservice | SuiteCRM Modules | Domain |
|-------------|-----------------|--------|
| **auth-service** | Users, ACL, ACLRoles, ACLActions, SecurityGroups | Authentication & Authorization |
| **contact-service** | Contacts, Leads, Prospects, ProspectLists | Contact Management |
| **account-service** | Accounts, Employees | Account Management |
| **opportunity-service** | Opportunities, AOS_Quotes, AOS_Invoices, AOS_Products, AOS_Contracts | Sales Pipeline |
| **case-service** | Cases, Bugs, AOK_KnowledgeBase, AOP_Case_Events | Customer Support |
| **campaign-service** | Campaigns, CampaignLog, EmailMarketing, EmailTemplates | Marketing |
| **activity-service** | Calls, Meetings, Tasks, Calendar, Notes, Emails | Activities & Collaboration |
| **report-service** | AOR_Reports, AOR_Charts, Dashboards | Reporting & Analytics |
| **document-service** | Documents, DocumentRevisions | Document Management |
| **workflow-service** | AOW_WorkFlow, AOW_Actions, Schedulers | Workflow & Automation |

### 5.2 Service Communication Patterns

| Pattern | Use Case |
|---------|----------|
| **Synchronous (REST)** | Real-time CRUD operations, user-facing APIs |
| **Asynchronous (Events/SQS)** | Workflow triggers, email sending, audit logging |
| **API Gateway** | Request routing, rate limiting, authentication |
| **Service Registry** | Dynamic service discovery (Eureka) |
| **Circuit Breaker** | Resilience (Resilience4j) |
| **Saga Pattern** | Distributed transactions (Lead conversion) |

## 6. Microfrontend Strategy

### 6.1 Architecture: Module Federation (Webpack 5)

| Microfrontend | Routes | SuiteCRM Equivalent |
|---------------|--------|---------------------|
| **shell-app** | `/`, `/login`, `/dashboard` | Main Menu, Login |
| **mfe-contacts** | `/contacts/*`, `/leads/*` | Contacts, Leads modules |
| **mfe-accounts** | `/accounts/*` | Accounts module |
| **mfe-opportunities** | `/opportunities/*`, `/quotes/*`, `/invoices/*` | Sales modules |
| **mfe-cases** | `/cases/*`, `/knowledge/*` | Support modules |
| **mfe-campaigns** | `/campaigns/*` | Marketing modules |
| **mfe-activities** | `/activities/*`, `/calendar/*`, `/emails/*` | Activity modules |
| **mfe-reports** | `/reports/*`, `/dashboards/*` | Reporting modules |
| **mfe-admin** | `/admin/*`, `/users/*`, `/roles/*` | Administration |

### 6.2 Shared Libraries
- **@suitecrm/ui-lib**: Common Angular components (data tables, forms, dialogs)
- **@suitecrm/auth-lib**: Authentication guards, interceptors, token management
- **@suitecrm/core-lib**: Shared models, services, utilities

## 7. Security Migration Strategy

### 7.1 Authentication Migration
| Source | Target |
|--------|--------|
| PHP session-based auth | JWT + OAuth2 (Keycloak) |
| MD5 password hashing | BCrypt (Spring Security) |
| SAML SSO (OneLogin) | OIDC/SAML (Keycloak) |
| LDAP integration | Keycloak LDAP federation |

### 7.2 Authorization Migration
| Source | Target |
|--------|--------|
| ACL Roles (module-level) | Spring Security roles + `@PreAuthorize` |
| Security Groups (record-level) | Custom `SecurityGroupFilter` + row-level security |
| Permission levels (-1, 0, 1, -99) | RBAC with ADMIN, USER, MANAGER, VIEWER roles |

### 7.3 Role Mapping
| SuiteCRM Role | Spring Boot Role | Permissions |
|--------------|-----------------|-------------|
| Administrator | ROLE_ADMIN | Full access to all modules and configuration |
| Regular User | ROLE_USER | CRUD on assigned records, read on shared |
| Manager | ROLE_MANAGER | CRUD on team records, approve workflows |
| Sales Rep | ROLE_SALES | Full access to Sales modules, read others |
| Support Agent | ROLE_SUPPORT | Full access to Service modules, read others |
| Marketing | ROLE_MARKETING | Full access to Marketing modules, read others |
| Viewer | ROLE_VIEWER | Read-only access across modules |
| Portal User | ROLE_PORTAL | Limited access to Cases and Knowledge Base |

## 8. Data Migration Phases

### Phase 1: Core Data
1. Users and Roles → `auth-service` database
2. Accounts → `account-service` database
3. Contacts → `contact-service` database

### Phase 2: Sales Data
4. Opportunities → `opportunity-service` database
5. Quotes and Invoices → `opportunity-service` database
6. Products → `opportunity-service` database

### Phase 3: Service & Marketing Data
7. Cases and Bugs → `case-service` database
8. Knowledge Base → `case-service` database
9. Campaigns → `campaign-service` database

### Phase 4: Activity & Document Data
10. Calls, Meetings, Tasks → `activity-service` database
11. Emails → `activity-service` database
12. Documents → `document-service` (files to S3)
13. Reports → `report-service` database

## 9. Risk Assessment & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|------------|------------|
| Data loss during migration | High | Low | Parallel run, checksums, rollback plan |
| Performance degradation | Medium | Medium | Load testing, caching, DB optimization |
| Feature parity gaps | Medium | High | Feature mapping matrix, phased rollout |
| Integration breakage | High | Medium | API compatibility layer, contract testing |
| Security vulnerabilities | High | Low | Penetration testing, OWASP compliance |
| Team skill gaps (PHP→Java) | Medium | Medium | Training, pair programming, code reviews |
| Timeline overrun | Medium | High | Agile sprints, MVP approach, prioritization |

## 10. Success Criteria

| Metric | Target |
|--------|--------|
| Feature parity | 95% of core SuiteCRM features migrated |
| Performance | API response time < 200ms (P95) |
| Availability | 99.9% uptime SLA |
| Security | OWASP Top 10 compliance, SOC2 audit ready |
| Scalability | Handle 10x current load with auto-scaling |
| Data integrity | 100% data migration accuracy |
| Test coverage | >80% unit test, >60% integration test |
| Deployment | Zero-downtime deployments via CI/CD |
