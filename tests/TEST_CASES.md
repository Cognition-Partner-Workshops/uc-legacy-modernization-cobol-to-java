# CardDemo Test Cases

> **Application:** AWS CardDemo — Mainframe Credit Card Management System (Modernized Web UI)  
> **Generated:** 2026-03-25

---

## Test Data

| Role | User ID | Password | Description |
|---|---|---|---|
| Admin | ADMIN001 | PASSWORD | Full access: user mgmt + all regular functions |
| Regular | USER0001 | PASSWORD | Regular user: account, card, transaction operations |

| Entity | Test ID | Description |
|---|---|---|
| Account | 00000000011 | Active test account with balance |
| Card | 4000000000000001 | Active card linked to test account |
| Account (zero bal) | 00000000099 | Account with zero balance (bill pay edge case) |

---

## Module 1: Authentication (COSGN00C)

### TC-AUTH-001: Successful login as regular user
- **Precondition:** User USER0001 exists in USRSEC
- **Steps:** Navigate to login page → Enter "USER0001" / "PASSWORD" → Click Sign On
- **Expected:** Redirect to Main Menu. Menu displays 11 options (Account View through Pending Authorization View)

### TC-AUTH-002: Successful login as admin user
- **Precondition:** User ADMIN001 exists in USRSEC
- **Steps:** Navigate to login page → Enter "ADMIN001" / "PASSWORD" → Click Sign On
- **Expected:** Redirect to Admin Menu. Menu displays 6 options (User List through Transaction Type Maintenance)

### TC-AUTH-003: Login with empty User ID
- **Steps:** Navigate to login page → Leave User ID blank → Enter password → Click Sign On
- **Expected:** Error message "Please enter User ID ..."

### TC-AUTH-004: Login with empty Password
- **Steps:** Navigate to login page → Enter "USER0001" → Leave password blank → Click Sign On
- **Expected:** Error message "Please enter Password ..."

### TC-AUTH-005: Login with wrong password
- **Steps:** Navigate to login page → Enter "USER0001" / "WRONGPWD" → Click Sign On
- **Expected:** Error message "Wrong Password. Try again ..."

### TC-AUTH-006: Login with non-existent user
- **Steps:** Navigate to login page → Enter "BADUSER1" / "PASSWORD" → Click Sign On
- **Expected:** Error message "User not found. Try again ..."

### TC-AUTH-007: Case-insensitive User ID
- **Steps:** Navigate to login page → Enter "user0001" (lowercase) / "PASSWORD" → Click Sign On
- **Expected:** Successful login (COBOL UPPER-CASE function applied to input)

### TC-AUTH-008: Logout via F3/Exit
- **Steps:** Login as USER0001 → Press F3/Exit from Main Menu
- **Expected:** Return to sign-on screen with "Thank you" message

---

## Module 2: Main Menu Navigation (COMEN01C)

### TC-MENU-001: Display all menu options for regular user
- **Precondition:** Logged in as USER0001
- **Expected:** 11 options displayed: (1) Account View, (2) Account Update, (3) Credit Card List, (4) Credit Card View, (5) Credit Card Update, (6) Transaction List, (7) Transaction View, (8) Transaction Add, (9) Transaction Reports, (10) Bill Payment, (11) Pending Authorization View

### TC-MENU-002: Navigate to Account View (option 1)
- **Precondition:** Logged in as USER0001
- **Steps:** Enter "1" → Press Enter
- **Expected:** Account View screen is displayed

### TC-MENU-003: Navigate to each menu option
- **Precondition:** Logged in as USER0001
- **Steps:** For each option 1-10, enter number → Press Enter → Verify correct screen → Return via F3
- **Expected:** Each option navigates to the correct module screen

### TC-MENU-004: Invalid option number
- **Precondition:** Logged in as USER0001
- **Steps:** Enter "99" → Press Enter
- **Expected:** Error message "Please enter a valid option number..."

