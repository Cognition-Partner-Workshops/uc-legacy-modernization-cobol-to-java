import { Page } from 'playwright';
import { TransactionListLocators } from '../locators/transactionList.locators';

export class TransactionListPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(TransactionListLocators.screenTitle).first().isVisible();
  }

  async searchByTransactionId(transactionId: string): Promise<void> {
    await this.page.locator(TransactionListLocators.searchInput).first().fill(transactionId);
    await this.page.keyboard.press('Enter');
  }

  async selectTransactionRow(rowNumber: number): Promise<void> {
    const key = `row${rowNumber}` as keyof typeof TransactionListLocators.transactionRows;
    const row = TransactionListLocators.transactionRows[key];
    if (row) {
      await this.page.locator(row.select).first().fill('S');
      await this.page.keyboard.press('Enter');
    }
  }

  async getTransactionRowData(rowNumber: number): Promise<{ tranId: string; date: string; desc: string; amount: string }> {
    const key = `row${rowNumber}` as keyof typeof TransactionListLocators.transactionRows;
    const row = TransactionListLocators.transactionRows[key];
    return {
      tranId: row ? (await this.page.locator(row.tranId).first().textContent()) || '' : '',
      date: row ? (await this.page.locator(row.date).first().textContent()) || '' : '',
      desc: row ? (await this.page.locator(row.desc).first().textContent()) || '' : '',
      amount: row ? (await this.page.locator(row.amount).first().textContent()) || '' : '',
    };
  }

  async getPageNumber(): Promise<string> {
    return (await this.page.locator(TransactionListLocators.pageNumber).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(TransactionListLocators.errorMessage).first().textContent()) || '';
  }

  async navigateNextPage(): Promise<void> {
    await this.page.keyboard.press('F8');
  }

  async navigatePreviousPage(): Promise<void> {
    await this.page.keyboard.press('F7');
  }
}
