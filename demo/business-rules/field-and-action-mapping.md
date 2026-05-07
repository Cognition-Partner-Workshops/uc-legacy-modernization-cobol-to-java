# End-to-End Field & Action Mapping

## Copybook → REST JSON → React UI

This document traces every field from the original COBOL mapper/copybook files through the Spring Boot REST API JSON contract to the React frontend UI components.

---

## 1. Field Mapping: Copybook → JSON → React UI

### `CVACT02Y.cpy` (Card Record, RECLN 150) → `Card.java` → React

| COBOL Copybook Field | PIC Clause | REST JSON Key | CardListPage.jsx | CardDetailPage.jsx | CardUpdatePage.jsx |
|---|---|---|---|---|---|
| `CARD-NUM` | X(16) | `cardNumber` | Table column (clickable link) | Read-only text | Read-only greyed input |
| `CARD-ACCT-ID` | 9(11) | `accountId` | Table column + search filter | Read-only text | Read-only greyed input |
| `CARD-CVV-CD` | 9(03) | `cvvCode` | — (hidden) | — (hidden) | — (hidden, security) |
| `CARD-EMBOSSED-NAME` | X(50) | `embossedName` | Table column "Cardholder Name" | Read-only "Name on Card" | Editable `<input>` (max 50 chars) |
| `CARD-EXPIRAION-DATE` | X(10) | `expirationDate` | Table column "Expiry" | Read-only "Expiry Date" | Editable `<input>` (YYYY-MM-DD) |
| `CARD-ACTIVE-STATUS` | X(01) | `activeStatus` | Green/red badge "Active"/"Inactive" | Badge "Y — Active" / "N — Inactive" | Editable `<input>` (Y or N only) |
| `FILLER` | X(59) | — | — | — | — (padding dropped) |

### `CVACT01Y.cpy` (Account Record, RECLN 300) → `Account.java` → Backend Only

| COBOL Copybook Field | PIC Clause | REST JSON Key | UI Usage |
|---|---|---|---|
| `ACCT-ID` | 9(11) | `accountId` | Search filter param: `GET /api/cards?accountId=` |
| `ACCT-ACTIVE-STATUS` | X(01) | `activeStatus` | Not exposed in card UI |
| `ACCT-CURR-BAL` | S9(10)V99 | `currentBalance` | Not exposed in card UI |
| `ACCT-CREDIT-LIMIT` | S9(10)V99 | `creditLimit` | Not exposed in card UI |
| `ACCT-CASH-CREDIT-LIMIT` | S9(10)V99 | `cashCreditLimit` | Not exposed in card UI |
| `ACCT-OPEN-DATE` | X(10) | `openDate` | Not exposed in card UI |
| `ACCT-EXPIRAION-DATE` | X(10) | `expirationDate` | Not exposed in card UI |
| `ACCT-REISSUE-DATE` | X(10) | `reissueDate` | Not exposed in card UI |
| `ACCT-CURR-CYC-CREDIT` | S9(10)V99 | — | Not mapped (demo scope) |
| `ACCT-CURR-CYC-DEBIT` | S9(10)V99 | — | Not mapped (demo scope) |
| `ACCT-ADDR-ZIP` | X(10) | — | Not mapped (demo scope) |
| `ACCT-GROUP-ID` | X(10) | — | Not mapped (demo scope) |
| `FILLER` | X(178) | — | — (padding dropped) |

### `CVACT03Y.cpy` (Card Cross-Reference, RECLN 50) → `CardXref.java` → Backend Only

| COBOL Copybook Field | PIC Clause | REST JSON Key | UI Usage |
|---|---|---|---|
| `XREF-CARD-NUM` | X(16) | `cardNumber` | Links cards to accounts internally |
| `XREF-CUST-ID` | 9(09) | `customerId` | Not exposed in card UI |
| `XREF-ACCT-ID` | 9(11) | `accountId` | Not exposed in card UI |
| `FILLER` | X(14) | — | — (padding dropped) |

---

## 2. Action Mapping: COBOL Paragraphs → Java Methods → React Actions

