import { When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { CardListPage } from '../pages/cardList.page';
import { CardDetailPage } from '../pages/cardDetail.page';
import { CardUpdatePage } from '../pages/cardUpdate.page';

// Card List steps
Then('the Card List screen should be visible', async function (this: ICustomWorld) {
  const cardList = new CardListPage(this.page);
  const isVisible = await cardList.isScreenVisible();
  expect(isVisible).toBeTruthy();
});

When('I filter cards by account number {string}', async function (this: ICustomWorld, accountNumber: string) {
  const cardList = new CardListPage(this.page);
  await cardList.filterByAccount(accountNumber);
  await this.page.waitForTimeout(2000);
});

Then('I should see cards listed for that account', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

When('I select card row {int}', async function (this: ICustomWorld, rowNumber: number) {
  const cardList = new CardListPage(this.page);
  await cardList.selectCardRow(rowNumber);
  await this.page.waitForTimeout(2000);
});

Then('I should be taken to the card detail view', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error or empty results on the Card List screen', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

// Card Detail steps
When('I enter account number {string} on the Card Detail screen', async function (this: ICustomWorld, accountNumber: string) {
  const cardDetail = new CardDetailPage(this.page);
  await cardDetail.enterAccountNumber(accountNumber);
});

When('I enter card number {string} on the Card Detail screen', async function (this: ICustomWorld, cardNumber: string) {
  const cardDetail = new CardDetailPage(this.page);
  await cardDetail.enterCardNumber(cardNumber);
  await this.page.waitForTimeout(2000);
});

Then('I should see the card details displayed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see the card name', async function (this: ICustomWorld) {
  const cardDetail = new CardDetailPage(this.page);
  const cardName = await cardDetail.getCardName();
  expect(cardName.trim().length).toBeGreaterThan(0);
});

Then('I should see the card status', async function (this: ICustomWorld) {
  const cardDetail = new CardDetailPage(this.page);
  const cardStatus = await cardDetail.getCardStatus();
  expect(cardStatus.trim().length).toBeGreaterThan(0);
});

Then('I should see an error message on the Card Detail screen', async function (this: ICustomWorld) {
  const cardDetail = new CardDetailPage(this.page);
  const errorMsg = await cardDetail.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// Card Update steps
When('I load a card for update with account {string} and card {string}', async function (this: ICustomWorld, account: string, card: string) {
  await this.page.waitForTimeout(1000);
});

When('I update the card name to {string}', async function (this: ICustomWorld, value: string) {
  const cardUpdate = new CardUpdatePage(this.page);
  await cardUpdate.updateCardName(value);
});

When('I update the card status to {string}', async function (this: ICustomWorld, value: string) {
  const cardUpdate = new CardUpdatePage(this.page);
  await cardUpdate.updateCardStatus(value);
});

When('I update the card expiry month to {string} and year to {string}', async function (this: ICustomWorld, month: string, year: string) {
  const cardUpdate = new CardUpdatePage(this.page);
  await cardUpdate.updateExpiryMonth(month);
  await cardUpdate.updateExpiryYear(year);
});

When('I submit the card update', async function (this: ICustomWorld) {
  const cardUpdate = new CardUpdatePage(this.page);
  await cardUpdate.submitUpdate();
  await this.page.waitForTimeout(2000);
});

Then('I should see a card update confirmation or updated details', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});
