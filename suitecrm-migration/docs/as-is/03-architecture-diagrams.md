# SuiteCRM As-Is Architecture Diagrams

## 1. System Context Diagram (C4 Level 1)

```mermaid
graph TB
    subgraph External
        EU[End Users<br/>Browser/Mobile]
        EA[External Applications<br/>ERP, Email, Maps]
        ES[Email Servers<br/>SMTP/IMAP]
        LDAP[LDAP/AD<br/>Directory Service]
        SAML[SAML IdP<br/>SSO Provider]
    end

    subgraph SuiteCRM System
        SCRM[SuiteCRM Application<br/>PHP Monolith]
    end

    subgraph Data Stores
        DB[(MySQL/MariaDB<br/>Database)]
        FS[File System<br/>Document Storage]
        CACHE[APCu/Redis<br/>Cache]
        IDX[Elasticsearch<br/>Search Index]
    end

    EU -->|HTTP/HTTPS| SCRM
    EA -->|REST API V8<br/>SOAP API| SCRM
    SCRM -->|SMTP/IMAP| ES
    SCRM -->|LDAP Bind| LDAP
    SCRM -->|SAML Assert| SAML
    SCRM -->|SQL| DB
    SCRM -->|File I/O| FS
    SCRM -->|Get/Set| CACHE
    SCRM -->|Index/Search| IDX
```

## 2. Container Diagram (C4 Level 2)

```mermaid
graph TB
    subgraph Load Balancer
        LB[Nginx/Apache<br/>Reverse Proxy + SSL]
    end

    subgraph Application Server
        PHP[PHP-FPM<br/>Process Manager]

        subgraph SuiteCRM Application
            MVC[MVC Controller<br/>SugarController]
            MOD[Module Layer<br/>123 Modules]
            BL[Business Logic<br/>SugarBean ORM]
            TPL[View Layer<br/>Smarty Templates]
            API8[REST API V8<br/>Slim Framework]
            SOAP[SOAP/JSON-RPC<br/>Legacy API]
            WF[Workflow Engine<br/>AOW_WorkFlow]
            SCH[Scheduler<br/>Cron-based Jobs]
            ACL[ACL Engine<br/>Roles + Security Groups]
        end
    end

    subgraph Data Layer
        DB[(MySQL/MariaDB)]
        FS[File Storage<br/>upload/ directory]
        SESS[Session Store<br/>File/DB/Redis]
    end

    LB --> PHP
    PHP --> MVC
    MVC --> MOD
    MOD --> BL
    MOD --> TPL
    MVC --> API8
    MVC --> SOAP
    BL --> ACL
    BL --> DB
    BL --> FS
    PHP --> SESS
    WF --> BL
    SCH --> WF
```

## 3. Module Dependency Diagram

```mermaid
graph LR
    subgraph Core Framework
        SB[SugarBean<br/>Base ORM]
        SC[SugarController<br/>Request Router]
        SV[SugarView<br/>Base View]
        DB[DBManager<br/>Database Abstraction]
    end

    subgraph Sales Modules
        ACC[Accounts]
        CON[Contacts]
        LED[Leads]
        OPP[Opportunities]
        QUO[Quotes<br/>AOS_Quotes]
        INV[Invoices<br/>AOS_Invoices]
        PRD[Products<br/>AOS_Products]
    end

    subgraph Service Modules
        CAS[Cases]
        BUG[Bugs]
        KB[Knowledge Base<br/>AOK]
    end

    subgraph Marketing Modules
        CMP[Campaigns]
        TGT[Prospects/<br/>Targets]
        ETM[Email<br/>Templates]
    end

    subgraph Activity Modules
        CAL[Calls]
        MTG[Meetings]
        TSK[Tasks]
        EML[Emails]
        NTS[Notes]
    end

    subgraph Admin Modules
        USR[Users]
        ACR[ACL Roles]
        SG[Security<br/>Groups]
        WF[Workflow<br/>AOW]
        RPT[Reports<br/>AOR]
    end

    SB --> ACC & CON & LED & OPP & CAS & CMP & USR
    ACC --- CON
    ACC --- OPP
    ACC --- CAS
    CON --- OPP
    CON --- CAS
    LED -.->|Convert| CON & ACC & OPP
    OPP --> QUO
    QUO --> INV
    QUO --> PRD
    CMP --> TGT
    CMP --> ETM
    USR --> ACR
    USR --> SG
    ACC --- CAL & MTG & TSK & EML & NTS
    CON --- CAL & MTG & TSK & EML & NTS
    WF --> ACC & CON & LED & OPP & CAS
    RPT --> ACC & CON & LED & OPP & CAS & CMP
```

## 4. Database Entity-Relationship Diagram (Core)