### Card List: `COCRDLIC.cbl` → `CardListPage.jsx`

| COBOL Paragraph | CICS Operation | Java Method | React Action |
|---|---|---|---|
| `1200-SCREEN-ARRAY-INIT` | Initialize 10-row display array | `CardService.listCards()` | `useEffect → getCards()` on page load |
| `9000-READ-FORWARD` | `EXEC CICS STARTBR` + `READNEXT` on CARDDAT | `CardRepository.findAll()` | `axios.get('/api/cards')` → renders `<table>` rows |
| `9100-READ-BACKWARDS` | `EXEC CICS READPREV` (page back) | — (full list returned) | — (no pagination in demo) |
| `2000-RECEIVE-MAP` | `EXEC CICS RECEIVE MAP` (get search input) | `@RequestParam accountId` | `searchValue` state → `handleSearch()` button click |
| `EXEC CICS XCTL` to COCRDSLC | Transfer control to detail program | — (HTTP navigation) | `<Link to="/cards/{cardNumber}">` click |
| `1000-SEND-MAP` | `EXEC CICS SEND MAP` to 3270 terminal | Controller returns `List<Card>` JSON | React `cards.map()` renders table rows |
| `1500-SEND-SCREEN` | BMS MAP write to terminal buffer | — (HTTP response) | Browser renders DOM from React state |

### Card Detail: `COCRDSLC.cbl` → `CardDetailPage.jsx`

| COBOL Paragraph | CICS Operation | Java Method | React Action |
|---|---|---|---|
| `2100-RECEIVE-MAP` | `EXEC CICS RECEIVE MAP` (read acct/card input) | — (card number from URL path) | `useParams()` extracts `:cardNumber` from route |
| `2200-EDIT-MAP-INPUTS` | Call validation paragraphs | `CardService.getCard()` calls validators | — (no user input on detail page) |
| `2210-EDIT-ACCOUNT` | Validate account is numeric ≤11 digits | `validateAccountId()` | — (read-only display, no validation needed) |
| `2220-EDIT-CARD` | Validate card number = 16 digits | `validateCardNumber()` | — (from URL path param, validated on backend) |
| `9000-READ-DATA` | `EXEC CICS READ` CARDDAT file by key | `CardRepository.findById()` | `axios.get('/api/cards/{cardNumber}')` |
| `9100-GETCARD-BYACCTCARD` | VSAM KSDS read with composite RIDFLD | `CardRepository.findById()` | — (simplified to single-key lookup) |
| `9150-GETCARD-BYACCT` | `EXEC CICS STARTBR` browse by account | `CardRepository.findByAccountId()` | — (used in list page instead) |
| `1000-SEND-MAP` | `EXEC CICS SEND MAP` display fields on terminal | Controller returns `Card` JSON | React renders `card.accountId`, `card.embossedName`, etc. |
| `1100-SCREEN-INIT` | Clear screen fields to LOW-VALUES | — | React state initializes to `null` |
| `1200-SETUP-SCREEN-VARS` | Move data fields to BMS screen fields | — (JSON serialization) | React reads `card.*` properties into JSX |
| `1300-SETUP-SCREEN-ATTRS` | Set field colors/protection (ASKIP, UNPROT) | — | Inline CSS styles: `styles.label`, `styles.value` |
| `EXEC CICS XCTL` to COCRDUPC | Transfer control to update program | — (HTTP navigation) | "Edit Card" → `<Link to="/cards/{cardNumber}/edit">` |

### Card Update: `COCRDUPC.cbl` → `CardUpdatePage.jsx`