### TC-MENU-005: Zero option number
- **Precondition:** Logged in as USER0001
- **Steps:** Enter "0" → Press Enter
- **Expected:** Error message "Please enter a valid option number..."

### TC-MENU-006: Non-numeric option
- **Precondition:** Logged in as USER0001
- **Steps:** Enter "AB" → Press Enter
- **Expected:** Error message "Please enter a valid option number..."

### TC-MENU-007: Admin-only option restriction for regular user
- **Precondition:** Logged in as USER0001 (regular user)
- **Steps:** Attempt to access admin-only option
- **Expected:** Error message "No access - Admin Only option..."

---

## Module 3: Admin Menu (COADM01C)

### TC-ADMN-001: Display admin menu options
- **Precondition:** Logged in as ADMIN001
- **Expected:** 6 options: (1) User List, (2) User Add, (3) User Update, (4) User Delete, (5) Transaction Type List/Update (Db2), (6) Transaction Type Maintenance (Db2)

### TC-ADMN-002: Navigate to User List (option 1)
- **Steps:** Enter "1" → Press Enter
- **Expected:** User List screen displays list of users

### TC-ADMN-003: Navigate to User Add (option 2)
- **Steps:** Enter "2" → Press Enter
- **Expected:** User Add form is displayed

### TC-ADMN-004: Navigate to User Update (option 3)
- **Steps:** Enter "3" → Press Enter
- **Expected:** User Update screen is displayed

### TC-ADMN-005: Navigate to User Delete (option 4)
- **Steps:** Enter "4" → Press Enter
- **Expected:** User Delete screen is displayed

### TC-ADMN-006: Invalid option number
- **Steps:** Enter "99" → Press Enter
- **Expected:** Error "Please enter a valid option number..."

### TC-ADMN-007: Return to sign-on via F3
- **Steps:** Press F3/Exit from Admin Menu
- **Expected:** Return to sign-on screen

---

## Module 4: Account View (COACTVWC)

### TC-AVEW-001: View account by valid account ID
- **Precondition:** Logged in, on Account View screen
- **Steps:** Enter account ID "00000000011" → Press Enter
- **Expected:** Account details displayed: account ID, status, balances, credit limit, customer info

### TC-AVEW-002: View account with non-existent ID
- **Steps:** Enter account ID "99999999999" → Press Enter
- **Expected:** Error message "Did not find this account in account card xref file"

### TC-AVEW-003: View account with non-numeric ID
- **Steps:** Enter "ABCDEFGHIJK" → Press Enter
- **Expected:** Error message "Account number must be a non zero 11 digit number"

### TC-AVEW-004: View account with zero ID
- **Steps:** Enter "00000000000" → Press Enter
- **Expected:** Error message "Account number must be a non zero 11 digit number"

### TC-AVEW-005: View account with empty input
- **Steps:** Press Enter with no account ID
- **Expected:** Error message "No input received" or prompt for account number

### TC-AVEW-006: Return to menu via F3
- **Steps:** Press F3 from Account View
- **Expected:** Return to Main Menu

---

## Module 5: Account Update (COACTUPC)

### TC-AUPD-001: Display account for update
- **Precondition:** Logged in, on Account Update screen
- **Steps:** Enter account ID "00000000011" → Press Enter
- **Expected:** Account fields displayed in editable mode

### TC-AUPD-002: Update account credit limit
- **Steps:** Display account → Modify credit limit field → Confirm (Y) → Press Enter
- **Expected:** Success message confirming account updated. Credit limit changed in data.

### TC-AUPD-003: Cancel account update
- **Steps:** Display account → Modify fields → Confirm (N) → Press Enter
- **Expected:** Changes not saved. Screen cleared or returned to previous state.

### TC-AUPD-004: Update with invalid confirm value
- **Steps:** Display account → Enter "X" in confirm field → Press Enter
- **Expected:** Error "Invalid value. Valid values are (Y/N)..."