```mermaid
erDiagram
    USERS {
        char36 id PK
        varchar user_name
        varchar user_hash
        varchar first_name
        varchar last_name
        varchar email
        tinyint is_admin
        varchar status
        datetime date_entered
        datetime date_modified
        tinyint deleted
    }

    ACCOUNTS {
        char36 id PK
        varchar name
        varchar account_type
        varchar industry
        varchar phone_office
        varchar website
        text billing_address
        text shipping_address
        char36 assigned_user_id FK
        datetime date_entered
        datetime date_modified
        tinyint deleted
    }

    CONTACTS {
        char36 id PK
        varchar first_name
        varchar last_name
        varchar title
        varchar phone_work
        varchar email1
        char36 account_id FK
        char36 assigned_user_id FK
        datetime date_entered
        datetime date_modified
        tinyint deleted
    }

    LEADS {
        char36 id PK
        varchar first_name
        varchar last_name
        varchar company
        varchar status
        varchar lead_source
        char36 assigned_user_id FK
        char36 converted_contact_id FK
        tinyint converted
        tinyint deleted
    }

    OPPORTUNITIES {
        char36 id PK
        varchar name
        decimal amount
        varchar sales_stage
        date date_closed
        float probability
        varchar lead_source
        char36 account_id FK
        char36 assigned_user_id FK
        tinyint deleted
    }

    CASES {
        char36 id PK
        varchar name
        int case_number
        varchar status
        varchar priority
        varchar type
        text description
        text resolution
        char36 account_id FK
        char36 assigned_user_id FK
        tinyint deleted
    }

    CAMPAIGNS {
        char36 id PK
        varchar name
        varchar campaign_type
        varchar status
        date start_date
        date end_date
        decimal budget
        decimal actual_cost
        char36 assigned_user_id FK
        tinyint deleted
    }

    CALLS {
        char36 id PK
        varchar name
        varchar direction
        varchar status
        datetime date_start
        int duration_hours
        int duration_minutes
        char36 assigned_user_id FK
        tinyint deleted
    }

    MEETINGS {
        char36 id PK
        varchar name
        varchar status
        datetime date_start
        datetime date_end
        varchar location
        char36 assigned_user_id FK
        tinyint deleted
    }

    TASKS {
        char36 id PK
        varchar name
        varchar status
        varchar priority
        date date_due
        char36 assigned_user_id FK
        char36 contact_id FK
        tinyint deleted
    }

    EMAILS {
        char36 id PK
        varchar name
        varchar type
        varchar status
        datetime date_sent
        char36 assigned_user_id FK
        tinyint deleted
    }

    NOTES {
        char36 id PK
        varchar name
        text description
        varchar filename
        char36 contact_id FK
        char36 assigned_user_id FK
        tinyint deleted
    }

    ACL_ROLES {
        char36 id PK
        varchar name
        text description
        tinyint deleted
    }

    ACL_ROLES_ACTIONS {
        char36 id PK
        char36 role_id FK
        char36 action_id FK
        int access_override
        tinyint deleted
    }

    SECURITY_GROUPS {
        char36 id PK
        varchar name
        text description
        tinyint noninheritable
        tinyint deleted
    }

    AOS_QUOTES {
        char36 id PK
        varchar name
        varchar stage
        date expiration
        decimal total_amount
        decimal subtotal_amount
        decimal tax_amount
        decimal shipping_amount
        char36 billing_account_id FK
        char36 assigned_user_id FK
        tinyint deleted
    }

    AOS_INVOICES {
        char36 id PK
        varchar name
        varchar status
        date due_date
        decimal total_amount
        char36 quote_id FK
        char36 billing_account_id FK
        tinyint deleted
    }

    USERS ||--o{ ACCOUNTS : "assigned_user_id"
    USERS ||--o{ CONTACTS : "assigned_user_id"
    USERS ||--o{ LEADS : "assigned_user_id"
    USERS ||--o{ OPPORTUNITIES : "assigned_user_id"
    USERS ||--o{ CASES : "assigned_user_id"
    ACCOUNTS ||--o{ CONTACTS : "account_id"
    ACCOUNTS ||--o{ OPPORTUNITIES : "account_id"
    ACCOUNTS ||--o{ CASES : "account_id"
    CONTACTS }o--o{ OPPORTUNITIES : "opportunities_contacts"
    LEADS ||--o| CONTACTS : "converted_contact_id"
    CONTACTS ||--o{ TASKS : "contact_id"
    CONTACTS ||--o{ NOTES : "contact_id"
    ACL_ROLES ||--o{ ACL_ROLES_ACTIONS : "role_id"
    USERS }o--o{ ACL_ROLES : "acl_roles_users"
    USERS }o--o{ SECURITY_GROUPS : "securitygroups_users"
    AOS_QUOTES ||--o{ AOS_INVOICES : "quote_id"
    ACCOUNTS ||--o{ AOS_QUOTES : "billing_account_id"
```

## 5. Security Model Diagram

