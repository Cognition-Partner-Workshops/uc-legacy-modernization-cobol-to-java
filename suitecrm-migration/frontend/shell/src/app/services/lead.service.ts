import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Lead {
  id: string;
  salutation: string;
  firstName: string;
  lastName: string;
  title: string;
  department: string;
  accountName: string;
  phoneWork: string;
  phoneMobile: string;
  primaryEmail: string;
  status: string;
  leadSource: string;
  converted: boolean;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

@Injectable({ providedIn: 'root' })
export class LeadService {
  private apiUrl = '/api/leads';

  constructor(private http: HttpClient) {}

  getLeads(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.apiUrl}?page=${page}&size=${size}`);
  }
  getLeadById(id: string): Observable<Lead> {
    return this.http.get<Lead>(`${this.apiUrl}/${id}`);
  }
  createLead(lead: Partial<Lead>): Observable<Lead> {
    return this.http.post<Lead>(this.apiUrl, lead);
  }
  updateLead(id: string, lead: Partial<Lead>): Observable<Lead> {
    return this.http.put<Lead>(`${this.apiUrl}/${id}`, lead);
  }
  deleteLead(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  convertLead(request: { leadId: string; createContact?: boolean; createAccount?: boolean; createOpportunity?: boolean }): Observable<Lead> {
    return this.http.post<Lead>(`${this.apiUrl}/convert`, request);
  }
}
