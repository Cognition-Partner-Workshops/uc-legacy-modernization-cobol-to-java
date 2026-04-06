import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Account {
  id: string;
  name: string;
  accountType: string;
  industry: string;
  annualRevenue: number;
  employees: number;
  phoneOffice: string;
  phoneFax: string;
  website: string;
  rating: string;
  ownership: string;
  sicCode: string;
  tickerSymbol: string;
  billingAddressStreet: string;
  billingAddressCity: string;
  billingAddressState: string;
  billingAddressPostalCode: string;
  billingAddressCountry: string;
  shippingAddressStreet: string;
  shippingAddressCity: string;
  shippingAddressState: string;
  shippingAddressPostalCode: string;
  shippingAddressCountry: string;
  description: string;
  parentAccountId: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private apiUrl = `${environment.apiGatewayUrl}/api/v1/accounts`;

  constructor(private http: HttpClient) {}

  getAccounts(page = 0, size = 20, sort = 'name,asc'): Observable<PageResponse<Account>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<PageResponse<Account>>(this.apiUrl, { params });
  }

  getAccount(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/${id}`);
  }

  createAccount(account: Partial<Account>): Observable<Account> {
    return this.http.post<Account>(this.apiUrl, account);
  }

  updateAccount(id: string, account: Partial<Account>): Observable<Account> {
    return this.http.put<Account>(`${this.apiUrl}/${id}`, account);
  }

  deleteAccount(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchAccounts(query: string, page = 0, size = 20): Observable<PageResponse<Account>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Account>>(`${this.apiUrl}/search`, { params });
  }
}
