import { test, expect } from '@playwright/test';
import { Cards, Selectors } from '../fixtures/test-data';
import {
  loginAsRegularUser,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  confirmAction,
  expectError,
  expectSuccess,
} from '../fixtures/helpers';

/**
 * Module 8: Credit Card Update (COCRDUPC)
 *
 * Business rules from COBOL source:
 * - Reads CARDDAT for update (CICS READ UPDATE)
 * - Confirm Y → REWRITE card record
 * - Confirm N → discard
 * - Invalid confirm → "Invalid value. Valid values are (Y/N)..."
 * - Non-existent card → not-found error
 */

test.describe('Credit Card Update (COCRDUPC)', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page }) => {
    await loginAsRegularUser(page);
    await selectMenuOption(page, 5); // Option 5 = Credit Card Update
  });

  test('TC-CUPD-001: Display card for update', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);
    await expect(page.locator(Selectors.cardStatus)).toBeVisible();
  });

  test('TC-CUPD-002: Update card status', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);

    await page.fill(Selectors.cardStatus, 'Y');
    await confirmAction(page, 'Y');
    await expectSuccess(page, /updated|success/i);
  });

  test('TC-CUPD-003: Cancel card update', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);
    await confirmAction(page, 'N');
    await expect(page.locator(Selectors.successMessage)).not.toBeVisible();
  });

  test('TC-CUPD-004: Update with invalid confirm value', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.active.number);
    await pressEnter(page);
    await confirmAction(page, 'X');
    await expectError(page, /Invalid value.*Valid values are \(Y\/N\)/);
  });

  test('TC-CUPD-005: Update non-existent card', async ({ page }) => {
    await page.fill(Selectors.cardNumberInput, Cards.nonExistent.number);
    await pressEnter(page);
    await expectError(page, /not found|Did not find/i);
  });
});
