/**
 * Test data constants derived from CardDemo COBOL source analysis.
 *
 * Default credentials come from the original USRSEC VSAM dataset.
 * Account/card IDs match the sample data shipped in app/data/ASCII/.
 */

export const Users = {
  admin: {
    userId: 'ADMIN001',
    password: 'PASSWORD',
    type: 'A',
    firstName: 'ADMIN',
    lastName: 'USER',
  },
  regular: {
    userId: 'USER0001',
    password: 'PASSWORD',
    type: 'U',
    firstName: 'REGULAR',
    lastName: 'USER',
  },
  /** User that will be created / deleted during tests */
  ephemeral: {
    userId: 'TESTUS99',
    password: 'TESTPW99',
    type: 'U',
    firstName: 'TESTFN',
    lastName: 'TESTLN',
  },
} as const;

export const Accounts = {
  /** Active account with a positive balance */
  active: {
    id: '00000000011',
    description: 'Active test account',
  },
  /** Account that does not exist */
  nonExistent: {
    id: '99999999999',
  },
  /** Account with zero balance (bill-pay edge case) */
  zeroBalance: {
    id: '00000000099',
  },
} as const;

export const Cards = {
  active: {
    number: '4000000000000001',
    description: 'Active card linked to test account',
  },
  nonExistent: {
    number: '9999999999999999',
  },
} as const;

export const Transactions = {
  /** Valid new-transaction payload for COTRN02C */
  validAdd: {
    typeCd: '01',
    categoryCd: '01',
    source: 'POS TERM',
    description: 'E2E TEST PURCHASE',
    amount: '-00000100.00',
    origDate: '2026-03-25',
    procDate: '2026-03-25',
    merchantId: '123456789',
    merchantName: 'TEST MERCHANT',
    merchantCity: 'TESTCITY',
    merchantZip: '12345',
  },
} as const;

/** Navigation selectors – adjust these to match the modernized UI's DOM */
export const Selectors = {
  // Login page
  loginUserId: '[data-testid="login-userid"]',
  loginPassword: '[data-testid="login-password"]',
  loginSubmit: '[data-testid="login-submit"]',
  loginError: '[data-testid="login-error"]',

  // Common header
  headerTranName: '[data-testid="header-tran-name"]',
  headerPgmName: '[data-testid="header-pgm-name"]',
  headerDate: '[data-testid="header-date"]',
  headerTime: '[data-testid="header-time"]',

  // Common controls
  errorMessage: '[data-testid="error-message"]',
  successMessage: '[data-testid="success-message"]',
  confirmField: '[data-testid="confirm"]',

  // Menu
  menuOption: '[data-testid="menu-option"]',
  menuOptionInput: '[data-testid="menu-option-input"]',

  // Function keys mapped to buttons in modernized UI
  btnEnter: '[data-testid="btn-enter"]',
  btnF3Exit: '[data-testid="btn-f3"]',
  btnF4Clear: '[data-testid="btn-f4"]',
  btnF5Copy: '[data-testid="btn-f5"]',
  btnF7Prev: '[data-testid="btn-f7"]',
  btnF8Next: '[data-testid="btn-f8"]',

  // Account View
  acctIdInput: '[data-testid="account-id-input"]',
  acctStatus: '[data-testid="account-status"]',
  acctBalance: '[data-testid="account-balance"]',
  acctCreditLimit: '[data-testid="account-credit-limit"]',

  // Card fields
  cardNumberInput: '[data-testid="card-number-input"]',
  cardStatus: '[data-testid="card-status"]',
  cardExpiry: '[data-testid="card-expiry"]',

  // Transaction Add fields
  txnAcctId: '[data-testid="txn-account-id"]',
  txnCardNum: '[data-testid="txn-card-number"]',
  txnTypeCd: '[data-testid="txn-type-cd"]',
  txnCatCd: '[data-testid="txn-category-cd"]',
  txnSource: '[data-testid="txn-source"]',
  txnDesc: '[data-testid="txn-description"]',
  txnAmount: '[data-testid="txn-amount"]',
  txnOrigDate: '[data-testid="txn-orig-date"]',
  txnProcDate: '[data-testid="txn-proc-date"]',
  txnMerchantId: '[data-testid="txn-merchant-id"]',
  txnMerchantName: '[data-testid="txn-merchant-name"]',
  txnMerchantCity: '[data-testid="txn-merchant-city"]',
  txnMerchantZip: '[data-testid="txn-merchant-zip"]',

  // Bill Payment
  billAcctId: '[data-testid="bill-account-id"]',
  billCurBalance: '[data-testid="bill-current-balance"]',

  // User Management
  userFirstName: '[data-testid="user-firstname"]',
  userLastName: '[data-testid="user-lastname"]',
  userIdInput: '[data-testid="user-id-input"]',
  userPassword: '[data-testid="user-password"]',
  userType: '[data-testid="user-type"]',

  // User List
  userListRows: '[data-testid="user-list-row"]',
  userListSelection: '[data-testid="user-selection"]',
  userListFilterInput: '[data-testid="user-filter-input"]',

  // Pagination
  pageNumber: '[data-testid="page-number"]',

  // List rows (generic)
  listRow: '[data-testid="list-row"]',
  listSelection: '[data-testid="row-selection"]',
} as const;
