# CardDemo Playwright E2E Tests

Playwright + Cucumber BDD end-to-end test framework for the CardDemo credit card management application (COBOL-to-Java modernized).

## Prerequisites

- **Node.js** 18+ and **npm**
- A running instance of the CardDemo application (default: `http://localhost:8080`)

## Setup

```bash
cd tests
npm install
npx playwright install chromium
```

Copy `.env.example` to `.env` and configure the values:

```bash
cp .env.example .env
```

Edit `.env` to match your environment:

| Variable           | Description                        | Default             |
|--------------------|------------------------------------|---------------------|
| `BASE_URL`         | CardDemo application URL           | `http://localhost:8080` |
| `HEADED`           | Run browser in headed mode         | `false`             |
| `REGULAR_USER_ID`  | Regular user login ID              | `USER0001`          |
| `REGULAR_USER_PWD` | Regular user password              | `PASSWORD`          |
| `ADMIN_USER_ID`    | Admin user login ID                | `ADMIN001`          |
| `ADMIN_USER_PWD`   | Admin user password                | `PASSWORD`          |

## Running Tests

```bash
# Run all tests (headless)
npm test

# Run tests in headed mode (visible browser)
npm run test:headed

# Run tests with Playwright debugger
npm run test:debug

# Run a specific feature file
npx cucumber-js features/login.feature

# Run tests by tag
npx cucumber-js --tags @smoke
```

## Generating Reports

After running tests, generate an HTML report:

```bash
npm run report
```

The report will be available at `reports/html/index.html`.

## Type Checking

```bash
npm run lint
```

## Folder Structure

```
tests/
  features/              # Cucumber .feature files (Gherkin scenarios)
    login.feature
    mainMenu.feature
    accountView.feature
    ...
  step-definitions/      # Step definition implementations (TypeScript)
    login.steps.ts
    navigation.steps.ts
    account.steps.ts
    ...
  locators/              # Page element locators derived from BMS maps
    common.locators.ts
    login.locators.ts
    mainMenu.locators.ts
    ...
  pages/                 # Page Object Model classes
    login.page.ts
    mainMenu.page.ts
    accountView.page.ts
    ...
  support/               # Test framework support files
    config.ts            # Environment configuration
    world.ts             # Custom Cucumber World with Playwright
    hooks.ts             # Before/After hooks, screenshot on failure
  reports/               # Test output (gitignored)
    screenshots/         # Failure screenshots
    cucumber-report.json # Raw JSON report
    html/                # Generated HTML report
  package.json
  tsconfig.json
  cucumber.js            # Cucumber configuration
  .env.example           # Environment variable template
```

## Architecture

- **Cucumber BDD**: Business-readable Gherkin scenarios drive the tests
- **Playwright**: Browser automation for interacting with the modernized web UI
- **Page Object Model**: Locators and page interactions are encapsulated in page objects
- **BMS-Derived Locators**: All field selectors are derived from the original COBOL BMS map definitions with multiple fallback strategies (`data-testid`, `id`, `name`)
- **Screenshot on Failure**: Automatic screenshot capture when a scenario fails
