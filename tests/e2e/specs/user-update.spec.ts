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
 * Module 16: User Update (COUSR02C) — Admin Only
 *
 * Business rules from COBOL source:
 * - Reads USRSEC by User ID (8-char key)
 * - Editable fields: First Name, Last Name, Password, User Type
 * - Confirm Y → REWRITE USRSEC
 * - Confirm N → "Update has been cancelled..."
 * - Invalid confirm → "Invalid value. Valid values are (Y/N)..."
 * - F3 → return to User List
 */

test.describe('User Update (COUSR02C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await selectMenuOption(page, 3); // Admin option 3 = User Update
  });

  test('TC-UUPD-001: Display user for update', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await expect(page.locator(Selectors.userFirstName)).toBeVisible();
    await expect(page.locator(Selectors.userLastName)).toBeVisible();
  });

  test('TC-UUPD-002: Update user first name', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await page.fill(Selectors.userFirstName, 'UPDATED');
    await confirmAction(page, 'Y');
    await expectSuccess(page, /updated|success/i);
  });

  test('TC-UUPD-003: Cancel user update', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await confirmAction(page, 'N');
    await expect(page.locator(Selectors.errorMessage)).toContainText(/cancelled/i);
  });

  test('TC-UUPD-004: Invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.userIdInput, Users.regular.userId);
    await pressEnter(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-UUPD-005: Return to Admin Menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/admin/);
  });
});
