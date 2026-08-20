# CardDemo Web Mapping

The web foundation exposes the CICS pseudo-conversational programs as stateless
JSON endpoints. `Context` carries the values previously held in
`CARDDEMO-COMMAREA`: caller/callee program and transaction, user identity and
type, and selected account/card. Clients echo the context on the next request;
the service does not use server-side conversational session state. A client
requesting PF3/back supplies its caller in `fromProgram`, and the response
returns that program as `nextRoute`.

| REST route | COBOL program | Access |
| --- | --- | --- |
| `POST /api/signon` | `COSGN00C` | Public |
| `POST /api/menu` | `COMEN01C` | Authenticated |
| `POST /api/admin/menu` | `COADM01C` | `ROLE_ADMIN` |
| `GET /api/accounts/{accountId}` | `COACTVWC` | Authenticated |
| `PUT /api/accounts/{accountId}` | `COACTUPC` | Authenticated |
| `GET /api/cards` | `COCRDLIC` | Authenticated |
| `GET /api/cards/{cardNumber}` | `COCRDSLC` | Authenticated |
| `PUT /api/cards/{cardNumber}` | `COCRDUPC` | Authenticated |

The corresponding `CARDDEMO.CSD` transaction IDs are `CC00` (signon),
`CM00` (main menu), `CA00` (admin menu), `CAVW` (account view), `CAUP`
(account update), `CCLI` (card list), `CCDL` (card detail), and `CCUP`
(card update).

`COMEN02Y.cpy` and `COADM02Y.cpy` are ported as menu data. Menu option numbers
are validated server-side and the user-type flag is never trusted from the
request. `A` users receive `ROLE_ADMIN`; all other legacy user types receive
`ROLE_USER`.

Signon issues an HMAC-SHA256 JWT containing the legacy user ID and role. The
signing key is configurable through `CARDDEMO_JWT_SECRET`, with a development
default only. The default must be replaced in every deployed environment.

The legacy `USRSEC` password is an eight-character plaintext field. The web
port intentionally compares the supplied value to that stored value (after the
same upper-case edit as `COSGN00C`) so the migrated seed data and mainframe
behavior remain compatible. This is **not acceptable for production security**.
The intended migration remediation is to hash passwords during a controlled
data migration and replace the compatibility encoder, rather than silently
changing the comparison now.

Account and card updates use an echoed pre-image. The service re-reads the
current row and rejects a mismatch with the COBOL message
`Record changed by some one else. Please review`. `confirm=false` returns the
review response without persisting changes; `confirm=true` persists after the
same check.

Card listing uses seven rows per page, matching the `COCRDLI` map array, and
supports account/card filters plus forward page metadata. Account and card
DTOs use the BMS field names and preserve the fixed-width values represented by
the domain model.
