// Derived from COUSR02.bms: USRIDIN (search input), FNAME, LNAME, PASSWD, USRTYPE
export const UserUpdateLocators = {
  screenTitle: 'text=Update User',
  userIdInput: '[data-testid="usridin"], #usridin, input[name="userId"]',
  firstName: '[data-testid="fname"], #fname, input[name="firstName"]',
  lastName: '[data-testid="lname"], #lname, input[name="lastName"]',
  password: '[data-testid="passwd"], #passwd, input[name="password"]',
  userType: '[data-testid="usrtype"], #usrtype, input[name="userType"]',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  fetchHint: 'text=ENTER=Fetch',
  saveExitHint: 'text=F3=Save',
  clearHint: 'text=F4=Clear',
  saveHint: 'text=F5=Save',
  cancelHint: 'text=F12=Cancel',
};
