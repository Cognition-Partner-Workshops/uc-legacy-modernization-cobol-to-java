import { test as setup } from '@playwright/test';
import { Users, Selectors } from '../fixtures/test-data';

/**
 * Authentication setup – runs once before all test projects.
 * Saves browser storage state so subsequent specs skip the login step.
 */
const authFile = 'playwright/.auth/user.json';

setup('authenticate as regular user', async ({ page }) => {
  await page.goto('/');
  await page.fill(Selectors.loginUserId, Users.regular.userId);
  await page.fill(Selectors.loginPassword, Users.regular.password);
  await page.click(Selectors.loginSubmit);
  await page.waitForURL(/\/(menu|admin|dashboard)/, { timeout: 10_000 });
  await page.context().storageState({ path: authFile });
});
