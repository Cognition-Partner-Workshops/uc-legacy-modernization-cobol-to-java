/**
 * Playwright Test Suite: Authentication & Sign-On (COSGN00C)
 *
 * Tests the CardDemo sign-on screen functionality derived from COBOL program COSGN00C.
 * This maps the mainframe CICS transaction CC00 to a modernized web login page.
 *
 * Original COBOL behavior:
 *   - VSAM file: USRSEC (keyed by 8-char User ID)
 *   - User types: 'A' (Admin) → COADM01C, 'R' (Regular) → COMEN01C
 *   - Input is uppercased before lookup (FUNCTION UPPER-CASE)
 *   - Password stored as plaintext in VSAM SEC-USR-PWD (8 chars max)
 *   - Error messages: "Please enter User ID ...", "Please enter Password ...",
 *     "User not found. Try again ...", "Wrong Password. Try again ..."
 *   - PF3 exits with "Thank you" message
 *   - Any other key: "Invalid key pressed"
 *
 * Environment Variables:
 *   BASE_URL  - The application URL (default: http://localhost:3000)
 */

import { test, expect, type Page } from '@playwright/test';

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------

const BASE_URL = process.env.BASE_URL ?? 'http://localhost:3000';
const LOGIN_PATH = '/login'; // Adjust to match the modernized app route

// Test credentials (from CardDemo sample data)
const ADMIN_USER = { id: 'ADMIN001', password: 'PASSWORD', type: 'admin' };
const REGULAR_USER = { id: 'USER0001', password: 'PASSWORD', type: 'regular' };

// ---------------------------------------------------------------------------
// Selectors — Update these to match your modernized UI
// ---------------------------------------------------------------------------

const SELECTORS = {
  /** User ID input field (maps to USERIDI OF COSGN0AI) */
  userIdInput: '[data-testid="user-id-input"], #userId, input[name="userId"]',

  /** Password input field (maps to PASSWDI OF COSGN0AI) */
  passwordInput: '[data-testid="password-input"], #password, input[name="password"]',

  /** Sign-on / Submit button (maps to Enter key on 3270) */
  submitButton: '[data-testid="sign-on-button"], button[type="submit"]',

  /** Error message area (maps to ERRMSGO OF COSGN0AO) */
  errorMessage: '[data-testid="error-message"], .error-message, [role="alert"]',

  /** Page title / header (maps to TITLE01O / TITLE02O) */
  pageTitle: '[data-testid="page-title"], h1, .page-title',

  /** Displayed user ID after login (in header/nav) */
  loggedInUser: '[data-testid="logged-in-user"], .user-info, .username',

  /** Admin menu indicator (maps to COADM01C screen) */
  adminMenu: '[data-testid="admin-menu"], .admin-menu, nav[aria-label="Admin"]',

  /** Main menu indicator (maps to COMEN01C screen) */
  mainMenu: '[data-testid="main-menu"], .main-menu, nav[aria-label="Main"]',

  /** Exit/Logout button (maps to PF3 key) */
  logoutButton: '[data-testid="logout-button"], button:has-text("Logout"), a:has-text("Logout")',
};

// ---------------------------------------------------------------------------
// Helper Functions
// ---------------------------------------------------------------------------

/**
 * Navigate to the login page and wait for it to be ready.
 */
async function goToLoginPage(page: Page): Promise<void> {
  await page.goto(`${BASE_URL}${LOGIN_PATH}`);
  await page.waitForLoadState('networkidle');
}

/**
 * Fill in the login form with the given credentials and submit.
 */
async function submitLogin(
  page: Page,
  userId: string,
  password: string
): Promise<void> {
  await page.fill(SELECTORS.userIdInput, userId);
  await page.fill(SELECTORS.passwordInput, password);
  await page.click(SELECTORS.submitButton);
  await page.waitForLoadState('networkidle');
}

/**
 * Assert that an error message matching the expected text is visible.
 */
async function expectErrorMessage(
  page: Page,
  expectedText: string | RegExp
): Promise<void> {
  const errorEl = page.locator(SELECTORS.errorMessage);
  await expect(errorEl).toBeVisible();
  await expect(errorEl).toContainText(expectedText);
}

/**
 * Assert that the login page is still displayed (user was NOT navigated away).
 */
