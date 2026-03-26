import { Page } from 'playwright';
import { ReportLocators } from '../locators/report.locators';

export class ReportPage {
  constructor(private page: Page) {}

  async isScreenVisible(): Promise<boolean> {
    return this.page.locator(ReportLocators.screenTitle).first().isVisible();
  }

  async selectMonthlyReport(): Promise<void> {
    await this.page.locator(ReportLocators.monthlyOption).first().fill('X');
  }

  async selectYearlyReport(): Promise<void> {
    await this.page.locator(ReportLocators.yearlyOption).first().fill('X');
  }

  async selectCustomReport(): Promise<void> {
    await this.page.locator(ReportLocators.customOption).first().fill('X');
  }

  async enterStartDate(month: string, day: string, year: string): Promise<void> {
    await this.page.locator(ReportLocators.startDateMonth).first().fill(month);
    await this.page.locator(ReportLocators.startDateDay).first().fill(day);
    await this.page.locator(ReportLocators.startDateYear).first().fill(year);
  }

  async enterEndDate(month: string, day: string, year: string): Promise<void> {
    await this.page.locator(ReportLocators.endDateMonth).first().fill(month);
    await this.page.locator(ReportLocators.endDateDay).first().fill(day);
    await this.page.locator(ReportLocators.endDateYear).first().fill(year);
  }

  async confirmReport(value: string): Promise<void> {
    await this.page.locator(ReportLocators.confirm).first().fill(value);
    await this.page.keyboard.press('Enter');
  }

  async getErrorMessage(): Promise<string> {
    return (await this.page.locator(ReportLocators.errorMessage).first().textContent()) || '';
  }
}
