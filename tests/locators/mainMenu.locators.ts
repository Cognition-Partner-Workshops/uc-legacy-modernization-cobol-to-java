// Derived from COMEN01.bms: OPTION (POS 20,41, LENGTH 2, NUM), OPTN001-OPTN012, ERRMSG
export const MainMenuLocators = {
  menuTitle: 'text=Main Menu',
  optionInput: '[data-testid="option"], #option, input[name="option"]',
  menuOptions: {
    option1: '[data-testid="optn001"], #optn001',
    option2: '[data-testid="optn002"], #optn002',
    option3: '[data-testid="optn003"], #optn003',
    option4: '[data-testid="optn004"], #optn004',
    option5: '[data-testid="optn005"], #optn005',
    option6: '[data-testid="optn006"], #optn006',
    option7: '[data-testid="optn007"], #optn007',
    option8: '[data-testid="optn008"], #optn008',
    option9: '[data-testid="optn009"], #optn009',
    option10: '[data-testid="optn010"], #optn010',
    option11: '[data-testid="optn011"], #optn011',
    option12: '[data-testid="optn012"], #optn012',
  },
  errorMessage: '[data-testid="errmsg"], #errmsg',
  continueHint: 'text=ENTER=Continue',
  exitHint: 'text=F3=Exit',
};
