import api from './authApi';
import { PageResponse } from './cardApi';

export interface Transaction {
  tranId: string;
  typeCd: string;
  catCd: number;
  source: string;
  description: string;
  amount: number;
  merchantId: string;
  merchantName: string;
  merchantCity: string;
  merchantZip: string;
  cardNum: string;
  origTs: string;
  procTs: string;
}

export interface AddTransactionRequest {
  cardNum: string;
  typeCd: string;
  catCd?: number;
  source?: string;
  description?: string;
  amount: number;
  merchantId?: string;
  merchantName?: string;
  merchantCity?: string;
  merchantZip?: string;
}

export const transactionApi = {
  listByCard: async (cardNum: string, page = 0, size = 20): Promise<PageResponse<Transaction>> => {
    const response = await api.get<PageResponse<Transaction>>('/transactions', {
      params: { cardNum, page, size },
    });
    return response.data;
  },
  getTransaction: async (tranId: string): Promise<Transaction> => {
    const response = await api.get<Transaction>(`/transactions/${tranId}`);
    return response.data;
  },
  addTransaction: async (data: AddTransactionRequest): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transactions', data);
    return response.data;
  },
  billPayment: async (acctId: string): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transactions/bill-payment', { acctId });
    return response.data;
  },
};
