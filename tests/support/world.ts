import { setWorldConstructor, World, IWorldOptions } from '@cucumber/cucumber';
import { Browser, BrowserContext, Page, chromium } from 'playwright';
import { config } from './config';

export interface ICustomWorld extends World {
  browser: Browser;
  context: BrowserContext;
  page: Page;
  init(): Promise<void>;
  teardown(): Promise<void>;
}

export class CustomWorld extends World implements ICustomWorld {
  browser!: Browser;
  context!: BrowserContext;
  page!: Page;

  constructor(options: IWorldOptions) {
    super(options);
  }

  async init(): Promise<void> {
    this.browser = await chromium.launch({ headless: config.headless });
    this.context = await this.browser.newContext({
      baseURL: config.baseURL,
      viewport: { width: 1280, height: 720 },
    });
    this.page = await this.context.newPage();
    this.page.setDefaultTimeout(config.defaultTimeout);
  }

  async teardown(): Promise<void> {
    await this.page?.close();
    await this.context?.close();
    await this.browser?.close();
  }
}

setWorldConstructor(CustomWorld);
