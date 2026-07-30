# Statement JSON Contract (DJ-69) — LOCKED

Authoritative source: Jira **DJ-69**. Where the COBOL copybooks and this document
disagree with DJ-69, DJ-69 wins. This file is frozen: the batch migration
(`StatementGenerator`) and the web viewer must both build against exactly this shape.
Do not change field names, nesting, or types without updating DJ-69 first.

## 1. File layout

One JSON document per generated statement, written to:

```
carddemo-java/output/statements/statement-<accountId>.json
```

- `<accountId>` is the 11-character zero-padded account id (e.g. `00000000011`).
- The COBOL driver (`CBSTM03A`) emits one statement per **card cross-reference
  record**. If the same account id occurs on more than one card xref record, later
  files are disambiguated as `statement-<accountId>-<last4OfCard>.json`.
- Encoding: UTF-8, one JSON object per file, pretty-printed (2-space indent).
- The viewer discovers statements by scanning the configured directory for
  `statement-*.json`; it must not depend on any index or manifest file.
- The statement directory is configurable via the Spring property
  `carddemo.statements.dir`, defaulting to `output/statements`.

## 2. Document shape

```json
{
  "accountId": "00000000011",
  "customer": {
    "firstName": "Immanuel",
    "lastName": "Kessler",
    "addressLines": [
      "618 Deshaun Route",
      "Apt. 802",
      "North Enoshaven NY USA 12345"
    ]
  },
  "cardNumber": "**-**-****-1234",
  "currentBalance": 194.00,
  "ficoScore": 688,
  "transactions": [
    {
      "transactionId": "0000000000683580",
      "description": "Purchase at Abshire-Lowe",
      "amount": -123.45
    }
  ],
  "totalAmount": -456.78
}
```

### Field reference

| Field | Type | Required | Source | Rules |
|---|---|---|---|---|
| `accountId` | string | yes | `ACCT-ID` (`CVACT01Y.cpy`, bytes 1–11) | 11 chars, zero-padded, digits only |
| `customer.firstName` | string | yes | `CUST-FIRST-NAME` (`CUSTREC.cpy`, 10–34) | trailing spaces trimmed |
| `customer.lastName` | string | yes | `CUST-LAST-NAME` (`CUSTREC.cpy`, 60–84) | trailing spaces trimmed |
| `customer.addressLines` | array of string | yes | `CUST-ADDR-LINE-1/2/3`, `CUST-ADDR-STATE-CD`, `CUST-ADDR-COUNTRY-CD`, `CUST-ADDR-ZIP` | exactly 3 entries, mirroring the COBOL HTML: line 1, line 2, and `"<line3> <state> <country> <zip>"` (single-space separated, each part trimmed). Blank lines are emitted as `""`, never dropped |
| `cardNumber` | string | yes | `XREF-CARD-NUM` (`CVACT03Y.cpy`, 1–16) | masked as `**-**-****-<last4>`; literal asterisks, exactly this format |
| `currentBalance` | number | yes | `ACCT-CURR-BAL` (`CVACT01Y.cpy`, 13–24) | signed, 2 decimal places |
| `ficoScore` | integer | yes | `CUST-FICO-CREDIT-SCORE` (`CUSTREC.cpy`, 330–332) | 3-digit integer |
| `transactions` | array of object | yes | transaction records grouped by card number | may be empty (`[]`); order = order of records in `dailytran.txt` |
| `transactions[].transactionId` | string | yes | `TRAN-ID` (`CVTRA05Y.cpy`, 1–16) / `TRNX-ID` (`COSTM01.CPY`, 17–32) | 16 chars, trimmed |
| `transactions[].description` | string | yes | `TRAN-DESC` (33–132) / `TRNX-DESC` (49–148) | trailing spaces trimmed |
| `transactions[].amount` | number | yes | `TRAN-AMT` (133–143) / `TRNX-AMT` (149–159) | signed, 2 decimal places |
| `totalAmount` | number | yes | computed | sum of `transactions[].amount`, 2 decimal places; `0.00` when there are no transactions |

`currentBalance` and `ficoScore` are carried over from the COBOL "Basic Details"
block in `CBSTM03A.CBL` so the viewer can reproduce it; they are additive to the
DJ-69 shape and do not alter any DJ-69 field.

## 3. Numeric and sign handling

The ASCII sample files use zoned-decimal overpunch signs in the final byte of
signed numeric fields (COBOL `S9(n)V99`):

- positive: `{`=0, `A`=1 … `I`=9
- negative: `}`=0, `J`=1 … `R`=9

Example: `0000005047G` → digits `00000050477` → `504.77`;
`0000009190}` → `-919.00`.

All monetary values are serialized as JSON numbers with exactly two decimal
places (no thousands separators, no currency symbol, no quotes). Consumers must
treat them as `BigDecimal`-precision decimals, not floats. Rounding, when
required, is `HALF_UP`.

## 4. JSON Schema

The machine-readable schema lives next to this file at
[`statement.schema.json`](statement.schema.json) (JSON Schema draft 2020-12).
An identical classpath copy also exists at
`src/main/resources/statement.schema.json`; it must remain byte-for-byte
identical to the authoritative schema next to this contract.
Both the generator and the viewer validate their fixtures/output against it.