### TC-AUPD-005: Update non-existent account
- **Steps:** Enter account ID "99999999999" → Press Enter
- **Expected:** Error message indicating account not found

### TC-AUPD-006: Return to menu via F3
- **Steps:** Press F3 from Account Update
- **Expected:** Return to Main Menu without saving changes

---

## Module 6: Credit Card List (COCRDLIC)

### TC-CLST-001: Display card list
- **Precondition:** Logged in, on Credit Card List screen
- **Steps:** Press Enter (no filter)
- **Expected:** List of credit cards displayed with card number, account ID, status

### TC-CLST-002: Filter by account ID
- **Steps:** Enter account ID → Press Enter
- **Expected:** Only cards for that account are displayed

### TC-CLST-003: Page forward in card list
- **Steps:** Display card list → Press PF8/Next Page
- **Expected:** Next page of cards displayed (if more records exist)

### TC-CLST-004: Page backward in card list
- **Steps:** Navigate to page 2+ → Press PF7/Previous Page
- **Expected:** Previous page of cards displayed

### TC-CLST-005: Page backward at first page
- **Steps:** On page 1 → Press PF7
- **Expected:** Message "You are already at the top of the page..."

### TC-CLST-006: Select card for view
- **Steps:** Enter selection marker next to a card → Press Enter
- **Expected:** Navigation to Credit Card View screen with selected card details

### TC-CLST-007: Select card for update
- **Steps:** Enter update marker next to a card → Press Enter
- **Expected:** Navigation to Credit Card Update screen

---

## Module 7: Credit Card View (COCRDSLC)

### TC-CVEW-001: View card details
- **Precondition:** Card selected from Card List or card number entered
- **Expected:** Card number, account ID, customer ID, card status, expiry date displayed

### TC-CVEW-002: View card with invalid number
- **Steps:** Enter non-existent card number → Press Enter
- **Expected:** Error message indicating card not found

### TC-CVEW-003: Return to Card List via F3
- **Steps:** Press F3
- **Expected:** Return to Credit Card List screen

---

## Module 8: Credit Card Update (COCRDUPC)

### TC-CUPD-001: Display card for update
- **Steps:** Enter valid card number → Press Enter
- **Expected:** Card details displayed in editable fields

### TC-CUPD-002: Update card status
- **Steps:** Display card → Change status field → Confirm (Y) → Press Enter
- **Expected:** Success message. Card status updated.

### TC-CUPD-003: Cancel card update
- **Steps:** Display card → Change fields → Confirm (N) → Press Enter
- **Expected:** Changes not saved

### TC-CUPD-004: Update with invalid confirm value
- **Steps:** Enter "X" in confirm → Press Enter
- **Expected:** Error "Invalid value. Valid values are (Y/N)..."

### TC-CUPD-005: Update non-existent card
- **Steps:** Enter non-existent card number → Press Enter
- **Expected:** Error message indicating card not found

---

## Module 9: Transaction List (COTRN00C)

### TC-TLST-001: Display transaction list
- **Precondition:** Logged in, on Transaction List screen
- **Steps:** Press Enter (no filter)
- **Expected:** List of transactions with ID, card number, type, amount, date

### TC-TLST-002: Filter by account ID
- **Steps:** Enter account ID → Press Enter
- **Expected:** Only transactions for that account

### TC-TLST-003: Filter by card number
- **Steps:** Enter card number → Press Enter
- **Expected:** Only transactions for that card

### TC-TLST-004: Page forward
- **Steps:** Press PF8/Next
- **Expected:** Next page of transactions

### TC-TLST-005: Page backward
- **Steps:** On page 2+ → Press PF7/Prev
- **Expected:** Previous page of transactions

### TC-TLST-006: Select transaction for view
- **Steps:** Enter selection next to transaction → Press Enter
- **Expected:** Navigate to Transaction View

