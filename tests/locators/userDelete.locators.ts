// Derived from COUSR03.bms: USRIDIN (search input), FNAME (ASKIP), LNAME (ASKIP), USRTYPE (ASKIP)
export const UserDeleteLocators = {
  screenTitle: 'text=Delete User',
  userIdInput: '[data-testid="usridin"], #usridin, input[name="userId"]',
  firstName: '[data-testid="fname"], #fname',
  lastName: '[data-testid="lname"], #lname',
  userType: '[data-testid="usrtype"], #usrtype',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  fetchHint: 'text=ENTER=Fetch',
  backHint: 'text=F3=Back',
  clearHint: 'text=F4=Clear',
  deleteHint: 'text=F5=Delete',
};
