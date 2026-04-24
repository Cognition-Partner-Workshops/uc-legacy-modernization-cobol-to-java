# Business Rules: Card Detail View

**Source**: `app/cbl/COCRDSLC.cbl` — Card Selection / Detail Display  
**BMS Map**: `app/bms/COCRDSL.bms`  
**Copybooks**: `CVACT02Y.cpy` (Card Record), `COCOM01Y.cpy` (COMMAREA)

## Overview

The Card Detail View program (`COCRDSLC`) accepts an account number and/or card number from the user, validates the input, reads the card record from VSAM, and displays the card details on the terminal screen.

## Program Flow

```
0000-MAIN
  ├── 2000-PROCESS-INPUTS (on re-entry)
  │     ├── 2100-RECEIVE-MAP         — Read screen input
  │     ├── 2200-EDIT-MAP-INPUTS     — Validate fields
  │     │     ├── 2210-EDIT-ACCOUNT  — Validate account number
  │     │     └── 2220-EDIT-CARD     — Validate card number
  │     └── 9000-READ-DATA           — Fetch from VSAM
  │           ├── 9100-GETCARD-BYACCTCARD  — Read by account + card
  │           └── 9150-GETCARD-BYACCT      — Read by account only
  └── 1000-SEND-MAP
        ├── 1100-SCREEN-INIT         — Initialize screen fields
        ├── 1200-SETUP-SCREEN-VARS   — Map data to screen fields
        ├── 1300-SETUP-SCREEN-ATTRS  — Set field attributes (colors, protection)
        └── 1400-SEND-SCREEN         — Send BMS map to terminal
```

## Validation Rules

### Account Number Validation (`2210-EDIT-ACCOUNT`)
- Field: `ACCTSID` — 11 characters (POS 7,45)
- Must be numeric when provided
- If blank and card number is also blank → error "Please provide Acct or Card"
- Stored as `CARD-ACCT-ID-N` (PIC 9(11))

### Card Number Validation (`2220-EDIT-CARD`)
- Field: `CARDSID` — 16 characters (POS 8,45)
- Must be numeric when provided
- If provided with account number, both must match the same record
- Stored as `CARD-CARD-NUM-N` (PIC 9(16))

## Data Access

### VSAM Read Operations
- **9100-GETCARD-BYACCTCARD**: Uses `EXEC CICS READ` with `RIDFLD` set to card number + account ID as a composite key. Reads from the `CARDDAT` file (VSAM KSDS).
- **9150-GETCARD-BYACCT**: Uses `EXEC CICS STARTBR` / `READNEXT` to browse cards by account ID. Returns the first matching card.
- Error handling: `RESP` codes checked — `DFHRESP(NORMAL)` for success, `DFHRESP(NOTFND)` for not found.

## Screen Field Mappings

| BMS Field | POS      | Length | Copybook Field          | Description           |
|-----------|----------|--------|-------------------------|-----------------------|
| ACCTSID   | (7,45)   | 11     | CARD-ACCT-ID            | Account Number        |
| CARDSID   | (8,45)   | 16     | CARD-NUM                | Card Number           |
| CRDNAME   | (11,25)  | 50     | CARD-EMBOSSED-NAME      | Name on Card          |
| CRDSTCD   | (13,25)  | 1      | CARD-ACTIVE-STATUS      | Active Status (Y/N)   |
| EXPMON    | (15,25)  | 2      | CARD-EXPIRY-MONTH       | Expiry Month          |
| EXPYEAR   | (15,30)  | 4      | CARD-EXPIRY-YEAR        | Expiry Year           |

## Modern Equivalent

- **React Component**: `CardDetailPage.jsx`
- **REST Endpoint**: `GET /api/cards/{cardNumber}`
- **Java Service Method**: `CardService.getCard()`
