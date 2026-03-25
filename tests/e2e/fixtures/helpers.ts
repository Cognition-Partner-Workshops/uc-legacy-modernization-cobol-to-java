import { type Page, expect } from '@playwright/test';
import { Users, Selectors } from './test-data';

/**
 * Shared helper functions for CardDemo Playwright tests.
 * These encapsulate common workflows so individual specs stay concise.
 */

/** Log in as a given user and wait for the post-login page to load. */
export async function login(
  page: Page,
  userId: string,
  password: string,
): Promise<void> {
  await page.goto('/');
  await page.fill(Selectors.loginUserId, userId);
  await page.fill(Selectors.loginPassword, password);
  await page.click(Selectors.loginSubmit);
  // Wait for navigation away from the login page
  await page.waitForURL(/\/(menu|admin)/, { timeout: 10_000 });
}

/** Log in as the default regular user. */
export async function loginAsRegularUser(page: Page): Promise<void> {
  await login(page, Users.regular.userId, Users.regular.password);
}

/** Log in as the default admin user. */
export async function loginAsAdmin(page: Page): Promise<void> {
  await login(page, Users.admin.userId, Users.admin.password);
}

/** Select a numbered menu option and wait for the target screen. */
export async function selectMenuOption(
  page: Page,
  optionNumber: number,
): Promise<void> {
  await page.fill(Selectors.menuOptionInput, String(optionNumber));
  await page.click(Selectors.btnEnter);
  // Allow time for the target screen to render
  await page.waitForLoadState('networkidle');
}

/** Press F3 / Exit and wait for navigation. */
export async function pressF3Exit(page: Page): Promise<void> {
  await page.click(Selectors.btnF3Exit);
  await page.waitForLoadState('networkidle');
}

/** Press F4 / Clear. */
export async function pressF4Clear(page: Page): Promise<void> {
  await page.click(Selectors.btnF4Clear);
}

/** Press F7 / Previous Page. */
export async function pressF7Prev(page: Page): Promise<void> {
  await page.click(Selectors.btnF7Prev);
  await page.waitForLoadState('networkidle');
}

/** Press F8 / Next Page. */
export async function pressF8Next(page: Page): Promise<void> {
  await page.click(Selectors.btnF8Next);
  await page.waitForLoadState('networkidle');
}

/** Press Enter. */
export async function pressEnter(page: Page): Promise<void> {
  await page.click(Selectors.btnEnter);
  await page.waitForLoadState('networkidle');
}

/** Assert that an error message matching `text` is visible. */
export async function expectError(page: Page, text: string | RegExp): Promise<void> {
  const msg = page.locator(Selectors.errorMessage);
  await expect(msg).toBeVisible();
  await expect(msg).toContainText(text);
}

/** Assert that a success message matching `text` is visible. */
export async function expectSuccess(page: Page, text: string | RegExp): Promise<void> {
  const msg = page.locator(Selectors.successMessage);
  await expect(msg).toBeVisible();
  await expect(msg).toContainText(text);
}

/** Fill the confirm field and press Enter. */
export async function confirmAction(
  page: Page,
  value: 'Y' | 'N' | string,
): Promise<void> {
  await page.fill(Selectors.confirmField, value);
  await pressEnter(page);
}

/**
 * Navigate to a specific module from the Main Menu.
 * Assumes the user is already logged in as a regular user and on the menu.
 */
export async function navigateToModule(
  page: Page,
  optionNumber: number,
): Promise<void> {
  await selectMenuOption(page, optionNumber);
}

/**
 * Navigate to a specific admin module from the Admin Menu.
 * Assumes the user is already logged in as admin and on the admin menu.
 */
export async function navigateToAdminModule(
  page: Page,
  optionNumber: number,
): Promise<void> {
  await selectMenuOption(page, optionNumber);
}
