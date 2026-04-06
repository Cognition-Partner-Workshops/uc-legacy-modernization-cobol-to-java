import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EmailTemplate {
  id: string;
  name: string;
  subject: string;
  body: string;
  bodyHtml: string;
  type: string;
  textOnly: boolean;
}

export interface PdfTemplate {
  id: string;
  name: string;
  type: string;
  moduleName: string;
  body: string;
  pageSize: string;
  orientation: string;
}

@Injectable({ providedIn: 'root' })
export class TemplateService {
  private apiUrl = '/api/templates';

  constructor(private http: HttpClient) {}

  getEmailTemplates(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}/email?page=${page}&size=${size}`); }
  getEmailTemplateById(id: string): Observable<EmailTemplate> { return this.http.get<EmailTemplate>(`${this.apiUrl}/email/${id}`); }
  getPdfTemplates(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}/pdf?page=${page}&size=${size}`); }
  getPdfTemplateById(id: string): Observable<PdfTemplate> { return this.http.get<PdfTemplate>(`${this.apiUrl}/pdf/${id}`); }
}
