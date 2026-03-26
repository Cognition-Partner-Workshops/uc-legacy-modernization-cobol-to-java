// Derived from COUSR01.bms: FNAME, LNAME, USERID, PASSWD, USRTYPE
export const UserAddLocators = {
  screenTitle: 'text=Add User',
  firstName: '[data-testid="fname"], #fname, input[name="firstName"]',
  lastName: '[data-testid="lname"], #lname, input[name="lastName"]',
  userId: '[data-testid="userid"], #userid, input[name="userId"]',
  password: '[data-testid="passwd"], #passwd, input[name="password"]',
  userType: '[data-testid="usrtype"], #usrtype, input[name="userType"]',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  addUserHint: 'text=ENTER=Add User',
  backHint: 'text=F3=Back',
  clearHint: 'text=F4=Clear',
  exitHint: 'text=F12=Exit',
};
