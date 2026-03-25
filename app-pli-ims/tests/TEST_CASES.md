# CardDemo PL/I/IMS - Test Cases

## Overview

This document provides comprehensive test cases for the CardDemo application converted from COBOL/CICS to PL/I/IMS. These tests cover online transactions (MPP programs), batch processing (BMP programs), IMS infrastructure (DBD/PSB/MFS), and end-to-end integration scenarios.

**Prerequisites for all tests:**
- IMS system with DL/I runtime available
- PL/I compiler (IBM Enterprise PL/I or equivalent)
- DBD/PSB generation utilities (DBDGEN, PSBGEN)
- MFS generation utilities (MFSUTL)
- Sample data from `app/data/ASCII/` loaded into IMS databases
- IMS SYSGEN completed with transaction definitions from `ims/SYSGEN01.ims`

---

## 1. Infrastructure Tests (DBD/PSB/MFS)

### TC-INF-001: DBD Generation - All Databases

| Field | Value |
|-------|-------|
| **Objective** | Verify all 7 DBD definitions compile successfully via DBDGEN |
| **Preconditions** | IMS system utilities available; JCL `DBDGEN.jcl` customized with site-specific dataset names |
| **Test Steps** | 1. Submit `DBDGEN.jcl` which runs DBDGEN for: DBUSRSEC, DBACCTDT, DBCARDDT, DBCUSTDT, DBTRANSC, DBCXREF, DBCXRIDX<br>2. Check return code for each DBDGEN step<br>3. Verify DBD load modules are created in IMS.DBDLIB |
| **Expected Result** | All 7 DBDGEN steps complete with RC=0. DBD load modules exist in DBDLIB. |
| **Validation** | Run `/DISPLAY DB ALL` in IMS to confirm databases are registered |

### TC-INF-002: DBD Segment Definitions Match Data Layouts

| Field | Value |
|-------|-------|
| **Objective** | Verify DBD segment sizes match PL/I include record lengths |
| **Test Steps** | For each DBD/include pair, verify BYTES= matches the record size:<br>- DBUSRSEC: USRSEG0 BYTES=80 vs CSUSR01Y SEC_USER_DATA<br>- DBACCTDT: ACCTSEG0 BYTES=300 vs CVACT01Y ACCOUNT_RECORD<br>- DBCARDDT: CARDSEG0 BYTES=150 vs CVACT02Y CARD_RECORD<br>- DBCUSTDT: CUSTSEG0 BYTES=500 vs CVCUS01Y CUSTOMER_RECORD<br>- DBTRANSC: TRANSEG0 BYTES=350 vs CVTRA05Y TRAN_RECORD<br>- DBCXREF: XREFSEG0 BYTES=50 vs CVACT03Y CARD_XREF_RECORD |
| **Expected Result** | Each DBD segment BYTES value exactly matches the total size of the corresponding PL/I structure |

### TC-INF-003: PSB Generation - All Programs

| Field | Value |
|-------|-------|
| **Objective** | Verify all 23 PSB definitions compile successfully via PSBGEN |
| **Preconditions** | All DBDs generated successfully (TC-INF-001 passed) |
| **Test Steps** | 1. Submit `PSBGEN.jcl` for all PSBs<br>2. Verify each PSBGEN step completes RC=0<br>3. Verify PSB load modules exist in IMS.PSBLIB |
| **Expected Result** | All 23 PSBGENs complete with RC=0 |

### TC-INF-004: PSB PCB Order Matches Program Parameter Lists

| Field | Value |
|-------|-------|
| **Objective** | Verify the PCB order in each PSB matches the PROC parameter list in the corresponding PL/I program |
| **Test Steps** | For each program/PSB pair, verify the PCB types and order:<br>- COSGN00C.pli: `(IO_PCB_PTR, ALT_PCB_PTR, DB_USRSEC_PTR)` vs PCOSGN00.psb: TP, TP(ALTER), DB(DBUSRSEC)<br>- COMEN01C.pli: `(IO_PCB_PTR, ALT_PCB_PTR)` vs PCOMEN01.psb: TP, TP(ALTER)<br>- COACTVWC.pli: `(IO_PCB_PTR, ALT_PCB_PTR, DB_ACCT_PTR, DB_CARD_PTR, DB_CUST_PTR, DB_CXREF_PTR)` vs PCOACTVW.psb<br>- *(Repeat for all 30 programs)* |
| **Expected Result** | Every PL/I program's parameter list matches the PCB order in its PSB exactly (count, type, and database name) |
| **Severity** | CRITICAL - Misalignment causes the program to reference wrong databases |

### TC-INF-005: MFS Generation - All Screen Definitions

| Field | Value |
|-------|-------|
| **Objective** | Verify all 17 MFS source files compile successfully |
| **Test Steps** | 1. Submit `MFSGEN.jcl` for all 17 MFS files<br>2. Check RC=0 for each MFS generation<br>3. Verify MID/MOD load modules created in IMS.FORMAT |
| **Expected Result** | All 17 MFS generations complete RC=0 |

### TC-INF-006: MFS Field Positions Match BMS Originals

| Field | Value |
|-------|-------|
| **Objective** | Verify MFS DFLD positions (row, column) match original BMS DFHMDF positions |
| **Test Steps** | For 3 representative screens, compare field-by-field:<br>1. COSGN00.mfs vs app/bms/COSGN00.bms - Signon screen<br>2. COMEN01.mfs vs app/bms/COMEN01.bms - Main menu<br>3. COTRN02.mfs vs app/bms/COTRN02.bms - Add transaction<br>For each: verify row/column positions, field lengths, and attribute bytes match |
| **Expected Result** | All field positions and lengths match within each pair. Screen layout on 3270 terminal is identical to original. |

### TC-INF-007: IMS SYSGEN - Transaction Registration

| Field | Value |
|-------|-------|
| **Objective** | Verify SYSGEN macros correctly define all transactions |
| **Test Steps** | 1. Submit `IMSSYSGE.jcl` with SYSGEN01.ims macros<br>2. Verify APPLCTN/TRANSACT definitions for all online transactions:<br>   CC00→COSGN00P, CM00→COMEN01P, CA00→COADM01P, CAVW→COACTVWP, CAUP→COACTUPP,<br>   CRDL→COCRDLIP, CRDS→COCRDSLP, CRDU→COCRDUPP, CT00→COTRN00P, CT01→COTRN01P,<br>   CT02→COTRN02P, CR00→CORPT00P, CB00→COBIL00P, CU00→COUSR00P, CU01→COUSR01P,<br>   CU02→COUSR02P, CU03→COUSR03P<br>3. Verify `/DISPLAY TRAN ALL` shows all transactions |
| **Expected Result** | All 17 transactions registered and schedulable in IMS |

