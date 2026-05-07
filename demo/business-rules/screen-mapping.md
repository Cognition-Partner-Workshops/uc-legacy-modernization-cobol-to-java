# Screen Mapping: BMS Maps → React Components

## COCRDSL.bms → CardDetailPage.jsx

| BMS Field | POS (Row,Col) | Length | Attrs | React Prop | Component |
|-----------|---------------|--------|-------|------------|-----------|
| `TRNNAME` | (1,7) | 4 | ASKIP | — | Header (nav bar) |
| `TITLE01` | (1,21) | 40 | ASKIP,YELLOW | — | Page title |
| `CURDATE` | (1,71) | 8 | ASKIP,BLUE | — | Not shown (browser has clock) |
| `PGMNAME` | (2,7) | 8 | ASKIP,BLUE | — | Legacy badge label |
| `ACCTSID` | (7,45) | 11 | FSET,IC,UNPROT | `card.accountId` | Read-only field |
| `CARDSID` | (8,45) | 16 | FSET,UNPROT | `card.cardNumber` | Read-only field |
| `CRDNAME` | (11,25) | 50 | UNDERLINE | `card.embossedName` | Read-only field |
| `CRDSTCD` | (13,25) | 1 | ASKIP | `card.activeStatus` | Status badge (Y=green, N=red) |
| `EXPMON` | (15,25) | 2 | ASKIP | `card.expirationDate` | Combined into date display |
| `EXPYEAR` | (15,30) | 4 | ASKIP | `card.expirationDate` | Combined into date display |
| `ERRMSG` | (23,1) | 80 | RED | — | Error toast/banner |
| `FKEYS` | (24,1) | 75 | YELLOW | — | Button actions (Edit, Back) |

### Attribute Translation

| BMS Attribute | Meaning | React Equivalent |
|---------------|---------|------------------|
| `ASKIP` | Auto-skip (read-only) | `readOnly` or plain text display |
| `UNPROT` | Unprotected (editable) | `<input>` element |
| `PROT` | Protected (read-only) | `readOnly` input with gray background |
| `FSET` | Field set (always transmit) | Default behavior (always in JSON) |
| `IC` | Initial cursor position | `autoFocus` |
| `BRT` | Bright (highlighted) | Bold text or colored background |
| `COLOR=RED` | Red text for errors | `color: '#c62828'` |
| `COLOR=TURQUOISE` | Turquoise for labels | `color: '#555'` (muted gray) |
| `COLOR=YELLOW` | Yellow for titles | `fontWeight: 700` |
| `HILIGHT=UNDERLINE` | Underlined field | Border-bottom on input |

## COCRDUP.bms → CardUpdatePage.jsx

| BMS Field | POS (Row,Col) | Length | Attrs | React Prop | Editable |
|-----------|---------------|--------|-------|------------|----------|
| `ACCTSID` | (7,45) | 11 | PROT | `form.accountId` | No (read-only) |
| `CARDSID` | (8,45) | 16 | UNPROT | `form.cardNumber` | No (primary key) |
| `CRDNAME` | (11,25) | 50 | UNPROT | `form.embossedName` | Yes |
| `CRDSTCD` | (13,25) | 1 | UNPROT | `form.activeStatus` | Yes (Y/N only) |
| `EXPMON` | (15,25) | 2 | UNPROT | `form.expirationDate` | Yes (combined field) |
| `EXPYEAR` | (15,30) | 4 | UNPROT | `form.expirationDate` | Yes (combined field) |
| `EXPDAY` | (15,36) | 2 | DRK,PROT | — | Hidden (dark attribute) |

### Key Differences from Detail View
- `ACCTSID` is `PROT` (protected) on update screen vs `UNPROT` on detail — account number cannot be changed
- `CRDNAME`, `CRDSTCD`, `EXPMON`, `EXPYEAR` are all `UNPROT` — editable fields
- `EXPDAY` has `DRK` (dark) attribute — hidden from the user, auto-set to "01"
- Function keys: `ENTER=Update` instead of `ENTER=Search`

## COCRDLI.bms → CardListPage.jsx

| BMS Field | POS (Row,Col) | Length | Attrs | React Prop | Notes |
|-----------|---------------|--------|-------|------------|-------|
| `ACCTSID` | (6,44) | 11 | UNPROT,GREEN | Search input | Filter by account |
| `CARDSID` | (7,44) | 16 | UNPROT,GREEN | Search input | Filter by card |
| `PAGENO` | (4,76) | 3 | — | Pagination | Page number display |
| `CRDSEL1..10` | (11-20,12) | 1 | PROT | Row select | Click handler in React |
| `ACCTNO1..10` | (11-20,22) | 11 | PROT | `card.accountId` | Table column |
| `CRDNUM1..10` | (11-20,43) | 16 | PROT | `card.cardNumber` | Table column (link) |
| `CRDSTS1..10` | (11-20,66) | 1 | PROT | `card.activeStatus` | Status badge |

### Screen Array → Table
The BMS map defines 10 repeated row groups (CRDSEL1-10, ACCTNO1-10, CRDNUM1-10, CRDSTS1-10) for a fixed-size display. In React, this becomes a dynamic `<table>` with `map()` over the cards array, supporting any number of rows with scroll.
