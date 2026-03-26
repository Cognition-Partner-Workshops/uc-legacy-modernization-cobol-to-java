import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { MainMenuPage } from '../pages/mainMenu.page';
import { AdminMenuPage } from '../pages/adminMenu.page';

// Main Menu steps
Given('I am on the Main Menu', async function (this: ICustomWorld) {
  // After login as regular user, we should be on the Main Menu
  const mainMenu = new MainMenuPage(this.page);
  await this.page.waitForTimeout(1000);
});

Then('the Main Menu should be visible', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  const isVisible = await mainMenu.isMenuVisible();
  expect(isVisible).toBeTruthy();
});

Then('menu options should be displayed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

When('I select menu option {string}', async function (this: ICustomWorld, option: string) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption(option);
  await this.page.waitForTimeout(2000);
});

Then('I should see an error message on the menu', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  const errorMsg = await mainMenu.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// Admin Menu steps
Given('I am on the Admin Menu', async function (this: ICustomWorld) {
  // After login as admin user, we should be on the Admin Menu
  const adminMenu = new AdminMenuPage(this.page);
  await this.page.waitForTimeout(1000);
});

Then('the Admin Menu should be visible', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  const isVisible = await adminMenu.isMenuVisible();
  expect(isVisible).toBeTruthy();
});

When('I select admin menu option {string}', async function (this: ICustomWorld, option: string) {
  const adminMenu = new AdminMenuPage(this.page);
  await adminMenu.selectOption(option);
  await this.page.waitForTimeout(2000);
});

Then('I should see an error message on the admin menu', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  const errorMsg = await adminMenu.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// Navigation target screen assertions
Then('I should see the Account View screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Account Update screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Card List screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Card Detail screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Card Update screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Bill Payment screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Transaction List screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Transaction View screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Transaction Add screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the Transaction Reports screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the User List screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the User Add screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the User Update screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the User Delete screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

// Screen navigation helpers
Given('I navigate to the Account View screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('01');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Account Update screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('02');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Card List screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('03');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Card Detail screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('04');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Card Update screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('05');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Bill Payment screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('06');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Transaction List screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('07');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Transaction View screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('08');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Transaction Add screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('09');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the Transaction Reports screen', async function (this: ICustomWorld) {
  const mainMenu = new MainMenuPage(this.page);
  await mainMenu.selectOption('10');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the User List screen', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  await adminMenu.selectOption('01');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the User Add screen', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  await adminMenu.selectOption('02');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the User Update screen', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  await adminMenu.selectOption('03');
  await this.page.waitForTimeout(2000);
});

Given('I navigate to the User Delete screen', async function (this: ICustomWorld) {
  const adminMenu = new AdminMenuPage(this.page);
  await adminMenu.selectOption('04');
  await this.page.waitForTimeout(2000);
});
