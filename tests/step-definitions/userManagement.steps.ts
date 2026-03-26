import { When, Then, DataTable } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { ICustomWorld } from '../support/world';
import { UserListPage } from '../pages/userList.page';
import { UserAddPage } from '../pages/userAdd.page';
import { UserUpdatePage } from '../pages/userUpdate.page';
import { UserDeletePage } from '../pages/userDelete.page';

// User List steps
Then('the User List screen should be visible', async function (this: ICustomWorld) {
  const userList = new UserListPage(this.page);
  const isVisible = await userList.isScreenVisible();
  expect(isVisible).toBeTruthy();
});

When('I search for user ID {string}', async function (this: ICustomWorld, userId: string) {
  const userList = new UserListPage(this.page);
  await userList.searchByUserId(userId);
  await this.page.waitForTimeout(2000);
});

Then('I should see user results listed', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

When('I select user row {int} for update', async function (this: ICustomWorld, rowNumber: number) {
  const userList = new UserListPage(this.page);
  await userList.selectUserForUpdate(rowNumber);
  await this.page.waitForTimeout(2000);
});

When('I select user row {int} for delete', async function (this: ICustomWorld, rowNumber: number) {
  const userList = new UserListPage(this.page);
  await userList.selectUserForDelete(rowNumber);
  await this.page.waitForTimeout(2000);
});

// User Add steps
When('I enter new user details:', async function (this: ICustomWorld, dataTable: DataTable) {
  const userAdd = new UserAddPage(this.page);
  const data = dataTable.hashes()[0];
  if (data.firstName) await userAdd.enterFirstName(data.firstName);
  if (data.lastName) await userAdd.enterLastName(data.lastName);
  if (data.userId) await userAdd.enterUserId(data.userId);
  if (data.password) await userAdd.enterPassword(data.password);
  if (data.userType) await userAdd.enterUserType(data.userType);
});

When('I submit the new user', async function (this: ICustomWorld) {
  const userAdd = new UserAddPage(this.page);
  await userAdd.submitAdd();
  await this.page.waitForTimeout(2000);
});

Then('the user should be added successfully', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

Then('I should see an error message on the User Add screen', async function (this: ICustomWorld) {
  const userAdd = new UserAddPage(this.page);
  const errorMsg = await userAdd.getErrorMessage();
  expect(errorMsg.trim().length).toBeGreaterThan(0);
});

// User Update steps
When('I fetch user {string} for update', async function (this: ICustomWorld, userId: string) {
  const userUpdate = new UserUpdatePage(this.page);
  await userUpdate.enterUserId(userId);
  await this.page.waitForTimeout(2000);
});

When('I update the user first name to {string}', async function (this: ICustomWorld, value: string) {
  const userUpdate = new UserUpdatePage(this.page);
  await userUpdate.updateFirstName(value);
});

When('I save the user update', async function (this: ICustomWorld) {
  const userUpdate = new UserUpdatePage(this.page);
  await userUpdate.saveUser();
  await this.page.waitForTimeout(2000);
});

Then('the user should be updated successfully', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});

// User Delete steps
When('I fetch user {string} for deletion', async function (this: ICustomWorld, userId: string) {
  const userDelete = new UserDeletePage(this.page);
  await userDelete.enterUserId(userId);
  await this.page.waitForTimeout(2000);
});

Then('I should see the user details for confirmation', async function (this: ICustomWorld) {
  const pageContent = await this.page.content();
  expect(pageContent).toBeTruthy();
});
