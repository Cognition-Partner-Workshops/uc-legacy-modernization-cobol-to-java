export interface UserSecurity {
  secUsrId: string;
  secUsrFname: string | null;
  secUsrLname: string | null;
  secUsrPwd: string;
  secUsrType: string;
}

export interface LoginResponse {
  userId: string;
  userType: string;
  firstName: string;
  lastName: string;
}

export interface LoginError {
  error: string;
}

export interface Account {
  acctId: number;
  acctActiveStatus: string | null;
  acctCurrBal: number | null;
  acctCreditLimit: number | null;
  acctCashCreditLimit: number | null;
  acctOpenDate: string | null;
  acctExpiraionDate: string | null;
  acctReissueDate: string | null;
  acctCurrCycCredit: number | null;
  acctCurrCycDebit: number | null;
  acctAddrZip: string | null;
  acctGroupId: string | null;
}

export interface Card {
  cardNum: string;
  cardAcctId: number | null;
  cardCvvCd: number | null;
  cardEmbossedName: string | null;
  cardExpiraionDate: string | null;
  cardActiveStatus: string | null;
}

export interface CardXref {
  xrefCardNum: string;
  xrefCustId: number;
  xrefAcctId: number;
}

export interface Customer {
  custId: number;
  custFirstName: string | null;
  custMiddleName: string | null;
  custLastName: string | null;
  custAddrLine1: string | null;
  custAddrLine2: string | null;
  custAddrLine3: string | null;
  custAddrStateCd: string | null;
  custAddrCountryCd: string | null;
  custAddrZip: string | null;
  custPhoneNum1: string | null;
  custPhoneNum2: string | null;
  custSsn: string | null;
  custGovtIssuedId: string | null;
  custDobYyyyMmDd: string | null;
  custEftAccountId: string | null;
  custPriCardHolderInd: string | null;
  custFicoCreditScore: number | null;
}

export interface Transaction {
  tranId: string;
  tranTypeCd: string | null;
  tranCatCd: number | null;
  tranSource: string | null;
  tranDesc: string | null;
  tranAmt: number | null;
  tranMerchantId: number | null;
  tranMerchantName: string | null;
  tranMerchantCity: string | null;
  tranMerchantZip: string | null;
  tranCardNum: string | null;
  tranOrigTs: string | null;
  tranProcTs: string | null;
}
