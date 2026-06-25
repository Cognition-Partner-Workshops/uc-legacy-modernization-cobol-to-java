# CardDemo AWS Migration — API & Data Contract

This document is the **source of truth** for all components of the CardDemo
serverless migration. The Lambda API handler, the data-migration / batch code,
and the web frontend are built by independent sessions and MUST conform to the
schemas, field names, and response shapes defined here.

Region: `us-east-1` · Table name prefix: `carddemo` · Lambda runtime: Python 3.12

---

## 1. DynamoDB tables

All tables use **on-demand** (`PAY_PER_REQUEST`) billing. **All numeric values
(balances, amounts, limits, rates) are stored as DynamoDB *strings*** (e.g.
`"369.00"`), NOT the DynamoDB Number type. IDs are stored as zero-padded strings
exactly as they appear in the COBOL data (e.g. `acct_id = "00000000001"`).

| Table | PK (type) | SK (type) | GSIs |
|-------|-----------|-----------|------|
| `carddemo-accounts` | `acct_id` (S) | — | — |
| `carddemo-customers` | `cust_id` (S) | — | — |
| `carddemo-cards` | `card_num` (S) | — | `acct_id-index` (HASH `acct_id`) |
| `carddemo-card-xref` | `card_num` (S) | — | — |
| `carddemo-transactions` | `tran_id` (S) | `tran_orig_ts` (S) | `card_num-index` (HASH `card_num`), `acct_id-index` (HASH `acct_id`) |
| `carddemo-transaction-types` | `type_cd` (S) | — | — |
| `carddemo-transaction-categories` | `type_cd` (S) | `cat_cd` (S) | — |

All GSIs use `ProjectionType = ALL`.

### 1.1 Item attribute schemas

Field names below are the canonical attribute names. Derived from the COBOL
copybooks (`CVACT01Y`, `CVCUS01Y`, `CVACT02Y`, `CVACT03Y`, `CVTRA05Y`,
`CVTRA03Y`, `CVTRA04Y`).

**carddemo-accounts** (from `CVACT01Y` / `acctdata.txt`)
```
acct_id            (S, PK)   zero-padded 11-digit id
acct_active_status (S)       "Y" / "N"
curr_bal           (S)       decimal string, e.g. "194.00"
credit_limit       (S)       decimal string
cash_credit_limit  (S)       decimal string
open_date          (S)       "YYYY-MM-DD"
expiration_date    (S)       "YYYY-MM-DD"
reissue_date       (S)       "YYYY-MM-DD"
curr_cyc_credit    (S)       decimal string
curr_cyc_debit     (S)       decimal string
addr_zip           (S)
group_id           (S)
```

**carddemo-customers** (from `CVCUS01Y` / `custdata.txt`)
```
cust_id            (S, PK)   zero-padded 9-digit id
first_name         (S)
middle_name        (S)
last_name          (S)
addr_line_1        (S)
addr_line_2        (S)
addr_line_3        (S)
addr_state_cd      (S)
addr_country_cd    (S)
addr_zip           (S)
phone_num_1        (S)
phone_num_2        (S)
ssn                (S)
govt_issued_id     (S)
dob                (S)       "YYYY-MM-DD"
eft_account_id     (S)
pri_card_holder_ind(S)
fico_credit_score  (S)
```

**carddemo-cards** (from `CVACT02Y` / `carddata.txt`)
```
card_num           (S, PK)   16-char card number
acct_id            (S)       zero-padded 11-digit id  (GSI acct_id-index HASH)
cvv_cd             (S)
embossed_name      (S)
expiration_date    (S)
active_status      (S)       "Y" / "N"
```

**carddemo-card-xref** (from `CVACT03Y` / `cardxref.txt`)
```
card_num           (S, PK)   16-char card number
cust_id            (S)       zero-padded 9-digit id
acct_id            (S)       zero-padded 11-digit id
```

**carddemo-transactions** (from `CVTRA05Y` / `dailytran.txt`)
```
tran_id            (S, PK)   16-char id
tran_orig_ts       (S, SK)   "YYYY-MM-DD HH:MM:SS.ffffff"
type_cd            (S)       2-char, e.g. "01"
cat_cd             (S)       4-char, e.g. "0001"
tran_source        (S)
tran_desc          (S)
amt                (S)       decimal string, e.g. "50.47"
merchant_id        (S)
merchant_name      (S)
merchant_city      (S)
merchant_zip       (S)
card_num           (S)       (GSI card_num-index HASH)
acct_id            (S)       enriched via card-xref lookup (GSI acct_id-index HASH)
tran_proc_ts       (S)
# batch fields (added by the batch processor, absent until processed):
batch_processed    (BOOL)    true once processed
```

