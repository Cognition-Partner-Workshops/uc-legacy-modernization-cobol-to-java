# CardDemo API Contract

All child sessions MUST follow this contract exactly.

## DynamoDB Table Schemas

### carddemo-accounts
- **PK**: `acct_id` (String) — 11-digit zero-padded account ID
- Attributes: `active_status`, `curr_bal`, `credit_limit`, `cash_credit_limit`, `open_date`, `expiration_date`, `reissue_date`, `curr_cyc_credit`, `curr_cyc_debit`, `group_id`

### carddemo-customers
- **PK**: `cust_id` (String) — 9-digit zero-padded customer ID
- Attributes: `first_name`, `middle_name`, `last_name`, `addr_line_1`, `addr_line_2`, `addr_line_3`, `state_cd`, `country_cd`, `zip`, `phone_1`, `phone_2`, `ssn`, `govt_issued_id`, `dob`, `eft_account_id`, `pri_card_holder_ind`, `fico_score`

### carddemo-cards
- **PK**: `card_num` (String) — 16-digit card number
- Attributes: `acct_id`, `cust_id`, `card_active_status`, `expiry_date`

### carddemo-card-xref
- **PK**: `card_num` (String) — 16-digit card number
- Attributes: `acct_id`, `cust_id`

### carddemo-transactions
- **PK**: `tran_id` (String) — 16-char transaction ID
- **SK**: `tran_orig_ts` (String) — ISO timestamp
- Attributes: `tran_type_cd`, `tran_cat_cd`, `tran_source`, `tran_desc`, `tran_amt`, `merchant_id`, `merchant_name`, `merchant_city`, `merchant_zip`, `card_num`, `tran_proc_ts`, `acct_id`

### carddemo-transaction-types
- **PK**: `type_cd` (String) — 2-digit type code
- Attributes: `type_desc`, `type_count`

### carddemo-transaction-categories
- **PK**: `type_cd` (String) — 2-digit type code
- **SK**: `cat_cd` (String) — 4-digit category code
- Attributes: `cat_desc`, `cat_count`

## API Endpoints (API Gateway)

Base path: `/prod`

### Accounts
- `GET /accounts` — List all accounts (paginated via `last_key` query param)
- `GET /accounts/{acct_id}` — Get account details
- `PUT /accounts/{acct_id}` — Update account (body: JSON with updatable fields)

### Customers
- `GET /customers` — List all customers (paginated)
- `GET /customers/{cust_id}` — Get customer details

### Cards
- `GET /cards` — List all cards (paginated)
- `GET /cards?acct_id={acct_id}` — List cards for an account
- `GET /cards/{card_num}` — Get card details

### Transactions
- `GET /transactions` — List transactions (paginated, filter by `acct_id` or `card_num`)
- `GET /transactions/{tran_id}` — Get transaction details
- `POST /transactions` — Create a new transaction

### Batch Operations
- `POST /batch/process-daily` — Trigger daily transaction processing

### Reference Data
- `GET /transaction-types` — List transaction types
- `GET /transaction-categories` — List transaction categories

## Response Format

All responses follow:
```json
{
  "statusCode": 200,
  "body": {
    "data": [...],
    "last_key": "optional_pagination_key"
  }
}
```

Error responses:
```json
{
  "statusCode": 400|404|500,
  "body": {
    "error": "description"
  }
}
```

## Lambda Handler Conventions

- Each Lambda function file exports a `handler(event, context)` function
- Use `boto3` DynamoDB resource interface
- Table name prefix: `carddemo-` (passed via environment variable `TABLE_PREFIX`)
- Region passed via environment variable `AWS_REGION` (default: `us-east-1`)
- CORS headers included in every response:
  ```python
  headers = {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
  }
  ```

## Data Types

- All monetary values stored as strings with 2 decimal places (e.g., "19400.00")
- Dates stored as ISO strings "YYYY-MM-DD"
- Timestamps stored as ISO strings "YYYY-MM-DDTHH:MM:SS"
- IDs stored as zero-padded strings (account: 11 digits, customer: 9 digits)
