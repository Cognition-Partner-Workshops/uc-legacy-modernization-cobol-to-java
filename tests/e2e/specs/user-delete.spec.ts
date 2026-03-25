import { test, expect } from '@playwright/test';
import { Users, Selectors } from '../fixtures/test-data';
import {
  loginAsAdmin,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  confirmAction,
  expectError,
  expectSuccess,
} from '../fixtures/helpers';

/**
 * Module 17: User Delete (COUSR03C) — Admin Only
 *
 * Business rules from COBOL source:
 * - Reads USRSEC by User ID for display (read-only view)
 * - Confirm Y → DELETE USRSEC record
 * - Confirm N → "Deletion has been cancelled..."
 * - Invalid confirm → "Invalid value. Valid values are (Y/N)..."
 * - F3 → return to User List
 */

test.describe('User Delete (COUSR03C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await selectMenuOption(page, 4); // Admin option 4 = User Delete
  });

  test('TC-UDEL-001: Display user for deletion', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.ephemeral.userId);
    await pressEnter(page);
    // User details should appear in read-only mode
    await expect(page.locator(Selectors.userFirstName)).toBeVisible();
  });

  test('TC-UDEL-002: Confirm delete user', async ({ page }) => {
    // Use ephemeral user to avoid deleting required accounts
    await page.fill(Selectors.userIdInput, Users.ephemeral.userId);
    await pressEnter(page);
    await confirmAction(page, 'Y');
    await expectSuccess(page, /deleted|removed|success/i);
  });

  test('TC-UDEL-003: Cancel delete user', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await confirmAction(page, 'N');
    await expect(page.locator(Selectors.errorMessage)).toContainText(/cancelled/i);
  });

  test('TC-UDEL-004: Invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-UDEL-005: Return to Admin Menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/admin/);
  });
});
