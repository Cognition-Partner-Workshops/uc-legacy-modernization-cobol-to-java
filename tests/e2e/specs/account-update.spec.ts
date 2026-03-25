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
 * Module 5: Account Update (COACTUPC)
 *
 * Business rules from COBOL source (4,236 LOC — largest online program):
 * - Display account in editable mode
 * - Confirm Y → REWRITE account record
 * - Confirm N → discard changes
 * - Invalid confirm → error
 * - Non-existent account → not-found error
 * - F3 → return to Main Menu
 */

test.describe('Account Update (COACTUPC)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 2); // Option 2 = Account Update
  });

  test('TC-AUPD-001: Display account for update', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    // Account fields should be displayed in editable mode
    await expect(page.locator(Selectors.acctCreditLimit)).toBeVisible();
  });

  test('TC-AUPD-002: Update account credit limit', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);

    // Modify credit limit
    await page.fill(Selectors.acctCreditLimit, '15000.00');
    await confirmAction(page, 'Y');

    await expectSuccess(page, /updated|success/i);
  });

  test('TC-AUPD-003: Cancel account update', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);

    await page.fill(Selectors.acctCreditLimit, '99999.99');
    await confirmAction(page, 'N');

    // Changes should not be saved — no success message
    const errorOrInfo = page.locator(Selectors.errorMessage);
    // Should not show a success message
    await expect(page.locator(Selectors.successMessage)).not.toBeVisible();
  });

  test('TC-AUPD-004: Update with invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-AUPD-005: Update non-existent account', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.nonExistent.id);
    await pressEnter(page);
    await expectError(page, /not found|Did not find/i);
  });

  test('TC-AUPD-006: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/menu/);
  });
});
