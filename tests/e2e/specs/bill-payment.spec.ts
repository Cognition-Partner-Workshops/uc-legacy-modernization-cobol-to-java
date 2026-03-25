import { test, expect } from '@playwright/test';
import { Accounts, Selectors } from '../fixtures/test-data';
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
 * Module 13: Bill Payment (COBIL00C)
 *
 * Business rules from COBOL source:
 * - Accepts 11-digit account ID
 * - Reads ACCTDAT (with UPDATE lock) to get current balance
 * - Balance <= 0 → "You have nothing to pay..."
 * - Confirm Y → creates TRANSACT record (type 02, "BILL PAYMENT - ONLINE"),
 *   sets balance to zero via REWRITE ACCTDAT
 * - Confirm N → clears screen
 * - Invalid confirm → "Invalid value. Valid values are (Y/N)..."
 * - Empty account → "Acct ID can NOT be empty..."
 * - Not found → "Account ID NOT found..."
 * - F4 = clear, F3 = return
 */

test.describe('Bill Payment (COBIL00C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 10); // Option 10 = Bill Payment
  });

  test('TC-BILL-001: Display account balance for payment', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await pressEnter(page);
    // Current balance should be displayed
    await expect(page.locator(Selectors.billCurBalance)).toBeVisible();
  });

  test('TC-BILL-002: Confirm and process bill payment', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await pressEnter(page);

    // Verify balance is shown before confirming
    await expect(page.locator(Selectors.billCurBalance)).toBeVisible();

    await confirmAction(page, 'Y');
    await expectSuccess(page, /payment|processed|success/i);
  });

  test('TC-BILL-003: Decline bill payment', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await pressEnter(page);
    await confirmAction(page, 'N');
    // Screen should be cleared — no success message
    await expect(page.locator(Selectors.successMessage)).not.toBeVisible();
  });

  test('TC-BILL-004: Invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await pressEnter(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-BILL-005: Empty Account ID', async ({ page }) => {
    await pressEnter(page);
    await expectError(page, /Acct ID can NOT be empty/);
  });

  test('TC-BILL-006: Non-existent Account ID', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.nonExistent.id);
    await pressEnter(page);
    await expectError(page, /Account ID NOT found/);
  });

  test('TC-BILL-007: Zero balance account', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.zeroBalance.id);
    await pressEnter(page);
    await expectError(page, /You have nothing to pay/);
  });

  test('TC-BILL-008: Clear screen via F4', async ({ page }) => {
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await page.click(Selectors.btnF4Clear);
    await expect(page.locator(Selectors.billAcctId)).toHaveValue('');
  });

  test('TC-BILL-009: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/menu/);
  });
});
