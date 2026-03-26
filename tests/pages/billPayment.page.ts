import { Page } from 'playwright';
import { BillPaymentLocators } from '../locators/billPayment.locators';

export class BillPaymentPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(BillPaymentLocators.screenTitle).first().isVisible();
  }

  async enterAccountId(accountId: string): Promise<void> {
    await this.page.locator(BillPaymentLocators.accountInput).first().fill(accountId);
    await this.page.keyboard.press('Enter');
  }

  async getCurrentBalance(): Promise<string> {
    return (await this.page.locator(BillPaymentLocators.currentBalance).first().textContent()) || '';
  }

  async confirmPayment(value: string): Promise<void> {
    await this.page.locator(BillPaymentLocators.confirm).first().fill(value);
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(BillPaymentLocators.errorMessage).first().textContent()) || '';
  }
}
