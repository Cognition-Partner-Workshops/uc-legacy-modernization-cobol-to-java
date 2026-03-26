import { Before, After, BeforeAll, AfterAll, Status } from '@cucumber/cucumber';
import { ICustomWorld } from './world';
import * as fs from 'fs';

// Ensure reports directory exists
BeforeAll(async function () {
  if (!fs.existsSync('reports/screenshots')) {
    fs.mkdirSync('reports/screenshots', { recursive: true });
  }
});

Before(async function (this: ICustomWorld) {
  await this.init();
});

After(async function (this: ICustomWorld, scenario) {
  if (scenario.result?.status === Status.FAILED) {
    const screenshotName = scenario.pickle.name.replace(/[^a-zA-Z0-9]/g, '_');
    const screenshot = await this.page.screenshot({
      path: `reports/screenshots/${screenshotName}_${Date.now()}.png`,
      fullPage: true,
    });
    this.attach(screenshot, 'image/png');
  }
  await this.teardown();
});

// Tagged hooks for login
Before({ tags: '@loggedInRegular' }, async function (this: ICustomWorld) {
  // Navigate and login as regular user - will be handled by step definitions
});

Before({ tags: '@loggedInAdmin' }, async function (this: ICustomWorld) {
  // Navigate and login as admin user - will be handled by step definitions
});
