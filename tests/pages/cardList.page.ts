import { Page } from 'playwright';
import { CardListLocators } from '../locators/cardList.locators';

export class CardListPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(CardListLocators.screenTitle).first().isVisible();
  }

  async filterByAccount(accountNumber: string): Promise<void> {
    await this.page.locator(CardListLocators.accountFilter).first().fill(accountNumber);
    await this.page.keyboard.press('Enter');
  }

  async filterByCard(cardNumber: string): Promise<void> {
    await this.page.locator(CardListLocators.cardFilter).first().fill(cardNumber);
    await this.page.keyboard.press('Enter');
  }

  async selectCardRow(rowNumber: number): Promise<void> {
    const key = `row${rowNumber}` as keyof typeof CardListLocators.cardRows;
    const row = CardListLocators.cardRows[key];
    if (row) {
      await this.page.locator(row.select).first().fill('S');
      await this.page.keyboard.press('Enter');
    }
  }

  async getCardRowData(rowNumber: number): Promise<{ account: string; cardNum: string; status: string }> {
    const key = `row${rowNumber}` as keyof typeof CardListLocators.cardRows;
    const row = CardListLocators.cardRows[key];
    return {
      account: row ? (await this.page.locator(row.account).first().textContent()) || '' : '',
      cardNum: row ? (await this.page.locator(row.cardNum).first().textContent()) || '' : '',
      status: row ? (await this.page.locator(row.status).first().textContent()) || '' : '',
    };
  }

  async getPageNumber(): Promise<string> {
    return (await this.page.locator(CardListLocators.pageNumber).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(CardListLocators.errorMessage).first().textContent()) || '';
  }
}
