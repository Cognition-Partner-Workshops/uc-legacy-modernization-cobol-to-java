import { Page } from 'playwright';
import { UserListLocators } from '../locators/userList.locators';

export class UserListPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(UserListLocators.screenTitle).first().isVisible();
  }

  async searchByUserId(userId: string): Promise<void> {
    await this.page.locator(UserListLocators.searchInput).first().fill(userId);
    await this.page.keyboard.press('Enter');
  }

  async selectUserForUpdate(rowNumber: number): Promise<void> {
    const key = `row${rowNumber}` as keyof typeof UserListLocators.userRows;
    const row = UserListLocators.userRows[key];
    if (row) {
      await this.page.locator(row.select).first().fill('U');
      await this.page.keyboard.press('Enter');
    }
  }

  async selectUserForDelete(rowNumber: number): Promise<void> {
    const key = `row${rowNumber}` as keyof typeof UserListLocators.userRows;
    const row = UserListLocators.userRows[key];
    if (row) {
      await this.page.locator(row.select).first().fill('D');
      await this.page.keyboard.press('Enter');
    }
  }

  async getUserRowData(rowNumber: number): Promise<{ userId: string; firstName: string; lastName: string; type: string }> {
    const key = `row${rowNumber}` as keyof typeof UserListLocators.userRows;
    const row = UserListLocators.userRows[key];
    return {
      userId: row ? (await this.page.locator(row.userId).first().textContent()) || '' : '',
      firstName: row ? (await this.page.locator(row.firstName).first().textContent()) || '' : '',
      lastName: row ? (await this.page.locator(row.lastName).first().textContent()) || '' : '',
      type: row ? (await this.page.locator(row.type).first().textContent()) || '' : '',
    };
  }

  async getPageNumber(): Promise<string> {
    return (await this.page.locator(UserListLocators.pageNumber).first().textContent()) || '';
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(UserListLocators.errorMessage).first().textContent()) || '';
  }
}