### TC-TLST-007: Select transaction for add
- **Steps:** Select add option
- **Expected:** Navigate to Transaction Add screen

---

## Module 10: Transaction View (COTRN01C)

### TC-TVEW-001: View transaction details
- **Precondition:** Transaction selected from list
- **Expected:** Transaction ID, card number, type code, category, source, description, amount, orig date, proc date, merchant info all displayed

### TC-TVEW-002: View non-existent transaction
- **Steps:** Enter non-existent transaction ID
- **Expected:** Error message

### TC-TVEW-003: Return to list via F3
- **Steps:** Press F3
- **Expected:** Return to Transaction List

---

## Module 11: Transaction Add (COTRN02C)

### TC-TADD-001: Add transaction with valid data via account ID
- **Steps:** Enter Account ID "00000000011" → Enter all required fields (Type CD, Category CD, Source, Description, Amount "-00000100.00", Orig Date "2026-03-25", Proc Date "2026-03-25", Merchant ID, Name, City, Zip) → Confirm (Y)
- **Expected:** Transaction added successfully. New transaction ID generated.

### TC-TADD-002: Add transaction with valid data via card number
- **Steps:** Enter Card Number "4000000000000001" → Enter all required fields → Confirm (Y)
- **Expected:** Transaction added successfully. Account ID auto-populated from cross-reference.

### TC-TADD-003: Reject add when confirm is N
- **Steps:** Fill all fields → Enter "N" in confirm → Press Enter
- **Expected:** Message "Confirm to add this transaction..."

### TC-TADD-004: Invalid confirm value
- **Steps:** Fill all fields → Enter "X" in confirm
- **Expected:** Error "Invalid value. Valid values are (Y/N)..."

### TC-TADD-005: Empty Account ID and Card Number
- **Steps:** Leave both Account ID and Card Number empty → Press Enter
- **Expected:** Error "Account or Card Number must be entered..."

### TC-TADD-006: Non-numeric Account ID
- **Steps:** Enter "ABCDEFGHIJK" in Account ID → Press Enter
- **Expected:** Error "Account ID must be Numeric..."

### TC-TADD-007: Non-numeric Card Number
- **Steps:** Enter "ABCDEFGHIJKLMNOP" in Card Number → Press Enter
- **Expected:** Error "Card Number must be Numeric..."

### TC-TADD-008: Empty Type CD
- **Steps:** Enter valid Account ID → Leave Type CD empty → Press Enter
- **Expected:** Error "Type CD can NOT be empty..."

### TC-TADD-009: Non-numeric Type CD
- **Steps:** Enter "AB" in Type CD → Press Enter
- **Expected:** Error "Type CD must be Numeric..."

### TC-TADD-010: Empty Category CD
- **Steps:** Leave Category CD empty
- **Expected:** Error "Category CD can NOT be empty..."

### TC-TADD-011: Non-numeric Category CD
- **Steps:** Enter "XY" in Category CD
- **Expected:** Error "Category CD must be Numeric..."

### TC-TADD-012: Empty Source field
- **Steps:** Leave Source empty
- **Expected:** Error "Source can NOT be empty..."

### TC-TADD-013: Empty Description
- **Steps:** Leave Description empty
- **Expected:** Error "Description can NOT be empty..."

### TC-TADD-014: Empty Amount
- **Steps:** Leave Amount empty
- **Expected:** Error "Amount can NOT be empty..."

### TC-TADD-015: Invalid Amount format
- **Steps:** Enter "ABCDE" in Amount
- **Expected:** Error "Amount should be in format -99999999.99"

### TC-TADD-016: Empty Origination Date
- **Steps:** Leave Orig Date empty
- **Expected:** Error "Orig Date can NOT be empty..."

### TC-TADD-017: Invalid Origination Date format
- **Steps:** Enter "03/25/2026" (wrong format)
- **Expected:** Error "Orig Date should be in format YYYY-MM-DD"

