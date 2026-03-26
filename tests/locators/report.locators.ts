// Derived from CORPT00.bms: MONTHLY, YEARLY, CUSTOM (radio-like selections),
// SDTMM/SDTDD/SDTYYYY (start date), EDTMM/EDTDD/EDTYYYY (end date), CONFIRM
export const ReportLocators = {
  screenTitle: 'text=Transaction Reports',
  monthlyOption: '[data-testid="monthly"], #monthly, input[name="monthly"]',
  yearlyOption: '[data-testid="yearly"], #yearly, input[name="yearly"]',
  customOption: '[data-testid="custom"], #custom, input[name="custom"]',
  startDateMonth: '[data-testid="sdtmm"], #sdtmm, input[name="startMonth"]',
  startDateDay: '[data-testid="sdtdd"], #sdtdd, input[name="startDay"]',
  startDateYear: '[data-testid="sdtyyyy"], #sdtyyyy, input[name="startYear"]',
  endDateMonth: '[data-testid="edtmm"], #edtmm, input[name="endMonth"]',
  endDateDay: '[data-testid="edtdd"], #edtdd, input[name="endDay"]',
  endDateYear: '[data-testid="edtyyyy"], #edtyyyy, input[name="endYear"]',
  confirmPrompt: 'text=The Report will be submitted for printing',
  confirm: '[data-testid="confirm"], #confirm, input[name="confirm"]',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  continueHint: 'text=ENTER=Continue',
  backHint: 'text=F3=Back',
};
