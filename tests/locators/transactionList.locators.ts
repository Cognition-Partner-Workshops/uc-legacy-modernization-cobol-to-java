// Derived from COTRN00.bms: TRNIDIN (search), PAGENUM, 10 rows of SEL/TRNID/TDATE/TDESC/TAMT
export const TransactionListLocators = {
  screenTitle: 'text=List Transactions',
  searchInput: '[data-testid="trnidin"], #trnidin, input[name="transactionId"]',
  pageNumber: '[data-testid="pagenum"], #pagenum',
  // 10 transaction rows
  transactionRows: {
    row1: { select: '#sel0001', tranId: '#trnid01', date: '#tdate01', desc: '#tdesc01', amount: '#tamt001' },
    row2: { select: '#sel0002', tranId: '#trnid02', date: '#tdate02', desc: '#tdesc02', amount: '#tamt002' },
    row3: { select: '#sel0003', tranId: '#trnid03', date: '#tdate03', desc: '#tdesc03', amount: '#tamt003' },
    row4: { select: '#sel0004', tranId: '#trnid04', date: '#tdate04', desc: '#tdesc04', amount: '#tamt004' },
    row5: { select: '#sel0005', tranId: '#trnid05', date: '#tdate05', desc: '#tdesc05', amount: '#tamt005' },
    row6: { select: '#sel0006', tranId: '#trnid06', date: '#tdate06', desc: '#tdesc06', amount: '#tamt006' },
    row7: { select: '#sel0007', tranId: '#trnid07', date: '#tdate07', desc: '#tdesc07', amount: '#tamt007' },
    row8: { select: '#sel0008', tranId: '#trnid08', date: '#tdate08', desc: '#tdesc08', amount: '#tamt008' },
    row9: { select: '#sel0009', tranId: '#trnid09', date: '#tdate09', desc: '#tdesc09', amount: '#tamt009' },
    row10: { select: '#sel0010', tranId: '#trnid10', date: '#tdate10', desc: '#tdesc10', amount: '#tamt010' },
  },
  errorMessage: '[data-testid="errmsg"], #errmsg',
  infoMessage: '[data-testid="infomsg"], #infomsg',
};
