import { Page } from 'playwright';
import { TransactionViewLocators } from '../locators/transactionView.locators';

export class TransactionViewPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(TransactionViewLocators.screenTitle).first().isVisible();
  }

  async enterTransactionId(transactionId: string): Promise<void> {
    await this.page.locator(TransactionViewLocators.searchInput).first().fill(transactionId);
    await this.page.keyboard.press('Enter');
  }

  async getTransactionId(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.transactionId).first().textContent()) || '';
  }

  async getCardNumber(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.cardNumber).first().textContent()) || '';
  }

  async getTypeCode(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.typeCode).first().textContent()) || '';
  }

  async getCategoryCode(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.categoryCode).first().textContent()) || '';
  }

  async getDescription(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.description).first().textContent()) || '';
  }

  async getAmount(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.amount).first().textContent()) || '';
  }

  async getMerchantName(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.merchantName).first().textContent()) || '';
  }

  async getMerchantCity(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.merchantCity).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(TransactionViewLocators.errorMessage).first().textContent()) || '';
  }
}
