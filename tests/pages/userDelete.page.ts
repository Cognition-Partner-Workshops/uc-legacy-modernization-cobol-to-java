import { Page } from 'playwright';
import { UserDeleteLocators } from '../locators/userDelete.locators';

export class UserDeletePage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(UserDeleteLocators.screenTitle).first().isVisible();
  }

  async enterUserId(userId: string): Promise<void> {
    await this.page.locator(UserDeleteLocators.userIdInput).first().fill(userId);
    await this.page.keyboard.press('Enter');
  }

  async getFirstName(): Promise<string> {
    return (await this.page.locator(UserDeleteLocators.firstName).first().textContent()) || '';
  }

  async getLastName(): Promise<string> {
    return (await this.page.locator(UserDeleteLocators.lastName).first().textContent()) || '';
  }

  async getUserType(): Promise<string> {
    return (await this.page.locator(UserDeleteLocators.userType).first().textContent()) || '';
  }

  async confirmDelete(): Promise<void> {
    await this.page.keyboard.press('F5');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(UserDeleteLocators.errorMessage).first().textContent()) || '';
  }
}
