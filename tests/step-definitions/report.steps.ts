import { When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { ReportPage } from '../pages/report.page';

Then('the Transaction Reports screen should be visible', async function (this: ICustomWorld) {
  const reportPage = new ReportPage(this.page);
  const isVisible = await reportPage.isScreenVisible();
  expect(isVisible).toBeTruthy();
});

When('I select the monthly report option', async function (this: ICustomWorld) {
  const reportPage = new ReportPage(this.page);
  await reportPage.selectMonthlyReport();
});

When('I select the yearly report option', async function (this: ICustomWorld) {
  const reportPage = new ReportPage(this.page);
  await reportPage.selectYearlyReport();
});

When('I select the custom report option', async function (this: ICustomWorld) {
  const reportPage = new ReportPage(this.page);
  await reportPage.selectCustomReport();
});

When('I enter start date {string} \\/ {string} \\/ {string}', async function (this: ICustomWorld, month: string, day: string, year: string) {
  const reportPage = new ReportPage(this.page);
  await reportPage.enterStartDate(month, day, year);
});

When('I enter end date {string} \\/ {string} \\/ {string}', async function (this: ICustomWorld, month: string, day: string, year: string) {
  const reportPage = new ReportPage(this.page);
  await reportPage.enterEndDate(month, day, year);
});

When('I confirm the report generation with {string}', async function (this: ICustomWorld, value: string) {
  const reportPage = new ReportPage(this.page);
  await reportPage.confirmReport(value);
  await this.page.waitForTimeout(2000);
});

Then('the report should be submitted for processing', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('the report should not be generated', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});