### TC-TADD-018: Empty Processing Date
- **Steps:** Leave Proc Date empty
- **Expected:** Error "Proc Date can NOT be empty..."

### TC-TADD-019: Invalid Processing Date format
- **Steps:** Enter "03-25-26" (wrong format)
- **Expected:** Error "Proc Date should be in format YYYY-MM-DD"

### TC-TADD-020: Empty Merchant ID
- **Steps:** Leave Merchant ID empty
- **Expected:** Error "Merchant ID can NOT be empty..."

### TC-TADD-021: Empty Merchant Name
- **Steps:** Leave Merchant Name empty
- **Expected:** Error "Merchant Name can NOT be empty..."

### TC-TADD-022: Empty Merchant City
- **Steps:** Leave Merchant City empty
- **Expected:** Error "Merchant City can NOT be empty..."

### TC-TADD-023: Empty Merchant Zip
- **Steps:** Leave Merchant Zip empty
- **Expected:** Error "Merchant Zip can NOT be empty..."

### TC-TADD-024: Clear screen via F4
- **Steps:** Enter some data → Press F4
- **Expected:** All fields cleared

### TC-TADD-025: Copy last transaction data via F5
- **Steps:** Press F5
- **Expected:** Fields populated with data from last added transaction

### TC-TADD-026: Return to menu via F3
- **Steps:** Press F3
- **Expected:** Return to previous screen (Main Menu or Transaction List)

---

## Module 12: Transaction Reports (CORPT00C)

### TC-REPT-001: Generate report with default parameters
- **Steps:** Navigate to Transaction Reports → Press Enter
- **Expected:** Report generation initiated or report screen displayed

### TC-REPT-002: Generate report with date range
- **Steps:** Enter start date and end date → Press Enter
- **Expected:** Report filtered to specified date range

### TC-REPT-003: Return to menu via F3
- **Steps:** Press F3
- **Expected:** Return to Main Menu

---

## Module 13: Bill Payment (COBIL00C)

### TC-BILL-001: Display account balance for payment
- **Steps:** Navigate to Bill Payment → Enter Account ID "00000000011" → Press Enter
- **Expected:** Current balance displayed. Confirm prompt shown.

### TC-BILL-002: Confirm and process bill payment
- **Steps:** Enter Account ID → View balance → Enter "Y" to confirm → Press Enter
- **Expected:** Payment processed. Transaction created with type "02", description "BILL PAYMENT - ONLINE". Account balance reduced to zero.

### TC-BILL-003: Decline bill payment
- **Steps:** Enter Account ID → Enter "N" to confirm → Press Enter
- **Expected:** Payment cancelled. Screen cleared. No transaction created.

### TC-BILL-004: Invalid confirm value
- **Steps:** Enter "X" in confirm field
- **Expected:** Error "Invalid value. Valid values are (Y/N)..."

### TC-BILL-005: Empty Account ID
- **Steps:** Leave Account ID empty → Press Enter
- **Expected:** Error "Acct ID can NOT be empty..."

### TC-BILL-006: Non-existent Account ID
- **Steps:** Enter "99999999999" → Press Enter
- **Expected:** Error "Account ID NOT found..."

### TC-BILL-007: Zero balance account
- **Steps:** Enter account with zero balance → Press Enter
- **Expected:** Error "You have nothing to pay..."

### TC-BILL-008: Clear screen via F4
- **Steps:** Press F4
- **Expected:** All fields cleared

### TC-BILL-009: Return to menu via F3
- **Steps:** Press F3
- **Expected:** Return to Main Menu

---

## Module 14: User List (COUSR00C) — Admin Only

### TC-ULST-001: Display user list
- **Precondition:** Logged in as ADMIN001, on User List screen
- **Expected:** List of users displayed (up to 10 per page) with User ID, First Name, Last Name, User Type

### TC-ULST-002: Page forward
- **Steps:** Press PF8/Next
- **Expected:** Next page of users (if more exist)

