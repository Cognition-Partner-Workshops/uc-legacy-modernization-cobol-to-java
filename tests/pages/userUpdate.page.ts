import { Page } from 'playwright';
import { UserUpdateLocators } from '../locators/userUpdate.locators';

export class UserUpdatePage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(UserUpdateLocators.screenTitle).first().isVisible();
  }

  async enterUserId(userId: string): Promise<void> {
    await this.page.locator(UserUpdateLocators.userIdInput).first().fill(userId);
    await this.page.keyboard.press('Enter');
  }

  async updateFirstName(value: string): Promise<void> {
    await this.page.locator(UserUpdateLocators.firstName).first().fill(value);
  }

  async updateLastName(value: string): Promise<void> {
    await this.page.locator(UserUpdateLocators.lastName).first().fill(value);
  }

  async updatePassword(value: string): Promise<void> {
    await this.page.locator(UserUpdateLocators.password).first().fill(value);
  }

  async updateUserType(value: string): Promise<void> {
    await this.page.locator(UserUpdateLocators.userType).first().fill(value);
  }

  async saveUser(): Promise<void> {
    await this.page.keyboard.press('F5');
  }

  async saveAndExit(): Promise<void> {
    await this.page.keyboard.press('F3');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(UserUpdateLocators.errorMessage).first().textContent()) || '';
  }
}
