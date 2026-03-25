import { test, expect } from '@playwright/test';
import { Selectors } from '../fixtures/test-data';
import {
  loginAsAdmin,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  pressF7Prev,
  pressF8Next,
  expectError,
} from '../fixtures/helpers';

/**
 * Module 14: User List (COUSR00C) — Admin Only
 *
 * Business rules from COBOL source:
 * - Browse USRSEC VSAM file
 * - 10 users per page
 * - Selection "U" → navigate to User Update (COUSR02C)
 * - Selection "D" → navigate to User Delete (COUSR03C)
 * - Invalid selection → "Invalid selection. Valid values are U and D"
 * - PF7 = previous page, PF8 = next page
 * - F3 → return to Admin Menu
 */

test.describe('User List (COUSR00C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await selectMenuOption(page, 1); // Admin option 1 = User List
  });

  test('TC-ULST-001: Display user list', async ({ page }) => {
    const rows = page.locator(Selectors.userListRows);
    await expect(rows.first()).toBeVisible();
    // Should show up to 10 rows per page
    const count = await rows.count();
    expect(count).toBeGreaterThan(0);
    expect(count).toBeLessThanOrEqual(10);
  });

  test('TC-ULST-002: Page forward', async ({ page }) => {
    await pressF8Next(page);
    const pageNum = page.locator(Selectors.pageNumber);
    const errMsg = page.locator(Selectors.errorMessage);
    const hasAdvanced = await pageNum.textContent();
    const hasMsg = await errMsg.isVisible();
    expect(hasAdvanced !== '1' || hasMsg).toBeTruthy();
  });

  test('TC-ULST-003: Page backward', async ({ page }) => {
    await pressF8Next(page);
    await pressF7Prev(page);
    await expect(page.locator(Selectors.pageNumber)).toContainText('1');
  });

  test('TC-ULST-004: Page backward at first page', async ({ page }) => {
    await pressF7Prev(page);
    await expectError(page, /already at the top/);
  });

  test('TC-ULST-005: Page forward at last page', async ({ page }) => {
    // Navigate to the last page by pressing PF8 until we can't
    for (let i = 0; i < 20; i++) {
      await pressF8Next(page);
      const errMsg = page.locator(Selectors.errorMessage);
      if (await errMsg.isVisible()) {
        const text = await errMsg.textContent();
        if (text?.includes('bottom')) break;
      }
    }
    await pressF8Next(page);
    await expectError(page, /already at the bottom/);
  });

  test('TC-ULST-006: Select user for update (U)', async ({ page }) => {
    const firstSel = page.locator(Selectors.userListSelection).first();
    await firstSel.fill('U');
    await pressEnter(page);
    // Should navigate to User Update (COUSR02C)
    await expect(page.locator(Selectors.userFirstName)).toBeVisible();
  });

  test('TC-ULST-007: Select user for delete (D)', async ({ page }) => {
    const firstSel = page.locator(Selectors.userListSelection).first();
    await firstSel.fill('D');
    await pressEnter(page);
    // Should navigate to User Delete (COUSR03C)
    await expect(page.locator(Selectors.confirmField)).toBeVisible();
  });

  test('TC-ULST-008: Invalid selection', async ({ page }) => {
    const firstSel = page.locator(Selectors.userListSelection).first();
    await firstSel.fill('X');
    await pressEnter(page);
    await expectError(page, /Invalid selection.*Valid values are U and D/);
  });

  test('TC-ULST-009: Filter by User ID', async ({ page }) => {
    await page.fill(Selectors.userListFilterInput, 'ADMIN');
    await pressEnter(page);
    const rows = page.locator(Selectors.userListRows);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-ULST-010: Return to Admin Menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/admin/);
  });
});
