import { Page } from 'playwright';
import { AccountUpdateLocators } from '../locators/accountUpdate.locators';

export class AccountUpdatePage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(AccountUpdateLocators.screenTitle).first().isVisible();
  }

  async getAccountNumber(): Promise<string> {
    return (await this.page.locator(AccountUpdateLocators.accountNumber).first().textContent()) || '';
  }

  async updateCreditLimit(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.creditLimit).first().fill(value);
  }

  async updateFirstName(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.firstName).first().fill(value);
  }

  async updateLastName(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.lastName).first().fill(value);
  }

  async updateActiveStatus(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.activeStatus).first().fill(value);
  }

  async updateAddressLine1(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.addressLine1).first().fill(value);
  }

  async updateCity(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.city).first().fill(value);
  }

  async updateState(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.state).first().fill(value);
  }

  async updateZipCode(value: string): Promise<void> {
    await this.page.locator(AccountUpdateLocators.zipCode).first().fill(value);
  }

  async submitUpdate(): Promise<void> {
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(AccountUpdateLocators.errorMessage).first().textContent()) || '';
  }

  async getInfoMessage(): Promise<string> {
    return (await this.page.locator(AccountUpdateLocators.infoMessage).first().textContent()) || '';
  }
}
