# CardDemo — COBOL-to-AWS Serverless Migration

This directory contains a fully serverless AWS migration of the mainframe COBOL
**CardDemo** credit-card management application. It replaces VSAM files, CICS
online programs, and JCL batch jobs with DynamoDB, Lambda, API Gateway, and an
S3-hosted single-page frontend — all provisioned with Terraform.

> Built as a working demonstration. The original COBOL sources under `app/`
> are left untouched; this migration is additive.

## Architecture

```
                         ┌──────────────────────────┐
   Browser  ───────────► │ S3 static website (SPA)  │
                         │  index.html/app.js/css   │
                         └────────────┬─────────────┘
                                      │ fetch (CORS)
                                      ▼
                         ┌──────────────────────────┐
                         │ HTTP API Gateway v2       │
                         │  stage: prod, 14 routes   │
                         └────────────┬─────────────┘
                                      │ AWS_PROXY
                                      ▼
              ┌───────────────────────┴───────────────────────┐
              ▼                                                ▼
   ┌────────────────────┐                        ┌────────────────────────┐
   │ API Lambda (py3.12)│                        │ Batch Lambda (py3.12)  │
   │ handler.py + db.py │                        │ daily_processor.py     │
   │ 14 REST routes     │                        │ CBTRN01C/02C/03C logic │
   └─────────┬──────────┘                        └───────────┬────────────┘
             │                                                │
             └───────────────────┬────────────────────────────┘
                                 ▼
                  ┌──────────────────────────────┐
                  │ DynamoDB (7 tables, on-demand)│
                  └──────────────────────────────┘
```

| Layer | Service |
|-------|---------|
| Database | DynamoDB (on-demand) — 7 tables replacing VSAM files |
| Compute | Lambda (Python 3.12) — 1 API handler + 1 batch handler |
| API | HTTP API Gateway v2 — single `prod` stage, CORS enabled |
| Frontend | S3 static website — vanilla HTML/CSS/JS, no build step |
| Scheduling | EventBridge (optional, disabled) — daily batch trigger |
| IaC | Terraform |

## COBOL → AWS mapping

| COBOL / Mainframe | AWS replacement |
|-------------------|-----------------|
| VSAM `ACCTDAT` (KSDS) | DynamoDB `carddemo-accounts` |
| VSAM `CUSTDAT` (KSDS) | DynamoDB `carddemo-customers` |
| VSAM `CARDDAT` (KSDS) + AIX | DynamoDB `carddemo-cards` + `acct_id-index` |
| VSAM `CXACAIX` (xref) | DynamoDB `carddemo-card-xref` |
| VSAM `TRANSACT` (KSDS) | DynamoDB `carddemo-transactions` + GSIs |
| VSAM `TRANTYPE` | DynamoDB `carddemo-transaction-types` |
| VSAM `TRANCATG` | DynamoDB `carddemo-transaction-categories` |
| CICS online programs (COACTVWC, COCRDLIC, COTRN00C, …) | API Lambda routes |
| JCL batch (CBTRN01C/02C/03C) | Batch Lambda `daily_processor.py` |
| BMS 3270 maps | S3-hosted SPA frontend |
| IDCAMS / data load | `data-migration/load_data.py` |

## Directory layout

```
aws-migration/
  README.md            # this file
  API_CONTRACT.md      # DynamoDB schemas + REST specs (source of truth)
  terraform/           # all infrastructure as code
  lambdas/api/         # API Lambda (handler.py, db.py)
  lambdas/batch/       # Batch Lambda (daily_processor.py)
  data-migration/      # COBOL flat-file parsers + DynamoDB loader
  frontend/            # static SPA (index.html, styles.css, app.js, config.js)
```

## Deploy

Prerequisites: Terraform >= 1.5, AWS credentials with permissions for DynamoDB,
Lambda, API Gateway, IAM, S3, and CloudWatch. Region: `us-east-1`.

```bash
# 1. Provision infrastructure
cd aws-migration/terraform
terraform init
terraform apply

# 2. Load seed data from the COBOL flat files into DynamoDB
cd ../data-migration
pip install -r requirements.txt
python load_data.py --data-dir ../../app/data/ASCII

# 3. Point the frontend at the deployed API and upload it
API_URL=$(terraform -chdir=../terraform output -raw api_url)
BUCKET=$(terraform -chdir=../terraform output -raw frontend_bucket)
printf "window.CARDDEMO_API_URL = '%s';\n" "$API_URL" > ../frontend/config.js
aws s3 sync ../frontend "s3://$BUCKET" --delete

# 4. Open the site
terraform -chdir=../terraform output -raw frontend_url
```

## Components

- **`terraform/`** — `dynamodb.tf` (7 tables + GSIs), `iam.tf` (least-privilege
  Lambda role), `lambda.tf` (both functions + log groups + optional schedule),
  `api_gateway.tf` (HTTP API + 14 routes + prod stage), `s3.tf` (frontend
  website bucket + private data bucket), `outputs.tf`.
- **`lambdas/api/`** — single regex-routed `handler.py` implementing all REST
  routes, plus `db.py` DynamoDB helpers. Strips the API Gateway stage prefix,
  paginates via `last_key`, and serializes Decimals as strings.
- **`lambdas/batch/`** — `daily_processor.py` replicating the CBTRN01C/02C/03C
  daily posting logic using a read-compute-write pattern over string-typed money
  fields.
- **`data-migration/`** — `parsers.py` (fixed-width COBOL parsers with EBCDIC
  overpunch sign decoding) and `load_data.py` (batch-loads parsed records into
  DynamoDB).
- **`frontend/`** — dark-sidebar SPA with dashboard, accounts, customers, cards,
  transactions, batch processing, and system-info sections.

See [`API_CONTRACT.md`](./API_CONTRACT.md) for the authoritative DynamoDB
schemas, COBOL record layouts, and REST endpoint specifications.