**carddemo-transaction-types** (from `CVTRA03Y` / `trantype.txt`)
```
type_cd            (S, PK)   2-char, e.g. "01"
type_desc          (S)       e.g. "Purchase"
```

**carddemo-transaction-categories** (from `CVTRA04Y` / `trancatg.txt`)
```
type_cd            (S, PK)   2-char
cat_cd             (S, SK)   4-char
cat_desc           (S)
```

---

## 2. COBOL fixed-width record layouts (for data-migration parsers)

Files live in `app/data/ASCII/`. Each record is a fixed-width line; read the
file line by line and `rstrip("\r\n")` (some files are CRLF, some LF). Trim
trailing spaces on text fields. Strip leading zeros only where noted (IDs keep
their zero padding as the canonical string key).

### Overpunch sign decoding (`PIC S9(n)V99` fields)
The sign is encoded in the **last character** of the numeric field via EBCDIC
overpunch. `V99` means 2 implied decimal places (no literal `.` in the data).
```
Positive last digit:  {=0 A=1 B=2 C=3 D=4 E=5 F=6 G=7 H=8 I=9
Negative last digit:  }=0 J=1 K=2 L=3 M=4 N=5 O=6 P=7 Q=8 R=9
```
Decode: take all but the last char as leading digits, map the last char to its
digit + sign, concatenate digits, then insert the decimal point 2 places from
the right. Store the result as a string, e.g. `"194.00"` or `"-12.50"`.

### accounts — `acctdata.txt` (RECLN 300) — copybook `CVACT01Y`
| field | start (0-idx) | len | PIC | attr |
|-------|------|-----|-----|------|
| acct_id | 0 | 11 | 9(11) | `acct_id` (keep zero-pad) |
| acct_active_status | 11 | 1 | X(01) | `acct_active_status` |
| acct_curr_bal | 12 | 12 | S9(10)V99 | `curr_bal` (overpunch) |
| acct_credit_limit | 24 | 12 | S9(10)V99 | `credit_limit` (overpunch) |
| acct_cash_credit_limit | 36 | 12 | S9(10)V99 | `cash_credit_limit` (overpunch) |
| acct_open_date | 48 | 10 | X(10) | `open_date` |
| acct_expiration_date | 58 | 10 | X(10) | `expiration_date` |
| acct_reissue_date | 68 | 10 | X(10) | `reissue_date` |
| acct_curr_cyc_credit | 78 | 12 | S9(10)V99 | `curr_cyc_credit` (overpunch) |
| acct_curr_cyc_debit | 90 | 12 | S9(10)V99 | `curr_cyc_debit` (overpunch) |
| acct_addr_zip | 102 | 10 | X(10) | `addr_zip` |
| acct_group_id | 112 | 10 | X(10) | `group_id` |
| FILLER | 122 | 178 | X(178) | — |

### customers — `custdata.txt` (RECLN 500) — copybook `CVCUS01Y`
| field | start | len | attr |
|-------|------|-----|------|
| cust_id | 0 | 9 | `cust_id` (keep zero-pad) |
| first_name | 9 | 25 | `first_name` |
| middle_name | 34 | 25 | `middle_name` |
| last_name | 59 | 25 | `last_name` |
| addr_line_1 | 84 | 50 | `addr_line_1` |
| addr_line_2 | 134 | 50 | `addr_line_2` |
| addr_line_3 | 184 | 50 | `addr_line_3` |
| addr_state_cd | 234 | 2 | `addr_state_cd` |
| addr_country_cd | 236 | 3 | `addr_country_cd` |
| addr_zip | 239 | 10 | `addr_zip` |
| phone_num_1 | 249 | 15 | `phone_num_1` |
| phone_num_2 | 264 | 15 | `phone_num_2` |
| ssn | 279 | 9 | `ssn` |
| govt_issued_id | 288 | 20 | `govt_issued_id` |
| dob | 308 | 10 | `dob` |
| eft_account_id | 318 | 10 | `eft_account_id` |
| pri_card_holder_ind | 328 | 1 | `pri_card_holder_ind` |
| fico_credit_score | 329 | 3 | `fico_credit_score` |
| FILLER | 332 | 168 | — |

### cards — `carddata.txt` (RECLN 150) — copybook `CVACT02Y`
| field | start | len | attr |
|-------|------|-----|------|
| card_num | 0 | 16 | `card_num` |
| card_acct_id | 16 | 11 | `acct_id` (keep zero-pad) |
| card_cvv_cd | 27 | 3 | `cvv_cd` |
| card_embossed_name | 30 | 50 | `embossed_name` |
| card_expiration_date | 80 | 10 | `expiration_date` |
| card_active_status | 90 | 1 | `active_status` |
| FILLER | 91 | 59 | — |

