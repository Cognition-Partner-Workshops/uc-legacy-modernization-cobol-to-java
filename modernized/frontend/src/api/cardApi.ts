import api from './authApi';

export interface Card {
  cardNum: string;
  acctId: string;
  cvvCd: number;
  embossedName: string;
  expirationDate: string;
  activeStatus: string;
  version: number;
}

export interface CardListItem {
  acctId: string;
  cardNum: string;
  activeStatus: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const cardApi = {
  listByAccount: async (acctId: string, page = 0, size = 7): Promise<PageResponse<CardListItem>> => {
    const response = await api.get<PageResponse<CardListItem>>('/cards', {
      params: { acctId, page, size },
    });
    return response.data;
  },
  getCard: async (cardNum: string): Promise<Card> => {
    const response = await api.get<Card>(`/cards/${cardNum}`);
    return response.data;
  },
  updateCard: async (cardNum: string, data: Record<string, unknown>): Promise<Card> => {
    const response = await api.put<Card>(`/cards/${cardNum}`, data);
    return response.data;
  },
};
