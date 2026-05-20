import api from './authApi';

export interface Account {
  acctId: string;
  activeStatus: string;
  currBal: number;
  creditLimit: number;
  cashCreditLimit: number;
  openDate: string;
  expirationDate: string;
  reissueDate: string;
  currCycCredit: number;
  currCycDebit: number;
  addrZip: string;
  groupId: string;
  version: number;
}

export interface UpdateAccountRequest {
  activeStatus?: string;
  creditLimit?: number;
  cashCreditLimit?: number;
  openDate?: string;
  expirationDate?: string;
  reissueDate?: string;
  groupId?: string;
  addrZip?: string;
  version: number;
}

export const accountApi = {
  getAccount: async (acctId: string): Promise<Account> => {
    const response = await api.get<Account>(`/accounts/${acctId}`);
    return response.data;
  },
  updateAccount: async (acctId: string, data: UpdateAccountRequest): Promise<Account> => {
    const response = await api.put<Account>(`/accounts/${acctId}`, data);
    return response.data;
  },
};