---

## 2. Compilation Tests

### TC-CMP-001: PL/I Compilation - All Online Programs (17)

| Field | Value |
|-------|-------|
| **Objective** | Verify all 17 online PL/I programs compile without errors |
| **Test Steps** | 1. Submit `PLICOMP.jcl` for each online program<br>2. Programs: COSGN00C, COMEN01C, COADM01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C<br>3. Verify each compilation step RC<=4 (0=success, 4=warnings)<br>4. Verify load modules created |
| **Expected Result** | All 17 online programs compile with RC<=4. No RC=8 or RC=12 (errors). |
| **Known Risk** | PLITDLI calling convention may need parameter count as first argument. If compilation fails on PLITDLI calls, add a count parameter to each call. |

### TC-CMP-002: PL/I Compilation - All Batch Programs (12)

| Field | Value |
|-------|-------|
| **Objective** | Verify all 12 batch PL/I programs compile without errors |
| **Test Steps** | Programs: CBACT01C, CBACT02C, CBACT03C, CBACT04C, CBCUS01C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, CBSTM03B, CBEXPORT, CBIMPORT, COBSWAIT<br>Submit `PLICOMP.jcl` for each and verify RC<=4 |
| **Expected Result** | All 12 batch programs compile with RC<=4 |

### TC-CMP-003: Include Member Resolution

| Field | Value |
|-------|-------|
| **Objective** | Verify all %INCLUDE directives resolve correctly |
| **Test Steps** | 1. Ensure SYSLIB DD points to `app-pli-ims/include/` directory<br>2. Compile programs that use %INCLUDE CSUTIL01Y (all 17 online programs)<br>3. Verify no "include member not found" errors |
| **Expected Result** | All %INCLUDE directives resolve. No IEL0340I or IBM1312I errors. |

---

## 3. Online Transaction Tests - Authentication

### TC-AUTH-001: Signon - Valid Regular User

| Field | Value |
|-------|-------|
| **Objective** | Verify regular user can sign on successfully |
| **Transaction** | CC00 (COSGN00C) |
| **Preconditions** | User `USER0001` with password `PASSWORD` exists in DBUSRSEC, type=`U` |
| **Test Steps** | 1. Enter transaction `CC00` at IMS terminal<br>2. Enter User ID: `USER0001`<br>3. Enter Password: `PASSWORD`<br>4. Press Enter |
| **Expected Result** | - IO PCB GU returns status `'  '` (success)<br>- DL/I GU on USRSEG0 returns user record<br>- Password matches<br>- User type = 'U' triggers CHNG to `CM00` (main menu)<br>- Terminal displays main menu screen (COMEN01 MOD) |

### TC-AUTH-002: Signon - Valid Admin User

| Field | Value |
|-------|-------|
| **Objective** | Verify admin user signs on and routes to admin menu |
| **Transaction** | CC00 |
| **Preconditions** | User `ADMIN001` with password `PASSWORD` exists, type=`A` |
| **Test Steps** | 1. Enter CC00, User ID: `ADMIN001`, Password: `PASSWORD` |
| **Expected Result** | - User type = 'A' triggers CHNG to `CA00` (admin menu)<br>- Terminal displays admin menu screen (COADM01 MOD) |

### TC-AUTH-003: Signon - Invalid User ID

| Field | Value |
|-------|-------|
| **Objective** | Verify error message for non-existent user |
| **Transaction** | CC00 |
| **Test Steps** | 1. Enter CC00, User ID: `BADUSER1`, Password: `PASSWORD` |
| **Expected Result** | - DL/I GU returns status `'GE'` (segment not found)<br>- Error message: `'User not found. Try again ...'`<br>- Signon screen redisplayed |

### TC-AUTH-004: Signon - Wrong Password

| Field | Value |
|-------|-------|
| **Objective** | Verify error message for incorrect password |
| **Transaction** | CC00 |
| **Test Steps** | 1. Enter CC00, User ID: `USER0001`, Password: `WRONGPWD` |
| **Expected Result** | - DL/I GU finds user record (status `'  '`)<br>- SEC_USR_PWD does not match input<br>- Error message: `'Wrong Password. Try again ...'`<br>- Signon screen redisplayed |

### TC-AUTH-005: Signon - Missing User ID

| Field | Value |
|-------|-------|
| **Objective** | Verify error when user ID field is blank |
| **Transaction** | CC00 |
| **Test Steps** | 1. Enter CC00 with blank User ID, any Password |
| **Expected Result** | Error message: `'Please enter User ID ...'` |

### TC-AUTH-006: Signon - Missing Password

| Field | Value |
|-------|-------|
| **Objective** | Verify error when password field is blank |
| **Transaction** | CC00 |
| **Test Steps** | 1. Enter CC00, User ID: `USER0001`, blank Password |
| **Expected Result** | Error message: `'Please enter Password ...'` |

### TC-AUTH-007: Signon - Case Insensitivity

| Field | Value |
|-------|-------|
| **Objective** | Verify user ID and password are uppercased before comparison |
| **Transaction** | CC00 |
| **Test Steps** | 1. Enter CC00, User ID: `user0001`, Password: `password` |
| **Expected Result** | - TRANSLATE converts to uppercase<br>- Signon succeeds (same as TC-AUTH-001) |

---

## 4. Online Transaction Tests - Navigation

### TC-NAV-001: Main Menu - Display Options

| Field | Value |
|-------|-------|
| **Objective** | Verify main menu displays all available options |
| **Transaction** | CM00 (COMEN01C) |
| **Preconditions** | User signed in as regular user (CDEMO_PGM_CONTEXT='0') |
| **Test Steps** | 1. Arrive at main menu after signon |
| **Expected Result** | - BUILD_MENU_OPTIONS populates OUT_OPTN array from CSMEN02Y include<br>- Menu options displayed: Account View, Card List, Transaction List, etc.<br>- Header shows: transaction name CM00, program COMEN01C, current date/time |

### TC-NAV-002: Main Menu - Valid Option Selection

| Field | Value |
|-------|-------|
| **Objective** | Verify selecting a valid menu option navigates to correct program |
| **Transaction** | CM00 |
| **Test Steps** | 1. At main menu, enter option `1` (Account View) |
| **Expected Result** | - CHNG ALT PCB to CAVW transaction<br>- ISRT CARDDEMO_COMMAREA to ALT PCB<br>- PURG to complete message switch<br>- Account View screen (COACTVWC) displayed |

