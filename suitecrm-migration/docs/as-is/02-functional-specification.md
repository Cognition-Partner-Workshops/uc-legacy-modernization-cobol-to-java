# SuiteCRM As-Is Functional Specification

## 1. Overview

SuiteCRM provides comprehensive Customer Relationship Management functionality organized into functional domains: Sales, Marketing, Customer Service, Collaboration, Reporting, and Administration.

## 2. User Roles & Access

### 2.1 Built-in Roles
| Role | Description | Access Level |
|------|------------|--------------|
| **Administrator** | Full system access, user management, configuration | All modules, all operations |
| **Regular User** | Standard CRM operations | Assigned modules per ACL role |
| **Group User** | Collaboration-focused access | Security group-scoped records |
| **Portal User** | External customer access | Cases, Knowledge Base (limited) |

### 2.2 Permission Matrix
| Module | Admin | Sales Rep | Support Agent | Marketing |
|--------|-------|-----------|---------------|-----------|
| Accounts | CRUD | CRUD (own) | Read | Read |
| Contacts | CRUD | CRUD (own) | CRUD (own) | Read |
| Leads | CRUD | CRUD (own) | None | CRUD (own) |
| Opportunities | CRUD | CRUD (own) | Read | Read |
| Cases | CRUD | Read | CRUD (own) | None |
| Campaigns | CRUD | Read | None | CRUD (own) |
| Users | CRUD | Read (own) | Read (own) | Read (own) |
| Reports | CRUD | CRUD (own) | CRUD (own) | CRUD (own) |

## 3. Sales Module Functions

### 3.1 Account Management
- **List Accounts**: Paginated list with search, filter, sort
- **View Account**: Detail view with related contacts, opportunities, cases, activities
- **Create/Edit Account**: Form with validation (name, industry, address, phone, website)
- **Delete Account**: Soft delete with cascade check
- **Import Accounts**: CSV/spreadsheet bulk import with field mapping
- **Merge Accounts**: Deduplication with merge wizard

### 3.2 Contact Management
- **List Contacts**: Alphabetical, by account, by assignment
- **View Contact**: Full profile with related accounts, opportunities, activities, history
- **Create/Edit Contact**: Personal info, address, communication preferences
- **Portal Access**: Enable/disable customer portal login for contacts
- **Email Integration**: Link emails to contacts, track communication history
- **vCard Import/Export**: Standard business card format support

### 3.3 Lead Management
- **Lead Capture**: Web-to-lead forms, email parsing, manual entry
- **Lead Qualification**: Status tracking (New, Assigned, In Process, Converted, Dead)
- **Lead Conversion**: Convert to Contact + Account + Opportunity
- **Lead Assignment**: Round-robin, manual, workflow-based assignment rules
- **Duplicate Detection**: Automatic duplicate checking on creation

### 3.4 Opportunity Management
- **Pipeline Tracking**: Stage-based tracking (Prospecting → Closed Won/Lost)
- **Revenue Forecasting**: Amount, probability, expected close date
- **Line Items**: Products/services with quantity, pricing, discounts
- **Sales Stages**: Customizable pipeline stages with probability mapping
- **Win/Loss Analysis**: Tracking of outcomes with reasons

### 3.5 Quotes & Invoicing (SuiteCRM Extension)
- **Quote Generation**: Line items, taxes, shipping, discounts, terms
- **PDF Generation**: Customizable PDF templates for quotes
- **Invoice Creation**: Convert quotes to invoices
- **Contract Management**: Contract lifecycle tracking
- **Product Catalog**: Products with categories, pricing tiers

## 4. Marketing Module Functions

### 4.1 Campaign Management
- **Campaign Types**: Email, Newsletter, Telesales, Mail, Web
- **Target Lists**: Dynamic and static prospect lists
- **Email Templates**: WYSIWYG email template editor with merge fields
- **Campaign Scheduling**: Automated email dispatch with throttling
- **Bounce Processing**: Automatic bounce detection and handling
- **Opt-out Management**: Unsubscribe link handling, compliance tracking

### 4.2 Campaign Analytics
- **Response Tracking**: Opens, clicks, bounces, opt-outs
- **ROI Tracking**: Budget vs. revenue attribution
- **Conversion Tracking**: Lead/opportunity generation from campaigns
- **A/B Testing**: Basic split testing via target lists

### 4.3 Targets/Prospects
- **Prospect Management**: Pre-lead marketing contacts
- **List Management**: Create, segment, and manage prospect lists
- **Data Enrichment**: Import and merge prospect data

## 5. Customer Service Functions

### 5.1 Case Management
- **Case Creation**: Via portal, email-to-case, manual entry
- **Case Routing**: Assignment rules, escalation policies
- **Case Status**: New → Assigned → Closed (with custom statuses)
- **Priority Levels**: P1 (Critical) through P4 (Low)
- **Case History**: Full audit trail of updates, emails, notes
- **SLA Tracking**: Business hours-aware response/resolution timers

