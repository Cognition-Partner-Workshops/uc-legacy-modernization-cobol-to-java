// Derived from COCRDUP.bms: ACCTSID (PROT), CARDSID (UNPROT), CRDNAME (UNPROT),
// CRDSTCD (UNPROT), EXPMON (UNPROT), EXPYEAR (UNPROT), EXPDAY (DRK,PROT)
export const CardUpdateLocators = {
  screenTitle: 'text=Update Credit Card Details',
  accountNumber: '[data-testid="acctsid"], #acctsid',
  cardNumber: '[data-testid="cardsid"], #cardsid, input[name="cardNumber"]',
  cardName: '[data-testid="crdname"], #crdname, input[name="cardName"]',
  cardStatus: '[data-testid="crdstcd"], #crdstcd, input[name="cardStatus"]',
  expiryMonth: '[data-testid="expmon"], #expmon, input[name="expiryMonth"]',
  expiryYear: '[data-testid="expyear"], #expyear, input[name="expiryYear"]',
  expiryDay: '[data-testid="expday"], #expday',
  errorMessage: '[data-testid="errmsg"], #errmsg',
  infoMessage: '[data-testid="infomsg"], #infomsg',
  processHint: 'text=ENTER=Process',
  exitHint: 'text=F3=Exit',
  saveHint: 'text=F5=Save',
  cancelHint: 'text=F12=Cancel',
};
