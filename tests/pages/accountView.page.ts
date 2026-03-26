import { Page } from 'playwright';
import { AccountViewLocators } from '../locators/accountView.locators';

export class AccountViewPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(AccountViewLocators.screenTitle).first().isVisible();
  }

  async enterAccountNumber(accountNumber: string): Promise<void> {
    await this.page.locator(AccountViewLocators.accountNumber).first().fill(accountNumber);
    await this.page.keyboard.press('Enter');
  }

  async getAccountNumber(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.accountNumber).first().textContent()) || '';
  }

  async getActiveStatus(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.activeStatus).first().textContent()) || '';
  }

  async getCreditLimit(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.creditLimit).first().textContent()) || '';
  }

  async getCurrentBalance(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.currentBalance).first().textContent()) || '';
  }

  async getFirstName(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.firstName).first().textContent()) || '';
  }

  async getLastName(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.lastName).first().textContent()) || '';
  }

  async getCustomerId(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.customerId).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.errorMessage).first().textContent()) || '';
  }

  async getInfoMessage(): Promise<string> {
    return (await this.page.locator(AccountViewLocators.infoMessage).first().textContent()) || '';
  }
}