```mermaid
graph TB
    subgraph Authentication
        LP[Login Page]
        SAML[SAML SSO]
        LDAP[LDAP/AD]
        OAuth[OAuth2<br/>API Auth]
    end

    subgraph Authorization Engine
        ACL[ACL Engine]
        RL[Role Resolver]
        SG[Security Group<br/>Resolver]
        ML[Module-Level<br/>Permissions]
        RL2[Record-Level<br/>Permissions]
    end

    subgraph Permission Store
        UR[users table]
        ARU[acl_roles_users<br/>M:N]
        AR[acl_roles table]
        ARA[acl_roles_actions<br/>Permission Overrides]
        AA[acl_actions table<br/>Module Permissions]
        SGT[securitygroups table]
        SGU[securitygroups_users<br/>M:N]
        SGR[securitygroups_records<br/>M:N]
    end

    LP --> ACL
    SAML --> ACL
    LDAP --> ACL
    OAuth --> ACL
    ACL --> RL
    ACL --> SG
    RL --> ML
    SG --> RL2
    RL --> UR --> ARU --> AR --> ARA --> AA
    SG --> SGT --> SGU
    SG --> SGR
```

## 6. Data Flow Diagram - Lead to Opportunity Conversion

```mermaid
sequenceDiagram
    actor User
    participant UI as SuiteCRM UI
    participant Ctrl as SugarController
    participant LM as Leads Module
    participant CM as Contacts Module
    participant AM as Accounts Module
    participant OM as Opportunities Module
    participant DB as MySQL Database
    participant WF as Workflow Engine

    User->>UI: Click "Convert Lead"
    UI->>Ctrl: POST /index.php?module=Leads&action=ConvertLead
    Ctrl->>LM: initiate_conversion(lead_id)
    LM->>DB: SELECT * FROM leads WHERE id=?

    Note over UI,DB: Step 1: Create Contact
    LM->>CM: create_contact(lead_data)
    CM->>DB: INSERT INTO contacts (...)
    DB-->>CM: contact_id

    Note over UI,DB: Step 2: Create/Link Account
    LM->>AM: create_or_link_account(lead.company)
    AM->>DB: INSERT INTO accounts (...)
    DB-->>AM: account_id

    Note over UI,DB: Step 3: Create Opportunity
    LM->>OM: create_opportunity(lead_data, account_id)
    OM->>DB: INSERT INTO opportunities (...)
    DB-->>OM: opportunity_id

    Note over UI,DB: Step 4: Update Lead
    LM->>DB: UPDATE leads SET converted=1, contact_id=?
    LM->>DB: INSERT INTO accounts_contacts (account_id, contact_id)
    LM->>DB: INSERT INTO opportunities_contacts (opportunity_id, contact_id)

    LM-->>Ctrl: conversion_result
    Ctrl-->>UI: Redirect to Contact Detail View

    Note over WF,DB: Async: Workflow Processing
    WF->>DB: Check workflow triggers for new records
    WF->>DB: Execute workflow actions (emails, field updates)
```

## 7. Batch Processing Flow

```mermaid
graph TB
    subgraph Cron Scheduler
        CR[cron.php<br/>Every Minute]
    end

    subgraph Scheduled Jobs
        J1[Email Send<br/>Queue Processing]
        J2[Workflow<br/>Processing]
        J3[Search Index<br/>Update]
        J4[Campaign<br/>Email Dispatch]
        J5[Bounce<br/>Processing]
        J6[Report<br/>Generation]
        J7[Tracker<br/>Cleanup]
        J8[Database<br/>Pruning]
    end

    subgraph Data Stores
        DB[(MySQL)]
        IDX[Elasticsearch]
        FS[File System]
        SMTP[SMTP Server]
    end

    CR --> J1 & J2 & J3 & J4 & J5 & J6 & J7 & J8
    J1 --> SMTP
    J2 --> DB
    J3 --> IDX
    J4 --> SMTP
    J5 --> DB
    J6 --> DB & FS
    J7 --> DB
    J8 --> DB
```

## 8. Deployment Architecture Diagram

```mermaid
graph TB
    subgraph Internet
        USERS[Users / Browsers]
        API_CLIENTS[API Consumers]
    end

    subgraph DMZ
        LB[Load Balancer<br/>Nginx/HAProxy]
        WAF[Web Application<br/>Firewall]
    end

    subgraph Application Tier
        WEB1[Web Server 1<br/>Apache + PHP-FPM<br/>SuiteCRM]
        WEB2[Web Server 2<br/>Apache + PHP-FPM<br/>SuiteCRM]
    end

    subgraph Data Tier
        DB_PRIMARY[(MySQL Primary<br/>Read/Write)]
        DB_REPLICA[(MySQL Replica<br/>Read-Only)]
        NFS[NFS/EFS<br/>Shared Files]
        REDIS[Redis<br/>Sessions + Cache]
        ES[Elasticsearch<br/>Search Index]
    end

    subgraph Batch Tier
        CRON[Cron Server<br/>Scheduled Jobs]
    end

    USERS --> WAF --> LB
    API_CLIENTS --> WAF --> LB
    LB --> WEB1 & WEB2
    WEB1 & WEB2 --> DB_PRIMARY
    WEB1 & WEB2 --> DB_REPLICA
    WEB1 & WEB2 --> NFS
    WEB1 & WEB2 --> REDIS
    WEB1 & WEB2 --> ES
    CRON --> DB_PRIMARY
    CRON --> NFS
    DB_PRIMARY --> DB_REPLICA
```
