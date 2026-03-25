import { test, expect } from '@playwright/test';
import { Accounts, Cards, Transactions, Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  confirmAction,
  expectError,
  expectSuccess,
} from '../fixtures/helpers';

/**
 * Module 11: Transaction Add (COTRN02C)
 *
 * Business rules from COBOL source:
 * - Key fields: Account ID or Card Number (cross-referenced via CCXREF / CXACAIX)
 * - Data fields: Type CD (numeric), Category CD (numeric), Source, Description,
 *   Amount (format ±99999999.99), Orig Date (YYYY-MM-DD), Proc Date (YYYY-MM-DD),
 *   Merchant ID, Name, City, Zip
 * - Confirm Y → WRITE to TRANSACT (auto-generated transaction ID)
 * - Confirm N → "Confirm to add this transaction..."
 * - Invalid confirm → "Invalid value. Valid values are (Y/N)..."
 * - F4 = clear, F5 = copy last transaction, F3 = return
 */

test.describe('Transaction Add (COTRN02C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 8); // Option 8 = Transaction Add
  });

  /** Fill all transaction data fields (not key fields). */
  async function fillDataFields(page: import('@playwright/test').Page) {
    const t = Transactions.validAdd;
    await page.fill(Selectors.txnTypeCd, t.typeCd);
    await page.fill(Selectors.txnCatCd, t.categoryCd);
    await page.fill(Selectors.txnSource, t.source);
    await page.fill(Selectors.txnDesc, t.description);
    await page.fill(Selectors.txnAmount, t.amount);
    await page.fill(Selectors.txnOrigDate, t.origDate);
    await page.fill(Selectors.txnProcDate, t.procDate);
    await page.fill(Selectors.txnMerchantId, t.merchantId);
    await page.fill(Selectors.txnMerchantName, t.merchantName);
    await page.fill(Selectors.txnMerchantCity, t.merchantCity);
    await page.fill(Selectors.txnMerchantZip, t.merchantZip);
  }

  test('TC-TADD-001: Add transaction with valid data via account ID', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await fillDataFields(page);
    await confirmAction(page, 'Y');
    await expectSuccess(page, /added|success/i);
  });

  test('TC-TADD-002: Add transaction with valid data via card number', async ({ page }) => {
    await page.fill(Selectors.txnCardNum, Cards.active.number);
    await pressEnter(page);
    await fillDataFields(page);
    await confirmAction(page, 'Y');
    await expectSuccess(page, /added|success/i);
  });

  test('TC-TADD-003: Reject add when confirm is N', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await fillDataFields(page);
    await confirmAction(page, 'N');
    await expectError(page, /Confirm to add this transaction/);
  });

  test('TC-TADD-004: Invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await fillDataFields(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-TADD-005: Empty Account ID and Card Number', async ({ page }) => {
    await pressEnter(page);
    await expectError(page, /Account or Card Number must be entered/);
  });

  test('TC-TADD-006: Non-numeric Account ID', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, 'ABCDEFGHIJK');
    await pressEnter(page);
    await expectError(page, /Account ID must be Numeric/);
  });

  test('TC-TADD-007: Non-numeric Card Number', async ({ page }) => {
    await page.fill(Selectors.txnCardNum, 'ABCDEFGHIJKLMNOP');
    await pressEnter(page);
    await expectError(page, /Card Number must be Numeric/);
  });

  test('TC-TADD-008: Empty Type CD', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    // Leave Type CD empty, fill rest
    await page.fill(Selectors.txnCatCd, '01');
    await pressEnter(page);
    await expectError(page, /Type CD can NOT be empty/);
  });

  test('TC-TADD-009: Non-numeric Type CD', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, 'AB');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    await page.fill(Selectors.txnMerchantZip, '12345');
    await confirmAction(page, 'Y');
    await expectError(page, /Type CD must be Numeric/);
  });

  test('TC-TADD-010: Empty Category CD', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    // Leave Category CD empty
    await pressEnter(page);
    await expectError(page, /Category CD can NOT be empty/);
  });

  test('TC-TADD-011: Non-numeric Category CD', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, 'XY');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    await page.fill(Selectors.txnMerchantZip, '12345');
    await confirmAction(page, 'Y');
    await expectError(page, /Category CD must be Numeric/);
  });

  test('TC-TADD-012: Empty Source field', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    // Leave Source empty
    await pressEnter(page);
    await expectError(page, /Source can NOT be empty/);
  });

  test('TC-TADD-013: Empty Description', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    // Leave Description empty
    await pressEnter(page);
    await expectError(page, /Description can NOT be empty/);
  });

  test('TC-TADD-014: Empty Amount', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    // Leave Amount empty
    await pressEnter(page);
    await expectError(page, /Amount can NOT be empty/);
  });

  test('TC-TADD-015: Invalid Amount format', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, 'ABCDE');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    await page.fill(Selectors.txnMerchantZip, '12345');
    await confirmAction(page, 'Y');
    await expectError(page, /Amount should be in format/);
  });

  test('TC-TADD-016: Empty Origination Date', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    // Leave Orig Date empty
    await pressEnter(page);
    await expectError(page, /Orig Date can NOT be empty/);
  });

  test('TC-TADD-017: Invalid Origination Date format', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '03/25/2026');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    await page.fill(Selectors.txnMerchantZip, '12345');
    await confirmAction(page, 'Y');
    await expectError(page, /Orig Date should be in format YYYY-MM-DD/);
  });

  test('TC-TADD-018: Empty Processing Date', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    // Leave Proc Date empty
    await pressEnter(page);
    await expectError(page, /Proc Date can NOT be empty/);
  });

  test('TC-TADD-019: Invalid Processing Date format', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '03-25-26');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    await page.fill(Selectors.txnMerchantZip, '12345');
    await confirmAction(page, 'Y');
    await expectError(page, /Proc Date should be in format YYYY-MM-DD/);
  });

  test('TC-TADD-020: Empty Merchant ID', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    // Leave Merchant ID empty
    await pressEnter(page);
    await expectError(page, /Merchant ID can NOT be empty/);
  });

  test('TC-TADD-021: Empty Merchant Name', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    // Leave Merchant Name empty
    await pressEnter(page);
    await expectError(page, /Merchant Name can NOT be empty/);
  });

  test('TC-TADD-022: Empty Merchant City', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    // Leave Merchant City empty
    await pressEnter(page);
    await expectError(page, /Merchant City can NOT be empty/);
  });

  test('TC-TADD-023: Empty Merchant Zip', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await page.fill(Selectors.txnTypeCd, '01');
    await page.fill(Selectors.txnCatCd, '01');
    await page.fill(Selectors.txnSource, 'TEST');
    await page.fill(Selectors.txnDesc, 'TEST');
    await page.fill(Selectors.txnAmount, '-00000001.00');
    await page.fill(Selectors.txnOrigDate, '2026-03-25');
    await page.fill(Selectors.txnProcDate, '2026-03-25');
    await page.fill(Selectors.txnMerchantId, '123');
    await page.fill(Selectors.txnMerchantName, 'TEST');
    await page.fill(Selectors.txnMerchantCity, 'CITY');
    // Leave Merchant Zip empty
    await pressEnter(page);
    await expectError(page, /Merchant Zip can NOT be empty/);
  });

  test('TC-TADD-024: Clear screen via F4', async ({ page }) => {
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await page.click(Selectors.btnF4Clear);
    await expect(page.locator(Selectors.txnAcctId)).toHaveValue('');
  });

  test('TC-TADD-025: Copy last transaction data via F5', async ({ page }) => {
    // First add a transaction
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);
    await fillDataFields(page);
    await confirmAction(page, 'Y');

    // Clear and then copy
    await page.click(Selectors.btnF4Clear);
    await page.click(Selectors.btnF5Copy);

    // Fields should be populated from last transaction
    await expect(page.locator(Selectors.txnDesc)).not.toHaveValue('');
  });

  test('TC-TADD-026: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/(menu|transaction)/);
  });

});
