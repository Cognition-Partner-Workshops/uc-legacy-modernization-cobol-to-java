import { Page } from 'playwright';
import { UserAddLocators } from '../locators/userAdd.locators';

export class UserAddPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(UserAddLocators.screenTitle).first().isVisible();
  }

  async enterFirstName(value: string): Promise<void> {
    await this.page.locator(UserAddLocators.firstName).first().fill(value);
  }

  async enterLastName(value: string): Promise<void> {
    await this.page.locator(UserAddLocators.lastName).first().fill(value);
  }

  async enterUserId(value: string): Promise<void> {
    await this.page.locator(UserAddLocators.userId).first().fill(value);
  }

  async enterPassword(value: string): Promise<void> {
    await this.page.locator(UserAddLocators.password).first().fill(value);
  }

  async enterUserType(value: string): Promise<void> {
    await this.page.locator(UserAddLocators.userType).first().fill(value);
  }

  async submitAdd(): Promise<void> {
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(UserAddLocators.errorMessage).first().textContent()) || '';
  }
}