### TC-NAV-003: Main Menu - Invalid Option (Out of Range)

| Field | Value |
|-------|-------|
| **Objective** | Verify error for option number out of range |
| **Transaction** | CM00 |
| **Test Steps** | 1. At main menu, enter option `99` |
| **Expected Result** | Error message: `'Please enter a valid option number...'` |

### TC-NAV-004: Main Menu - Non-numeric Option

| Field | Value |
|-------|-------|
| **Objective** | Verify error for non-numeric input in option field |
| **Transaction** | CM00 |
| **Test Steps** | 1. At main menu, enter option `AB` |
| **Expected Result** | VERIFY detects non-numeric; error message displayed |

### TC-NAV-005: Main Menu - Regular User Blocked from Admin Option

| Field | Value |
|-------|-------|
| **Objective** | Verify regular users cannot access admin-only menu options |
| **Transaction** | CM00 |
| **Preconditions** | Signed in as USER0001 (type=U) |
| **Test Steps** | 1. At main menu, select an admin-only option (if available in menu) |
| **Expected Result** | Error message: `'No access - Admin Only option... '` |

### TC-NAV-006: Admin Menu - Display and Navigate

| Field | Value |
|-------|-------|
| **Objective** | Verify admin menu displays correctly and navigation works |
| **Transaction** | CA00 (COADM01C) |
| **Preconditions** | Signed in as ADMIN001 (type=A) |
| **Test Steps** | 1. View admin menu, verify options displayed<br>2. Select option for User List (CU00) |
| **Expected Result** | - Admin options from COADM02Y include are displayed<br>- Selecting User List triggers CHNG/ISRT/PURG to CU00 transaction |

### TC-NAV-007: Return to Signon on IMS Error

| Field | Value |
|-------|-------|
| **Objective** | Verify menu programs return to signon when IO_STATUS indicates error |
| **Transaction** | CM00 |
| **Test Steps** | 1. Simulate IO_STATUS not equal to `'  '` or `'QC'` |
| **Expected Result** | - RETURN_TO_SIGNON triggers CHNG/ISRT/PURG to CC00<br>- Signon screen redisplayed |

---

## 5. Online Transaction Tests - Account Operations

### TC-ACCT-001: Account View - Valid Account

| Field | Value |
|-------|-------|
| **Objective** | Verify account details are displayed correctly |
| **Transaction** | CAVW (COACTVWC) |
| **Preconditions** | Account ID exists in DBACCTDT, related card/customer/xref data loaded |
| **Test Steps** | 1. Navigate to Account View<br>2. Enter a valid Account ID<br>3. Press Enter |
| **Expected Result** | - GU on ACCTSEG0 retrieves account record<br>- GU on XREFSEG0 via secondary index retrieves cross-reference<br>- GU on CUSTSEG0 retrieves customer record<br>- All fields populated in output message<br>- Account balance, status, customer name displayed |

### TC-ACCT-002: Account View - Non-existent Account

| Field | Value |
|-------|-------|
| **Objective** | Verify error for invalid account ID |
| **Transaction** | CAVW |
| **Test Steps** | 1. Enter invalid Account ID that doesn't exist in DBACCTDT |
| **Expected Result** | - GU returns `'GE'` status<br>- Error message: `'Account not found'` or similar |

### TC-ACCT-003: Account Update - Modify Account Details

| Field | Value |
|-------|-------|
| **Objective** | Verify account record can be updated |
| **Transaction** | CAUP (COACTUPC) |
| **Preconditions** | Account exists in DBACCTDT |
| **Test Steps** | 1. Navigate to Account Update<br>2. Enter Account ID and press Enter to retrieve<br>3. Modify a field (e.g., account status)<br>4. Press Enter to confirm |
| **Expected Result** | - GHU retrieves record with hold<br>- REPL updates the segment<br>- DB PCB status = `'  '` (success)<br>- Success message displayed |

---

## 6. Online Transaction Tests - Card Operations

### TC-CARD-001: Card List - By Account ID

| Field | Value |
|-------|-------|
| **Objective** | Verify card list displays all cards for an account |
| **Transaction** | CRDL (COCRDLIC) |
| **Test Steps** | 1. Enter Account ID that has multiple cards<br>2. Press Enter |
| **Expected Result** | - GU on CARDSEG0 with ACCTID qualification retrieves first card<br>- GN retrieves subsequent cards<br>- All cards for the account displayed in scrollable list |

### TC-CARD-002: Card List - By Card Number

| Field | Value |
|-------|-------|
| **Objective** | Verify single card lookup by card number |
| **Transaction** | CRDL |
| **Test Steps** | 1. Enter a valid Card Number |
| **Expected Result** | - GU on CARDSEG0 with CARDNUM qualification returns the card<br>- Card details displayed |

### TC-CARD-003: Card View - Display Card Details

| Field | Value |
|-------|-------|
| **Objective** | Verify card details screen shows full information |
| **Transaction** | CRDS (COCRDSLC) |
| **Test Steps** | 1. Select a card from card list or enter card number directly |
| **Expected Result** | - Card number, account ID, expiration date, status displayed<br>- Cross-reference data (XREFSEG0) retrieved successfully |

### TC-CARD-004: Card Update - Modify Card Details

| Field | Value |
|-------|-------|
| **Objective** | Verify card record can be updated |
| **Transaction** | CRDU (COCRDUPC) |
| **Test Steps** | 1. Enter Card Number<br>2. Modify card status or expiration<br>3. Confirm update |
| **Expected Result** | - GHU on CARDSEG0 retrieves with hold<br>- REPL updates the card segment<br>- Success message displayed |

---

## 7. Online Transaction Tests - Transaction Operations

### TC-TRAN-001: Transaction List - By Card Number

| Field | Value |
|-------|-------|
| **Objective** | Verify transaction list shows transactions for a card |
| **Transaction** | CT00 (COTRN00C) |
| **Test Steps** | 1. Enter Account ID<br>2. Cross-reference lookup finds associated card numbers<br>3. Transactions listed for those cards |
| **Expected Result** | - XREFSEG0 GU retrieves card-account mapping<br>- TRANSEG0 GU/GN with CARDNUM qualification lists transactions<br>- Transaction ID, date, amount, merchant displayed in list |

### TC-TRAN-002: Transaction View - Single Transaction

| Field | Value |
|-------|-------|
| **Objective** | Verify transaction details are displayed |
| **Transaction** | CT01 (COTRN01C) |
| **Test Steps** | 1. Enter Transaction ID |
| **Expected Result** | - GU on TRANSEG0 with TRANID qualification<br>- All transaction fields displayed: ID, type, category, amount, merchant info, timestamps |

