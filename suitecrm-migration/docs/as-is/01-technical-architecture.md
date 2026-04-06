# SuiteCRM As-Is Technical Architecture Document

## 1. Executive Summary

SuiteCRM is an open-source Customer Relationship Management (CRM) application forked from SugarCRM Community Edition. It is built on a PHP/MySQL monolithic architecture and provides comprehensive CRM functionality including contact management, sales pipeline, customer support, marketing campaigns, and reporting.

**Version Analyzed**: SuiteCRM 7.15.1 (based on SugarCRM CE 6.5.25)

## 2. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Programming Language** | PHP | 8.1+ |
| **Web Server** | Apache/Nginx | 2.4+/1.18+ |
| **Database** | MySQL / MariaDB | 5.7+ / 10.3+ |
| **Template Engine** | Smarty | 4.x |
| **Frontend** | jQuery, YUI, HTML/CSS | Legacy |
| **Search Engine** | Elasticsearch / Lucene | 7.x |
| **Email** | PHPMailer | 6.x |
| **PDF Generation** | TCPDF | 6.x |
| **API Framework** | Slim Framework | 3.x |
| **Authentication** | SAML (OneLogin), OAuth2 | - |
| **Package Manager** | Composer | 2.x |
| **Caching** | APC/APCu/Redis | - |

## 3. Architecture Overview

```
+------------------------------------------------------------------+
|                        SuiteCRM Monolith                          |
|                                                                   |
|  +--------------------+  +--------------------+  +--------------+ |
|  |   Presentation     |  |   Business Logic   |  |   Data       | |
|  |   Layer (Smarty    |  |   Layer (PHP       |  |   Access     | |
|  |   Templates +      |  |   Modules +        |  |   Layer      | |
|  |   jQuery/YUI)      |  |   SugarBean ORM)   |  |   (SugarBean)| |
|  +--------------------+  +--------------------+  +--------------+ |
|                                                                   |
|  +--------------------+  +--------------------+  +--------------+ |
|  |   REST API V8      |  |   Legacy SOAP/     |  |   Scheduler  | |
|  |   (Slim Framework) |  |   JSON-RPC API     |  |   (Cron)     | |
|  +--------------------+  +--------------------+  +--------------+ |
|                                                                   |
|  +--------------------+  +--------------------+  +--------------+ |
|  |   ACL/Security     |  |   Workflow Engine   |  |   Report     | |
|  |   Groups           |  |   (AOW_WorkFlow)    |  |   Engine     | |
|  +--------------------+  +--------------------+  +--------------+ |
+------------------------------------------------------------------+
                              |
                    +-------------------+
                    |   MySQL/MariaDB   |
                    |   Database        |
                    +-------------------+
```

## 4. Module Architecture (MVC Pattern)

SuiteCRM follows a modified MVC (Model-View-Controller) pattern inherited from SugarCRM:

### 4.1 Model Layer (SugarBean ORM)
- **SugarBean**: Base class for all data models
- **vardefs.php**: Schema definitions for each module (field types, relationships, indexes)
- **TableDictionary.php**: Relationship table definitions
- **Database abstraction**: SugarQuery / DBManager classes

### 4.2 View Layer
- **Smarty Templates**: `.tpl` files for HTML rendering
- **BMS Maps (legacy)**: Screen layout definitions
- **SugarView**: Base view class with DetailView, EditView, ListView
- **JavaScript**: jQuery + YUI for client-side interactivity
- **Dashlets**: Widget-based dashboard components

### 4.3 Controller Layer
- **SugarController**: Routes requests to appropriate actions
- **Entry Points**: `index.php` main entry, `cron.php` for scheduled tasks
- **REST API V8**: Modern JSON:API compliant endpoints (Slim Framework)
- **Legacy SOAP API**: v2-v4.1 for backward compatibility

## 5. Module Inventory (123 Total Modules)

### 5.1 Core CRM Modules (81 modules)

