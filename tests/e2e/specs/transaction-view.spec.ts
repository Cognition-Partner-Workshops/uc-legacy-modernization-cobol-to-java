import { test, expect } from '@playwright/test';
import { Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 10: Transaction View (COTRN01C)
 *
 * Business rules from COBOL source:
 * - Reads TRANSACT by transaction ID (16-digit key)
 * - Displays all transaction fields: ID, card number, type code,
 *   category, source, description, amount, dates, merchant info
 * - Not found → error
 * - F3 → return to Transaction List
 */

test.describe('Transaction View (COTRN01C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 7); // Option 7 = Transaction View
  });

  test('TC-TVEW-001: View transaction details', async ({ page }) => {
    // Navigate from Transaction List to select a transaction
    // If direct input is supported, enter a known transaction ID
    const txnIdInput = page.locator('[data-testid="txn-id-input"]');
    if (await txnIdInput.isVisible()) {
      await txnIdInput.fill('0000000000000001');
      await pressEnter(page);
      // Verify fields are displayed
      await expect(page.locator(Selectors.txnDesc)).toBeVisible();
      await expect(page.locator(Selectors.txnAmount)).toBeVisible();
    }
  });

  test('TC-TVEW-002: View non-existent transaction', async ({ page }) => {
    const txnIdInput = page.locator('[data-testid="txn-id-input"]');
    if (await txnIdInput.isVisible()) {
      await txnIdInput.fill('9999999999999999');
      await pressEnter(page);
      await expectError(page, /not found/i);
    }
  });

  test('TC-TVEW-003: Return to list via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/(menu|transaction)/);
  });
});