### TC-TRAN-003: Transaction Add - Complete Flow

| Field | Value |
|-------|-------|
| **Objective** | Verify adding a new transaction with validation and confirmation |
| **Transaction** | CT02 (COTRN02C) |
| **Preconditions** | Valid card number exists; transaction type and category DBs loaded |
| **Test Steps** | 1. Navigate to Add Transaction<br>2. Enter: Card Number, Transaction Type, Category, Amount, Source, Merchant info<br>3. Press Enter (validation pass - prompts for confirmation)<br>4. Enter confirmation: `Y`<br>5. Press Enter |
| **Expected Result** | - Validation: TTYPSEG GU verifies transaction type exists<br>- Validation: TCATSEG GU verifies category exists<br>- First Enter: `'Confirm transaction with Y'` message<br>- GENERATE_TRAN_ID: GU on TRANSEG0 with high-value key to find last ID<br>- SET_TIMESTAMPS: DATETIME() populates TRAN_ORIG_TS and TRAN_PROC_TS<br>- ISRT to TRANSEG0 creates new record<br>- Success: `'Transaction added successfully. ID: xxxxxx'` |

### TC-TRAN-004: Transaction Add - Missing Required Fields

| Field | Value |
|-------|-------|
| **Objective** | Verify field-level validation for missing inputs |
| **Transaction** | CT02 |
| **Test Steps** | Test each required field blank in turn:<br>1. Blank Card Number → `'Card Number is required'`<br>2. Blank Transaction Type → `'Transaction type is required'`<br>3. Blank Category → `'Transaction category is required'`<br>4. Blank Amount → `'Transaction amount is required'` |
| **Expected Result** | Each missing field produces appropriate error message and redisplays form |

### TC-TRAN-005: Transaction Add - Invalid Type Code

| Field | Value |
|-------|-------|
| **Objective** | Verify error for non-existent transaction type |
| **Transaction** | CT02 |
| **Test Steps** | 1. Enter valid Card Number but invalid Transaction Type code (e.g., `ZZ`) |
| **Expected Result** | - TTYPSEG GU returns non-blank status<br>- Error: `'Invalid transaction type code'` |

### TC-TRAN-006: Transaction Add - Unique ID Generation

| Field | Value |
|-------|-------|
| **Objective** | Verify GENERATE_TRAN_ID produces unique, incrementing IDs |
| **Transaction** | CT02 |
| **Test Steps** | 1. Add first transaction → note generated ID (should be highest_existing + 1)<br>2. Add second transaction → note generated ID (should be previous + 1) |
| **Expected Result** | Each new transaction gets a unique ID that doesn't collide with existing records |
| **Known Risk** | GU with high-value key on HIDAM may return GE instead of nearest lower key. If this happens, the approach needs to change to sequential GN scan. |

---

## 8. Online Transaction Tests - Bill Payment

### TC-BILL-001: Bill Payment - Full Balance Payment

| Field | Value |
|-------|-------|
| **Objective** | Verify bill payment processes correctly for full balance |
| **Transaction** | CB00 (COBIL00C) |
| **Preconditions** | Account has positive balance (e.g., $500.00) |
| **Test Steps** | 1. Enter Account ID<br>2. Press Enter (displays current balance)<br>3. Enter confirmation `Y`<br>4. Press Enter |
| **Expected Result** | - GHU on ACCTSEG0 retrieves account with hold<br>- Balance displayed<br>- GU on XREFSEG0 retrieves card number for account<br>- GENERATE_TRAN_ID produces unique ID<br>- Transaction record created: type='02', desc='BILL PAYMENT - ONLINE'<br>- ISRT to TRANSEG0<br>- Account balance updated to $0.00 via REPL on ACCTSEG0<br>- Success: `'Bill payment processed successfully'` |

### TC-BILL-002: Bill Payment - Cancelled

| Field | Value |
|-------|-------|
| **Objective** | Verify cancellation of bill payment |
| **Transaction** | CB00 |
| **Test Steps** | 1. Enter Account ID<br>2. Enter confirmation `N` |
| **Expected Result** | Message: `'Payment cancelled'`. No database changes. |

### TC-BILL-003: Bill Payment - Zero Balance

| Field | Value |
|-------|-------|
| **Objective** | Verify error when account has no balance to pay |
| **Transaction** | CB00 |
| **Preconditions** | Account has balance <= 0 |
| **Test Steps** | 1. Enter Account ID for zero-balance account |
| **Expected Result** | Error: `'You have nothing to pay...'` |

### TC-BILL-004: Bill Payment - Invalid Account

| Field | Value |
|-------|-------|
| **Objective** | Verify error for non-existent account |
| **Transaction** | CB00 |
| **Test Steps** | 1. Enter invalid Account ID |
| **Expected Result** | - GHU returns `'GE'` status<br>- Error: `'Account ID NOT found...'` |

### TC-BILL-005: Bill Payment - Missing Account ID

| Field | Value |
|-------|-------|
| **Objective** | Verify error when account ID is blank |
| **Transaction** | CB00 |
| **Test Steps** | 1. Leave Account ID blank, press Enter |
| **Expected Result** | Error: `'Acct ID can NOT be empty...'` |

### TC-BILL-006: Bill Payment - Invalid Confirmation Input

| Field | Value |
|-------|-------|
| **Objective** | Verify error for invalid confirmation value |
| **Transaction** | CB00 |
| **Test Steps** | 1. Enter Account ID<br>2. Enter confirmation `X` |
| **Expected Result** | Error: `'Invalid value. Valid values are (Y/N)...'` |

---

## 9. Online Transaction Tests - User Management (Admin)

### TC-USER-001: User List - Display Users

| Field | Value |
|-------|-------|
| **Objective** | Verify user list displays all users from DBUSRSEC |
| **Transaction** | CU00 (COUSR00C) |
| **Preconditions** | Signed in as admin |
| **Test Steps** | 1. Navigate to User List from admin menu |
| **Expected Result** | - GU/GN on USRSEG0 retrieves user records sequentially<br>- User IDs, names, types displayed in list |

### TC-USER-002: User Add - Valid New User