### TC-ULST-003: Page backward
- **Steps:** On page 2+ → Press PF7/Prev
- **Expected:** Previous page of users

### TC-ULST-004: Page backward at first page
- **Steps:** On page 1 → Press PF7
- **Expected:** Message "You are already at the top of the page..."

### TC-ULST-005: Page forward at last page
- **Steps:** On last page → Press PF8
- **Expected:** Message "You are already at the bottom of the page..."

### TC-ULST-006: Select user for update (U)
- **Steps:** Enter "U" next to a user → Press Enter
- **Expected:** Navigate to User Update (COUSR02C) with selected user

### TC-ULST-007: Select user for delete (D)
- **Steps:** Enter "D" next to a user → Press Enter
- **Expected:** Navigate to User Delete (COUSR03C) with selected user

### TC-ULST-008: Invalid selection
- **Steps:** Enter "X" next to a user → Press Enter
- **Expected:** Error "Invalid selection. Valid values are U and D"

### TC-ULST-009: Filter by User ID
- **Steps:** Enter user ID prefix in filter field → Press Enter
- **Expected:** List filtered to users matching the prefix

### TC-ULST-010: Return to Admin Menu via F3
- **Steps:** Press F3
- **Expected:** Return to Admin Menu

---

## Module 15: User Add (COUSR01C) — Admin Only

### TC-UADD-001: Add new user with valid data
- **Precondition:** Logged in as ADMIN001, on User Add screen
- **Steps:** Enter First Name "TEST", Last Name "USER", User ID "TESTUS01", Password "PASS1234", User Type "U" → Press Enter
- **Expected:** Success message "User TESTUS01 has been added ..." All fields cleared.

### TC-UADD-002: Add user with duplicate User ID
- **Steps:** Enter existing user ID "ADMIN001" with other details → Press Enter
- **Expected:** Error "User ID already exist..."

### TC-UADD-003: Empty First Name
- **Steps:** Leave First Name empty → Press Enter
- **Expected:** Error "First Name can NOT be empty..."

### TC-UADD-004: Empty Last Name
- **Steps:** Enter First Name → Leave Last Name empty → Press Enter
- **Expected:** Error "Last Name can NOT be empty..."

### TC-UADD-005: Empty User ID
- **Steps:** Enter First/Last Name → Leave User ID empty → Press Enter
- **Expected:** Error "User ID can NOT be empty..."

### TC-UADD-006: Empty Password
- **Steps:** Enter First/Last/UserID → Leave Password empty → Press Enter
- **Expected:** Error "Password can NOT be empty..."

### TC-UADD-007: Empty User Type
- **Steps:** Enter all fields except User Type → Press Enter
- **Expected:** Error "User Type can NOT be empty..."

### TC-UADD-008: Add admin user
- **Steps:** Enter all fields with User Type "A" → Press Enter
- **Expected:** Admin user created successfully

### TC-UADD-009: Clear screen via F4
- **Steps:** Enter some data → Press F4
- **Expected:** All fields cleared

### TC-UADD-010: Return to Admin Menu via F3
- **Steps:** Press F3
- **Expected:** Return to Admin Menu

---

## Module 16: User Update (COUSR02C) — Admin Only

### TC-UUPD-001: Display user for update
- **Precondition:** User selected from User List (selection "U")
- **Expected:** User details (First Name, Last Name, Password, User Type) displayed in editable fields

### TC-UUPD-002: Update user first name
- **Steps:** Modify First Name → Press Enter
- **Expected:** Success message. First name updated.

### TC-UUPD-003: Update user password
- **Steps:** Modify Password → Press Enter
- **Expected:** Success message. Password updated.

### TC-UUPD-004: Update user type
- **Steps:** Change User Type from "U" to "A" → Press Enter
- **Expected:** Success message. User promoted to admin.

### TC-UUPD-005: Return via F3
- **Steps:** Press F3
- **Expected:** Return to Admin Menu

