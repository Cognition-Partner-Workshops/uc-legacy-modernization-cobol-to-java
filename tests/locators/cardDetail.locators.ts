// Derived from COCRDSL.bms: ACCTSID, CARDSID, CRDNAME, CRDSTCD, EXPMON, EXPYEAR
export const CardDetailLocators = {
  screenTitle: 'text=View Credit Card Detail',
  accountNumber: '[data-testid="acctsid"], #acctsid, input[name="accountNumber"]',
  cardNumber: '[data-testid="cardsid"], #cardsid, input[name="cardNumber"]',
  cardName: '[data-testid="crdname"], #crdname',
  cardStatus: '[data-testid="crdstcd"], #crdstcd',
  expiryMonth: '[data-testid="expmon"], #expmon',
  expiryYear: '[data-testid="expyear"], #expyear',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  infoMessage: '[data-testid="infomsg"], #infomsg',
  searchHint: 'text=ENTER=Search Cards',
  exitHint: 'text=F3=Exit',
};
