# CardDemo Java migration

This Maven reactor is the incremental target for the CardDemo migration. The
current handoff contains the persistent domain model and COBOL-equivalent
utilities; later handoffs will fill in the loaders, batch jobs, web layer, and
frontend.

| Module | Status | Scope |
|---|---|---|
| `common` | scaffolded | packed/zoned decimals, date validation/conversion/day-of-week, lookup tables, messages |
| `domain` | scaffolded | Flyway PostgreSQL schema, JPA entities, composite keys, Spring Data repositories |
| `dataload` | reactor skeleton | seed and EBCDIC loaders in a later handoff |
| `batch` | reactor skeleton | Spring Batch jobs in a later handoff |
| `web` | reactor skeleton | REST/controllers in a later handoff |
| frontend | not a Maven module | React application in a later handoff |

All migration mapping decisions are recorded in
[`docs/DATA-MODEL.md`](../docs/DATA-MODEL.md), including fixed-width offsets,
dropped fillers, date-as-text handling, and XREF-to-FK decisions.