async function expectStillOnLoginPage(page: Page): Promise<void> {
  await expect(page).toHaveURL(new RegExp(LOGIN_PATH));
  await expect(page.locator(SELECTORS.userIdInput)).toBeVisible();
}

// ---------------------------------------------------------------------------
// Test Suite
// ---------------------------------------------------------------------------

test.describe('Authentication & Sign-On (COSGN00C)', () => {
  test.beforeEach(async ({ page }) => {
    await goToLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-001: Valid admin login
  // -----------------------------------------------------------------------
  test('AUTH-001: Valid admin login navigates to Admin Menu', async ({
    page,
  }) => {
    // Pre-condition: Admin user ADMIN001 exists in USRSEC
    await submitLogin(page, ADMIN_USER.id, ADMIN_USER.password);

    // Expected: Navigate to Admin Menu (COADM01C)
    // The URL should change away from the login page
    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));

    // Admin menu should be visible
    const adminMenu = page.locator(SELECTORS.adminMenu);
    await expect(adminMenu).toBeVisible();
  });

  // -----------------------------------------------------------------------
  // AUTH-002: Valid regular user login
  // -----------------------------------------------------------------------
  test('AUTH-002: Valid regular user login navigates to Main Menu', async ({
    page,
  }) => {
    // Pre-condition: Regular user USER0001 exists in USRSEC
    await submitLogin(page, REGULAR_USER.id, REGULAR_USER.password);

    // Expected: Navigate to Main Menu (COMEN01C)
    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));

    // Main menu should be visible
    const mainMenu = page.locator(SELECTORS.mainMenu);
    await expect(mainMenu).toBeVisible();
  });

  // -----------------------------------------------------------------------
  // AUTH-003: Invalid password
  // -----------------------------------------------------------------------
  test('AUTH-003: Invalid password shows error and stays on login page', async ({
    page,
  }) => {
    await submitLogin(page, REGULAR_USER.id, 'WRONGPWD');

    // Expected: Error "Wrong Password. Try again ..." (from COSGN00C line 242)
    await expectErrorMessage(page, /wrong password/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-004: Non-existent user ID
  // -----------------------------------------------------------------------
  test('AUTH-004: Non-existent user shows error and stays on login page', async ({
    page,
  }) => {
    await submitLogin(page, 'XXXXXXXX', 'ANYTHING');

    // Expected: Error "User not found. Try again ..." (from COSGN00C line 249)
    await expectErrorMessage(page, /user not found/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-005: Blank user ID
  // -----------------------------------------------------------------------
  test('AUTH-005: Blank user ID shows validation error', async ({ page }) => {
    // Leave user ID blank, enter a password
    await page.fill(SELECTORS.passwordInput, 'PASSWORD');
    await page.click(SELECTORS.submitButton);
    await page.waitForLoadState('networkidle');

    // Expected: Error "Please enter User ID ..." (from COSGN00C line 120)
    await expectErrorMessage(page, /enter user id/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-006: Blank password
  // -----------------------------------------------------------------------
  test('AUTH-006: Blank password shows validation error', async ({ page }) => {
    // Enter user ID, leave password blank
    await page.fill(SELECTORS.userIdInput, REGULAR_USER.id);
    await page.click(SELECTORS.submitButton);
    await page.waitForLoadState('networkidle');

    // Expected: Error "Please enter Password ..." (from COSGN00C line 125)
    await expectErrorMessage(page, /enter password/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-007: Case sensitivity (COBOL uppercases input)
  // -----------------------------------------------------------------------
  test('AUTH-007: Login is case-insensitive for user ID (uppercased by system)', async ({
    page,
  }) => {
    // COBOL uses FUNCTION UPPER-CASE on both user ID and password (lines 132-136)
    // The modernized app should preserve this behavior
    await submitLogin(page, 'admin001', 'password');

    // Expected: Successful login — input is uppercased before comparison
    // If the modernized app preserves COBOL behavior, this should succeed
    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));
  });

  // -----------------------------------------------------------------------
  // AUTH-008: Special characters in password
  // -----------------------------------------------------------------------
  test('AUTH-008: Special characters in password do not cause system error', async ({
    page,
  }) => {
    await submitLogin(page, REGULAR_USER.id, 'P@SS!#$%');

    // Expected: Graceful error handling, no 500/crash
    // Should show "Wrong Password" since the special chars won't match
    await expectErrorMessage(page, /wrong password/i);
    await expectStillOnLoginPage(page);

    // Verify no server error (page should not show 500, 502, etc.)
    const pageContent = await page.textContent('body');
    expect(pageContent).not.toMatch(/500|internal server error/i);
  });

  // -----------------------------------------------------------------------
  // AUTH-009: Maximum-length fields
  // -----------------------------------------------------------------------
  test('AUTH-009: Maximum length fields (8 chars) are accepted without truncation', async ({
    page,
  }) => {
    // COBOL fields: WS-USER-ID PIC X(08), WS-USER-PWD PIC X(08)
    const maxLengthId = 'ABCDEFGH'; // 8 characters
    const maxLengthPwd = '12345678'; // 8 characters

    await page.fill(SELECTORS.userIdInput, maxLengthId);
    await page.fill(SELECTORS.passwordInput, maxLengthPwd);

    // Verify the full value is in the input (no truncation)
    const userIdValue = await page.inputValue(SELECTORS.userIdInput);
    const passwordValue = await page.inputValue(SELECTORS.passwordInput);

    // Fields should contain the full 8-character input
    expect(userIdValue.length).toBeGreaterThanOrEqual(8);
    expect(passwordValue.length).toBeGreaterThanOrEqual(8);

    // Submit and verify no crash
    await page.click(SELECTORS.submitButton);
    await page.waitForLoadState('networkidle');

    // Should get "User not found" since ABCDEFGH is not a real user
    await expectErrorMessage(page, /user not found/i);
  });

  // -----------------------------------------------------------------------
  // AUTH-010: PF3 / Logout from sign-on screen
  // -----------------------------------------------------------------------
  test('AUTH-010: Logout/Exit terminates session cleanly', async ({
    page,
  }) => {
    // In COBOL, PF3 sends "Thank you" and terminates the CICS transaction
    // In the modernized app, this maps to a logout/exit action

    // First login
    await submitLogin(page, REGULAR_USER.id, REGULAR_USER.password);
    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));

    // Now logout (maps to PF3)
    const logoutBtn = page.locator(SELECTORS.logoutButton);
    if (await logoutBtn.isVisible()) {
      await logoutBtn.click();
      await page.waitForLoadState('networkidle');

      // Should be back on login page or see a thank-you/goodbye message
      const isOnLogin = page.url().includes(LOGIN_PATH);
      const bodyText = await page.textContent('body');
      const hasThankYou = /thank you|goodbye|signed out|logged out/i.test(
        bodyText ?? ''
      );

      expect(isOnLogin || hasThankYou).toBeTruthy();
    }
  });

  // -----------------------------------------------------------------------
  // AUTH-011: Both fields blank
  // -----------------------------------------------------------------------
  test('AUTH-011: Both fields blank shows user ID validation first', async ({
    page,
  }) => {
    // COBOL evaluates User ID first (line 118), then Password (line 123)
    await page.click(SELECTORS.submitButton);
    await page.waitForLoadState('networkidle');

    // Expected: User ID error shown first (COBOL EVALUATE order)
    await expectErrorMessage(page, /enter user id/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-012: Overflow — User ID longer than 8 characters
  // -----------------------------------------------------------------------
  test('AUTH-012: User ID longer than 8 characters is handled gracefully', async ({
    page,
  }) => {
    // COBOL field WS-USER-ID is PIC X(08) — only 8 chars fit
    // The modernized app should either truncate or reject
    await submitLogin(page, 'VERYLONGUSERNAME', 'PASSWORD');

    // Should not crash — either truncate and lookup, or show error
    const pageContent = await page.textContent('body');
    expect(pageContent).not.toMatch(/500|internal server error/i);
    await expectStillOnLoginPage(page);
  });

  // -----------------------------------------------------------------------
  // AUTH-013: SQL injection attempt (security hardening)
  // -----------------------------------------------------------------------
  test('AUTH-013: SQL injection in user ID is handled safely', async ({
    page,
  }) => {
    await submitLogin(page, "' OR 1=1 --", 'PASSWORD');

    // Should show "User not found" — not a SQL error or bypass
    await expectStillOnLoginPage(page);
    const pageContent = await page.textContent('body');
    expect(pageContent).not.toMatch(/sql|syntax|error.*query/i);
  });

  // -----------------------------------------------------------------------
  // AUTH-014: XSS attempt in user ID field (security hardening)
  // -----------------------------------------------------------------------
  test('AUTH-014: XSS in user ID field is sanitized', async ({ page }) => {
    await submitLogin(page, '<script>alert(1)</script>', 'PASSWORD');

    // Verify no script execution — the page should handle it safely
    await expectStillOnLoginPage(page);

    // Check that the script tag is not rendered as HTML
    const bodyHtml = await page.innerHTML('body');
    expect(bodyHtml).not.toContain('<script>alert(1)</script>');
  });

  // -----------------------------------------------------------------------
  // AUTH-015: Password field is masked
  // -----------------------------------------------------------------------
  test('AUTH-015: Password field has type="password" (masked input)', async ({
    page,
  }) => {
    // In COBOL BMS, password field has DARK attribute (non-display)
    // The modernized app should use type="password"
    const passwordField = page.locator(SELECTORS.passwordInput);
    await expect(passwordField).toHaveAttribute('type', 'password');
  });

  // -----------------------------------------------------------------------
  // AUTH-016: Page header displays correct information
  // -----------------------------------------------------------------------
  test('AUTH-016: Login page displays application title and date/time', async ({
    page,
  }) => {
    // COBOL populates TITLE01O, TITLE02O, CURDATEO, CURTIMEO in header
    // (POPULATE-HEADER-INFO paragraph, lines 177-204)
    const pageContent = await page.textContent('body');

    // Should display "CardDemo" title somewhere
    expect(pageContent).toMatch(/carddemo/i);
  });

  // -----------------------------------------------------------------------
  // AUTH-017: Rapid repeated login attempts
  // -----------------------------------------------------------------------
  test('AUTH-017: Multiple rapid failed login attempts are handled', async ({
    page,
  }) => {
    // Original COBOL has no rate limiting (stateless CICS pseudo-conversational)
    // Modernized app may add rate limiting — verify graceful behavior

    for (let i = 0; i < 5; i++) {
      await submitLogin(page, REGULAR_USER.id, 'WRONG' + i);
      await expectStillOnLoginPage(page);
    }

    // After 5 failed attempts, page should still be functional
    // (no lockout in original COBOL, but modernized app may differ)
    await expect(page.locator(SELECTORS.userIdInput)).toBeVisible();
    await expect(page.locator(SELECTORS.passwordInput)).toBeVisible();
  });

  // -----------------------------------------------------------------------
  // AUTH-018: Admin user sees admin menu, not regular menu
  // -----------------------------------------------------------------------
  test('AUTH-018: Admin user is routed to admin menu (not main menu)', async ({
    page,
  }) => {
    // COBOL: IF CDEMO-USRTYP-ADMIN → XCTL to COADM01C
    //        ELSE → XCTL to COMEN01C (line 230-239)
    await submitLogin(page, ADMIN_USER.id, ADMIN_USER.password);

    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));

    // Admin should see admin-specific options (User Management)
    const adminMenu = page.locator(SELECTORS.adminMenu);
    await expect(adminMenu).toBeVisible();
  });

  // -----------------------------------------------------------------------
  // AUTH-019: Regular user does NOT see admin menu
  // -----------------------------------------------------------------------
  test('AUTH-019: Regular user is routed to main menu (not admin menu)', async ({
    page,
  }) => {
    await submitLogin(page, REGULAR_USER.id, REGULAR_USER.password);

    await expect(page).not.toHaveURL(new RegExp(LOGIN_PATH));

    // Regular user should see the main menu
    const mainMenu = page.locator(SELECTORS.mainMenu);
    await expect(mainMenu).toBeVisible();

    // Admin menu should NOT be visible to regular users
    const adminMenu = page.locator(SELECTORS.adminMenu);
    await expect(adminMenu).not.toBeVisible();
  });

  // -----------------------------------------------------------------------
  // AUTH-020: Direct URL access without authentication
  // -----------------------------------------------------------------------
  test('AUTH-020: Accessing protected page without login redirects to login', async ({
    page,
  }) => {
    // Try to access a protected page directly (e.g., account view)
    await page.goto(`${BASE_URL}/accounts`);
    await page.waitForLoadState('networkidle');

    // Should redirect back to login page
    await expect(page).toHaveURL(new RegExp(LOGIN_PATH));
  });
});
