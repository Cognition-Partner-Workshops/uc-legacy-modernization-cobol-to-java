import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Case {
  id: string;
  name: string;
  caseNumber: number;
  status: string;
  priority: string;
  type: string;
  description: string;
  resolution: string;
  accountId: string;
  accountName: string;
  contactId: string;
  contactName: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface CaseUpdate {
  id: string;
  caseId: string;
  name: string;
  description: string;
  internal: boolean;
  createdBy: string;
  dateEntered: string;
}

export interface Bug {
  id: string;
  bugNumber: number;
  name: string;
  status: string;
  priority: string;
  type: string;
  description: string;
  resolution: string;
  foundInRelease: string;
  fixedInRelease: string;
}

@Injectable({ providedIn: 'root' })
export class CaseService {
  private caseUrl = `${environment.apiGatewayUrl}/api/v1/cases`;
  private bugUrl = `${environment.apiGatewayUrl}/api/v1/bugs`;

  constructor(private http: HttpClient) {}

  getCases(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.caseUrl, { params });
  }

  getCase(id: string): Observable<Case> {
    return this.http.get<Case>(`${this.caseUrl}/${id}`);
  }

  createCase(caseData: Partial<Case>): Observable<Case> {
    return this.http.post<Case>(this.caseUrl, caseData);
  }

  updateCase(id: string, caseData: Partial<Case>): Observable<Case> {
    return this.http.put<Case>(`${this.caseUrl}/${id}`, caseData);
  }

  deleteCase(id: string): Observable<void> {
    return this.http.delete<void>(`${this.caseUrl}/${id}`);
  }

  getCaseUpdates(caseId: string): Observable<CaseUpdate[]> {
    return this.http.get<CaseUpdate[]>(`${this.caseUrl}/${caseId}/updates`);
  }

  addCaseUpdate(caseId: string, update: Partial<CaseUpdate>): Observable<CaseUpdate> {
    return this.http.post<CaseUpdate>(`${this.caseUrl}/${caseId}/updates`, update);
  }

  getBugs(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.bugUrl, { params });
  }

  getBug(id: string): Observable<Bug> {
    return this.http.get<Bug>(`${this.bugUrl}/${id}`);
  }
}
