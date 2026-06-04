# carddemo-shared

Phase 0 (Shared Utilities) of the CardDemo COBOL-to-Java modernization.

This Maven module re-implements the shared CardDemo date/timer utility programs
and their copybooks in Java 17 using only the `java.time` API.

## Mapping to legacy programs

| Java class | Legacy source | Purpose |
|------------|---------------|---------|
| `util/DateValidator` | `app/cbl/CSUTLDTC.cbl` | Validate a date against a format mask (originally via the LE `CEEDAYS` API). |
| `util/DateFormatConverter` | `app/asm/COBDATFT.asm` (`app/maclib/COCDATFT.mac`) | Convert between ISO (`YYYY-MM-DD`) and US (`MM/DD/YYYY`) date formats. |
| `util/BatchWait` | `app/cbl/COBSWAIT.cbl`, `app/asm/MVSWAIT.asm` | Wait for a number of centiseconds (1/100 s). |
| `model/CsDat01y` | `app/cpy/CSDAT01Y.cpy` | Date/time fields (`WS-DATE-TIME`). |
| `model/CsUtlDpy` | `app/cpy/CSUTLDPY.cpy` | Date-edit routine parameters. |
| `model/CsUtlDwy` | `app/cpy/CSUTLDWY.cpy` | Date-edit working storage. |
| `model/DateValidationResult` | output of `CSUTLDTC` | Typed validation result. |
| `model/ConversionResult` | output of `COBDATFT` | Typed conversion result. |

### Note on `DateFormatConverter` semantics

The original `COBDATFT` assembler converts between `YYYYMMDD` and `YYYY-MM-DD`
driven by `COINTYPE` (`'1'` = pack to ISO with dashes, `'2'` = strip dashes).
The modernization spec for this module instead targets ISO ⇄ US conversion, so
this port maps the input-type byte as:

- `'1'`: `YYYY-MM-DD` → `MM/DD/YYYY`
- `'2'`: `MM/DD/YYYY` → `YYYY-MM-DD`

The `COINTYPE` dispatch and the `COERMSG` = `"INVALID INPUT"` error contract are
preserved.

## Build & test

```bash
# Requires JDK 17+
mvn test
```

Supported `DateValidator` masks: `YYYY-MM-DD`, `MM/DD/YYYY`, `DD/MM/YYYY`,
`YYYYMMDD`. Validation is strict, so impossible dates (e.g. `2023-02-30`,
non-leap `2023-02-29`) are rejected.
