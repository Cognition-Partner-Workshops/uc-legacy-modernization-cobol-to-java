# CardDemo QA Test Cases

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Platform:** IBM z/OS | COBOL / CICS / VSAM / JCL
> **Test Scope:** Functional, Integration, Boundary, Negative, Regression

---

## Table of Contents

1. [Authentication & Sign-On (COSGN00C)](#1-authentication--sign-on-cosgn00c)
2. [Main Menu Navigation (COMEN01C)](#2-main-menu-navigation-comen01c)
3. [Account View (COACTVWC)](#3-account-view-coactvwc)
4. [Account Update (COACTUPC)](#4-account-update-coactupc)
5. [Card List (COCRDLIC)](#5-card-list-cocrdlic)
6. [Card View (COCRDSLC)](#6-card-view-cocrdslc)
7. [Card Update (COCRDUPC)](#7-card-update-cocrdupc)
8. [Transaction List (COTRN00C)](#8-transaction-list-cotrn00c)
9. [Transaction View (COTRN01C)](#9-transaction-view-cotrn01c)
10. [Transaction Add (COTRN02C)](#10-transaction-add-cotrn02c)
11. [Bill Payment (COBIL00C)](#11-bill-payment-cobil00c)
12. [Transaction Reports (CORPT00C)](#12-transaction-reports-corpt00c)
13. [Admin Menu (COADM01C)](#13-admin-menu-coadm01c)
14. [User Management — List (COUSR00C)](#14-user-management--list-cousr00c)
15. [User Management — Add (COUSR01C)](#15-user-management--add-cousr01c)
16. [User Management — Update (COUSR02C)](#16-user-management--update-cousr02c)
17. [User Management — Delete (COUSR03C)](#17-user-management--delete-cousr03c)
18. [Batch: Transaction Posting (CBTRN02C)](#18-batch-transaction-posting-cbtrn02c)
19. [Batch: Interest Calculation (CBACT04C)](#19-batch-interest-calculation-cbact04c)
20. [Batch: Statement Generation (CBSTM03A/B)](#20-batch-statement-generation-cbstm03ab)
21. [Batch: Transaction Report (CBTRN03C)](#21-batch-transaction-report-cbtrn03c)
22. [Batch: Data Export/Import (CBEXPORT/CBIMPORT)](#22-batch-data-exportimport-cbexportcbimport)
23. [Batch Job Cycle (JCL Integration)](#23-batch-job-cycle-jcl-integration)
24. [Cross-Cutting & Non-Functional](#24-cross-cutting--non-functional)

---

## 1. Authentication & Sign-On (COSGN00C)

**Program:** COSGN00C | **Transaction:** CC00 | **Screen:** COSGN00 | **VSAM:** USRSEC

### Functional Tests

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| AUTH-001 | Valid admin login | Admin user ADMIN001 exists in USRSEC | 1. Enter CC00 transaction<br>2. Enter User ID: ADMIN001<br>3. Enter Password: PASSWORD<br>4. Press Enter | Navigate to Admin Menu (COADM01C). COMMAREA has user type 'A'. | Critical |
| AUTH-002 | Valid regular user login | User USER0001 exists in USRSEC | 1. Enter CC00 transaction<br>2. Enter User ID: USER0001<br>3. Enter Password: PASSWORD<br>4. Press Enter | Navigate to Main Menu (COMEN01C). COMMAREA has user type 'R'. | Critical |
| AUTH-003 | Invalid password | User USER0001 exists | 1. Enter User ID: USER0001<br>2. Enter Password: WRONGPWD<br>3. Press Enter | Error message: "Invalid credentials". Remain on sign-on screen. | Critical |
| AUTH-004 | Non-existent user ID | User XXXXXXXX does not exist | 1. Enter User ID: XXXXXXXX<br>2. Enter Password: anything<br>3. Press Enter | Error message: "User ID not found". Remain on sign-on screen. | Critical |
| AUTH-005 | Blank user ID | -- | 1. Leave User ID blank<br>2. Press Enter | Error message prompting for User ID. | High |
| AUTH-006 | Blank password | -- | 1. Enter valid User ID<br>2. Leave Password blank<br>3. Press Enter | Error message prompting for Password. | High |
| AUTH-007 | Case sensitivity | User ADMIN001 exists | 1. Enter User ID: admin001 (lowercase)<br>2. Enter Password: PASSWORD<br>3. Press Enter | Verify behavior — document whether login is case-sensitive. | Medium |
| AUTH-008 | Special characters in password | -- | 1. Enter User ID: USER0001<br>2. Enter Password with special chars: P@SS!<br>3. Press Enter | Error message or appropriate handling. No system abend. | Medium |
| AUTH-009 | Maximum-length fields | -- | 1. Enter 8-char User ID (max)<br>2. Enter 8-char Password (max)<br>3. Press Enter | Fields accept full length without truncation. | Medium |
| AUTH-010 | PF3 from sign-on screen | -- | 1. Press PF3 on sign-on screen | CICS transaction terminates cleanly. | Low |

---

## 2. Main Menu Navigation (COMEN01C)

**Program:** COMEN01C | **Transaction:** CM00 | **Screen:** COMEN01

### Functional Tests

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| MENU-001 | Display main menu | Logged in as regular user | 1. Successful login as USER0001 | Main menu displays with all options. User ID shown in header. | Critical |
| MENU-002 | Navigate to Account View | On main menu | 1. Select Account View option<br>2. Press Enter | Transfer to COACTVWC. Screen shows account view. | Critical |
| MENU-003 | Navigate to Card List | On main menu | 1. Select Card List option<br>2. Press Enter | Transfer to COCRDLIC. Screen shows card list. | Critical |
| MENU-004 | Navigate to Transaction List | On main menu | 1. Select Transaction List option<br>2. Press Enter | Transfer to COTRN00C. Screen shows transaction list. | Critical |
| MENU-005 | Navigate to Bill Payment | On main menu | 1. Select Bill Payment option<br>2. Press Enter | Transfer to COBIL00C. Screen shows bill payment form. | Critical |
| MENU-006 | Navigate to Reports | On main menu | 1. Select Reports option<br>2. Press Enter | Transfer to CORPT00C. Screen shows report selection. | High |
| MENU-007 | Invalid menu option | On main menu | 1. Enter invalid option number<br>2. Press Enter | Error message: "Invalid option". Remain on menu. | High |
| MENU-008 | PF3 return to sign-on | On main menu | 1. Press PF3 | Return to sign-on screen (COSGN00C). | High |
| MENU-009 | Regular user cannot see admin options | Logged in as USER0001 | 1. View main menu | Admin-only options are not visible or are disabled. | Critical |
| MENU-010 | Header displays correct user info | Logged in as USER0001 | 1. View main menu header | Displays application title, user ID, date, time. | Medium |

---

## 3. Account View (COACTVWC)

**Program:** COACTVWC | **Transaction:** CA00 | **Screen:** COACTVW | **VSAM:** ACCTDATA, CUSTDATA, CARDXREF

### Functional Tests

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| ACVW-001 | View existing account | Account 00000000001 exists | 1. Navigate to Account View<br>2. Enter Account ID: 00000000001<br>3. Press Enter | Display account details: status, balance, credit limit, open date, customer info. | Critical |
| ACVW-002 | View non-existent account | Account 99999999999 does not exist | 1. Enter Account ID: 99999999999<br>2. Press Enter | Error message: "Account not found". | Critical |
| ACVW-003 | Blank account ID | -- | 1. Leave Account ID blank<br>2. Press Enter | Error message prompting for Account ID. | High |
| ACVW-004 | Non-numeric account ID | -- | 1. Enter Account ID: ABCDEFGHIJK<br>2. Press Enter | Error message: "Invalid Account ID format". | High |
| ACVW-005 | Display associated customer data | Account with linked customer exists | 1. View valid account | Customer name, address, phone, SSN (masked) displayed correctly. | Critical |
| ACVW-006 | Display associated cards | Account has cards via CARDXREF | 1. View valid account | Associated card numbers displayed (verify cross-reference lookup). | High |
| ACVW-007 | Balance display formatting | Account has balance $1,234.56 | 1. View account | Balance shows as $1,234.56 with proper sign and formatting. | High |
| ACVW-008 | Negative balance display | Account has negative balance | 1. View account with credit balance | Negative amount displays correctly with sign indicator. | Medium |
| ACVW-009 | Navigate to Account Update | Viewing valid account | 1. Select update option<br>2. Press Enter | Transfer to COACTUPC with account data pre-populated. | Critical |
| ACVW-010 | PF3 return to menu | On account view | 1. Press PF3 | Return to main menu (COMEN01C). | High |

---

## 4. Account Update (COACTUPC)

**Program:** COACTUPC | **Transaction:** CA01 | **Screen:** COACTUP | **VSAM:** ACCTDATA, CUSTDATA, CARDXREF

### Functional Tests

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| ACUP-001 | Update account status | Active account exists | 1. Navigate to account update<br>2. Change status from 'Y' to 'N'<br>3. Press Enter to confirm | Account status updated to 'N'. Confirmation message displayed. VSAM record updated. | Critical |
| ACUP-002 | Update credit limit | Account exists | 1. Change credit limit to 50000.00<br>2. Press Enter | Credit limit updated. Balance validation passes. | Critical |
| ACUP-003 | Update customer name | Account with customer exists | 1. Change first name to 'JANE'<br>2. Press Enter | Customer name updated in CUSTDATA VSAM. | Critical |
| ACUP-004 | Update customer address | Account with customer exists | 1. Change address line 1<br>2. Change city, state, ZIP<br>3. Press Enter | All address fields updated correctly. | High |
| ACUP-005 | Update customer phone | Account with customer exists | 1. Change phone to (555)123-4567<br>2. Press Enter | Phone number updated. Format validated as (NNN)NNN-NNNN. | High |
| ACUP-006 | Invalid phone format | -- | 1. Enter phone: 5551234567 (no formatting)<br>2. Press Enter | Error message about invalid phone format. | High |
| ACUP-007 | Invalid SSN - starts with 000 | -- | 1. Enter SSN starting with 000<br>2. Press Enter | Error: "Invalid SSN". SSN part 1 cannot be 000. | High |
| ACUP-008 | Invalid SSN - starts with 666 | -- | 1. Enter SSN starting with 666<br>2. Press Enter | Error: "Invalid SSN". SSN part 1 cannot be 666. | High |
| ACUP-009 | Invalid SSN - starts with 900-999 | -- | 1. Enter SSN starting with 950<br>2. Press Enter | Error: "Invalid SSN". SSN part 1 cannot be 900-999. | High |
| ACUP-010 | Invalid date of birth | -- | 1. Enter DOB: 2099-13-32 (invalid month/day)<br>2. Press Enter | Error: "Invalid date of birth". | High |
| ACUP-011 | Future date of birth | -- | 1. Enter DOB in the future<br>2. Press Enter | Error: "Date of birth cannot be in the future". | Medium |
| ACUP-012 | Invalid FICO score | -- | 1. Enter FICO: 999 (out of range)<br>2. Press Enter | Error: "Invalid FICO score" (valid range 300-850). | High |
| ACUP-013 | FICO score boundary - lower | -- | 1. Enter FICO: 300 (minimum valid)<br>2. Press Enter | Accepted. FICO score updated to 300. | Medium |
| ACUP-014 | FICO score boundary - upper | -- | 1. Enter FICO: 850 (maximum valid)<br>2. Press Enter | Accepted. FICO score updated to 850. | Medium |
| ACUP-015 | Credit limit less than cash limit | -- | 1. Set credit limit: 5000<br>2. Set cash credit limit: 10000<br>3. Press Enter | Error: "Cash credit limit cannot exceed credit limit". | High |
| ACUP-016 | No changes made | Account loaded | 1. Press Enter without changes | Message: "No changes detected" or return without update. | Medium |
| ACUP-017 | Update expiration date | -- | 1. Change expiration date to valid future date<br>2. Press Enter | Expiration date updated. | High |
| ACUP-018 | Invalid expiration date format | -- | 1. Enter expiration: ABCD-EF-GH<br>2. Press Enter | Error: "Invalid date format". | High |
| ACUP-019 | Account status invalid value | -- | 1. Enter status: 'X' (not Y or N)<br>2. Press Enter | Error: "Invalid status. Must be Y or N". | High |
| ACUP-020 | Concurrent update (optimistic lock) | Two terminals viewing same account | 1. Terminal A loads account<br>2. Terminal B updates account<br>3. Terminal A tries to update | Verify behavior — should detect conflict or apply last-write-wins. | Medium |
| ACUP-021 | PF3 cancel without saving | Changes made but not submitted | 1. Make changes<br>2. Press PF3 | Return to account view without saving changes. | High |
| ACUP-022 | Update reissue date | -- | 1. Enter valid reissue date<br>2. Press Enter | Reissue date updated in account record. | Medium |
| ACUP-023 | Negative credit limit | -- | 1. Enter credit limit: -5000.00<br>2. Press Enter | Error: "Credit limit must be positive". | High |
| ACUP-024 | Zero credit limit | -- | 1. Enter credit limit: 0.00<br>2. Press Enter | Verify behavior — document whether zero is allowed. | Medium |
| ACUP-025 | State code validation | -- | 1. Enter state code: XX (invalid)<br>2. Press Enter | Error via CSLKPCDY lookup: "Invalid state code". | Medium |

---

## 5. Card List (COCRDLIC)

**Program:** COCRDLIC | **Transaction:** CC01 | **Screen:** COCRDLI | **VSAM:** CARDDATA, CARDXREF

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| CLST-001 | Display card list | Cards exist in CARDDATA | 1. Navigate to Card List | First page of cards displayed with card number, account, status. | Critical |
| CLST-002 | Page forward | Multiple pages of cards | 1. View card list<br>2. Press PF8 (Forward) | Next page of cards displayed. | Critical |
| CLST-003 | Page backward | On page 2+ of cards | 1. Navigate to page 2<br>2. Press PF7 (Backward) | Previous page of cards displayed. | Critical |
| CLST-004 | Select card for viewing | Cards displayed | 1. Enter 'S' next to a card<br>2. Press Enter | Transfer to COCRDSLC with selected card details. | Critical |
| CLST-005 | Select card for update | Cards displayed | 1. Enter 'U' next to a card<br>2. Press Enter | Transfer to COCRDUPC with selected card. | High |
| CLST-006 | No cards found | Empty CARDDATA file | 1. Navigate to Card List | Message: "No cards found". Empty list. | High |
| CLST-007 | Filter by account ID | Cards for specific account exist | 1. Enter account ID in filter<br>2. Press Enter | Only cards linked to that account displayed. | High |
| CLST-008 | Invalid selection code | -- | 1. Enter 'X' next to a card<br>2. Press Enter | Error: "Invalid selection". | Medium |
| CLST-009 | First page - no backward | On first page | 1. Press PF7 | Message: "Already on first page" or no action. | Medium |
| CLST-010 | Last page - no forward | On last page | 1. Press PF8 | Message: "No more records" or no action. | Medium |

---

## 6. Card View (COCRDSLC)

**Program:** COCRDSLC | **Transaction:** CC02 | **Screen:** COCRDSL | **VSAM:** CARDDATA, CARDXREF

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| CVEW-001 | View card details | Card exists | 1. Select card from list | Display: card number, account ID, embossed name, expiration, CVV, status. | Critical |
| CVEW-002 | View inactive card | Inactive card exists | 1. View inactive card | Status shows 'N'. All fields displayed read-only. | High |
| CVEW-003 | Navigate to card update | Viewing card | 1. Select update option | Transfer to COCRDUPC with card data. | Critical |
| CVEW-004 | PF3 return to card list | Viewing card | 1. Press PF3 | Return to COCRDLIC card list. | High |
| CVEW-005 | Card with no cross-reference | Card exists but no XREF | 1. View card | Handle gracefully — display card without customer/account link. | Medium |

---

## 7. Card Update (COCRDUPC)

**Program:** COCRDUPC | **Transaction:** CC03 | **Screen:** COCRDUP | **VSAM:** CARDDATA, CARDXREF

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| CUPD-001 | Activate card | Inactive card exists | 1. Change status from 'N' to 'Y'<br>2. Press Enter | Card activated. Status updated in CARDDATA. | Critical |
| CUPD-002 | Deactivate card | Active card exists | 1. Change status from 'Y' to 'N'<br>2. Press Enter | Card deactivated. Status updated in CARDDATA. | Critical |
| CUPD-003 | Update embossed name | Card exists | 1. Change embossed name<br>2. Press Enter | Name updated in CARDDATA. Max 50 chars. | High |
| CUPD-004 | Update expiration date | Card exists | 1. Change expiration to valid future date<br>2. Press Enter | Date updated. Validated as valid date. | High |
| CUPD-005 | Invalid expiration date | -- | 1. Enter expiration: 2020-13-45<br>2. Press Enter | Error: "Invalid expiration date". | High |
| CUPD-006 | Past expiration date | -- | 1. Enter expiration date in the past<br>2. Press Enter | Warning or error about past date. | Medium |
| CUPD-007 | Invalid status value | -- | 1. Enter status: 'X'<br>2. Press Enter | Error: "Invalid status. Must be Y or N". | High |
| CUPD-008 | No changes made | Card loaded | 1. Press Enter without changes | No update performed. Appropriate message. | Medium |
| CUPD-009 | PF3 cancel | Changes pending | 1. Press PF3 | Return without saving. | High |
| CUPD-010 | Embossed name with special chars | -- | 1. Enter name with special characters<br>2. Press Enter | Verify alphanumeric validation behavior. | Low |

---

## 8. Transaction List (COTRN00C)

**Program:** COTRN00C | **Transaction:** CT00 | **Screen:** COTRN00 | **VSAM:** TRANSACT, CARDXREF

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| TLST-001 | Display transaction list | Transactions exist | 1. Navigate to Transaction List | Paginated list of transactions with ID, type, amount, date. | Critical |
| TLST-002 | Page forward | Multiple pages | 1. Press PF8 | Next page displayed. | Critical |
| TLST-003 | Page backward | On page 2+ | 1. Press PF7 | Previous page displayed. | Critical |
| TLST-004 | Select transaction for view | Transactions displayed | 1. Select a transaction<br>2. Press Enter | Transfer to COTRN01C with transaction details. | Critical |
| TLST-005 | Select add transaction | On transaction list | 1. Select add option | Transfer to COTRN02C for new transaction entry. | High |
| TLST-006 | Filter by account/card | -- | 1. Enter filter criteria<br>2. Press Enter | Only matching transactions displayed. | High |
| TLST-007 | No transactions found | Empty TRANSACT file | 1. Navigate to Transaction List | Message: "No transactions found". | Medium |
| TLST-008 | PF3 return to menu | On transaction list | 1. Press PF3 | Return to main menu. | High |

---

## 9. Transaction View (COTRN01C)

**Program:** COTRN01C | **Transaction:** CT01 | **Screen:** COTRN01 | **VSAM:** TRANSACT

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| TVEW-001 | View transaction details | Transaction exists | 1. Select transaction from list | Display: ID, type, category, source, amount, merchant info, card, timestamps. | Critical |
| TVEW-002 | View purchase transaction | Purchase type exists | 1. View purchase transaction | Amount shows as positive. Type/category descriptions displayed. | High |
| TVEW-003 | View refund transaction | Refund type exists | 1. View refund transaction | Amount shows as negative/credit. | High |
| TVEW-004 | PF3 return to list | Viewing transaction | 1. Press PF3 | Return to COTRN00C transaction list. | High |
| TVEW-005 | Large amount display | Transaction with amount $999,999,999.99 | 1. View transaction | Amount displays correctly without overflow. | Medium |

---

## 10. Transaction Add (COTRN02C)

**Program:** COTRN02C (online) | **Transaction:** CT02 | **Screen:** COTRN02 | **VSAM:** TRANSACT, CARDXREF, ACCTDATA, TCATBAL

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| TADD-001 | Add valid transaction | Valid card and account exist | 1. Enter card number<br>2. Enter type code, category<br>3. Enter amount: 100.00<br>4. Enter merchant info<br>5. Press Enter | Transaction created in TRANSACT. Account balance updated. Category balance updated. | Critical |
| TADD-002 | Invalid card number | Card does not exist in XREF | 1. Enter non-existent card number<br>2. Press Enter | Error: "Card number not found". Transaction not created. | Critical |
| TADD-003 | Invalid transaction type | -- | 1. Enter invalid type code: ZZ<br>2. Press Enter | Error: "Invalid transaction type". | High |
| TADD-004 | Zero amount | -- | 1. Enter amount: 0.00<br>2. Press Enter | Error: "Amount must be non-zero". | High |
| TADD-005 | Negative amount (credit/refund) | -- | 1. Enter amount: -50.00<br>2. Press Enter | Accepted as credit/refund if type supports it. | High |
| TADD-006 | Amount exceeding credit limit | Account credit limit: $5,000 | 1. Add transaction for $6,000<br>2. Press Enter | Verify behavior — may reject or allow with warning. | High |
| TADD-007 | Blank required fields | -- | 1. Leave card number blank<br>2. Press Enter | Error message for each required field. | High |
| TADD-008 | Maximum amount boundary | -- | 1. Enter amount: 999999999.99 (max PIC S9(09)V99)<br>2. Press Enter | Amount accepted or boundary error. No numeric overflow. | Medium |
| TADD-009 | Merchant info optional fields | -- | 1. Fill required fields only<br>2. Leave merchant city, ZIP blank<br>3. Press Enter | Transaction created with blank optional fields. | Medium |
| TADD-010 | Verify timestamp generation | -- | 1. Add valid transaction | TRAN-ORIG-TS and TRAN-PROC-TS populated with current date/time in DB2 format. | Medium |
| TADD-011 | PF3 cancel without saving | Entered data | 1. Press PF3 | Return to transaction list without creating transaction. | High |
| TADD-012 | Duplicate transaction ID | Transaction with same ID exists | 1. Attempt to add with existing ID | Error or system generates unique ID. No duplicate. | High |

---

## 11. Bill Payment (COBIL00C)

**Program:** COBIL00C | **Transaction:** CB00 | **Screen:** COBIL00 | **VSAM:** ACCTDATA, CARDXREF, TRANSACT

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BILL-001 | Make valid payment | Account with balance > 0 | 1. Enter account/card number<br>2. Enter payment amount: 500.00<br>3. Press Enter | Payment transaction created. Account balance reduced by 500.00. | Critical |
| BILL-002 | Payment exceeds balance | Balance: $100, Payment: $200 | 1. Enter payment: 200.00<br>2. Press Enter | Verify behavior — may create credit balance or reject overpayment. | High |
| BILL-003 | Zero payment amount | -- | 1. Enter amount: 0.00<br>2. Press Enter | Error: "Payment amount must be greater than zero". | High |
| BILL-004 | Negative payment amount | -- | 1. Enter amount: -100.00<br>2. Press Enter | Error: "Invalid payment amount". | High |
| BILL-005 | Invalid account/card | Non-existent card | 1. Enter invalid card number<br>2. Press Enter | Error: "Card/Account not found". | Critical |
| BILL-006 | Payment to inactive account | Account status = 'N' | 1. Attempt payment<br>2. Press Enter | Error or warning about inactive account. | High |
| BILL-007 | Full balance payment | Balance: $1,500.00 | 1. Enter exact balance as payment<br>2. Press Enter | Balance becomes $0.00. Transaction recorded. | High |
| BILL-008 | Very small payment | -- | 1. Enter amount: 0.01<br>2. Press Enter | Accepted. Balance reduced by $0.01. | Medium |
| BILL-009 | PF3 cancel payment | Data entered | 1. Press PF3 | Return without processing payment. | High |
| BILL-010 | Verify cycle credit update | -- | 1. Make payment | ACCT-CURR-CYC-CREDIT incremented by payment amount. | High |

---

## 12. Transaction Reports (CORPT00C)

**Program:** CORPT00C | **Transaction:** CR00 | **Screen:** CORPT00

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| REPT-001 | Generate monthly report | Transactions exist for date range | 1. Enter start date<br>2. Enter end date<br>3. Select report type<br>4. Press Enter | Report parameters accepted. Report triggered. | Critical |
| REPT-002 | Invalid date range | -- | 1. Enter start date after end date<br>2. Press Enter | Error: "Start date must be before end date". | High |
| REPT-003 | No transactions in range | Empty date range | 1. Enter date range with no data<br>2. Press Enter | Message: "No transactions found for date range". | High |
| REPT-004 | Invalid date format | -- | 1. Enter date: 13/32/2025<br>2. Press Enter | Error: "Invalid date format". | High |
| REPT-005 | PF3 return to menu | On report screen | 1. Press PF3 | Return to main menu. | Medium |

---

## 13. Admin Menu (COADM01C)

**Program:** COADM01C | **Transaction:** CA90 | **Screen:** COADM01

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| ADMN-001 | Display admin menu | Logged in as admin | 1. Login as ADMIN001 | Admin menu displays with user management options. | Critical |
| ADMN-002 | Navigate to user list | On admin menu | 1. Select User List option | Transfer to COUSR00C. | Critical |
| ADMN-003 | Regular user cannot access | Logged in as USER0001 | 1. Attempt to access CA90 transaction | Access denied or redirect to regular menu. | Critical |
| ADMN-004 | Invalid menu option | On admin menu | 1. Enter invalid option<br>2. Press Enter | Error: "Invalid option". | Medium |
| ADMN-005 | PF3 return to sign-on | On admin menu | 1. Press PF3 | Return to sign-on screen. | High |

---

## 14. User Management — List (COUSR00C)

**Program:** COUSR00C | **Transaction:** CU00 | **Screen:** COUSR00 | **VSAM:** USRSEC

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| ULST-001 | Display user list | Users exist in USRSEC | 1. Navigate to User List | Paginated list showing user ID, name, type (A/R). | Critical |
| ULST-002 | Page forward/backward | Multiple pages | 1. PF8 forward, PF7 backward | Pagination works correctly. | High |
| ULST-003 | Select user for update | Users displayed | 1. Select user for update | Transfer to COUSR02C with user data. | Critical |
| ULST-004 | Select user for delete | Users displayed | 1. Select user for delete | Transfer to COUSR03C with user data. | Critical |
| ULST-005 | Select add new user | On user list | 1. Select add option | Transfer to COUSR01C. | Critical |
| ULST-006 | No users found | Empty USRSEC | 1. View user list | Message: "No users found". | Medium |

---

## 15. User Management — Add (COUSR01C)

**Program:** COUSR01C | **Transaction:** CU01 | **Screen:** COUSR01 | **VSAM:** USRSEC

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| UADD-001 | Add regular user | -- | 1. Enter User ID: NEWUSR01<br>2. Enter First Name, Last Name<br>3. Enter Password<br>4. Select Type: R<br>5. Press Enter | User created in USRSEC with type 'R'. | Critical |
| UADD-002 | Add admin user | -- | 1. Enter User ID: NEWADM01<br>2. Enter details, Type: A<br>3. Press Enter | User created with type 'A'. | Critical |
| UADD-003 | Duplicate user ID | NEWUSR01 already exists | 1. Enter existing User ID<br>2. Press Enter | Error: "User ID already exists". | Critical |
| UADD-004 | Blank user ID | -- | 1. Leave User ID blank<br>2. Press Enter | Error: "User ID is required". | High |
| UADD-005 | Blank password | -- | 1. Leave password blank<br>2. Press Enter | Error: "Password is required". | High |
| UADD-006 | Invalid user type | -- | 1. Enter Type: X (not R or A)<br>2. Press Enter | Error: "Invalid user type. Must be R or A". | High |
| UADD-007 | Max length User ID (8 chars) | -- | 1. Enter 8-character user ID<br>2. Press Enter | Accepted. Stored correctly. | Medium |
| UADD-008 | Max length password (8 chars) | -- | 1. Enter 8-character password<br>2. Press Enter | Accepted. Stored correctly. | Medium |
| UADD-009 | PF3 cancel | Data entered | 1. Press PF3 | Return to user list without creating user. | High |

---

## 16. User Management — Update (COUSR02C)

**Program:** COUSR02C | **Transaction:** CU02 | **Screen:** COUSR02 | **VSAM:** USRSEC

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| UUPD-001 | Update user name | User exists | 1. Change first name<br>2. Press Enter | Name updated in USRSEC. | Critical |
| UUPD-002 | Change password | User exists | 1. Change password field<br>2. Press Enter | Password updated. New password works on next login. | Critical |
| UUPD-003 | Change user type R→A | Regular user exists | 1. Change type from R to A<br>2. Press Enter | User type updated. User now sees admin menu on login. | High |
| UUPD-004 | Change user type A→R | Admin user exists | 1. Change type from A to R<br>2. Press Enter | User type updated. User now sees regular menu. | High |
| UUPD-005 | No changes made | User loaded | 1. Press Enter without changes | Message: "No changes detected". | Medium |
| UUPD-006 | Invalid type value | -- | 1. Change type to 'X'<br>2. Press Enter | Error: "Invalid user type". | High |
| UUPD-007 | PF3 cancel | Changes pending | 1. Press PF3 | Return without saving. | High |

---

## 17. User Management — Delete (COUSR03C)

**Program:** COUSR03C | **Transaction:** CU03 | **Screen:** COUSR03 | **VSAM:** USRSEC

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| UDEL-001 | Delete existing user | User TESTDEL1 exists | 1. Select user for delete<br>2. Confirm deletion<br>3. Press Enter | User removed from USRSEC. No longer appears in list. | Critical |
| UDEL-002 | Cancel deletion | User selected for delete | 1. Select user<br>2. Press PF3 (cancel) | User NOT deleted. Return to user list. | Critical |
| UDEL-003 | Delete last admin user | Only 1 admin user remaining | 1. Attempt to delete last admin<br>2. Press Enter | Verify behavior — should warn or prevent. | High |
| UDEL-004 | Verify user gone after delete | User was deleted | 1. Delete user<br>2. Return to user list | Deleted user no longer in list. Login with deleted user fails. | Critical |
| UDEL-005 | Delete non-existent user | User already deleted | 1. Attempt to delete already-deleted user | Error: "User not found" or graceful handling. | Medium |

---

## 18. Batch: Transaction Posting (CBTRN02C)

**JCL:** POSTTRAN | **Program:** CBTRN02C | **Files:** DALYTRAN(in), TRANSACT(out), XREFFILE, ACCTDATA, TCATBAL, DALYREJS

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BPST-001 | Post valid daily transactions | DALYTRAN with valid records | 1. Run POSTTRAN JCL | All records posted to TRANSACT. Account balances updated. Return code 0. | Critical |
| BPST-002 | Invalid card number in transaction | DALYTRAN has record with non-existent card | 1. Run POSTTRAN | Invalid record written to DALYREJS. Other records posted. Return code 4. | Critical |
| BPST-003 | Invalid account in cross-reference | XREF points to non-existent account | 1. Run POSTTRAN | Transaction rejected. Written to DALYREJS with reason code. | High |
| BPST-004 | Empty daily transaction file | DALYTRAN is empty | 1. Run POSTTRAN | Job completes with 0 transactions processed, 0 rejected. Return code 0. | High |
| BPST-005 | All transactions rejected | DALYTRAN has only invalid records | 1. Run POSTTRAN | All written to DALYREJS. Return code 4. Transaction count matches. | High |
| BPST-006 | Account balance update accuracy | Known starting balance | 1. Post transaction of $100.00<br>2. Verify account balance | Balance increased by $100.00 exactly (for debit). CURR-CYC-DEBIT updated. | Critical |
| BPST-007 | Category balance update | Known starting cat balance | 1. Post transaction<br>2. Check TCATBAL | Category balance updated for account/type/category combination. | High |
| BPST-008 | New category balance record | No existing TCATBAL for combo | 1. Post transaction with new type/cat combo | New TCATBAL record created. | High |
| BPST-009 | Duplicate transaction ID | TRANSACT already has same ID | 1. Run POSTTRAN with duplicate | Verify handling — reject or overwrite. | High |
| BPST-010 | Transaction count verification | Known number of input records | 1. Run POSTTRAN<br>2. Check displayed counts | "TRANSACTIONS PROCESSED" + "TRANSACTIONS REJECTED" = total input. | High |
| BPST-011 | Large volume test | 10,000+ daily transactions | 1. Run POSTTRAN | Completes within SLA. All records processed/rejected. No abends. | Medium |
| BPST-012 | VSAM file not available | ACCTDATA file closed | 1. Run POSTTRAN | Job abends gracefully with file status error message. | High |

---

## 19. Batch: Interest Calculation (CBACT04C)

**JCL:** INTCALC | **Program:** CBACT04C | **Files:** TCATBAL, XREFFILE, DISCGRP, ACCTDATA(I/O), TRANSACT(out)

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BINT-001 | Calculate interest on positive balance | Account with category balance > 0 | 1. Run INTCALC | Interest computed = balance × rate. Account balance updated. Interest transaction created. | Critical |
| BINT-002 | Zero balance - no interest | Account with category balance = 0 | 1. Run INTCALC | No interest applied. No new transaction. | High |
| BINT-003 | Multiple categories per account | Account with 3 different cat balances | 1. Run INTCALC | Interest calculated separately for each category. 3 interest transactions created. | Critical |
| BINT-004 | Disclosure group rate lookup | Account assigned to group 'PREMIUM' | 1. Run INTCALC | Rate looked up from DISCGRP for 'PREMIUM' group. Correct rate applied. | Critical |
| BINT-005 | Default rate when group not found | Account with unknown disclosure group | 1. Run INTCALC | Default interest rate applied. No abend. | High |
| BINT-006 | Fee calculation | Applicable fee conditions met | 1. Run INTCALC | Fees computed and applied. Fee transaction created. | High |
| BINT-007 | Interest precision (2 decimal places) | Balance: $1,000.00, Rate: 18.99% monthly | 1. Run INTCALC<br>2. Verify interest amount | Interest calculated with proper decimal precision. No rounding errors. | Critical |
| BINT-008 | Empty TCATBAL file | No category balances | 1. Run INTCALC | Job completes with 0 records processed. Return code 0. | Medium |
| BINT-009 | REWRITE account verification | Known account | 1. Run INTCALC<br>2. Read account record | Account balance reflects interest charges. Cycle debit updated. | Critical |
| BINT-010 | File not available | DISCGRP file missing | 1. Run INTCALC | Job abends with file status error. | High |

---

## 20. Batch: Statement Generation (CBSTM03A/B)

**JCL:** CREASTMT | **Programs:** CBSTM03A (main), CBSTM03B (subroutine) | **Files:** TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE, STMTFILE, HTMLFILE

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BSTM-001 | Generate text statement | Cards with transactions exist | 1. Run CREASTMT | Text statement file created with customer info, transactions, totals. | Critical |
| BSTM-002 | Generate HTML statement | Same as above | 1. Run CREASTMT | HTML statement file created with proper HTML tags, formatting. | Critical |
| BSTM-003 | Statement for multiple cards | Multiple cards in XREFFILE | 1. Run CREASTMT | Separate statement section per card. All cards processed. | Critical |
| BSTM-004 | Card with no transactions | Card exists but no transactions | 1. Run CREASTMT | Statement generated with zero activity. | High |
| BSTM-005 | Customer info on statement | Customer with full address | 1. Run CREASTMT<br>2. Review output | Customer name, address correctly shown in statement header. | High |
| BSTM-006 | Account totals accuracy | Known transactions | 1. Run CREASTMT<br>2. Check totals | Page totals, account totals, grand total are mathematically correct. | Critical |
| BSTM-007 | Page break handling | Many transactions (>50 per card) | 1. Run CREASTMT | Page breaks inserted. Page totals calculated per page. | High |
| BSTM-008 | Empty transaction file | No transactions | 1. Run CREASTMT | Job completes. Output files may be empty or contain headers only. | Medium |
| BSTM-009 | CBSTM03B subroutine calls | -- | 1. Run CREASTMT | CBSTM03B handles all file OPEN/READ/CLOSE without error. | High |
| BSTM-010 | SORT step correctness | TRANSACT VSAM has data | 1. Run CREASTMT<br>2. Check sorted output | Transactions sorted by card number then transaction ID. | High |

---

## 21. Batch: Transaction Report (CBTRN03C)

**JCL:** TRANREPT | **Program:** CBTRN03C | **Files:** TRANSACT, TRANTYPE, TRANCATG, DALYREPT(out)

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BRPT-001 | Generate daily report | Transactions exist | 1. Run TRANREPT | Report file created with transaction details, type/category descriptions, totals. | Critical |
| BRPT-002 | Report header correctness | -- | 1. Run TRANREPT<br>2. Check output | Header shows report name, date range, column headers. | High |
| BRPT-003 | Type description lookup | Transaction types in TRANTYPE | 1. Run TRANREPT | Type codes resolved to descriptions (e.g., "01" → "Purchase"). | High |
| BRPT-004 | Category description lookup | Categories in TRANCATG | 1. Run TRANREPT | Category codes resolved to descriptions. | High |
| BRPT-005 | Grand total accuracy | Known transactions | 1. Run TRANREPT<br>2. Sum amounts | Grand total matches sum of all transaction amounts. | Critical |
| BRPT-006 | Account total accuracy | Multiple accounts | 1. Run TRANREPT | Account totals match sum of transactions per account. | High |
| BRPT-007 | No transactions | Empty TRANSACT file | 1. Run TRANREPT | Job completes. Report shows "No data" or headers only. | Medium |

---

## 22. Batch: Data Export/Import (CBEXPORT/CBIMPORT)

**JCL:** CBEXPORT, CBIMPORT | **Programs:** CBEXPORT, CBIMPORT

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BEXP-001 | Export all data | All 5 VSAM files have data | 1. Run CBEXPORT | Sequential export file created with all customers, accounts, xrefs, transactions, cards. | Critical |
| BEXP-002 | Export record type markers | -- | 1. Run CBEXPORT<br>2. Examine output | Each record has correct type flag: C, A, X, T, D. | High |
| BEXP-003 | Export record counts | Known counts in each file | 1. Run CBEXPORT<br>2. Count output records | Output count = sum of all input file records. | High |
| BEXP-004 | Export with empty files | One or more VSAM files empty | 1. Run CBEXPORT | Job completes. Empty file sections skipped. No abend. | Medium |
| BIMP-001 | Import all data | Valid export file exists | 1. Run CBIMPORT | All VSAM files populated from export file. | Critical |
| BIMP-002 | Round-trip integrity | Original data known | 1. CBEXPORT → CBIMPORT<br>2. Compare VSAM records | All records match original data exactly. | Critical |
| BIMP-003 | Import with existing data | VSAM files already have data | 1. Run CBIMPORT | Verify behavior — overwrite, merge, or error. | High |
| BIMP-004 | Invalid export file | Corrupted/malformed file | 1. Run CBIMPORT with bad data | Job abends gracefully with error message. | High |
| BIMP-005 | Export timestamp verification | -- | 1. Run CBEXPORT<br>2. Check timestamp | Export timestamp in header matches execution time. | Medium |

---

## 23. Batch Job Cycle (JCL Integration)

**Scope:** End-to-end batch cycle: CLOSEFIL → Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL

| TC-ID | Test Case | Pre-Condition | Steps | Expected Result | Priority |
|-------|-----------|---------------|-------|-----------------|----------|
| BCYC-001 | Full batch cycle execution | All data files and VSAM datasets ready | 1. Run full batch cycle in order | All jobs complete with RC=0 (or RC=4 for warnings). | Critical |
| BCYC-002 | CLOSEFIL closes CICS files | CICS is active, files open | 1. Run CLOSEFIL | All CardDemo CICS files closed. Online transactions suspended. | Critical |
| BCYC-003 | OPENFIL reopens CICS files | CICS files closed by CLOSEFIL | 1. Run OPENFIL | All CardDemo CICS files reopened. Online transactions resume. | Critical |
| BCYC-004 | Data refresh - ACCTFILE | Account flat file available | 1. Run ACCTFILE JCL | ACCTDATA.VSAM.KSDS deleted, defined, loaded from flat file. | Critical |
| BCYC-005 | Data refresh - CARDFILE | Card flat file available | 1. Run CARDFILE JCL | CARDDATA.VSAM.KSDS refreshed. Alternate indexes built. | Critical |
| BCYC-006 | Data refresh - CUSTFILE | Customer flat file available | 1. Run CUSTFILE JCL | CUSTDATA.VSAM.KSDS refreshed. | Critical |
| BCYC-007 | Data refresh - XREFFILE | Cross-ref flat file available | 1. Run XREFFILE JCL | CARDXREF.VSAM.KSDS refreshed. AIX on account ID built. | Critical |
| BCYC-008 | Transaction backup | Transactions exist | 1. Run TRANBKP | TRANSACT.VSAM copied to GDG backup dataset. | High |
| BCYC-009 | Alternate index build | TRANSACT VSAM has data | 1. Run TRANIDX | Alternate indexes on TRANSACT built successfully. | High |
| BCYC-010 | Job dependency chain | Jobs submitted in wrong order | 1. Run POSTTRAN before CLOSEFIL | Verify behavior — should fail or produce incorrect results due to file contention. | Medium |
| BCYC-011 | GDG base definition | GDG bases don't exist | 1. Run DEFGDGB and DEFGDGD | GDG bases created for backup datasets. | Medium |
| BCYC-012 | Recovery after failed job | POSTTRAN abends midway | 1. Re-run from failed step | Data integrity maintained. Duplicate postings prevented. | High |

---

## 24. Cross-Cutting & Non-Functional

### Security Tests

| TC-ID | Test Case | Steps | Expected Result | Priority |
|-------|-----------|-------|-----------------|----------|
| SEC-001 | Admin functions blocked for regular user | 1. Login as USER0001<br>2. Attempt admin transaction | Access denied. | Critical |
| SEC-002 | Direct transaction ID access | 1. Enter admin CICS transaction ID directly | Verify security check occurs even without menu navigation. | High |
| SEC-003 | Password not displayed on screen | 1. Type password on sign-on | Password field masked (non-display attribute). | High |
| SEC-004 | Session timeout | 1. Leave terminal idle beyond timeout | Session terminated. Must re-authenticate. | Medium |
| SEC-005 | PII data handling | 1. View customer with SSN | SSN displayed — document for PCI/privacy compliance review. | High |

### Error Handling Tests

| TC-ID | Test Case | Steps | Expected Result | Priority |
|-------|-----------|-------|-----------------|----------|
| ERR-001 | VSAM file not found | 1. Attempt read on missing file | Program displays file status error. No system abend. | Critical |
| ERR-002 | VSAM record not found | 1. Read non-existent key | RESP code NOTFND handled. User message displayed. | Critical |
| ERR-003 | VSAM duplicate key on write | 1. Write record with existing key | RESP code DUPREC handled. User message displayed. | High |
| ERR-004 | CICS ABEND recovery | 1. Trigger error condition | HANDLE ABEND catches it. Error message shown. No system crash. | High |
| ERR-005 | Batch program file status errors | 1. Run batch with bad file | Display file status codes. Perform orderly shutdown. | High |
| ERR-006 | Invalid PF key | 1. Press unsupported PF key | Message: "Invalid key pressed" or ignored. No abend. | Medium |

### Performance Tests

| TC-ID | Test Case | Steps | Expected Result | Priority |
|-------|-----------|-------|-----------------|----------|
| PERF-001 | Online response time | 1. Time each CICS transaction | Response < 1 second for all inquiries. < 2 seconds for updates. | High |
| PERF-002 | Batch posting throughput | 1. Run POSTTRAN with 100K records<br>2. Time execution | Completes within batch window SLA. | High |
| PERF-003 | Card list pagination speed | 1. Paginate through 10,000+ cards | Each page loads in < 1 second. | Medium |
| PERF-004 | Statement generation volume | 1. Generate statements for 50K cards | Completes within batch window. Output files correct. | Medium |

### Data Integrity Tests

| TC-ID | Test Case | Steps | Expected Result | Priority |
|-------|-----------|-------|-----------------|----------|
| DINT-001 | Cross-reference consistency | 1. Verify all cards in CARDDATA have XREF entries | No orphan cards. All XREF entries point to valid accounts. | Critical |
| DINT-002 | Account balance reconciliation | 1. Sum all transactions for an account<br>2. Compare to ACCT-CURR-BAL | Balance matches transaction sum + prior balance. | Critical |
| DINT-003 | Category balance reconciliation | 1. Sum transactions by account/type/cat<br>2. Compare to TCATBAL | Category balances match. | High |
| DINT-004 | Export/Import round-trip | 1. Export data<br>2. Import to clean VSAM<br>3. Compare records | All records identical before and after. | High |
| DINT-005 | Customer-Account linkage | 1. Verify all accounts have valid customers via XREF | No orphan accounts. All customer IDs valid. | High |

---

## Test Environment Requirements

| Component | Requirement |
|-----------|-------------|
| **CICS Region** | CardDemo CICS resources defined (CARDDEMO.CSD installed) |
| **VSAM Files** | All 10 VSAM datasets defined and loaded with test data |
| **Batch Environment** | JCL libraries available, LOADLIB with compiled programs |
| **Test Data** | Use files from `app/data/ASCII/` or `app/data/EBCDIC/` |
| **Terminal** | 3270 terminal emulator (24×80) for online tests |
| **Credentials** | ADMIN001/PASSWORD (admin), USER0001/PASSWORD (regular) |

---

## Test Data Requirements

| Entity | Minimum Records | Notes |
|--------|----------------|-------|
| Customers | 50+ | Mix of complete and partial data |
| Accounts | 100+ | Active and inactive, various balances and limits |
| Cards | 200+ | Active and inactive, multiple per account |
| Cross-References | 200+ | Matching cards to accounts/customers |
| Transactions | 1,000+ | Various types, categories, amounts, merchants |
| Users | 10+ | Mix of admin (A) and regular (R) types |
| Transaction Types | 5+ | Purchase, Cash Advance, Payment, Refund, Fee |
| Disclosure Groups | 3+ | Different interest rate tiers |

---

## Traceability Matrix

| Business Function | Programs | Test Case IDs |
|-------------------|----------|---------------|
| Authentication | COSGN00C | AUTH-001 to AUTH-010 |
| Navigation | COMEN01C, COADM01C | MENU-001 to MENU-010, ADMN-001 to ADMN-005 |
| Account Management | COACTVWC, COACTUPC | ACVW-001 to ACVW-010, ACUP-001 to ACUP-025 |
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC | CLST-001 to CLST-010, CVEW-001 to CVEW-005, CUPD-001 to CUPD-010 |
| Transaction Management | COTRN00C, COTRN01C, COTRN02C | TLST-001 to TLST-008, TVEW-001 to TVEW-005, TADD-001 to TADD-012 |
| Bill Payment | COBIL00C | BILL-001 to BILL-010 |
| Reporting | CORPT00C, CBTRN03C, CBSTM03A/B | REPT-001 to REPT-005, BRPT-001 to BRPT-007, BSTM-001 to BSTM-010 |
| User Administration | COUSR00C-03C | ULST-001 to ULST-006, UADD-001 to UADD-009, UUPD-001 to UUPD-007, UDEL-001 to UDEL-005 |
| Transaction Posting | CBTRN02C | BPST-001 to BPST-012 |
| Interest Calculation | CBACT04C | BINT-001 to BINT-010 |
| Data Migration | CBEXPORT, CBIMPORT | BEXP-001 to BEXP-004, BIMP-001 to BIMP-005 |
| Batch Cycle | JCL jobs | BCYC-001 to BCYC-012 |
| Security | Cross-cutting | SEC-001 to SEC-005 |
| Error Handling | Cross-cutting | ERR-001 to ERR-006 |
| Performance | Cross-cutting | PERF-001 to PERF-004 |
| Data Integrity | Cross-cutting | DINT-001 to DINT-005 |

**Total Test Cases: 199**
