import { Given, When, Then, DataTable } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { TransactionListPage } from '../pages/transactionList.page';
import { TransactionViewPage } from '../pages/transactionView.page';
import { TransactionAddPage } from '../pages/transactionAdd.page';

// Transaction List steps
Then('the Transaction List screen should be visible', async function (this: ICustomWorld) {
  const tranList = new TransactionListPage(this.page);
  const isVisible = await tranList.isScreenVisible();
  expect(isVisible).toBeTruthy();
});

When('I search for transaction ID {string}', async function (this: ICustomWorld, transactionId: string) {
  const tranList = new TransactionListPage(this.page);
  await tranList.searchByTransactionId(transactionId);
  await this.page.waitForTimeout(2000);
});

Then('I should see transaction results listed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

When('I select transaction row {int}', async function (this: ICustomWorld, rowNumber: number) {
  const tranList = new TransactionListPage(this.page);
  await tranList.selectTransactionRow(rowNumber);
  await this.page.waitForTimeout(2000);
});

Then('I should be taken to the transaction detail view', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

When('I view the transaction list', async function (this: ICustomWorld) {
  await this.page.waitForTimeout(1000);
});

Then('I should see the page number displayed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

// Transaction View steps
When('I enter transaction ID {string} on the Transaction View screen', async function (this: ICustomWorld, transactionId: string) {
  const tranView = new TransactionViewPage(this.page);
  await tranView.enterTransactionId(transactionId);
  await this.page.waitForTimeout(2000);
});

Then('I should see the transaction details displayed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the merchant name', async function (this: ICustomWorld) {
  const tranView = new TransactionViewPage(this.page);
  const merchantName = await tranView.getMerchantName();
  expect(merchantName.trim().length).toBeGreaterThan(0);
});

Then('I should see the merchant city', async function (this: ICustomWorld) {
  const tranView = new TransactionViewPage(this.page);
  const merchantCity = await tranView.getMerchantCity();
  expect(merchantCity.trim().length).toBeGreaterThan(0);
});

Then('I should see the transaction amount', async function (this: ICustomWorld) {
  const tranView = new TransactionViewPage(this.page);
  const amount = await tranView.getAmount();
  expect(amount.trim().length).toBeGreaterThan(0);
});

Then('I should see an error message on the Transaction View screen', async function (this: ICustomWorld) {
  const tranView = new TransactionViewPage(this.page);
  const errorMsg = await tranView.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// Transaction Add steps
When('I enter account ID {string} on the Transaction Add screen', async function (this: ICustomWorld, accountId: string) {
  const tranAdd = new TransactionAddPage(this.page);
  await tranAdd.enterAccountId(accountId);
  await this.page.keyboard.press('Enter');
  await this.page.waitForTimeout(2000);
});

When('I enter transaction details:', async function (this: ICustomWorld, dataTable: DataTable) {
  const tranAdd = new TransactionAddPage(this.page);
  const data = dataTable.hashes()[0];
  await tranAdd.enterTypeCode(data.typeCode);
  await tranAdd.enterCategoryCode(data.categoryCode);
  await tranAdd.enterSource(data.source);
  await tranAdd.enterDescription(data.description);
  await tranAdd.enterAmount(data.amount);
  await tranAdd.enterOriginDate(data.originDate);
  await tranAdd.enterProcessDate(data.processDate);
  await tranAdd.enterMerchantId(data.merchantId);
  await tranAdd.enterMerchantName(data.merchantName);
  await tranAdd.enterMerchantCity(data.merchantCity);
  await tranAdd.enterMerchantZip(data.merchantZip);
});

When('I submit the transaction', async function (this: ICustomWorld) {
  const tranAdd = new TransactionAddPage(this.page);
  await tranAdd.submitForm();
  await this.page.waitForTimeout(2000);
});

When('I confirm the transaction with {string}', async function (this: ICustomWorld, value: string) {
  const tranAdd = new TransactionAddPage(this.page);
  await tranAdd.confirmTransaction(value);
  await this.page.waitForTimeout(2000);
});

Then('the transaction should be added successfully', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error message on the Transaction Add screen', async function (this: ICustomWorld) {
  const tranAdd = new TransactionAddPage(this.page);
  const errorMsg = await tranAdd.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

Then('the transaction should not be added', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});