---

## Module 17: User Delete (COUSR03C) — Admin Only

### TC-UDEL-001: Display user for deletion
- **Precondition:** User selected from User List (selection "D")
- **Expected:** User details displayed in read-only mode. Delete confirmation prompt.

### TC-UDEL-002: Confirm user deletion
- **Steps:** Enter "Y" to confirm → Press Enter
- **Expected:** Success message. User removed from USRSEC. Return to User List.

### TC-UDEL-003: Cancel user deletion
- **Steps:** Enter "N" to confirm → Press Enter
- **Expected:** Deletion cancelled. Return to User List. User still exists.

### TC-UDEL-004: Invalid confirm value
- **Steps:** Enter "X" in confirm
- **Expected:** Error "Invalid value. Valid values are (Y/N)..."

### TC-UDEL-005: Return via F3
- **Steps:** Press F3
- **Expected:** Return to Admin Menu

---

## Module 18: Batch — Transaction Posting (CBTRN02C)

### TC-BPST-001: Verify posted transactions appear in TRANSACT
- **Precondition:** Daily transactions exist in DALYTRAN
- **Steps:** Run POSTTRAN job → Query TRANSACT file
- **Expected:** All valid daily transactions written to TRANSACT. Rejected records in DALYREJS.

### TC-BPST-002: Verify account balance updated after posting
- **Steps:** Note account balance → Run POSTTRAN → Check account balance
- **Expected:** Account balance reflects posted transactions

### TC-BPST-003: Verify category balance updated
- **Steps:** Run POSTTRAN → Check TCATBALF
- **Expected:** Transaction category balances updated correctly

### TC-BPST-004: Verify rejected transactions
- **Steps:** Insert invalid transaction in DALYTRAN → Run POSTTRAN → Check DALYREJS
- **Expected:** Invalid transaction appears in reject file

---

## Module 19: Batch — Interest Calculation (CBACT04C)

### TC-BINT-001: Verify interest calculated for active accounts
- **Steps:** Run INTCALC job → Check TRANSACT for interest transactions
- **Expected:** Interest transactions created for accounts with balances

### TC-BINT-002: Verify interest rate from disclosure group
- **Steps:** Check disclosure group rate → Run INTCALC → Verify calculated amount
- **Expected:** Interest = balance × rate (matching disclosure group configuration)

### TC-BINT-003: Verify no interest for zero-balance accounts
- **Steps:** Ensure account has zero balance → Run INTCALC
- **Expected:** No interest transaction generated for zero-balance account

---

## Module 20: Batch — Statement Generation (CBSTM03A/B)

### TC-BSTM-001: Verify statement generated for active accounts
- **Steps:** Run CREASTMT job → Check output files
- **Expected:** Statement files created with transaction details, totals

### TC-BSTM-002: Verify HTML statement output
- **Steps:** Run CREASTMT → Check HTML output
- **Expected:** Valid HTML file generated with formatted statement

### TC-BSTM-003: Verify statement totals match transaction sum
- **Steps:** Sum transactions for account → Compare to statement total
- **Expected:** Statement total equals sum of all transactions in period

---

## Module 21: Batch — Transaction Report (CBTRN03C)

### TC-BRPT-001: Verify report generation
- **Steps:** Run TRANREPT job → Check report output
- **Expected:** Report file created with transaction details

### TC-BRPT-002: Verify report date range filter
- **Steps:** Run TRANREPT with date parameters → Check report
- **Expected:** Only transactions within date range appear in report

### TC-BRPT-003: Verify report totals
- **Steps:** Check page totals, account totals, grand total
- **Expected:** All running totals are mathematically correct

---

## Module 22: Batch — Data Export/Import (CBEXPORT/CBIMPORT)

### TC-BEXP-001: Verify data export
- **Steps:** Run export job → Check output file
- **Expected:** All records exported in correct format

