// Derived from COSGN00.bms fields:
// USERID (POS 19,43, LENGTH 8), PASSWD (POS 20,43, LENGTH 8, DRK), ERRMSG (POS 23,1)
// APPLID (POS 3,8), SYSID (POS 3,71)
export const LoginLocators = {
  userId: '[data-testid="userid"], #userid, input[name="userId"]',
  password: '[data-testid="passwd"], #passwd, input[name="password"]',
  signOnButton: 'button[type="submit"], [data-testid="sign-on"]',
  errorMessage: '[data-testid="errmsg"], #errmsg, .error-message',
  appTitle: 'text=Credit Card Demo Application',
  appDescription: 'text=This is a Credit Card Demo Application for Mainframe Modernization',
  exitHint: 'text=F3=Exit',
  applId: '[data-testid="applid"], #applid',
  sysId: '[data-testid="sysid"], #sysid',
};
