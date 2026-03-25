import { test, expect } from '@playwright/test';
import { Accounts, Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 4: Account View (COACTVWC)
 *
 * Business rules from COBOL source:
 * - Accepts 11-digit numeric account ID
 * - Reads CXACAIX (card xref by account), ACCTDAT, CUSTDAT
 * - Displays account status, balances, credit limit, customer info
 * - Non-numeric → "Account number must be a non zero 11 digit number"
 * - All zeroes  → same message
 * - Not found in xref → "Did not find this account in account card xref file"
 * - Not found in master → "Did not find this account in account master file"
 * - Empty input → "No input received"
 * - F3 → return to Main Menu
 */

test.describe('Account View (COACTVWC)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 1); // Option 1 = Account View
  });

  test('TC-AVEW-001: View account by valid account ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);

    await expect(page.locator(Selectors.acctStatus)).toBeVisible();
    await expect(page.locator(Selectors.acctBalance)).toBeVisible();
    await expect(page.locator(Selectors.acctCreditLimit)).toBeVisible();
  });

  test('TC-AVEW-002: View account with non-existent ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.nonExistent.id);
    await pressEnter(page);
    await expectError(page, /Did not find this account/);
  });

  test('TC-AVEW-003: View account with non-numeric ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, 'ABCDEFGHIJK');
    await pressEnter(page);
    await expectError(page, /Account number must be a non zero 11 digit number/);
  });

  test('TC-AVEW-004: View account with zero ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, '00000000000');
    await pressEnter(page);
    await expectError(page, /Account number must be a non zero 11 digit number/);
  });

  test('TC-AVEW-005: View account with empty input', async ({ page }) => {
    await pressEnter(page);
    await expectError(page, /No input received|Account number not provided/);
  });

  test('TC-AVEW-006: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/menu/);
  });
});
