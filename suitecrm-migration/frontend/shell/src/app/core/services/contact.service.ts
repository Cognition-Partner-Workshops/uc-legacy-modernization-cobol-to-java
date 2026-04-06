import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  title: string;
  department: string;
  email: string;
  phoneWork: string;
  phoneMobile: string;
  accountId: string;
  accountName: string;
  leadSource: string;
  primaryAddressStreet: string;
  primaryAddressCity: string;
  primaryAddressState: string;
  primaryAddressPostalCode: string;
  primaryAddressCountry: string;
  description: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface Lead {
  id: string;
  firstName: string;
  lastName: string;
  title: string;
  company: string;
  email: string;
  phoneWork: string;
  status: string;
  leadSource: string;
  description: string;
  converted: boolean;
  assignedUserId: string;
}

@Injectable({ providedIn: 'root' })
export class ContactService {
  private contactUrl = `${environment.apiGatewayUrl}/api/v1/contacts`;
  private leadUrl = `${environment.apiGatewayUrl}/api/v1/leads`;

  constructor(private http: HttpClient) {}

  getContacts(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.contactUrl, { params });
  }

  getContact(id: string): Observable<Contact> {
    return this.http.get<Contact>(`${this.contactUrl}/${id}`);
  }

  createContact(contact: Partial<Contact>): Observable<Contact> {
    return this.http.post<Contact>(this.contactUrl, contact);
  }

  updateContact(id: string, contact: Partial<Contact>): Observable<Contact> {
    return this.http.put<Contact>(`${this.contactUrl}/${id}`, contact);
  }

  deleteContact(id: string): Observable<void> {
    return this.http.delete<void>(`${this.contactUrl}/${id}`);
  }

  getLeads(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.leadUrl, { params });
  }

  getLead(id: string): Observable<Lead> {
    return this.http.get<Lead>(`${this.leadUrl}/${id}`);
  }

  createLead(lead: Partial<Lead>): Observable<Lead> {
    return this.http.post<Lead>(this.leadUrl, lead);
  }

  convertLead(id: string, request: any): Observable<any> {
    return this.http.post(`${this.leadUrl}/${id}/convert`, request);
  }
}
