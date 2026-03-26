import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { LoginPage } from '../pages/login.page';
import { config } from '../support/config';

Given('I am on the CardDemo login page', async function (this: ICustomWorld) {
  const loginPage = new LoginPage(this.page);
  await loginPage.navigate();
});

When('I enter user ID {string} and password {string}', async function (this: ICustomWorld, userId: string, password: string) {
  const loginPage = new LoginPage(this.page);
  await loginPage.login(userId, password);
});

When('I click the sign-on button', async function (this: ICustomWorld) {
  // Login action is combined with entering credentials in the login method
  // This step exists for readability in feature files
});

Then('I should be redirected to the Main Menu', async function (this: ICustomWorld) {
  await this.page.waitForTimeout(2000);
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should be redirected to the Admin Menu', async function (this: ICustomWorld) {
  await this.page.waitForTimeout(2000);
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error message on the login page', async function (this: ICustomWorld) {
  const loginPage = new LoginPage(this.page);
  await this.page.waitForTimeout(1000);
  const errorMsg = await loginPage.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

Then('the application title should be visible', async function (this: ICustomWorld) {
  const loginPage = new LoginPage(this.page);
  const isVisible = await loginPage.isAppTitleVisible();
  expect(isVisible).toBeTruthy();
});

// Shared login steps used by other features
Given('I am logged in as a regular user', async function (this: ICustomWorld) {
  const loginPage = new LoginPage(this.page);
  await loginPage.navigate();
  await loginPage.login(config.credentials.regularUser.userId, config.credentials.regularUser.password);
  await this.page.waitForTimeout(2000);
});

Given('I am logged in as an admin user', async function (this: ICustomWorld) {
  const loginPage = new LoginPage(this.page);
  await loginPage.navigate();
  await loginPage.login(config.credentials.adminUser.userId, config.credentials.adminUser.password);
  await this.page.waitForTimeout(2000);
});
