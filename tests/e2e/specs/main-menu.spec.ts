import { test, expect } from '@playwright/test';
import { Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressF3Exit,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 2: Main Menu Navigation (COMEN01C)
 *
 * Business rules from COBOL source:
 * - 11 menu options defined in COMEN02Y copybook
 * - Options 1-11 each map to a COBOL program via XCTL
 * - Non-numeric, zero, or out-of-range option → error
 * - Admin-only options blocked for regular users
 * - F3 → return to sign-on screen
 */

test.describe('Main Menu (COMEN01C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
  });

  test('TC-MENU-001: Display all 11 menu options for regular user', async ({ page }) => {
    const options = page.locator(Selectors.menuOption);
    await expect(options).toHaveCount(11);

    const expectedLabels = [
      'Account View',
      'Account Update',
      'Credit Card List',
      'Credit Card View',
      'Credit Card Update',
      'Transaction List',
      'Transaction View',
      'Transaction Add',
      'Transaction Reports',
      'Bill Payment',
      'Pending Authorization View',
    ];

    for (let i = 0; i < expectedLabels.length; i++) {
      await expect(options.nth(i)).toContainText(expectedLabels[i]);
    }
  });

  test('TC-MENU-002: Navigate to Account View (option 1)', async ({ page }) => {
    await selectMenuOption(page, 1);
    await expect(page.locator(Selectors.acctIdInput)).toBeVisible();
  });

  test('TC-MENU-003: Navigate to each menu option and return', async ({ page }) => {
    // Test options 1 through 10 (skip 11 — optional module may not be installed)
    for (let opt = 1; opt <= 10; opt++) {
      await selectMenuOption(page, opt);
      // Verify we navigated away from menu
      await expect(page).not.toHaveURL(/\/menu$/);
      // Return to menu
      await pressF3Exit(page);
      await expect(page).toHaveURL(/\/menu/);
    }
  });

  test('TC-MENU-004: Invalid option number (99)', async ({ page }) => {
    await selectMenuOption(page, 99);
    await expectError(page, /Please enter a valid option number/);
  });

  test('TC-MENU-005: Zero option number', async ({ page }) => {
    await selectMenuOption(page, 0);
    await expectError(page, /Please enter a valid option number/);
  });

  test('TC-MENU-006: Non-numeric option', async ({ page }) => {
    await page.fill(Selectors.menuOptionInput, 'AB');
    await page.click(Selectors.btnEnter);
    await expectError(page, /Please enter a valid option number/);
  });

  test('TC-MENU-007: F3 returns to sign-on screen', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/(login|signon|\/)$/);
  });
});
