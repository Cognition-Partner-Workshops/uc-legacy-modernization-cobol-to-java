import { Page } from 'playwright';
import { LoginLocators } from '../locators/login.locators';

export class LoginPage {
  constructor(private page: Page) {}

  async navigate(): Promise<void> {
    await this.page.goto('/');
  }

  async login(userId: string, password: string): Promise<void> {
    await this.page.locator(LoginLocators.userId).first().fill(userId);
    await this.page.locator(LoginLocators.password).first().fill(password);
    await this.page.locator(LoginLocators.signOnButton).first().click();
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(LoginLocators.errorMessage).first().textContent()) || '';
  }

  async isLoginPageVisible(): Promise<boolean> {
    return this.page.locator(LoginLocators.userId).first().isVisible();
  }

  async isAppTitleVisible(): Promise<boolean> {
    return this.page.locator(LoginLocators.appTitle).first().isVisible();
  }

  async getUserIdValue(): Promise<string> {
    return (await this.page.locator(LoginLocators.userId).first().inputValue()) || '';
  }
}
