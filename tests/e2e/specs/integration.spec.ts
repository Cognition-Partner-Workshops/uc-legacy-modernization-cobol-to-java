import { test, expect } from '@playwright/test';
import { Users, Accounts, Cards, Transactions, Selectors } from '../fixtures/test-data';
import {
  login,
  loginAsRegularUser,
  loginAsAdmin,
  selectMenuOption,
  pressEnter,
  pressF3Exit,
  confirmAction,
  navigateToModule,
  navigateToAdminModule,
  expectError,
  expectSuccess,
} from '../fixtures/helpers';

/**
 * Module 23: Cross-Module Integration Tests
 *
 * End-to-end flows that span multiple modules to verify
 * data consistency and navigation integrity.
 */

test.describe('Cross-Module Integration', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test('TC-INTG-001: Login → View Account → View Transactions → Logout', async ({ page }) => {
    // Step 1: Login
    await loginAsRegularUser(page);
    await expect(page).toHaveURL(/\/menu/);

    // Step 2: Navigate to Account View
    await selectMenuOption(page, 1);
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    // Verify account details are shown
    await expect(page.locator(Selectors.acctIdInput)).toHaveValue(Accounts.active.id);

    // Step 3: Return to menu and go to Transaction List
    await pressF3Exit(page);
    await selectMenuOption(page, 6);
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);
    // Should see transaction rows
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();

    // Step 4: Logout
    await pressF3Exit(page);
    await pressF3Exit(page);
    await expect(page).toHaveURL(/\/login|\/signon/);
  });

  test('TC-INTG-002: Admin creates user → User logs in → Admin deletes user', async ({ page }) => {
    const uniqueId = `IT${Date.now().toString().slice(-6)}`;
    const newUser = {
      userId: uniqueId,
      password: 'TESTPW99',
      firstName: 'INTG',
      lastName: 'TEST',
      type: 'U' as const,
    };

    // Step 1: Admin creates user
    await loginAsAdmin(page);
    await selectMenuOption(page, 2); // User Add
    await page.fill(Selectors.userFirstName, newUser.firstName);
    await page.fill(Selectors.userLastName, newUser.lastName);
    await page.fill(Selectors.userIdInput, newUser.userId);
    await page.fill(Selectors.userPassword, newUser.password);
    await page.fill(Selectors.userType, newUser.type);
    await pressEnter(page);
    await expectSuccess(page, /has been added/);

    // Step 2: Logout admin
    await pressF3Exit(page);
    await pressF3Exit(page);

    // Step 3: Login as new user
    await login(page, newUser.userId, newUser.password);
    await expect(page).toHaveURL(/\/menu/);

    // Step 4: Logout new user
    await pressF3Exit(page);

    // Step 5: Admin deletes user
    await loginAsAdmin(page);
    await selectMenuOption(page, 4); // User Delete
    await page.fill(Selectors.userIdInput, newUser.userId);
    await pressEnter(page);
    await confirmAction(page, 'Y');
    await expectSuccess(page, /deleted|removed|success/i);
  });

  test('TC-INTG-003: Add transaction → View in transaction list', async ({ page }) => {
    await loginAsRegularUser(page);

    // Step 1: Add a transaction
    await selectMenuOption(page, 8); // Transaction Add
    await page.fill(Selectors.txnAcctId, Accounts.active.id);
    await pressEnter(page);

    const t = Transactions.validAdd;
    await page.fill(Selectors.txnTypeCd, t.typeCd);
    await page.fill(Selectors.txnCatCd, t.categoryCd);
    await page.fill(Selectors.txnSource, t.source);
    await page.fill(Selectors.txnDesc, 'INTG TEST TXN');
    await page.fill(Selectors.txnAmount, t.amount);
    await page.fill(Selectors.txnOrigDate, t.origDate);
    await page.fill(Selectors.txnProcDate, t.procDate);
    await page.fill(Selectors.txnMerchantId, t.merchantId);
    await page.fill(Selectors.txnMerchantName, t.merchantName);
    await page.fill(Selectors.txnMerchantCity, t.merchantCity);
    await page.fill(Selectors.txnMerchantZip, t.merchantZip);
    await confirmAction(page, 'Y');
    await expectSuccess(page, /added|success/i);

    // Step 2: Return to menu and check Transaction List
    await pressF3Exit(page);
    await selectMenuOption(page, 6); // Transaction List
    await page.fill(Selectors.acctIdInput, Accounts.active.id);
    await pressEnter(page);

    // There should be at least one transaction
    const rows = page.locator(Selectors.listRow);
    await expect(rows.first()).toBeVisible();
  });

  test('TC-INTG-004: Bill payment zeroes balance then denies second payment', async ({ page }) => {
    await loginAsRegularUser(page);

    // Step 1: Go to Bill Payment
    await selectMenuOption(page, 10); // Bill Payment
    await page.fill(Selectors.billAcctId, Accounts.active.id);
    await pressEnter(page);

    // Check if there is a balance to pay
    const balanceEl = page.locator(Selectors.billCurBalance);
    if (await balanceEl.isVisible()) {
      const balText = await balanceEl.textContent();
      const balance = parseFloat(balText?.replace(/[^0-9.-]/g, '') || '0');

      if (balance > 0) {
        // Step 2: Pay the balance
        await confirmAction(page, 'Y');
        await expectSuccess(page, /payment|processed|success/i);

        // Step 3: Try to pay again — should fail with nothing-to-pay
        await page.fill(Selectors.billAcctId, Accounts.active.id);
        await pressEnter(page);
        await expectError(page, /nothing to pay/i);
      }
    }
  });

  test('TC-INTG-005: Regular user cannot access Admin Menu', async ({ page }) => {
    await loginAsRegularUser(page);

    // Regular user Main Menu should NOT show admin option
    // Try navigating to admin URL directly
    await page.goto('/admin');
    // Should be redirected or shown error
    const isOnAdmin = page.url().includes('/admin');
    const hasError = await page.locator(Selectors.errorMessage).isVisible();
    // Either redirected away from admin or shown an error
    expect(!isOnAdmin || hasError).toBeTruthy();
  });
});
