import { test, expect } from '@playwright/test';
import { Accounts, Cards, Selectors } from '../fixtures/test-data';
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
 * Module 9: Transaction List (COTRN00C)
 *
 * Business rules from COBOL source:
 * - Browse TRANSACT file (or TRANAIX alternate index by account)
 * - Filter by account ID or card number
 * - PF7 = previous page, PF8 = next page
 * - Row selection navigates to Transaction View or Transaction Add
 * - F3 → return to Main Menu
 */

test.describe('Transaction List (COTRN00C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 6); // Option 6 = Transaction List
  });

  test('TC-TLST-001: Display transaction list', async ({ page }) => {
    await pressEnter(page);
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-TLST-002: Filter by account ID', async ({ page }) => {
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-TLST-003: Filter by card number', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-TLST-004: Page forward', async ({ page }) => {
    await pressEnter(page);
    await pressF8Next(page);
    // Either page advances or we see a boundary message
    const pageNum = page.locator(Selectors.pageNumber);
    const errMsg = page.locator(Selectors.errorMessage);
    const advanced = await pageNum.textContent();
    const hasMsg = await errMsg.isVisible();
    expect(advanced !== '1' || hasMsg).toBeTruthy();
  });

  test('TC-TLST-005: Page backward', async ({ page }) => {
    await pressEnter(page);
    await pressF8Next(page);
    await pressF7Prev(page);
    await expect(page.locator(Selectors.pageNumber)).toContainText('1');
  });

  test('TC-TLST-006: Select transaction for view', async ({ page }) => {
    await pressEnter(page);
    const firstSel = page.locator(Selectors.listSelection).first();
    await firstSel.fill('V');
    await pressEnter(page);
    // Should navigate to Transaction View
    await page.waitForLoadState('networkidle');
  });

  test('TC-TLST-007: Return to menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/menu/);
  });
});
