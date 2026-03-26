// Derived from COBIL00.bms: ACTIDIN (account input), CURBAL (current balance),
// CONFIRM (Y/N confirmation)
export const BillPaymentLocators = {
  screenTitle: 'text=Bill Payment',
  accountInput: '[data-testid="actidin"], #actidin, input[name="accountId"]',
  currentBalance: '[data-testid="curbal"], #curbal',
  confirmPrompt: 'text=Do you want to pay your balance now',
  confirm: '[data-testid="confirm"], #confirm, input[name="confirm"]',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  continueHint: 'text=ENTER=Continue',
  backHint: 'text=F3=Back',
  clearHint: 'text=F4=Clear',
};
