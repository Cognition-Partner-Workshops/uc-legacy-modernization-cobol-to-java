import { test, expect } from '@playwright/test';
import { Accounts, Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  pressF7Prev,
  pressF8Next,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 6: Credit Card List (COCRDLIC)
 *
 * Business rules from COBOL source:
 * - Browse CARDAIX (card file alternate index by account)
 * - Displays card number, account ID, active status per row
 * - PF7 = previous page, PF8 = next page
 * - Selection marker navigates to Card View or Card Update
 * - F3 → return to Main Menu
 */

test.describe('Credit Card List (COCRDLIC)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 3); // Option 3 = Credit Card List
  });

  test('TC-CLST-001: Display card list', async ({ page }) => {
    await pressEnter(page);
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-CLST-002: Filter by account ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-CLST-003: Page forward in card list', async ({ page }) => {
    await pressEnter(page);
    const pageNumBefore = await page.locator(Selectors.pageNumber).textContent();
    await pressF8Next(page);
    // Either page advances or we get "bottom of page" message
    const pageNumAfter = await page.locator(Selectors.pageNumber).textContent();
    const hasMessage = await page.locator(Selectors.errorMessage).isVisible();
    expect(pageNumAfter !== pageNumBefore || hasMessage).toBeTruthy();
  });

  test('TC-CLST-004: Page backward in card list', async ({ page }) => {
    await pressEnter(page);
    await pressF8Next(page); // Go to page 2
    await pressF7Prev(page); // Back to page 1
    await expect(page.locator(Selectors.pageNumber)).toContainText('1');
  });

  test('TC-CLST-005: Page backward at first page', async ({ page }) => {
    await pressEnter(page);
    await pressF7Prev(page);
    await expectError(page, /already at the top/);
  });

  test('TC-CLST-006: Select card for view', async ({ page }) => {
    await pressEnter(page);
    const firstRow = page.locator(Selectors.listSelection).first();
    await firstRow.fill('V');
    await pressEnter(page);
    // Should navigate to Card View screen
    await expect(page.locator(Selectors.cardNumberInput)).toBeVisible();
  });

  test('TC-CLST-007: Select card for update', async ({ page }) => {
    await pressEnter(page);
    const firstRow = page.locator(Selectors.listSelection).first();
    await firstRow.fill('U');
    await pressEnter(page);
    // Should navigate to Card Update screen
    await expect(page.locator(Selectors.confirmField)).toBeVisible();
  });
});
