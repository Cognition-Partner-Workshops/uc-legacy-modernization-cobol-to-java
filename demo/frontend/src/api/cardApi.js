import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

export const getCards = (accountId) => {
  const params = accountId ? { accountId } : {};
  return api.get('/cards', { params });
};

export const getCard = (cardNumber) => {
  return api.get(`/cards/${cardNumber}`);
};

export const updateCard = (cardNumber, data) => {
  return api.put(`/cards/${cardNumber}`, data);
};

export default api;