### 5.2 Knowledge Base
- **Article Management**: Rich text articles with categories
- **Category Hierarchy**: Multi-level category tree
- **Search**: Full-text search across knowledge base
- **Portal Access**: Customer self-service via external portal
- **Article Ratings**: User feedback on articles

### 5.3 Bug Tracking
- **Bug Reports**: Track software defects with severity, priority
- **Status Workflow**: New → Assigned → Closed/Rejected
- **Release Tracking**: Associate bugs with product releases

## 6. Activity & Collaboration Functions

### 6.1 Calendar & Scheduling
- **Shared Calendar**: Team calendar with meeting scheduling
- **Call Logging**: Inbound/outbound call tracking with notes
- **Meeting Management**: Schedule, invite, track attendance
- **Task Management**: Task creation, assignment, due dates
- **Reminders**: Email and popup reminders for activities

### 6.2 Email Management
- **Inbound Email**: IMAP mailbox monitoring and import
- **Outbound Email**: SMTP sending with template support
- **Email Archiving**: Automatic association with CRM records
- **Email Compose**: Rich text editor with attachments
- **Group Inboxes**: Shared email accounts for teams

### 6.3 Notes & Attachments
- **Note Creation**: Rich text notes linked to any CRM record
- **File Attachments**: Upload and associate files with records
- **Document Management**: Version-controlled document library

## 7. Reporting & Analytics Functions

### 7.1 Report Builder (AOR_Reports)
- **Report Types**: Tabular, Summary, Matrix
- **Data Sources**: Any module with field selection
- **Conditions**: AND/OR condition groups with operators
- **Grouping**: Group by fields with aggregate functions (SUM, AVG, COUNT, MIN, MAX)
- **Charts**: Bar, Pie, Line, Funnel chart visualization
- **Scheduling**: Automated report generation and email delivery

### 7.2 Dashboards
- **Dashboard Builder**: Drag-and-drop dashlet arrangement
- **Standard Dashlets**: Pipeline, My Activities, Top Accounts, Charts
- **Custom Dashlets**: Configurable data dashlets
- **Per-User Dashboards**: Personalized home page layouts

### 7.3 Export & Integration
- **CSV Export**: Bulk data export from any list view
- **PDF Reports**: Formatted PDF report output
- **Scheduled Reports**: Automated email delivery of reports

## 8. Administration Functions

### 8.1 User Management
- **CRUD Operations**: Create, read, update, deactivate users
- **Role Assignment**: Assign ACL roles and security groups
- **Password Policy**: Complexity requirements, expiration
- **Login Audit**: Track login attempts and sessions

### 8.2 System Configuration
- **Company Settings**: Name, logo, address, fiscal year
- **Email Settings**: SMTP configuration, email templates
- **Module Management**: Enable/disable modules, rename tabs
- **Field Customization**: Studio for custom fields, layouts, relationships
- **Dropdown Editor**: Manage dropdown lists
- **Workflow Management**: Create automation rules

### 8.3 Workflow Engine (AOW_WorkFlow)
- **Trigger Types**: On create, on update, on schedule
- **Conditions**: Field-based condition evaluation
- **Actions**: Update fields, send email, create record, calculate fields
- **Processing**: Sequential action execution with logging

### 8.4 Scheduler
- **Cron Jobs**: PHP-based scheduled task execution
- **Built-in Jobs**: Email send, workflow processing, search indexing
- **Custom Jobs**: Pluggable job framework

## 9. Integration Functions

### 9.1 Email Integration
- **IMAP Monitoring**: Automatic inbound email processing
- **SMTP Sending**: Outbound email via configured SMTP servers
- **Bounce Handling**: Automatic bounce detection and processing
- **Email-to-Case**: Create support cases from emails

### 9.2 External Authentication
- **LDAP/AD**: Active Directory integration for SSO
- **SAML 2.0**: Enterprise SSO via SAML providers
- **OAuth2**: API authentication for third-party apps

### 9.3 REST API (V8)
- **JSON:API Compliant**: Standard CRUD operations for all modules
- **OAuth2 Authentication**: Bearer token security
- **Filtering & Sorting**: Server-side data operations
- **Relationships**: Navigate and manage related records

## 10. Cross-Cutting Concerns

### 10.1 Audit Trail
- Every audited module maintains an `_audit` table
- Tracks field-level changes with before/after values
- Records timestamp, user, and change type

### 10.2 Soft Delete
- All records use `deleted` flag (0/1) instead of physical delete
- Deleted records hidden from normal queries but recoverable

### 10.3 Multi-Language
- Language packs in `modules/{Module}/language/` directories
- Label-based translation system
- Supports 30+ languages

### 10.4 Currency Support
- Multi-currency with exchange rate management
- Per-record currency association
- System default currency configuration

### 10.5 Search
- **Basic Search**: Module-level field-based search
- **Global Search**: Cross-module full-text search (Elasticsearch/Lucene)
- **Advanced Search**: Multi-field search with operators