| Category | Modules | Description |
|----------|---------|-------------|
| **Sales** | Accounts, Contacts, Leads, Opportunities, Prospects, ProspectLists | Sales pipeline management |
| **Customer Service** | Cases, Bugs, Notes | Support ticket tracking |
| **Marketing** | Campaigns, CampaignLog, CampaignTrackers, EmailMarketing, EmailTemplates | Campaign management |
| **Activities** | Calls, Meetings, Tasks, Calendar, Emails, Reminders | Activity tracking |
| **Documents** | Documents, DocumentRevisions | Document management |
| **Users & Security** | Users, Employees, ACL, ACLActions, ACLRoles, SecurityGroups, Groups | RBAC and security |
| **Administration** | Administration, Configurator, ModuleBuilder, Studio, Schedulers | System configuration |
| **System** | Audit, Trackers, SugarFeed, Alerts, Favorites, Import, Currencies | Platform services |
| **Integration** | EAPM, Connectors, InboundEmail, OutboundEmailAccounts, OAuth2Clients | External integrations |

### 5.2 SuiteCRM Extension Modules (36 modules)

| Category | Modules | Description |
|----------|---------|-------------|
| **Quotes & Invoicing** | AOS_Quotes, AOS_Invoices, AOS_Contracts, AOS_Products, AOS_Product_Categories, AOS_Line_Item_Groups, AOS_Products_Quotes | Financial documents |
| **Reporting** | AOR_Reports, AOR_Charts, AOR_Conditions, AOR_Fields, AOR_Scheduled_Reports | Advanced reporting |
| **Workflow** | AOW_WorkFlow, AOW_Actions, AOW_Conditions, AOW_Processed | Business process automation |
| **Case Portal** | AOP_Case_Events, AOP_Case_Updates | Customer portal |
| **Knowledge Base** | AOK_KnowledgeBase, AOK_Knowledge_Base_Categories | Self-service knowledge |
| **PDF Templates** | AOS_PDF_Templates | Document generation |
| **Projects** | AM_ProjectTemplates, AM_TaskTemplates | Project management |
| **Events** | FP_events, FP_Event_Locations | Event management |
| **Surveys** | Surveys, SurveyQuestions, SurveyQuestionOptions, SurveyResponses, SurveyQuestionResponses | Survey management |
| **Maps** | jjwg_Maps, jjwg_Markers, jjwg_Areas, jjwg_Address_Cache | Geolocation |
| **Business Hours** | AOBH_BusinessHours | SLA management |
| **Search** | AOD_Index, AOD_IndexEvent | Full-text search |

## 6. Database Architecture

### 6.1 Database Engine
- **Primary**: MySQL 5.7+ / MariaDB 10.3+
- **Storage Engine**: InnoDB (transactional, row-level locking)
- **Character Set**: utf8mb4
- **Collation**: utf8mb4_general_ci

### 6.2 Schema Statistics
- **Estimated Tables**: 200+ (core + relationship + audit + custom)
- **Relationship Tables**: ~80 many-to-many junction tables
- **Audit Tables**: 1 per audited module (e.g., `accounts_audit`)
- **Custom Tables**: `*_cstm` tables for custom fields

### 6.3 Key Table Categories

| Category | Example Tables | Description |
|----------|---------------|-------------|
| **Entity Tables** | accounts, contacts, leads, opportunities, cases | Primary data entities |
| **Relationship Tables** | accounts_contacts, opportunities_contacts, calls_contacts | M:N relationships |
| **Security Tables** | users, acl_roles, acl_roles_actions, securitygroups, securitygroups_users | Access control |
| **System Tables** | config, upgrade_history, schedulers, job_queue | Configuration & scheduling |
| **Audit Tables** | accounts_audit, contacts_audit | Change tracking |
| **Custom Field Tables** | accounts_cstm, contacts_cstm | Dynamic fields |
| **Email Tables** | emails, emails_text, email_addresses, email_addr_bean_rel | Email management |
| **Campaign Tables** | campaigns, campaign_log, campaign_trkrs, emailman | Marketing |
| **Workflow Tables** | aow_workflow, aow_actions, aow_conditions, aow_processed | Automation |

