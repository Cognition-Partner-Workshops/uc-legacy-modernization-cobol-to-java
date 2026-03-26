import { Page } from 'playwright';
import { TransactionAddLocators } from '../locators/transactionAdd.locators';

export class TransactionAddPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(TransactionAddLocators.screenTitle).first().isVisible();
  }

  async enterAccountId(accountId: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.accountInput).first().fill(accountId);
  }

  async enterCardNumber(cardNumber: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.cardInput).first().fill(cardNumber);
  }

  async enterTypeCode(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.typeCode).first().fill(value);
  }

  async enterCategoryCode(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.categoryCode).first().fill(value);
  }

  async enterSource(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.source).first().fill(value);
  }

  async enterDescription(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.description).first().fill(value);
  }

  async enterAmount(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.amount).first().fill(value);
  }

  async enterOriginDate(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.originDate).first().fill(value);
  }

  async enterProcessDate(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.processDate).first().fill(value);
  }

  async enterMerchantId(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.merchantId).first().fill(value);
  }

  async enterMerchantName(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.merchantName).first().fill(value);
  }

  async enterMerchantCity(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.merchantCity).first().fill(value);
  }

  async enterMerchantZip(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.merchantZip).first().fill(value);
  }

  async confirmTransaction(value: string): Promise<void> {
    await this.page.locator(TransactionAddLocators.confirm).first().fill(value);
    await this.page.keyboard.press('Enter');
  }

  async submitForm(): Promise<void> {
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(TransactionAddLocators.errorMessage).first().textContent()) || '';
  }
}
