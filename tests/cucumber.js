module.exports = {
  default: {
    requireModule: ['ts-node/register'],
    require: [
      'step-definitions/**/*.ts',
      'support/**/*.ts'
    ],
    paths: ['features/**/*.feature'],
    format: [
      'progress-bar',
      'json:reports/cucumber-report.json'
    ],
    formatOptions: { snippetInterface: 'async-await' },
    publishQuiet: true
  }
};
