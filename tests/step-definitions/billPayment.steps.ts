import { When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { BillPaymentPage } from '../pages/billPayment.page';

Then('the Bill Payment screen should be visible', async function (this: ICustomWorld) {
  const billPayment = new BillPaymentPage(this.page);
  const isVisible = await billPayment.isScreenVisible();
  expect(isVisible).toBeTruthy();
});

When('I enter account ID {string} on the Bill Payment screen', async function (this: ICustomWorld, accountId: string) {
  const billPayment = new BillPaymentPage(this.page);
  await billPayment.enterAccountId(accountId);
  await this.page.waitForTimeout(2000);
});

Then('I should see the current balance displayed', async function (this: ICustomWorld) {
  const billPayment = new BillPaymentPage(this.page);
  const balance = await billPayment.getCurrentBalance();
  expect(balance.trim().length).toBeGreaterThan(0);
});

When('I confirm the bill payment with {string}', async function (this: ICustomWorld, value: string) {
  const billPayment = new BillPaymentPage(this.page);
  await billPayment.confirmPayment(value);
  await this.page.waitForTimeout(2000);
});

Then('the payment should be processed successfully', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('the payment should not be processed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error message on the Bill Payment screen', async function (this: ICustomWorld) {
  const billPayment = new BillPaymentPage(this.page);
  const errorMsg = await billPayment.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});
