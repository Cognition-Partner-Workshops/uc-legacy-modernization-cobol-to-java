import { test, expect } from '@playwright/test';
import { Selectors } from '../fixtures/test-data';
import {
  loginAsAdmin,
  selectMenuOption,
  pressF3Exit,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 3: Admin Menu (COADM01C)
 *
 * Business rules from COBOL source:
 * - 6 admin options defined in COADM02Y copybook
 * - Options: User List, User Add, User Update, User Delete,
 *   Tran Type List/Update (Db2), Tran Type Maintenance (Db2)
 * - PGMIDERR handler catches missing optional modules
 * - F3 → return to sign-on screen
 */

test.describe('Admin Menu (COADM01C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('TC-ADMN-001: Display 6 admin menu options', async ({ page }) => {
    const options = page.locator(Selectors.menuOption);
    await expect(options).toHaveCount(6);

    const expectedLabels = [
      'User List (Security)',
      'User Add (Security)',
      'User Update (Security)',
      'User Delete (Security)',
      'Transaction Type List/Update (Db2)',
      'Transaction Type Maintenance (Db2)',
    ];

    for (let i = 0; i < expectedLabels.length; i++) {
      await expect(options.nth(i)).toContainText(expectedLabels[i]);
    }
  });

  test('TC-ADMN-002: Navigate to User List (option 1)', async ({ page }) => {
    await selectMenuOption(page, 1);
    await expect(page.locator(Selectors.userListRows).first()).toBeVisible();
  });

  test('TC-ADMN-003: Navigate to User Add (option 2)', async ({ page }) => {
    await selectMenuOption(page, 2);
    await expect(page.locator(Selectors.userFirstName)).toBeVisible();
  });

  test('TC-ADMN-004: Navigate to User Update (option 3)', async ({ page }) => {
    await selectMenuOption(page, 3);
    await page.waitForLoadState('networkidle');
  });

  test('TC-ADMN-005: Navigate to User Delete (option 4)', async ({ page }) => {
    await selectMenuOption(page, 4);
    await page.waitForLoadState('networkidle');
  });

  test('TC-ADMN-006: Invalid option number', async ({ page }) => {
    await selectMenuOption(page, 99);
    await expectError(page, /Please enter a valid option number/);
  });

  test('TC-ADMN-007: F3 returns to sign-on screen', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/(login|signon|\/)$/);
  });
});
