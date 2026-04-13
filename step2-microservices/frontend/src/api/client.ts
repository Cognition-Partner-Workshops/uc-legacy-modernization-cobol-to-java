import axios from 'axios';
import type { Account, Card, CardXref, Customer, LoginResponse, Transaction, UserSecurity } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Auth Service
export const authApi = {
  login: (userId: string, password: string) =>
    api.post<LoginResponse>('/auth/login', { userId, password }),
  listUsers: () => api.get<UserSecurity[]>('/auth/users'),
  createUser: (user: Partial<UserSecurity>) => api.post<UserSecurity>('/auth/users', user),
  updateUser: (userId: string, user: Partial<UserSecurity>) =>
    api.put<UserSecurity>(`/auth/users/${userId}`, user),
  deleteUser: (userId: string) => api.delete(`/auth/users/${userId}`),
};

// Account Service
export const accountApi = {
  list: () => api.get<Account[]>('/accounts'),
  get: (id: number) => api.get<Account>(`/accounts/${id}`),
  create: (account: Partial<Account>) => api.post<Account>('/accounts', account),
  update: (id: number, account: Partial<Account>) =>
    api.put<Account>(`/accounts/${id}`, account),
};

// Card Service
export const cardApi = {
  list: () => api.get<Card[]>('/cards'),
  get: (cardNum: string) => api.get<Card>(`/cards/${cardNum}`),
  getByAccount: (acctId: number) => api.get<Card[]>(`/cards/account/${acctId}`),
  getXref: (cardNum: string) => api.get<CardXref>(`/cards/xref/${cardNum}`),
  create: (card: Partial<Card>) => api.post<Card>('/cards', card),
  update: (cardNum: string, card: Partial<Card>) => api.put<Card>(`/cards/${cardNum}`, card),
};

// Customer Service
export const customerApi = {
  list: () => api.get<Customer[]>('/customers'),
  get: (id: number) => api.get<Customer>(`/customers/${id}`),
  create: (customer: Partial<Customer>) => api.post<Customer>('/customers', customer),
  update: (id: number, customer: Partial<Customer>) =>
    api.put<Customer>(`/customers/${id}`, customer),
  delete: (id: number) => api.delete(`/customers/${id}`),
};

// Transaction Service
export const transactionApi = {
  list: () => api.get<Transaction[]>('/transactions'),
  get: (id: string) => api.get<Transaction>(`/transactions/${id}`),
  getByCard: (cardNum: string) => api.get<Transaction[]>(`/transactions/card/${cardNum}`),
  create: (txn: Partial<Transaction>) => api.post<Transaction>('/transactions', txn),
};

export default api;
