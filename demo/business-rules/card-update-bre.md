# Business Rules: Card Update

**Source**: `app/cbl/COCRDUPC.cbl` — Card Update Processing  
**BMS Map**: `app/bms/COCRDUP.bms`  
**Copybooks**: `CVACT02Y.cpy` (Card Record), `COCOM01Y.cpy` (COMMAREA)

## Overview

The Card Update program (`COCRDUPC`) allows modification of card details. It receives the current card data, presents an editable screen, validates all changed fields, and writes the updated record back to VSAM. This is the most complex of the card management programs with extensive field-level validation.

## Program Flow

```
0000-MAIN
  ├── 1000-PROCESS-INPUTS
  │     ├── 1100-RECEIVE-MAP              — Read screen input
  │     └── 1200-EDIT-MAP-INPUTS          — Validate all fields
  │           ├── 1210-EDIT-ACCOUNT       — Validate account (read-only check)
  │           ├── 1220-EDIT-CARD          — Validate card number
  │           ├── 1230-EDIT-NAME          — Validate cardholder name
  │           ├── 1240-EDIT-CARDSTATUS    — Validate active status
  │           ├── 1250-EDIT-EXPIRY-MON    — Validate expiry month
  │           └── 1260-EDIT-EXPIRY-YEAR   — Validate expiry year
  ├── 2000-DECIDE-ACTION                  — Determine update vs display
  │     ├── 9000-READ-DATA                — Read current record
  │     └── 9100-UPDATE-CARD              — Write updated record
  └── 3000-SEND-MAP
        ├── 3100-SCREEN-INIT              — Initialize screen
        └── 3200-SETUP-SCREEN-VARS        — Map data to screen
```

## Validation Rules

### Account Number (`1210-EDIT-ACCOUNT`)
- Read-only on the update screen (ATTRB PROT in BMS)
- Must be numeric 11 digits
- Cannot be modified — account association is immutable

### Card Number (`1220-EDIT-CARD`)
- Must be numeric 16 digits
- Validated but effectively immutable (primary key for the VSAM record)

### Cardholder Name (`1230-EDIT-NAME`)
- Field: `CRDNAME` — 50 characters
- Must not be blank (LOW-VALUES check)
- Compared against `CARD-NAME-CHECK` to detect changes

### Active Status (`1240-EDIT-CARDSTATUS`)
- Field: `CRDSTCD` — 1 character
- **Must be exactly 'Y' or 'N'** (88-level condition `FLG-YES-NO-VALID VALUES 'Y', 'N'`)
- Any other value triggers: "Card status must be Y or N"

### Expiry Month (`1250-EDIT-EXPIRY-MON`)
- Field: `EXPMON` — 2 characters
- Must be numeric
- **Must be in range 1-12** (88-level condition `VALID-MONTH VALUES 1 THRU 12`)
- Uses `CARD-MONTH-CHECK-N` REDEFINES for numeric comparison

### Expiry Year (`1260-EDIT-EXPIRY-YEAR`)
- Field: `EXPYEAR` — 4 characters
- Must be numeric
- **Must be in range 1950-2099** (88-level condition `VALID-YEAR VALUES 1950 THRU 2099`)
- Uses `CARD-YEAR-CHECK-N` REDEFINES for numeric comparison

## VSAM Write Operations

### 9000-READ-DATA
- `EXEC CICS READ` with `UPDATE` option to obtain exclusive lock
- `RIDFLD` set to card number
- `RESP` code checked for `DFHRESP(NORMAL)`

### 9100-UPDATE-CARD
- `EXEC CICS REWRITE` to write the modified record
- Only executed after all validations pass
- Record layout follows `CVACT02Y.cpy` exactly

## Error Handling
- Field-level errors are displayed in `ERRMSG` (POS 23,1 — 80 chars, RED)
- Informational messages in `INFOMSG` (POS 20,25 — 40 chars)
- File errors formatted as: "File Error: {operation} on {file} returned RESP {code}, RESP2 {code2}"

## Modern Equivalent

- **React Component**: `CardUpdatePage.jsx`
- **REST Endpoint**: `PUT /api/cards/{cardNumber}`
- **Java Service Method**: `CardService.updateCard()`
