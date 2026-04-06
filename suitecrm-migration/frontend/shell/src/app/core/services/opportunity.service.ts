import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Opportunity {
  id: string;
  name: string;
  amount: number;
  salesStage: string;
  probability: number;
  dateClosed: string;
  description: string;
  leadSource: string;
  nextStep: string;
  opportunityType: string;
  accountId: string;
  accountName: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface Quote {
  id: string;
  name: string;
  quoteStage: string;
  total: number;
  validUntil: string;
  opportunityId: string;
  accountId: string;
}

export interface Invoice {
  id: string;
  name: string;
  status: string;
  total: number;
  dueDate: string;
  amountDue: number;
  accountId: string;
}

@Injectable({ providedIn: 'root' })
export class OpportunityService {
  private oppUrl = `${environment.apiGatewayUrl}/api/v1/opportunities`;
  private quoteUrl = `${environment.apiGatewayUrl}/api/v1/quotes`;
  private invoiceUrl = `${environment.apiGatewayUrl}/api/v1/invoices`;

  constructor(private http: HttpClient) {}

  getOpportunities(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.oppUrl, { params });
  }

  getOpportunity(id: string): Observable<Opportunity> {
    return this.http.get<Opportunity>(`${this.oppUrl}/${id}`);
  }

  createOpportunity(opp: Partial<Opportunity>): Observable<Opportunity> {
    return this.http.post<Opportunity>(this.oppUrl, opp);
  }

  updateOpportunity(id: string, opp: Partial<Opportunity>): Observable<Opportunity> {
    return this.http.put<Opportunity>(`${this.oppUrl}/${id}`, opp);
  }

  deleteOpportunity(id: string): Observable<void> {
    return this.http.delete<void>(`${this.oppUrl}/${id}`);
  }

  getQuotes(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.quoteUrl, { params });
  }

  getQuote(id: string): Observable<Quote> {
    return this.http.get<Quote>(`${this.quoteUrl}/${id}`);
  }

  createQuote(quote: Partial<Quote>): Observable<Quote> {
    return this.http.post<Quote>(this.quoteUrl, quote);
  }

  getInvoices(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.invoiceUrl, { params });
  }

  getInvoice(id: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.invoiceUrl}/${id}`);
  }

  createInvoice(invoice: Partial<Invoice>): Observable<Invoice> {
    return this.http.post<Invoice>(this.invoiceUrl, invoice);
  }
}
