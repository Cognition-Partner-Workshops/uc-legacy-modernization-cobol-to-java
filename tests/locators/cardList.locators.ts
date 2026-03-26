// Derived from COCRDLI.bms: ACCTSID, CARDSID, PAGENO, 7 rows of CRDSEL/ACCTNO/CRDNUM/CRDSTS
export const CardListLocators = {
  screenTitle: 'text=List Credit Cards',
  accountFilter: '[data-testid="acctsid"], #acctsid, input[name="accountNumber"]',
  cardFilter: '[data-testid="cardsid"], #cardsid, input[name="cardNumber"]',
  pageNumber: '[data-testid="pageno"], #pageno',
  // 7 card rows
  cardRows: {
    row1: { select: '#crdsel1', account: '#acctno1', cardNum: '#crdnum1', status: '#crdsts1' },
    row2: { select: '#crdsel2', account: '#acctno2', cardNum: '#crdnum2', status: '#crdsts2' },
    row3: { select: '#crdsel3', account: '#acctno3', cardNum: '#crdnum3', status: '#crdsts3' },
    row4: { select: '#crdsel4', account: '#acctno4', cardNum: '#crdnum4', status: '#crdsts4' },
    row5: { select: '#crdsel5', account: '#acctno5', cardNum: '#crdnum5', status: '#crdsts5' },
    row6: { select: '#crdsel6', account: '#acctno6', cardNum: '#crdnum6', status: '#crdsts6' },
    row7: { select: '#crdsel7', account: '#acctno7', cardNum: '#crdnum7', status: '#crdsts7' },
  },
  errorMessage: '[data-testid="errmsg"], #errmsg',
  infoMessage: '[data-testid="infomsg"], #infomsg',
};
