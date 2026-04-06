import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Quote {
  id: string;
  name: string;
  quoteNum: string;
  quoteStage: string;
  paymentTerms: string;
  validUntil: string;
  subtotalAmount: number;
  totalAmount: number;
  accountId: string;
  contactId: string;
  assignedUserId: string;
  dateEntered: string;
}

export interface Invoice {
  id: string;
  name: string;
  invoiceNumber: string;
  quoteId: string;
  status: string;
  dueDate: string;
  totalAmount: number;
  accountId: string;
  dateEntered: string;
}

export interface Contract {
  id: string;
  name: string;
  referenceCode: string;
  status: string;
  contractType: string;
  startDate: string;
  endDate: string;
  totalContractValue: number;
  accountId: string;
  dateEntered: string;
}

@Injectable({ providedIn: 'root' })
export class QuoteService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  getQuotes(page = 0, size = 20): Observable<any> { return this.http.get(`${this.baseUrl}/quotes?page=${page}&size=${size}`); }
  getQuoteById(id: string): Observable<Quote> { return this.http.get<Quote>(`${this.baseUrl}/quotes/${id}`); }
  createQuote(quote: Partial<Quote>): Observable<Quote> { return this.http.post<Quote>(`${this.baseUrl}/quotes`, quote); }
  deleteQuote(id: string): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/quotes/${id}`); }
  getInvoices(page = 0, size = 20): Observable<any> { return this.http.get(`${this.baseUrl}/invoices?page=${page}&size=${size}`); }
  getContracts(page = 0, size = 20): Observable<any> { return this.http.get(`${this.baseUrl}/contracts?page=${page}&size=${size}`); }
}
