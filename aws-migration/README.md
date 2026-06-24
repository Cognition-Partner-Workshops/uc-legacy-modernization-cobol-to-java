# CardDemo AWS Migration

Migrated subset of the CardDemo mainframe COBOL credit card management system to AWS-native serverless services.

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   S3 Static     │     │   API Gateway    │     │   DynamoDB      │
│   Website       │────▶│   (REST API)     │────▶│   Tables        │
│   (Frontend)    │     │                  │     │                 │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                        ┌────────▼─────────┐     ┌─────────────────┐
                        │  Lambda Functions│     │  EventBridge    │
                        │  (API Handlers)  │     │  (Scheduler)    │
                        └──────────────────┘     └────────┬────────┘
                                                          │
                                                 ┌────────▼────────┐
                                                 │  Batch Lambda   │
                                                 │  (Daily Txns)   │
                                                 └─────────────────┘
```

## AWS Services Used

| Service | Purpose | Cost Model |
|---------|---------|------------|
| DynamoDB (On-Demand) | Data storage replacing VSAM files | Pay-per-request |
| Lambda | Business logic replacing COBOL programs | Pay-per-invocation |
| API Gateway (REST) | HTTP API replacing CICS transactions | Pay-per-request |
| S3 | Static website hosting + data files | Minimal storage cost |
| EventBridge | Batch job scheduling replacing JCL/Control-M | Free tier |
| IAM | Security replacing RACF | Free |

## COBOL-to-AWS Mapping

| COBOL Component | AWS Equivalent | Status |
|----------------|----------------|--------|
| VSAM KSDS files | DynamoDB tables | Migrated |
| CICS online programs | Lambda + API Gateway | Migrated |
| JCL batch jobs | Lambda + EventBridge | Migrated |
| BMS screen maps | React web frontend | Migrated |
| RACF security | IAM roles + API keys | Migrated |
| COBOL copybooks | Python data models | Migrated |

## Migrated Subsystems

### Online (CICS → API Gateway + Lambda)
- **Account Management** (COACTVWC, COACTUPC) → GET/PUT /accounts
- **Card Management** (COCRDLIC, COCRDSLC, COCRDUPC) → GET/PUT /cards
- **Transaction Viewing** (COTRN00C, COTRN01C, COTRN02C) → GET/POST /transactions
- **Customer Lookup** (CBCUS01C) → GET /customers

### Batch (JCL → Lambda + EventBridge)
- **Daily Transaction Posting** (CBTRN01C, CBTRN02C, CBTRN03C) → Scheduled Lambda
- **Interest Calculation** → Batch Lambda

## Directory Structure

```
aws-migration/
├── terraform/           # All IaC — `terraform apply` to deploy, `terraform destroy` to teardown
├── lambdas/
│   ├── api/             # REST API Lambda handlers
│   ├── batch/           # Batch processing Lambda handlers
│   └── layers/shared/   # Shared utilities (data models, DynamoDB helpers)
├── data-migration/      # Scripts to parse COBOL flat files and seed DynamoDB
├── frontend/            # Static React dashboard hosted on S3
└── README.md
```

## Quick Start

```bash
# 1. Deploy infrastructure
cd terraform
terraform init
terraform apply

# 2. Seed data from COBOL flat files
cd ../data-migration
pip install -r requirements.txt
python load_data.py

# 3. Access the frontend
# URL is output by terraform: frontend_url
```

## Teardown

```bash
cd terraform
terraform destroy
```