### TC-BIMP-001: Verify data import
- **Steps:** Run import job with valid data → Check VSAM files
- **Expected:** Records imported correctly into target datasets

---

## Cross-Module Integration Tests

### TC-INT-001: End-to-end transaction lifecycle
- **Steps:** Login → Add Transaction → View in Transaction List → View Transaction Detail → Run POSTTRAN batch → Verify in Account View balance changed
- **Expected:** Transaction flows from online entry through batch posting to updated account balance

### TC-INT-002: End-to-end bill payment
- **Steps:** Login → Navigate to Bill Payment → Pay balance → View Account → Verify balance is zero → Check new transaction in Transaction List
- **Expected:** Bill payment creates transaction and zeroes account balance

### TC-INT-003: Admin user CRUD lifecycle
- **Steps:** Login as Admin → Add User → Verify in User List → Update User → Verify changes → Delete User → Verify removed from list
- **Expected:** Full user lifecycle works correctly

### TC-INT-004: Card-Account cross-reference integrity
- **Steps:** View account → Note cards linked → View each card → Verify account ID matches
- **Expected:** Cross-reference data is consistent between accounts and cards

### TC-INT-005: Session state preservation across screens
- **Steps:** Login → Navigate Account View → Enter account → F3 to menu → Navigate Card List → F3 to menu → Navigate Account View again
- **Expected:** Each navigation preserves/restores correct state

---

## Summary

| Module | Program | Test Cases | Priority |
|---|---|---|---|
| Authentication | COSGN00C | TC-AUTH-001 to TC-AUTH-008 (8) | Critical |
| Main Menu | COMEN01C | TC-MENU-001 to TC-MENU-007 (7) | High |
| Admin Menu | COADM01C | TC-ADMN-001 to TC-ADMN-007 (7) | High |
| Account View | COACTVWC | TC-AVEW-001 to TC-AVEW-006 (6) | High |
| Account Update | COACTUPC | TC-AUPD-001 to TC-AUPD-006 (6) | Critical |
| Card List | COCRDLIC | TC-CLST-001 to TC-CLST-007 (7) | High |
| Card View | COCRDSLC | TC-CVEW-001 to TC-CVEW-003 (3) | Medium |
| Card Update | COCRDUPC | TC-CUPD-001 to TC-CUPD-005 (5) | Critical |
| Transaction List | COTRN00C | TC-TLST-001 to TC-TLST-007 (7) | High |
| Transaction View | COTRN01C | TC-TVEW-001 to TC-TVEW-003 (3) | Medium |
| Transaction Add | COTRN02C | TC-TADD-001 to TC-TADD-026 (26) | Critical |
| Reports | CORPT00C | TC-REPT-001 to TC-REPT-003 (3) | Medium |
| Bill Payment | COBIL00C | TC-BILL-001 to TC-BILL-009 (9) | Critical |
| User List | COUSR00C | TC-ULST-001 to TC-ULST-010 (10) | High |
| User Add | COUSR01C | TC-UADD-001 to TC-UADD-010 (10) | High |
| User Update | COUSR02C | TC-UUPD-001 to TC-UUPD-005 (5) | High |
| User Delete | COUSR03C | TC-UDEL-001 to TC-UDEL-005 (5) | High |
| Batch Posting | CBTRN02C | TC-BPST-001 to TC-BPST-004 (4) | Critical |
| Batch Interest | CBACT04C | TC-BINT-001 to TC-BINT-003 (3) | Critical |
| Batch Statement | CBSTM03A/B | TC-BSTM-001 to TC-BSTM-003 (3) | High |
| Batch Report | CBTRN03C | TC-BRPT-001 to TC-BRPT-003 (3) | High |
| Batch Export/Import | CBEXPORT/CBIMPORT | TC-BEXP/BIMP-001 (2) | Medium |
| Integration | Cross-module | TC-INT-001 to TC-INT-005 (5) | Critical |

**Total: 151 test cases**
