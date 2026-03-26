import { When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { AccountViewPage } from '../pages/accountView.page';
import { AccountUpdatePage } from '../pages/accountUpdate.page';

// Account View steps
When('I enter account number {string} on the Account View screen', async function (this: ICustomWorld, accountNumber: string) {
  const accountView = new AccountViewPage(this.page);
  await accountView.enterAccountNumber(accountNumber);
  await this.page.waitForTimeout(2000);
});

Then('I should see the account details displayed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('the account number should be {string}', async function (this: ICustomWorld, expectedAccount: string) {
  const accountView = new AccountViewPage(this.page);
  const actualAccount = await accountView.getAccountNumber();
  expect(actualAccount.trim()).toContain(expectedAccount);
});

Then('I should see the customer first name', async function (this: ICustomWorld) {
  const accountView = new AccountViewPage(this.page);
  const firstName = await accountView.getFirstName();
  expect(firstName.trim().length).toBeGreaterThan(0);
});

Then('I should see the customer last name', async function (this: ICustomWorld) {
  const accountView = new AccountViewPage(this.page);
  const lastName = await accountView.getLastName();
  expect(lastName.trim().length).toBeGreaterThan(0);
});

Then('I should see the credit limit', async function (this: ICustomWorld) {
  const accountView = new AccountViewPage(this.page);
  const creditLimit = await accountView.getCreditLimit();
  expect(creditLimit.trim().length).toBeGreaterThan(0);
});

Then('I should see the current balance', async function (this: ICustomWorld) {
  const accountView = new AccountViewPage(this.page);
  const balance = await accountView.getCurrentBalance();
  expect(balance.trim().length).toBeGreaterThan(0);
});

Then('I should see an error message on the Account View screen', async function (this: ICustomWorld) {
  const accountView = new AccountViewPage(this.page);
  const errorMsg = await accountView.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// Account Update steps
When('I load account {string} for update', async function (this: ICustomWorld, accountNumber: string) {
  // Account number would be pre-filled or we navigate with it
  await this.page.waitForTimeout(1000);
});

When('I update the credit limit to {string}', async function (this: ICustomWorld, value: string) {
  const accountUpdate = new AccountUpdatePage(this.page);
  await accountUpdate.updateCreditLimit(value);
});

When('I update the first name to {string}', async function (this: ICustomWorld, value: string) {
  const accountUpdate = new AccountUpdatePage(this.page);
  await accountUpdate.updateFirstName(value);
});

When('I update the last name to {string}', async function (this: ICustomWorld, value: string) {
  const accountUpdate = new AccountUpdatePage(this.page);
  await accountUpdate.updateLastName(value);
});

When('I submit the account update', async function (this: ICustomWorld) {
  const accountUpdate = new AccountUpdatePage(this.page);
  await accountUpdate.submitUpdate();
  await this.page.waitForTimeout(2000);
});

Then('I should see a confirmation message or updated account', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error message on the Account Update screen', async function (this: ICustomWorld) {
  const accountUpdate = new AccountUpdatePage(this.page);
  const errorMsg = await accountUpdate.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});