| Field | Value |
|-------|-------|
| **Objective** | Verify adding a new user to DBUSRSEC |
| **Transaction** | CU01 (COUSR01C) |
| **Test Steps** | 1. Enter: User ID=`NEWUSR01`, First=`JOHN`, Last=`DOE`, Password=`PASS1234`, Type=`U`<br>2. Press Enter |
| **Expected Result** | - GU on USRSEG0 with USRID returns `'GE'` (user doesn't exist)<br>- ISRT creates new user record<br>- Status `'  '` (success)<br>- Message: `'User added successfully'` |

### TC-USER-003: User Add - Duplicate User ID

| Field | Value |
|-------|-------|
| **Objective** | Verify error when adding existing user ID |
| **Transaction** | CU01 |
| **Test Steps** | 1. Enter User ID=`USER0001` (already exists) |
| **Expected Result** | - GU on USRSEG0 returns `'  '` (found)<br>- Error: `'User ID already exists'` |

### TC-USER-004: User Add - Field Validation

| Field | Value |
|-------|-------|
| **Objective** | Verify required field validation for user add |
| **Transaction** | CU01 |
| **Test Steps** | Test each blank field:<br>1. Blank User ID → `'User ID is required'`<br>2. Blank First Name → `'First Name is required'`<br>3. Blank Last Name → `'Last Name is required'`<br>4. Blank Password → `'Password is required'`<br>5. Invalid Type (not A/U) → `'User Type must be A(dmin) or U(ser)'` |
| **Expected Result** | Each validation produces appropriate error message |

### TC-USER-005: User Update - Modify User Details

| Field | Value |
|-------|-------|
| **Objective** | Verify user record can be updated |
| **Transaction** | CU02 (COUSR02C) |
| **Test Steps** | 1. Enter existing User ID<br>2. Modify password or user type<br>3. Confirm update |
| **Expected Result** | - GHU retrieves with hold<br>- REPL updates the segment<br>- Success message |

### TC-USER-006: User Delete - Remove User

| Field | Value |
|-------|-------|
| **Objective** | Verify user can be deleted from DBUSRSEC |
| **Transaction** | CU03 (COUSR03C) |
| **Preconditions** | PSB PCOUSR03 has PROCOPT=GD (Get + Delete) |
| **Test Steps** | 1. Enter User ID to delete<br>2. Confirm deletion |
| **Expected Result** | - GHU retrieves user with hold<br>- DLET removes the segment<br>- DB PCB status `'  '` (success)<br>- Message: `'User deleted successfully'` |
| **Validation** | Verify PCOUSR03.psb has `PROCOPT=GD` (not DLET) |

---

## 10. Online Transaction Tests - Reports

### TC-RPT-001: Transaction Report Request

| Field | Value |
|-------|-------|
| **Objective** | Verify report selection screen displays and processes correctly |
| **Transaction** | CR00 (CORPT00C) |
| **Test Steps** | 1. Navigate to Transaction Reports<br>2. Select report type<br>3. Enter date range or account filter |
| **Expected Result** | - Report parameters captured<br>- Output message shows report data or triggers batch job |

---

## 11. Batch Program Tests

### TC-BAT-001: Account Data Load (CBACT01C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch loading of account master data into DBACCTDT |
| **JCL** | DATALOAD.jcl (CBACT01C step) |
| **Preconditions** | Account data file in FB RECSIZE(300) format; DBACCTDT database empty or cleared |
| **Test Steps** | 1. Prepare account data input file<br>2. Submit DATALOAD.jcl<br>3. Check SYSPRINT output |
| **Expected Result** | - `'START OF EXECUTION OF PROGRAM CBACT01C'` message<br>- Each record: ISRT to ACCTSEG0, status `'  '` for success<br>- Final counts: RECORDS READ = input count, RECORDS LOADED = successful inserts<br>- `'END OF EXECUTION OF PROGRAM CBACT01C'` message |
| **Validation** | Query DBACCTDT to verify loaded records match input count and content |

### TC-BAT-002: Card Data Load (CBACT02C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch loading of card data into DBCARDDT |
| **JCL** | DATALOAD.jcl (CBACT02C step) |
| **Expected Result** | All card records loaded; counts match; no errors in SYSPRINT |

### TC-BAT-003: Card Cross-Reference Load (CBACT03C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch loading of cross-reference data into DBCXREF |
| **JCL** | DATALOAD.jcl (CBACT03C step) |
| **Expected Result** | - ISRT to XREFSEG0 (corrected from CXREFSEG) for each record<br>- All records loaded successfully |
| **Validation** | Verify SSA uses `'XREFSEG0'` (not `'CXREFSEG'`) - this was a bug fix |

### TC-BAT-004: Customer Data Load (CBCUS01C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch loading of customer data into DBCUSTDT |
| **JCL** | DATALOAD.jcl (CBCUS01C step) |
| **Expected Result** | All customer records loaded; counts reported |

### TC-BAT-005: Transaction Data Load (CBTRN01C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch loading of transaction data into DBTRANSC |
| **JCL** | DATALOAD.jcl (CBTRN01C step) |
| **Expected Result** | All transaction records loaded |

### TC-BAT-006: Daily Transaction Posting (CBTRN02C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch posting of daily transactions with validation |
| **JCL** | POSTTRAN.jcl |
| **Preconditions** | All master data loaded; daily transaction file prepared |
| **Test Steps** | 1. Prepare daily transaction input file (DALYTRAN_FILE)<br>2. Submit POSTTRAN.jcl<br>3. Review SYSPRINT and reject file |
| **Expected Result** | For each transaction:<br>- VALIDATE_TRANSACTION: Checks card exists (XREFSEG0 GU), account exists and active (ACCTSEG0 GU), amount > 0<br>- Valid: POST_TRANSACTION inserts to TRANSEG0, updates account balance via GHU/REPL on ACCTSEG0, updates category balance<br>- Invalid: Written to reject file<br>- Final counts: read, posted, rejected |
| **Test Data** | Include mix of: valid transactions, invalid card numbers, inactive accounts, zero/negative amounts |

### TC-BAT-007: Daily Transaction Posting - Reject Handling

| Field | Value |
|-------|-------|
| **Objective** | Verify rejected transactions are properly written to reject file |
| **JCL** | POSTTRAN.jcl |
| **Test Steps** | 1. Include transactions with non-existent card numbers in input<br>2. Submit job<br>3. Examine reject file |
| **Expected Result** | - Transactions with invalid cards: XREFSEG0 GU returns non-blank status → WS_VALID='N'<br>- Transactions with inactive accounts: ACCT_ACTIVE_STATUS ^= 'Y' → WS_VALID='N'<br>- All invalid records written to DALYREJS_FILE |

### TC-BAT-008: Interest Calculation (CBACT04C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch interest calculation on account balances |
| **JCL** | INTCALC.jcl |
| **Preconditions** | Accounts and cross-references loaded; accounts have balances |
| **Test Steps** | 1. Submit INTCALC.jcl<br>2. Check SYSPRINT for processing results |
| **Expected Result** | - Sequential scan of accounts via GN on ACCTSEG0<br>- For each account with balance: calculate interest<br>- GHU/REPL on ACCTSEG0 to update balance with interest<br>- Cross-reference (XREFSEG0) used to look up account-card mappings |

### TC-BAT-009: Statement Generation (CBSTM03A)

| Field | Value |
|-------|-------|
| **Objective** | Verify statement generation for accounts |
| **JCL** | CREASTMT.jcl |
| **Test Steps** | 1. Submit CREASTMT.jcl |
| **Expected Result** | - Sequential scan of accounts (ACCTSEG0 GU/GN)<br>- For each account: look up cross-reference (XREFSEG0), customer (CUSTSEG0), transactions (TRANSEG0)<br>- Statement data written to output file |

### TC-BAT-010: Statement Generation (CBSTM03B)

| Field | Value |
|-------|-------|
| **Objective** | Verify alternate statement generation batch program |
| **JCL** | CREASTMT.jcl (CBSTM03B step) |
| **Expected Result** | Similar to TC-BAT-009 with CBSTM03B-specific output format |

### TC-BAT-011: Transaction Report (CBTRN03C)

| Field | Value |
|-------|-------|
| **Objective** | Verify batch transaction report generation |
| **JCL** | TRANRPT.jcl |
| **Test Steps** | 1. Submit TRANRPT.jcl |
| **Expected Result** | - Sequential scan of TRANSEG0<br>- Report data written with totals |

### TC-BAT-012: Data Export (CBEXPORT)

| Field | Value |
|-------|-------|
| **Objective** | Verify full data export to sequential file |
| **JCL** | EXPIMPRT.jcl (export step) |
| **Test Steps** | 1. Submit export job |
| **Expected Result** | - Exports each database sequentially: accounts (A), customers (C), cards (D), xrefs (X), transactions (T)<br>- For each: GU to first segment, then GN loop until non-blank status<br>- EXPORT_RECORD written with record type prefix and sequence number<br>- Final counts printed for each entity type<br>- All SSA segment names correct: ACCTSEG0, CUSTSEG0, CARDSEG0, XREFSEG0, TRANSEG0 |

### TC-BAT-013: Data Import (CBIMPORT)

| Field | Value |
|-------|-------|
| **Objective** | Verify data import from sequential export file |
| **JCL** | EXPIMPRT.jcl (import step) |
| **Preconditions** | Export file created by TC-BAT-012 |
| **Test Steps** | 1. Clear databases<br>2. Submit import job with export file as input |
| **Expected Result** | - Reads export records<br>- Routes by record type: A→ACCTSEG0 ISRT, C→CUSTSEG0 ISRT, D→CARDSEG0 ISRT, X→XREFSEG0 ISRT, T→TRANSEG0 ISRT<br>- All records imported successfully<br>- Counts match export counts |

### TC-BAT-014: Export/Import Round-Trip Integrity

| Field | Value |
|-------|-------|
| **Objective** | Verify data integrity after export and re-import cycle |
| **Test Steps** | 1. Load sample data<br>2. Run export (TC-BAT-012)<br>3. Clear all databases<br>4. Run import (TC-BAT-013)<br>5. Run export again<br>6. Compare the two export files |
| **Expected Result** | Both export files are identical (or differ only in sequence numbers), confirming no data loss or corruption |

---

## 12. Cross-Reference and Secondary Index Tests

### TC-XREF-001: Card-to-Account Lookup via Secondary Index

| Field | Value |
|-------|-------|
| **Objective** | Verify DBCXRIDX secondary index enables account lookup by card number |
| **Preconditions** | DBCXREF loaded with cross-reference data; DBCXRIDX secondary index built |
| **Test Steps** | 1. GU on XREFSEG0 with CARDNUM qualification<br>2. Read XREF_ACCT_ID from returned segment |
| **Expected Result** | Secondary index returns correct account ID for the given card number |

### TC-XREF-002: Account-to-Card Lookup

| Field | Value |
|-------|-------|
| **Objective** | Verify cross-reference lookup by account ID |
| **Test Steps** | 1. GU on XREFSEG0 with ACCTID qualification<br>2. Multiple GN calls if account has multiple cards |
| **Expected Result** | All cards for the account are retrieved |

---

## 13. DL/I Call Pattern Verification Tests

### TC-DLI-001: PLITDLI Calling Convention

| Field | Value |
|-------|-------|
| **Objective** | Verify the PLITDLI external entry call pattern compiles and links correctly |
| **Test Steps** | 1. Compile any program (e.g., COSGN00C.pli)<br>2. Verify PLITDLI resolves to IMS DL/I interface module<br>3. Execute a simple GU call |
| **Expected Result** | PLITDLI calls execute without S0C1/S0C4 abends |
| **Known Risk** | Some IMS environments require `CALL PLITDLI(parm_count, func, pcb, area, ssa)` with an explicit parameter count as the first argument. If calls fail, every PLITDLI call across all 30 programs needs modification. |

### TC-DLI-002: GU (Get Unique) with Qualified SSA

| Field | Value |
|-------|-------|
| **Objective** | Verify GU retrieves correct segment with key qualification |
| **Test Steps** | 1. GU on USRSEG0 with `'USRSEG0 (USRID   =USER0001)'`<br>2. Check DB PCB status<br>3. Verify returned data matches expected user record |
| **Expected Result** | Status `'  '`, correct user record returned |

### TC-DLI-003: GHU (Get Hold Unique) for Update

| Field | Value |
|-------|-------|
| **Objective** | Verify GHU holds segment for subsequent REPL |
| **Test Steps** | 1. GHU on ACCTSEG0<br>2. Modify a field<br>3. REPL to update |
| **Expected Result** | GHU returns status `'  '`, REPL succeeds with status `'  '` |

### TC-DLI-004: GN (Get Next) Sequential Retrieval

| Field | Value |
|-------|-------|
| **Objective** | Verify GN retrieves segments sequentially |
| **Test Steps** | 1. GU to position at first segment<br>2. Repeated GN calls<br>3. Verify each call returns next segment in key sequence |
| **Expected Result** | Segments returned in ascending key order until `'GE'` status (end of database) |

### TC-DLI-005: ISRT (Insert) New Segment

| Field | Value |
|-------|-------|
| **Objective** | Verify ISRT creates new segment in database |
| **Test Steps** | 1. Build a new record (e.g., new user)<br>2. ISRT to USRSEG0<br>3. GU to verify record exists |
| **Expected Result** | ISRT status `'  '`, subsequent GU retrieves the inserted record |

### TC-DLI-006: DLET (Delete) Segment

| Field | Value |
|-------|-------|
| **Objective** | Verify DLET removes a segment (used by COUSR03C) |
| **Preconditions** | PCOUSR03.psb has PROCOPT=GD |
| **Test Steps** | 1. GHU to retrieve user with hold<br>2. DLET to delete<br>3. GU to verify record is gone |
| **Expected Result** | DLET status `'  '`, subsequent GU returns `'GE'` |

### TC-DLI-007: CHNG/ISRT/PURG Message Switch

| Field | Value |
|-------|-------|
| **Objective** | Verify IMS message switch pattern works correctly |
| **Test Steps** | 1. CHNG ALT PCB to target transaction code (e.g., 'CM00')<br>2. ISRT CARDDEMO_COMMAREA to ALT PCB<br>3. PURG to complete the switch |
| **Expected Result** | - CHNG status `'  '`<br>- ISRT status `'  '`<br>- Target transaction is scheduled<br>- COMMAREA data passed to target program |

### TC-DLI-008: SSA Field Name Padding Verification

| Field | Value |
|-------|-------|
| **Objective** | Verify all SSA field names are correctly padded to 8 characters |
| **Test Steps** | For each qualified SSA in the codebase, verify:<br>- `USRID   ` (5 chars + 3 spaces = 8)<br>- `ACCTID  ` (6 chars + 2 spaces = 8)<br>- `CARDNUM ` (7 chars + 1 space = 8)<br>- `CUSTID  ` (6 chars + 2 spaces = 8)<br>- `TRANID  ` (6 chars + 2 spaces = 8)<br>Match against FIELD NAME= in corresponding DBDs |
| **Expected Result** | Every SSA field name is exactly 8 characters and matches DBD field definition |

---

## 14. Shared Utility Include Tests

### TC-UTIL-001: POPULATE_HEADER_INFO - Date/Time Formatting

| Field | Value |
|-------|-------|
| **Objective** | Verify header info procedure formats date and time correctly |
| **Test Steps** | 1. Call POPULATE_HEADER_INFO from any online program<br>2. Check OUT_CURDATE format (MM/DD/YY)<br>3. Check OUT_CURTIME format (HH:MM:SS) |
| **Expected Result** | - OUT_TRNNAME = WS_TRANID (e.g., 'CC00')<br>- OUT_TITLE01 = CCDA_TITLE01 from COTTL01Y<br>- OUT_TITLE02 = CCDA_TITLE02 from COTTL01Y<br>- OUT_PGMNAME = WS_PGMNAME (e.g., 'COSGN00C')<br>- OUT_CURDATE = current date in MM/DD/YY<br>- OUT_CURTIME = current time in HH:MM:SS |

### TC-UTIL-002: RETURN_TO_PREV_SCREEN

| Field | Value |
|-------|-------|
| **Objective** | Verify screen return utility works with different target transactions |
| **Test Steps** | 1. Call RETURN_TO_PREV_SCREEN('CC00') from any online program<br>2. Verify CHNG sets destination, ISRT sends commarea, PURG completes switch |
| **Expected Result** | Terminal returns to signon screen (CC00 transaction) |

### TC-UTIL-003: CSUTIL01Y Inclusion in All Online Programs

| Field | Value |
|-------|-------|
| **Objective** | Verify all 17 online programs include CSUTIL01Y and no inline copies remain |
| **Test Steps** | 1. Grep all online .pli files for `%INCLUDE CSUTIL01Y`<br>2. Grep all online .pli files for `POPULATE_HEADER_INFO: PROC;` (inline) |
| **Expected Result** | - 17 files contain `%INCLUDE CSUTIL01Y`<br>- 0 files contain inline `POPULATE_HEADER_INFO: PROC;` definition |

---

## 15. End-to-End Integration Tests

### TC-E2E-001: Full Signon → Account View Flow

| Field | Value |
|-------|-------|
| **Objective** | Verify complete user journey: signon → menu → account view |
| **Test Steps** | 1. CC00: Sign on as USER0001/PASSWORD<br>2. CM00: Select Account View option<br>3. CAVW: Enter Account ID, view account details<br>4. Press PF3 to return to menu |
| **Expected Result** | - Each screen transition uses CHNG/ISRT/PURG correctly<br>- CARDDEMO_COMMAREA preserved across all transactions<br>- CDEMO_FROM_TRANID and CDEMO_FROM_PROGRAM track navigation history<br>- All screens display header info (date, time, program name, titles) |

### TC-E2E-002: Full Signon → Add Transaction → View Transaction

| Field | Value |
|-------|-------|
| **Objective** | Verify complete transaction add and view cycle |
| **Test Steps** | 1. CC00: Sign on<br>2. CM00: Select Add Transaction<br>3. CT02: Enter transaction details, confirm<br>4. Note generated transaction ID<br>5. Navigate to Transaction View (CT01)<br>6. Enter the generated transaction ID<br>7. Verify all fields match what was entered |
| **Expected Result** | Transaction persists in DBTRANSC and can be retrieved by ID |

### TC-E2E-003: Full Admin Flow - Add, View, Delete User

| Field | Value |
|-------|-------|
| **Objective** | Verify complete user lifecycle via admin functions |
| **Test Steps** | 1. CC00: Sign on as ADMIN001/PASSWORD<br>2. CA00: Navigate to User Add (CU01)<br>3. CU01: Add user TESTUSER/TESTFN/TESTLN/TESTPWD/U<br>4. Navigate to User List (CU00): Verify TESTUSER appears<br>5. Navigate to User Update (CU02): Modify password<br>6. Verify modified password works for signon<br>7. Navigate to User Delete (CU03): Delete TESTUSER<br>8. Verify TESTUSER no longer exists (signon fails) |
| **Expected Result** | Full CRUD lifecycle works correctly. User can be created, listed, updated, and deleted. |

### TC-E2E-004: Full Batch Cycle

| Field | Value |
|-------|-------|
| **Objective** | Verify complete batch processing cycle |
| **Test Steps** | 1. Run DATALOAD.jcl (load all master data)<br>2. Run POSTTRAN.jcl (post daily transactions)<br>3. Run INTCALC.jcl (calculate interest)<br>4. Run CREASTMT.jcl (generate statements)<br>5. Run TRANRPT.jcl (transaction report)<br>6. Run EXPIMPRT.jcl export step<br>7. Clear databases<br>8. Run EXPIMPRT.jcl import step<br>9. Verify data integrity |
| **Expected Result** | Complete batch cycle runs without errors. Export/import round-trip preserves all data. |

### TC-E2E-005: Bill Payment End-to-End with Balance Verification

| Field | Value |
|-------|-------|
| **Objective** | Verify bill payment updates both transaction and account databases |
| **Test Steps** | 1. Sign on, view account balance (note original amount)<br>2. Navigate to Bill Payment (CB00)<br>3. Enter Account ID, confirm payment<br>4. Return to Account View<br>5. Verify balance is now $0.00<br>6. Navigate to Transaction List<br>7. Verify bill payment transaction appears with correct amount |
| **Expected Result** | Account balance reduced to zero. New transaction record created with type '02' and 'BILL PAYMENT' description. |

---

## 16. Error Handling and Edge Case Tests

### TC-ERR-001: Database Unavailable

| Field | Value |
|-------|-------|
| **Objective** | Verify graceful handling when a database is stopped |
| **Test Steps** | 1. Stop DBUSRSEC database<br>2. Attempt signon |
| **Expected Result** | - DL/I call returns non-standard status code<br>- Program handles via OTHERWISE clause<br>- Error message displayed: `'Unable to verify the User ...'`<br>- No abend |

### TC-ERR-002: Concurrent Update Conflict

| Field | Value |
|-------|-------|
| **Objective** | Verify handling when another program has a segment held |
| **Test Steps** | 1. From terminal 1: GHU on an account (hold for update)<br>2. From terminal 2: Attempt GHU on same account |
| **Expected Result** | Terminal 2 receives appropriate status code and handles wait/retry |

### TC-ERR-003: Duplicate Key Insert

| Field | Value |
|-------|-------|
| **Objective** | Verify handling of duplicate key on ISRT |
| **Test Steps** | 1. Attempt to insert a user record with an existing USRID |
| **Expected Result** | - Program checks for existence first (GU before ISRT pattern)<br>- In COUSR01C: `'User ID already exists'` message |

### TC-ERR-004: Maximum Message Size

| Field | Value |
|-------|-------|
| **Objective** | Verify OUTPUT_MSG.LL is correctly set to message size |
| **Test Steps** | 1. Check each program's SEND_MAP: `OUTPUT_MSG.LL = SIZE(OUTPUT_MSG)`<br>2. Verify LL value doesn't exceed IMS message buffer limits |
| **Expected Result** | All output messages have correct LL field. No IMS message truncation. |

### TC-ERR-005: Empty Database Scan

| Field | Value |
|-------|-------|
| **Objective** | Verify batch programs handle empty databases gracefully |
| **Test Steps** | 1. Clear DBTRANSC database<br>2. Run CBEXPORT |
| **Expected Result** | - GU returns non-blank status immediately<br>- Loop terminates<br>- TRANS EXPORTED: 0<br>- Program completes normally (no abend) |

---

## 17. Performance and Stress Tests

### TC-PERF-001: Bulk Data Load Performance

| Field | Value |
|-------|-------|
| **Objective** | Measure time to load full sample dataset |
| **Test Steps** | 1. Load all sample data files (accounts, cards, customers, xrefs, transactions)<br>2. Record elapsed time and checkpoint frequency |
| **Expected Result** | Baseline timing established. No IMS region timeout. |

### TC-PERF-002: Large Transaction Posting Batch

| Field | Value |
|-------|-------|
| **Objective** | Verify batch posting handles large daily transaction files |
| **Test Steps** | 1. Create daily transaction file with 10,000+ records<br>2. Run POSTTRAN.jcl<br>3. Monitor IMS checkpoints and region performance |
| **Expected Result** | All records processed. Checkpoint frequency appropriate. No region failures. |

### TC-PERF-003: Concurrent Online Transaction Load

| Field | Value |
|-------|-------|
| **Objective** | Verify system handles multiple simultaneous online transactions |
| **Test Steps** | 1. Simulate 10 concurrent terminal sessions<br>2. Each performs: signon → account view → bill payment<br>3. Monitor IMS queue depth and response times |
| **Expected Result** | All transactions complete. No deadlocks. Reasonable response times. |

---

## Appendix A: Test Data Requirements

| Database | Data File | Record Count | Key Field |
|----------|-----------|--------------|-----------|
| DBUSRSEC | USRSEC.dat | 5+ users | USER-ID (8 bytes) |
| DBACCTDT | ACCTDATA.dat | 10+ accounts | ACCT-ID (11 bytes) |
| DBCARDDT | CARDDATA.dat | 15+ cards | CARD-NUM (16 bytes) |
| DBCUSTDT | CUSTDATA.dat | 10+ customers | CUST-ID (10 bytes) |
| DBTRANSC | TRANDATA.dat | 50+ transactions | TRAN-ID (16 bytes) |
| DBCXREF  | XREFDATA.dat | 15+ cross-refs | CARD-NUM (16 bytes) |

Sample data from `app/data/ASCII/` can be converted to fixed-length records matching the PL/I include record layouts.

## Appendix B: Known Issues and Risks

| ID | Description | Impact | Mitigation |
|----|-------------|--------|------------|
| RISK-001 | PLITDLI may require parameter count as first argument | Every DL/I call in all 30 programs fails | Test TC-DLI-001 first; if needed, add count to all calls |
| RISK-002 | GU with high-value key for transaction ID generation may return GE on HIDAM | Transaction add and bill payment fail to generate unique IDs | Replace with GN sequential scan to end of database |
| RISK-003 | SSA field name padding must match DBD FIELD definitions exactly | DL/I calls return GE (segment not found) | Test TC-DLI-008; verify all SSA strings |
| RISK-004 | MFS field positions may not match original BMS exactly | Garbled 3270 screens | Test TC-INF-006 with representative screens |
| RISK-005 | PL/I DATETIME() format may differ across compiler versions | Incorrect date/time display in headers and timestamps | Verify DATETIME() returns YYYYMMDDHHMMSS format |

## Appendix C: Test Execution Summary Template

| Test ID | Description | Status | RC | Notes |
|---------|-------------|--------|-----|-------|
| TC-INF-001 | DBD Generation | | | |
| TC-INF-002 | Segment Size Match | | | |
| TC-INF-003 | PSB Generation | | | |
| TC-INF-004 | PSB-Program Alignment | | | |
| ... | ... | | | |

**Status values:** PASS, FAIL, SKIP, BLOCKED
