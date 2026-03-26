import { Page } from 'playwright';
import { AdminMenuLocators } from '../locators/adminMenu.locators';

export class AdminMenuPage {
  constructor(private page: Page) {}

  async isMenuVisible(): Promise<boolean> {
    return this.page.locator(AdminMenuLocators.menuTitle).first().isVisible();
  }

  async selectOption(optionNumber: string): Promise<void> {
    await this.page.locator(AdminMenuLocators.optionInput).first().fill(optionNumber);
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(AdminMenuLocators.errorMessage).first().textContent()) || '';
  }

  async getMenuOptionText(optionNumber: number): Promise<string> {
    const key = `option${optionNumber}` as keyof typeof AdminMenuLocators.menuOptions;
    const locator = AdminMenuLocators.menuOptions[key];
    if (!locator) return '';
    return (await this.page.locator(locator).first().textContent()) || '';
  }
}
