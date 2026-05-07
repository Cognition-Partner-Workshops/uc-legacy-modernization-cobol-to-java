import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Non-paged card list (backward compatible)
export const getCards = (accountId) => {
  const params = accountId ? { accountId } : {};
  return api.get('/cards', { params });
};

// Paged card list — equivalent to PF7/PF8 navigation in COCRDLIC.cbl
// COCRDLIC uses a 7-row page display; page/size params control pagination.
export const getCardsPaged = (accountId, page = 0, size = 7) => {
  const params = { page, size };
  if (accountId) params.accountId = accountId;
  return api.get('/cards', { params });
};

export const getCard = (cardNumber) => {
  return api.get(`/cards/${cardNumber}`);
};

// Composite key verification — equivalent to 9100-GETCARD-BYACCTCARD in COCRDSLC.cbl
export const verifyCardByAccount = (cardNumber, accountId) => {
  return api.get(`/cards/${cardNumber}/verify`, { params: { accountId } });
};

export const updateCard = (cardNumber, data) => {
  return api.put(`/cards/${cardNumber}`, data);
};

export default api;
