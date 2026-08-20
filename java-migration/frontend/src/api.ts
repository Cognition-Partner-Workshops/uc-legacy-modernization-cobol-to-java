export type Role = "ROLE_ADMIN" | "ROLE_USER";

export type Context = {
  fromProgram?: string;
  toProgram?: string;
  fromTransaction?: string;
  toTransaction?: string;
  userId?: string;
  userType?: string;
  accountId?: number;
  cardNumber?: string;
};

export type ApiResponse<T> = {
  data: T | null;
  message: string;
  errorField: string;
  nextRoute: string;
  context: Context | null;
};

export type MenuOption = {
  number: number;
  name: string;
  program: string;
  userType: string;
};

export type MenuData = { options: MenuOption[] };
export type SignonData = { token: string; userId: string; role: Role };
export type TransactionPage = {
  transactions: Record<string, unknown>[];
  page: number;
  pageSize: number;
  nextPage: boolean;
  previousPage: boolean;
};
export type CardPage = {
  cards: Record<string, unknown>[];
  page: number;
  pageSize: number;
  nextPage: boolean;
  previousPage: boolean;
};
export type UserPage = {
  users: Record<string, unknown>[];
  page: number;
  pageSize: number;
  nextPage: boolean;
  previousPage: boolean;
};

export type ExtensionPage = {
  rows: Record<string, unknown>[];
  page?: number;
  pageSize?: number;
  nextPage?: boolean;
  previousPage?: boolean;
};

let token: string | null = null;
let authFailureHandler: (() => void) | null = null;
const TOKEN_KEY = "carddemo.jwt";

export function restoreToken(): string | null {
  token = sessionStorage.getItem(TOKEN_KEY);
  return token;
}

export function setToken(value: string | null): void {
  token = value;
  if (value) sessionStorage.setItem(TOKEN_KEY, value);
  else sessionStorage.removeItem(TOKEN_KEY);
}

export function getToken(): string | null {
  return token;
}

export function setAuthFailureHandler(handler: (() => void) | null): void {
  authFailureHandler = handler;
}

async function request<T>(
  path: string,
  init: RequestInit = {},
): Promise<ApiResponse<T>> {
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const response = await fetch(path, { ...init, headers });
  if (response.status === 401) {
    setToken(null);
    authFailureHandler?.();
    throw new Error("Unauthorized");
  }
  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok) throw new Error(body.message || response.statusText);
  return body;
}

const json = (value: unknown): RequestInit => ({
  method: "POST",
  body: JSON.stringify(value),
});

export const api = {
  signon: (value: { userId: string; password: string }) =>
    request<SignonData>("/api/signon", json(value)),
  menu: (context?: Context) =>
    request<MenuData>("/api/menu", json({ context })),
  adminMenu: (context?: Context) =>
    request<MenuData>("/api/admin/menu", json({ context })),
  account: (accountId: string, context?: Context) =>
    request<Record<string, unknown>>(
      `/api/accounts/${accountId}?fromProgram=${encodeURIComponent(context?.fromProgram ?? "")}`,
    ),
  updateAccount: (accountId: string, value: Record<string, unknown>) =>
    request<Record<string, unknown>>(`/api/accounts/${accountId}`, {
      method: "PUT",
      body: JSON.stringify(value),
    }),
  cards: (params: { accountId?: string; cardNumber?: string; page?: number; direction?: string; context?: Context }) =>
    request<CardPage>(
      `/api/cards?${new URLSearchParams(
        Object.entries(params)
          .filter(([key, value]) => key !== "context" && value !== undefined)
          .map(([key, value]) => [key, String(value)]),
      )}`,
    ),
  card: (cardNumber: string) =>
    request<Record<string, unknown>>(`/api/cards/${cardNumber}`),
  updateCard: (cardNumber: string, value: Record<string, unknown>) =>
    request<Record<string, unknown>>(`/api/cards/${cardNumber}`, {
      method: "PUT",
      body: JSON.stringify(value),
    }),
  transactions: (params: Record<string, unknown>) =>
    request<TransactionPage>(
      `/api/transactions?${new URLSearchParams(
        Object.entries(params)
          .filter(([key, value]) => key !== "context" && value !== undefined)
          .map(([key, value]) => [key, String(value)]),
      )}`,
    ),
  transaction: (id: string) => request<Record<string, unknown>>(`/api/transactions/${id}`),
  addTransaction: (value: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/transactions", json(value)),
  billPayment: (value: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/bill-payments", json(value)),
  report: (value: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/reports", json(value)),
  users: (page = 0) =>
    request<UserPage>(`/api/admin/users?page=${page}`),
  addUser: (value: Record<string, unknown>) =>
    request<Record<string, unknown>>("/api/admin/users", json(value)),
  updateUser: (id: string, value: Record<string, unknown>) =>
    request<Record<string, unknown>>(`/api/admin/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(value),
    }),
  deleteUser: (id: string, value: Record<string, unknown>) =>
    request<null>(`/api/admin/users/${id}`, { ...json(value), method: "DELETE" }),
  authorizations: (accountId: string, page = 0) =>
    request<Record<string, unknown>[]>(`/api/authorizations/summary/${accountId}?page=${page}`),
  authorization: (id: string) =>
    request<Record<string, unknown>>(`/api/authorizations/detail/${id}`),
  markAuthorizationFraud: (id: string, marked: boolean) =>
    request<string>(`/api/authorizations/detail/${id}/fraud?marked=${marked}`, {
      method: "POST",
    }),
  transactionTypes: (page = 0, direction = "FORWARD") =>
    request<Record<string, unknown>[]>(
      `/api/admin/transaction-types?page=${page}&direction=${direction}`,
    ),
  saveTransactionType: (value: Record<string, unknown>, remove = false) =>
    request<Record<string, unknown> | null>(
      `/api/admin/transaction-types?delete=${remove}`,
      json(value),
    ),
};
