import { Page } from 'playwright';
import { CardDetailLocators } from '../locators/cardDetail.locators';

export class CardDetailPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(CardDetailLocators.screenTitle).first().isVisible();
  }

  async enterAccountNumber(accountNumber: string): Promise<void> {
    await this.page.locator(CardDetailLocators.accountNumber).first().fill(accountNumber);
  }

  async enterCardNumber(cardNumber: string): Promise<void> {
    await this.page.locator(CardDetailLocators.cardNumber).first().fill(cardNumber);
    await this.page.keyboard.press('Enter');
  }

  async getCardName(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.cardName).first().textContent()) || '';
  }

  async getCardStatus(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.cardStatus).first().textContent()) || '';
  }

  async getExpiryMonth(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.expiryMonth).first().textContent()) || '';
  }

  async getExpiryYear(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.expiryYear).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.errorMessage).first().textContent()) || '';
  }

  async getInfoMessage(): Promise<string> {
    return (await this.page.locator(CardDetailLocators.infoMessage).first().textContent()) || '';
  }
}
