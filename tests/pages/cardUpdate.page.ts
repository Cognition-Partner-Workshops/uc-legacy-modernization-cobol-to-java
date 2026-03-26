import { Page } from 'playwright';
import { CardUpdateLocators } from '../locators/cardUpdate.locators';

export class CardUpdatePage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(CardUpdateLocators.screenTitle).first().isVisible();
  }

  async getAccountNumber(): Promise<string> {
    return (await this.page.locator(CardUpdateLocators.accountNumber).first().textContent()) || '';
  }

  async updateCardName(value: string): Promise<void> {
    await this.page.locator(CardUpdateLocators.cardName).first().fill(value);
  }

  async updateCardStatus(value: string): Promise<void> {
    await this.page.locator(CardUpdateLocators.cardStatus).first().fill(value);
  }

  async updateExpiryMonth(value: string): Promise<void> {
    await this.page.locator(CardUpdateLocators.expiryMonth).first().fill(value);
  }

  async updateExpiryYear(value: string): Promise<void> {
    await this.page.locator(CardUpdateLocators.expiryYear).first().fill(value);
  }

  async submitUpdate(): Promise<void> {
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(CardUpdateLocators.errorMessage).first().textContent()) || '';
  }

  async getInfoMessage(): Promise<string> {
    return (await this.page.locator(CardUpdateLocators.infoMessage).first().textContent()) || '';
  }
}
