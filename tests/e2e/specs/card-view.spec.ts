import { test, expect } from '@playwright/test';
import { Cards, Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 7: Credit Card View (COCRDSLC)
 *
 * Business rules from COBOL source:
 * - Reads CARDDAT by card number (16-digit key)
 * - Displays: card number, account ID, customer ID, status, expiry
 * - Not found → error
 * - F3 → return to Card List or Main Menu
 */

test.describe('Credit Card View (COCRDSLC)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 4); // Option 4 = Credit Card View
  });

  test('TC-CVEW-001: View card details', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);
    await expect(page.locator(Selectors.cardStatus)).toBeVisible();
    await expect(page.locator(Selectors.cardExpiry)).toBeVisible();
  });

  test('TC-CVEW-002: View card with invalid number', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.nonExistent.number);
    await pressEnter(page);
    await expectError(page, /not found|Did not find/i);
  });

  test('TC-CVEW-003: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/menu/);
  });
});
