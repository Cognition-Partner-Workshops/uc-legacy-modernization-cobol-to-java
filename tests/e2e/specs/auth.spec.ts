import { test, expect } from '@playwright/test';
import { Users, Selectors } from '../fixtures/test-data';
import { login, expectError } from '../fixtures/helpers';

/**
 * Module 1: Authentication (COSGN00C)
 *
 * Business rules extracted from COBOL source:
 * - User ID and password are upper-cased before lookup
 * - User ID looked up against USRSEC VSAM KSDS
 * - RESP 0  + password match  → XCTL to menu (admin or regular)
 * - RESP 0  + password mismatch → "Wrong Password. Try again ..."
 * - RESP 13 (NOTFND)          → "User not found. Try again ..."
 * - Empty User ID             → "Please enter User ID ..."
 * - Empty Password             → "Please enter Password ..."
 * - F3                         → "Thank you" message and session end
 */

test.describe('Authentication (COSGN00C)', () => {
  // These tests need a fresh session — do NOT reuse stored auth state
  test.use({ storageState: { cookies: [], origins: [] } });

  test('TC-AUTH-001: Successful login as regular user', async ({ page }) => {
    await login(page, Users.regular.userId, Users.regular.password);
    // Regular users land on the Main Menu (COMEN01C)
    await expect(page).toHaveURL(/\/menu/);
    // Verify the 11 menu options are rendered
    const options = page.locator(Selectors.menuOption);
    await expect(options).toHaveCount(11);
  });

  test('TC-AUTH-002: Successful login as admin user', async ({ page }) => {
    await login(page, Users.admin.userId, Users.admin.password);
    // Admin users land on the Admin Menu (COADM01C)
    await expect(page).toHaveURL(/\/admin/);
    const options = page.locator(Selectors.menuOption);
    await expect(options).toHaveCount(6);
  });

  test('TC-AUTH-003: Login with empty User ID', async ({ page }) => {
    await page.goto('/');
    // Leave userId blank, fill password
    await page.fill(Selectors.loginPassword, 'PASSWORD');
    await page.click(Selectors.loginSubmit);
    await expectError(page, /Please enter User ID/);
  });

  test('TC-AUTH-004: Login with empty Password', async ({ page }) => {
    await page.goto('/');
    await page.fill(Selectors.loginUserId, Users.regular.userId);
    // Leave password blank
    await page.click(Selectors.loginSubmit);
    await expectError(page, /Please enter Password/);
  });

  test('TC-AUTH-005: Login with wrong password', async ({ page }) => {
    await page.goto('/');
    await page.fill(Selectors.loginUserId, Users.regular.userId);
    await page.fill(Selectors.loginPassword, 'WRONGPWD');
    await page.click(Selectors.loginSubmit);
    await expectError(page, /Wrong Password/);
  });

  test('TC-AUTH-006: Login with non-existent user', async ({ page }) => {
    await page.goto('/');
    await page.fill(Selectors.loginUserId, 'BADUSER1');
    await page.fill(Selectors.loginPassword, 'PASSWORD');
    await page.click(Selectors.loginSubmit);
    await expectError(page, /User not found/);
  });

  test('TC-AUTH-007: Case-insensitive User ID', async ({ page }) => {
    // COBOL applies FUNCTION UPPER-CASE to the input
    await login(page, 'user0001', Users.regular.password);
    await expect(page).toHaveURL(/\/menu/);
  });

  test('TC-AUTH-008: Logout via F3/Exit', async ({ page }) => {
    await login(page, Users.regular.userId, Users.regular.password);
    await page.click(Selectors.btnF3Exit);
    // Should return to sign-on screen
    await expect(page).toHaveURL(/\/(login|signon|\/)$/);
  });
});
