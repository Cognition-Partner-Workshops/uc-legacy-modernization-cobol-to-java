// Derived from COUSR00.bms: USRIDIN (search), PAGENUM, 10 rows of SEL/USRID/FNAME/LNAME/UTYPE
export const UserListLocators = {
  screenTitle: 'text=List Users',
  searchInput: '[data-testid="usridin"], #usridin, input[name="userId"]',
  pageNumber: '[data-testid="pagenum"], #pagenum',
  // 10 user rows
  userRows: {
    row1: { select: '#sel0001', userId: '#usrid01', firstName: '#fname01', lastName: '#lname01', type: '#utype01' },
    row2: { select: '#sel0002', userId: '#usrid02', firstName: '#fname02', lastName: '#lname02', type: '#utype02' },
    row3: { select: '#sel0003', userId: '#usrid03', firstName: '#fname03', lastName: '#lname03', type: '#utype03' },
    row4: { select: '#sel0004', userId: '#usrid04', firstName: '#fname04', lastName: '#lname04', type: '#utype04' },
    row5: { select: '#sel0005', userId: '#usrid05', firstName: '#fname05', lastName: '#lname05', type: '#utype05' },
    row6: { select: '#sel0006', userId: '#usrid06', firstName: '#fname06', lastName: '#lname06', type: '#utype06' },
    row7: { select: '#sel0007', userId: '#usrid07', firstName: '#fname07', lastName: '#lname07', type: '#utype07' },
    row8: { select: '#sel0008', userId: '#usrid08', firstName: '#fname08', lastName: '#lname08', type: '#utype08' },
    row9: { select: '#sel0009', userId: '#usrid09', firstName: '#fname09', lastName: '#lname09', type: '#utype09' },
    row10: { select: '#sel0010', userId: '#usrid10', firstName: '#fname10', lastName: '#lname10', type: '#utype10' },
  },
  updateDeleteHint: "text=Type 'U' to Update or 'D' to Delete",
  errorMessage: '[data-testid="errmsg"], #errmsg',
  infoMessage: '[data-testid="infomsg"], #infomsg',
};
