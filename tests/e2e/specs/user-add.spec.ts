import { test, expect } from '@playwright/test';
import { Users, Selectors } from '../fixtures/test-data';
import {
  loginAsAdmin,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  expectError,
  expectSuccess,
} from '../fixtures/helpers';

/**
 * Module 15: User Add (COUSR01C) — Admin Only
 *
 * Business rules from COBOL source:
 * - Required fields: First Name, Last Name, User ID, Password, User Type
 * - Writes to USRSEC VSAM file
 * - Duplicate User ID → DUPREC/DUPKEY → "User ID already exist..."
 * - Success → "User {id} has been added ..." and fields cleared
 * - F4 = clear, F3 = return to Admin Menu
 */

test.describe('User Add (COUSR01C)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await selectMenuOption(page, 2); // Admin option 2 = User Add
  });

  /** Fill all user add fields. */
  async function fillUserFields(
    page: import('@playwright/test').Page,
    data: typeof Users.ephemeral,
  ) {
    await page.fill(Selectors.userFirstName, data.firstName);
    await page.fill(Selectors.userLastName, data.lastName);
    await page.fill(Selectors.userIdInput, data.userId);
    await page.fill(Selectors.userPassword, data.password);
    await page.fill(Selectors.userType, data.type);
  }

  test('TC-UADD-001: Add new user with valid data', async ({ page }) => {
    // Use a unique user ID with timestamp to avoid conflicts
    const uniqueUser = {
      ...Users.ephemeral,
      userId: `TST${Date.now().toString().slice(-5)}`,
    };
    await fillUserFields(page, uniqueUser);
    await pressEnter(page);
    await expectSuccess(page, /has been added/);
  });

  test('TC-UADD-002: Add user with duplicate User ID', async ({ page }) => {
    const dupeUser = {
      ...Users.ephemeral,
      userId: Users.admin.userId, // ADMIN001 already exists
    };
    await fillUserFields(page, dupeUser);
    await pressEnter(page);
    await expectError(page, /User ID already exist/);
  });

  test('TC-UADD-003: Empty First Name', async ({ page }) => {
    // Leave First Name empty
    await page.fill(Selectors.userLastName, 'TEST');
    await page.fill(Selectors.userIdInput, 'TESTID01');
    await page.fill(Selectors.userPassword, 'PASS1234');
    await page.fill(Selectors.userType, 'U');
    await pressEnter(page);
    await expectError(page, /First Name can NOT be empty/);
  });

  test('TC-UADD-004: Empty Last Name', async ({ page }) => {
    await page.fill(Selectors.userFirstName, 'TEST');
    // Leave Last Name empty
    await page.fill(Selectors.userIdInput, 'TESTID01');
    await page.fill(Selectors.userPassword, 'PASS1234');
    await page.fill(Selectors.userType, 'U');
    await pressEnter(page);
    await expectError(page, /Last Name can NOT be empty/);
  });

  test('TC-UADD-005: Empty User ID', async ({ page }) => {
    await page.fill(Selectors.userFirstName, 'TEST');
    await page.fill(Selectors.userLastName, 'USER');
    // Leave User ID empty
    await page.fill(Selectors.userPassword, 'PASS1234');
    await page.fill(Selectors.userType, 'U');
    await pressEnter(page);
    await expectError(page, /User ID can NOT be empty/);
  });

  test('TC-UADD-006: Empty Password', async ({ page }) => {
    await page.fill(Selectors.userFirstName, 'TEST');
    await page.fill(Selectors.userLastName, 'USER');
    await page.fill(Selectors.userIdInput, 'TESTID01');
    // Leave Password empty
    await page.fill(Selectors.userType, 'U');
    await pressEnter(page);
    await expectError(page, /Password can NOT be empty/);
  });

  test('TC-UADD-007: Empty User Type', async ({ page }) => {
    await page.fill(Selectors.userFirstName, 'TEST');
    await page.fill(Selectors.userLastName, 'USER');
    await page.fill(Selectors.userIdInput, 'TESTID01');
    await page.fill(Selectors.userPassword, 'PASS1234');
    // Leave User Type empty
    await pressEnter(page);
    await expectError(page, /User Type can NOT be empty/);
  });

  test('TC-UADD-008: Add admin user', async ({ page }) => {
    const adminUser = {
      ...Users.ephemeral,
      userId: `ADM${Date.now().toString().slice(-5)}`,
      type: 'A' as const,
    };
    await fillUserFields(page, adminUser);
    await pressEnter(page);
    await expectSuccess(page, /has been added/);
  });

  test('TC-UADD-009: Clear screen via F4', async ({ page }) => {
    await page.fill(Selectors.userFirstName, 'TEST');
    await page.fill(Selectors.userLastName, 'USER');
    await page.click(Selectors.btnF4Clear);
    await expect(page.locator(Selectors.userFirstName)).toHaveValue('');
    await expect(page.locator(Selectors.userLastName)).toHaveValue('');
  });

  test('TC-UADD-010: Return to Admin Menu via F3', async ({ page }) => {
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/admin/);
  });
});
