# Parity JSON contract (FROZEN)

`carddemo-java/target/parity/parity-summary.json` is the single interchange
artifact between the Java statement generator (`com.carddemo.statement`) and the
HTML parity report renderer (`com.carddemo.parity`). The shape below is frozen:
producers must emit exactly these fields, consumers must read exactly these
fields. The JSON Schema in `parity-summary.schema.json` is normative.

```json
{
  "generatedAt": "2026-08-18T15:00:00Z",
  "sourceProgram": "CBSTM03A",
  "summary": { "total": 3, "match": 2, "diff": 1, "missing": 0 },
  "accounts": [
    {
      "accountId": "00000000001",
      "customerName": "Immanuel Madeline Kessler",
      "transactionCount": 4,
      "totalAmount": 1234.56,
      "cobolTextPath": "carddemo-java/target/parity/cobol/00000000001.txt",
      "cobolHtmlPath": "carddemo-java/target/parity/cobol/00000000001.html",
      "javaTextPath": "carddemo-java/target/parity/java/00000000001.txt",
      "javaHtmlPath": "carddemo-java/target/parity/java/00000000001.html",
      "status": "MATCH",
      "diffLineCount": 0,
      "firstDiffLine": null
    }
  ]
}
```

## Field semantics

| Field | Type | Notes |
| --- | --- | --- |
| `generatedAt` | string | ISO-8601 instant (UTC) when the parity run was produced. |
| `sourceProgram` | string | COBOL program the Java port replaces; always `CBSTM03A`. |
| `summary.total` | integer | Number of entries in `accounts`. |
| `summary.match` | integer | Count of `accounts[].status == "MATCH"`. |
| `summary.diff` | integer | Count of `accounts[].status == "DIFF"`. |
| `summary.missing` | integer | Count of `accounts[].status == "MISSING"`. |
| `accounts[].accountId` | string | 11-char zero-padded account id, as printed by the statement. |
| `accounts[].customerName` | string | Name as rendered on the statement (single-space joined, trimmed). |
| `accounts[].transactionCount` | integer | Transactions on the Java statement for that account. |
| `accounts[].totalAmount` | number | `Total EXP` value as a decimal number (2 dp, may be negative). |
| `accounts[].cobolTextPath` | string | Repo-relative path to the per-account COBOL text statement. |
| `accounts[].cobolHtmlPath` | string | Repo-relative path to the per-account COBOL HTML statement. |
| `accounts[].javaTextPath` | string | Repo-relative path to the per-account Java text statement. |
| `accounts[].javaHtmlPath` | string | Repo-relative path to the per-account Java HTML statement. |
| `accounts[].status` | enum | `MATCH` (text and HTML identical), `DIFF` (both sides present, at least one line differs), `MISSING` (a COBOL or Java side is absent). |
| `accounts[].diffLineCount` | integer | Number of differing lines (text + HTML); `0` for `MATCH`, `0` allowed for `MISSING`. |
| `accounts[].firstDiffLine` | integer or null | 1-based line number of the first differing line; `null` when there is none. |

Rules:

- `summary.total == accounts.length`, and `match + diff + missing == total`.
- Paths are repo-relative, forward-slash separated, and may be `null` only for
  the side that is absent in a `MISSING` entry.
- Consumers must tolerate unknown extra keys being absent; no additional keys
  are required by the renderer.
