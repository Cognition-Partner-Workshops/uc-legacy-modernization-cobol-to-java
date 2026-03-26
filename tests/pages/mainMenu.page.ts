import { Page } from 'playwright';
import { MainMenuLocators } from '../locators/mainMenu.locators';

export class MainMenuPage {
  constructor(private page: Page) {}

  async isMenuVisible(): Promise<boolean> {
    return this.page.locator(MainMenuLocators.menuTitle).first().isVisible();
  }

  async selectOption(optionNumber: string): Promise<void> {
    await this.page.locator(MainMenuLocators.optionInput).first().fill(optionNumber);
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(MainMenuLocators.errorMessage).first().textContent()) || '';
  }

  async getMenuOptionText(optionNumber: number): Promise<string> {
    const key = `option${optionNumber}` as keyof typeof MainMenuLocators.menuOptions;
    const locator = MainMenuLocators.menuOptions[key];
    if (!locator) return '';
    return (await this.page.locator(locator).first().textContent()) || '';
  }
}
