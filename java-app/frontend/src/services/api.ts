const API_BASE = import.meta.env.VITE_API_URL || '';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });
  if (res.status === 401) {
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/app/login';
    }
    throw new Error('Not authenticated');
  }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(err.error || err.message || res.statusText);
  }
  if (res.status === 204) return {} as T;
  return res.json();
}

export const api = {
  // Auth
  login: (username: string, password: string) =>
    fetch(`${API_BASE}/api/login`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
    }).then(async (res) => {
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Login failed');
      return data;
    }),
  logout: () => request<{ status: string }>('/api/logout', { method: 'POST' }),
  me: () => request<{ authenticated: boolean; userId: string; isAdmin: boolean; role: string }>('/api/auth/me'),

  // Accounts
  getAccount: (acctId: number) => request<any>(`/api/accounts/${acctId}`),
  updateAccount: (acctId: number, data: any) =>
    request<any>(`/api/accounts/${acctId}`, { method: 'PUT', body: JSON.stringify(data) }),

  // Cards
  getCards: () => request<any[]>('/api/cards'),
  getCard: (cardNum: string) => request<any>(`/api/cards/${cardNum}`),
  updateCard: (cardNum: string, data: any) =>
    request<any>(`/api/cards/${cardNum}`, { method: 'PUT', body: JSON.stringify(data) }),

  // Transactions
  getTransactions: (page = 0, cardNum?: string) => {
    const params = new URLSearchParams({ page: String(page) });
    if (cardNum) params.set('cardNum', cardNum);
    return request<any>(`/api/transactions?${params}`);
  },
  getTransaction: (tranId: string) => request<any>(`/api/transactions/${tranId}`),
  addTransaction: (data: any) =>
    request<any>('/api/transactions', { method: 'POST', body: JSON.stringify(data) }),

  // Payments
  processPayment: (data: { acctId: number; amount: number }) =>
    request<{ status: string; message: string }>('/api/payments', { method: 'POST', body: JSON.stringify(data) }),

  // Reports
  getReport: (params: { acctId?: number; cardNum?: string; startDate?: string; endDate?: string }) => {
    const sp = new URLSearchParams();
    if (params.acctId) sp.set('acctId', String(params.acctId));
    if (params.cardNum) sp.set('cardNum', params.cardNum);
    if (params.startDate) sp.set('startDate', params.startDate);
    if (params.endDate) sp.set('endDate', params.endDate);
    return request<any>(`/api/reports?${sp}`);
  },

  // Users (admin)
  getUsers: () => request<any[]>('/api/admin/users'),
  getUser: (userId: string) => request<any>(`/api/admin/users/${userId}`),
  createUser: (data: any) =>
    request<any>('/api/admin/users', { method: 'POST', body: JSON.stringify(data) }),
  updateUser: (userId: string, data: any) =>
    request<any>(`/api/admin/users/${userId}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteUser: (userId: string) =>
    request<void>(`/api/admin/users/${userId}`, { method: 'DELETE' }),

  // Batch
  postTransactions: () => request<any>('/batch/post-transactions', { method: 'POST' }),
  calculateInterest: () => request<any>('/batch/calculate-interest', { method: 'POST' }),
  generateStatements: () => request<any>('/batch/generate-statements', { method: 'POST' }),
};