### card-xref — `cardxref.txt` (RECLN 50) — copybook `CVACT03Y`
| field | start | len | attr |
|-------|------|-----|------|
| xref_card_num | 0 | 16 | `card_num` |
| xref_cust_id | 16 | 9 | `cust_id` (keep zero-pad) |
| xref_acct_id | 25 | 11 | `acct_id` (keep zero-pad) |
| FILLER | 36 | 14 | — |

### transactions — `dailytran.txt` (RECLN 350) — copybook `CVTRA05Y`/`CVTRA06Y`
| field | start | len | PIC | attr |
|-------|------|-----|-----|------|
| tran_id | 0 | 16 | X(16) | `tran_id` |
| tran_type_cd | 16 | 2 | X(02) | `type_cd` |
| tran_cat_cd | 18 | 4 | 9(04) | `cat_cd` (keep 4-digit) |
| tran_source | 22 | 10 | X(10) | `tran_source` |
| tran_desc | 32 | 100 | X(100) | `tran_desc` |
| tran_amt | 132 | 11 | S9(09)V99 | `amt` (overpunch) |
| tran_merchant_id | 143 | 9 | 9(09) | `merchant_id` |
| tran_merchant_name | 152 | 50 | X(50) | `merchant_name` |
| tran_merchant_city | 202 | 50 | X(50) | `merchant_city` |
| tran_merchant_zip | 252 | 10 | X(10) | `merchant_zip` |
| tran_card_num | 262 | 16 | X(16) | `card_num` |
| tran_orig_ts | 278 | 26 | X(26) | `tran_orig_ts` |
| tran_proc_ts | 304 | 26 | X(26) | `tran_proc_ts` |
| FILLER | 330 | 20 | X(20) | — |

`acct_id` for each transaction is **enriched** by looking up `card_num` in the
card-xref data and copying its `acct_id`.

### transaction-types — `trantype.txt` (RECLN 60) — copybook `CVTRA03Y`
| field | start | len | attr |
|-------|------|-----|------|
| tran_type | 0 | 2 | `type_cd` |
| tran_type_desc | 2 | 50 | `type_desc` |
| FILLER | 52 | 8 | — |

### transaction-categories — `trancatg.txt` (RECLN 60) — copybook `CVTRA04Y`
| field | start | len | attr |
|-------|------|-----|------|
| tran_type_cd | 0 | 2 | `type_cd` |
| tran_cat_cd | 2 | 4 | `cat_cd` (keep 4-digit) |
| tran_cat_type_desc | 6 | 50 | `cat_desc` |
| FILLER | 56 | 4 | — |

---

## 3. REST API (HTTP API Gateway v2, stage `prod`)

Base URL: `https://{api-id}.execute-api.us-east-1.amazonaws.com/prod`

The frontend uses `window.CARDDEMO_API_URL` (set in `config.js`) as the base.

### Response envelope
Every successful response body is JSON:
```json
{ "data": <object|array>, "last_key": <string|null> }
```
- `last_key` is present only on paginated list endpoints; it is the
  JSON-encoded DynamoDB `LastEvaluatedKey` (or `null` when no more pages).
- Errors return `{ "error": "<message>" }` with an appropriate 4xx/5xx status.
- All responses (including errors and `OPTIONS` preflight) include CORS headers:
  ```
  Access-Control-Allow-Origin: *
  Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS
  Access-Control-Allow-Headers: Content-Type
  ```
- Numbers use `decimal.Decimal` end-to-end; a custom `DecimalEncoder`
  serializes Decimals **as strings** to preserve fidelity.

### Pagination
List endpoints accept `?limit=` (default 25) and `?last_key=<json>`. The handler
passes `last_key` (JSON-decoded) as `ExclusiveStartKey` to the scan/query and
returns the new `LastEvaluatedKey` as `last_key` (JSON-encoded string).

### Routes
| Method | Path | Description |
|--------|------|-------------|
| GET | `/accounts` | paginated scan of accounts |
| GET | `/accounts/{acct_id}` | get single account |
| PUT | `/accounts/{acct_id}` | update mutable account fields (body = JSON of fields) |
| GET | `/customers` | paginated scan of customers |
| GET | `/customers/{cust_id}` | get single customer |
| GET | `/cards` | paginated scan; optional `?acct_id=` filters via `acct_id-index` |
| GET | `/cards/{card_num}` | get single card |
| GET | `/transactions` | paginated scan; optional `?acct_id=` or `?card_num=` filters via GSIs |
| GET | `/transactions/{tran_id}` | get transaction(s) by id (query on PK) |
| POST | `/transactions` | create new transaction (body = JSON) |
| GET | `/transaction-types` | list all transaction types |
| GET | `/transaction-categories` | list all transaction categories |
| POST | `/batch/process-daily` | run daily batch processing, returns summary |
| GET | `/dashboard` | aggregate counts + recent transactions |