| COBOL Paragraph | CICS Operation | Java Method | React Action |
|---|---|---|---|
| `1100-RECEIVE-MAP` | `EXEC CICS RECEIVE MAP` (read edited fields) | — (JSON body from PUT request) | `form` state captures user input via `onChange` |
| `1200-EDIT-MAP-INPUTS` | Call all validation paragraphs | `CardService.updateCard()` calls validators | `validate()` function runs before save |
| `1210-EDIT-ACCOUNT` | Verify account not changed (PROT field) | — (not in PUT body, immutable) | `inputReadonly` style, greyed-out input |
| `1220-EDIT-CARD` | Verify card number (immutable primary key) | `validateCardNumber()` | `inputReadonly` style, greyed-out input |
| `1230-EDIT-NAME` | Check name not blank (LOW-VALUES check) | `if (name != null && !name.isBlank())` | `validate()` → error: `"Name on card is required"` |
| `1240-EDIT-CARDSTATUS` | Must be Y or N (`FLG-YES-NO-VALID VALUES 'Y','N'`) | `validateActiveStatus()` → Y/N check | `validate()` → error: `"Active status must be Y or N"` |
| `1250-EDIT-EXPIRY-MON` | Month 1–12 (`VALID-MONTH VALUES 1 THRU 12`) | `validateExpirationDate()` → month 1–12 | `validate()` → error: `"Month must be 01-12"` |
| `1260-EDIT-EXPIRY-YEAR` | Year 1950–2099 (`VALID-YEAR VALUES 1950 THRU 2099`) | `validateExpirationDate()` → year range | `validate()` → error: `"Year must be 1950-2099"` |
| `2000-DECIDE-ACTION` | Route to read-only display or update path | Controller PUT handler method | `handleSave()` triggered by "Save Changes" button |
| `9000-READ-DATA` | `EXEC CICS READ` with UPDATE lock | `CardRepository.findById()` | `useEffect → getCard()` loads current card data |
| `9200-WRITE-PROCESSING` | `EXEC CICS REWRITE` record to VSAM | `CardRepository.save(existing)` | `axios.put('/api/cards/{cardNumber}', form)` |
| `9100-UPDATE-CARD` | Execute the VSAM REWRITE | `CardRepository.save()` | — (same as above, single REST call) |
| `3000-SEND-MAP` | `EXEC CICS SEND MAP` with success/error msg | Controller returns updated `Card` JSON | `setSuccess(true)` → auto-navigates to detail page |
| Error display (`ERRMSG` POS 23,1) | Red text on row 23 of terminal | `IllegalArgumentException` → 400 JSON | `serverError` state renders red error banner |

---

## 3. JSON Contract (REST API Shapes)

### GET Response — `GET /api/cards/{cardNumber}`

```json
{
  "cardNumber": "0500024453765740",
  "accountId": "00000000050",
  "cvvCode": "747",
  "embossedName": "Aniya Von",
  "expirationDate": "2023-03-09",
  "activeStatus": "Y"
}
```

### GET List Response — `GET /api/cards` or `GET /api/cards?accountId=00000000050`

```json
[
  {
    "cardNumber": "0500024453765740",
    "accountId": "00000000050",
    "cvvCode": "747",
    "embossedName": "Aniya Von",
    "expirationDate": "2023-03-09",
    "activeStatus": "Y"
  },
  ...
]
```

### PUT Request Body — `PUT /api/cards/{cardNumber}`

```json
{
  "embossedName": "Aniya Von-Updated",
  "activeStatus": "Y",
  "expirationDate": "2023-03-09"
}
```

### PUT Error Response (400 Bad Request)

```json
{
  "message": "Active status must be 'Y' or 'N'"
}
```

---

## 4. Data Storage Mapping: VSAM → H2

| COBOL Storage | Access Method | Java/H2 Equivalent |
|---|---|---|
| `CARDDAT` VSAM KSDS | `EXEC CICS READ/REWRITE` with RIDFLD | `cards` table, `CardRepository.findById()` / `.save()` |
| `ACCTDAT` VSAM KSDS | `EXEC CICS READ` with RIDFLD | `accounts` table, `AccountRepository.findById()` |
| `CARDAIX` VSAM AIX | Alternate index by account ID | `CardRepository.findByAccountId()` (JPA query) |
| `CXREF` VSAM KSDS | Card-to-customer cross reference | `card_xref` table, `CardXref` entity |
| `COMMAREA` (memory) | `EXEC CICS RETURN COMMAREA` | HTTP session / React state (stateless REST) |
| BMS MAP buffer | `EXEC CICS SEND MAP` / `RECEIVE MAP` | JSON request/response body |