### 6.4 Common Field Pattern (SugarBean Base Fields)
Every entity table inherits these standard fields:
- `id` (CHAR(36)) - UUID primary key
- `name` (VARCHAR(255)) - Display name
- `date_entered` (DATETIME) - Creation timestamp
- `date_modified` (DATETIME) - Last update timestamp
- `modified_user_id` (CHAR(36)) - FK to users
- `created_by` (CHAR(36)) - FK to users
- `description` (TEXT) - Description
- `deleted` (TINYINT(1)) - Soft delete flag
- `assigned_user_id` (CHAR(36)) - Record owner

## 7. Security Architecture

### 7.1 Authentication
- **Native Authentication**: Username/password stored in `users` table (MD5/bcrypt hashed)
- **SAML SSO**: OneLogin PHP-SAML integration for enterprise SSO
- **OAuth2**: Server-side OAuth2 with League\OAuth2 for API access
- **LDAP**: Optional LDAP/Active Directory integration
- **Two-Factor**: Not built-in (available via plugins)

### 7.2 Authorization (ACL System)
```
Users ──> ACL Roles ──> ACL Actions (per module)
  |                        |
  └──> Security Groups ──> Module-level permissions
                            - Access, Create, Edit, Delete
                            - Import, Export, List, View
                            - Mass Update
```

- **ACL Roles**: Define permission sets (Admin, User, Custom)
- **ACL Actions**: Module-level CRUD + special operations
- **Security Groups**: Group-based access control for record-level security
- **Row-Level Security**: SecurityGroups module provides record ownership

### 7.3 Permission Levels
- `-1` (Default) - Inherit from role
- `0` (Enabled/All) - Full access
- `1` (Owner) - Only own records
- `-99` (Disabled/None) - No access

## 8. API Architecture

### 8.1 REST API V8 (Modern)
- **Framework**: Slim Framework 3.x
- **Standard**: JSON:API specification
- **Authentication**: OAuth2 Bearer tokens
- **Endpoints**: `/api/v8/{module}` for CRUD operations
- **Features**: Filtering, sorting, pagination, sparse fieldsets, includes

### 8.2 Legacy APIs
- **SOAP API** (v2-v4.1): XML-based, backward compatible
- **JSON-RPC API**: JSON-based remote procedure calls
- **Custom Entry Points**: Direct PHP endpoints for specific operations

## 9. Integration Points

| Integration | Technology | Purpose |
|-------------|-----------|---------|
| **Email** | IMAP/SMTP (PHPMailer) | Inbound/outbound email |
| **Calendar** | CalDAV/iCal | Calendar sync |
| **Search** | Elasticsearch/Lucene | Full-text search |
| **Maps** | Google Maps API | Geolocation |
| **Social** | Twitter, Facebook APIs | Social CRM |
| **OAuth Providers** | Google, Microsoft | External auth |
| **Document Storage** | Local filesystem | File attachments |
| **PDF** | TCPDF | Report/quote generation |

## 10. Deployment Architecture (Typical)

```
                    +-----------+
                    |   Users   |
                    +-----+-----+
                          |
                    +-----v-----+
                    |   Nginx   |
                    |   (SSL)   |
                    +-----+-----+
                          |
                    +-----v-----+
                    | PHP-FPM   |
                    | (SuiteCRM)|
                    +-----+-----+
                          |
              +-----------+-----------+
              |                       |
        +-----v-----+          +-----v-----+
        |  MySQL/    |          |  File     |
        |  MariaDB   |          |  Storage  |
        +------------+          +-----------+
```

## 11. Key Technical Debt & Limitations

1. **Monolithic Architecture**: All 123 modules tightly coupled in a single codebase
2. **Legacy Frontend**: jQuery/YUI-based UI, not responsive, poor mobile experience
3. **PHP Templating**: Smarty templates mixed with business logic
4. **Database Coupling**: Direct SQL queries mixed with ORM calls
5. **No Horizontal Scaling**: Single database, file-based sessions
6. **Limited API Coverage**: Not all modules fully exposed via REST API V8
7. **Security Concerns**: Legacy MD5 password hashing in some areas
8. **No Container Support**: Not designed for containerized deployment
9. **Synchronous Processing**: No event-driven architecture, batch processing limited
10. **Testing**: Limited unit test coverage, no integration test suite