### Endpoint details

**GET `/dashboard`** → `data`:
```json
{
  "accounts": <int>, "customers": <int>, "cards": <int>,
  "transactions": <int>,
  "total_balance": "<decimal string>",
  "recent_transactions": [ <transaction item>, ... up to 10 ]
}
```
Counts come from paginated COUNT scans (`Select="COUNT"`, follow
`LastEvaluatedKey` until exhausted). `total_balance` sums `curr_bal` across all
accounts (read as Decimal, returned as string).

**POST `/transactions`** body (server fills missing `tran_id`/timestamps):
```json
{
  "card_num": "...", "type_cd": "01", "cat_cd": "0001",
  "amt": "12.34", "tran_desc": "...", "merchant_name": "...",
  "merchant_city": "...", "merchant_zip": "...", "merchant_id": "...",
  "tran_source": "POS"
}
```
The handler generates `tran_id` and `tran_orig_ts` if absent, enriches `acct_id`
via the card-xref table, and writes the item. Returns the created item in `data`.

**PUT `/accounts/{acct_id}`** body = JSON object of fields to update (e.g.
`credit_limit`, `acct_active_status`, `addr_zip`). Uses `update_item` with an
`UpdateExpression`; returns the updated item.

**POST `/batch/process-daily`** → `data`:
```json
{ "transactions_processed": <int>, "skipped": <int>, "errors": <int> }
```

### Batch processing logic (replicates CBTRN01C/02C/03C)
1. Scan `carddemo-transactions` for items where `batch_processed` does NOT exist.
2. For each, `get_item` the account by `acct_id`.
3. Convert the account's string money fields to `Decimal`, apply:
   - type `"01"` (Purchase): `curr_bal -= amt`; `curr_cyc_debit += amt`
   - types `"02"`,`"03"`,`"05"` (Payment/Credit/Refund): `curr_bal += amt`;
     `curr_cyc_credit += amt`
4. Write results back as **strings** via `update_item` (read-compute-write — do
   NOT use `SET field = field + :val`, which fails on string-typed values).
5. Mark the transaction `batch_processed = true`, `tran_proc_ts = <now ISO>`.
6. Return `{ transactions_processed, skipped, errors }`. `skipped` counts
   transactions whose account is missing; `errors` counts unexpected failures.

---

## 4. Lambda handler requirements (api/handler.py)

- Single `handler(event, context)` entrypoint, regex router over `(method, path)`.
- **Stage prefix stripping** — HTTP API v2 puts the stage in
  `requestContext.http.path` (e.g. `/prod/accounts`). Strip it first:
  ```python
  raw = event["requestContext"]["http"]["path"]
  stage = event.get("requestContext", {}).get("stage", "")
  path = raw[len(f"/{stage}"):] if stage and raw.startswith(f"/{stage}") else raw
  ```
- Method from `event["requestContext"]["http"]["method"]`.
- Query params from `event.get("queryStringParameters") or {}`.
- Body from `event.get("body")` (JSON-decode; may be base64 per
  `isBase64Encoded`).
- Handle `OPTIONS` for any path → 204 with CORS headers.
- Table names come from environment variables set by Terraform:
  `ACCOUNTS_TABLE`, `CUSTOMERS_TABLE`, `CARDS_TABLE`, `CARD_XREF_TABLE`,
  `TRANSACTIONS_TABLE`, `TRANSACTION_TYPES_TABLE`,
  `TRANSACTION_CATEGORIES_TABLE`.

## 5. Batch Lambda (batch/daily_processor.py)
- Exports `handler(event, context)` implementing the batch logic in §3.
- Reads the same table-name environment variables.
- Returns the summary dict (also used by `POST /batch/process-daily`, which may
  invoke this module directly or re-implement the identical logic).

## 6. Environment variables (set on both Lambdas by Terraform)
```
ACCOUNTS_TABLE, CUSTOMERS_TABLE, CARDS_TABLE, CARD_XREF_TABLE,
TRANSACTIONS_TABLE, TRANSACTION_TYPES_TABLE, TRANSACTION_CATEGORIES_TABLE
```
