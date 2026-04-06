import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Document {
  id: string;
  documentName: string;
  status: string;
  categoryId: string;
  subcategory: string;
  description: string;
  activeDate: string;
  expDate: string;
  templateType: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface EmailTemplate {
  id: string;
  name: string;
  subject: string;
  body: string;
  bodyHtml: string;
  type: string;
  published: boolean;
}

export interface PdfTemplate {
  id: string;
  name: string;
  type: string;
  active: boolean;
  bodyHtml: string;
  pageSize: string;
  orientation: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private docUrl = `${environment.apiGatewayUrl}/api/v1/documents`;
  private emailTemplateUrl = `${environment.apiGatewayUrl}/api/v1/email-templates`;
  private pdfTemplateUrl = `${environment.apiGatewayUrl}/api/v1/pdf-templates`;

  constructor(private http: HttpClient) {}

  getDocuments(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.docUrl, { params });
  }

  getDocument(id: string): Observable<Document> {
    return this.http.get<Document>(`${this.docUrl}/${id}`);
  }

  createDocument(doc: Partial<Document>): Observable<Document> {
    return this.http.post<Document>(this.docUrl, doc);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.docUrl}/${id}`);
  }

  getEmailTemplates(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.emailTemplateUrl, { params });
  }

  getEmailTemplate(id: string): Observable<EmailTemplate> {
    return this.http.get<EmailTemplate>(`${this.emailTemplateUrl}/${id}`);
  }

  createEmailTemplate(template: Partial<EmailTemplate>): Observable<EmailTemplate> {
    return this.http.post<EmailTemplate>(this.emailTemplateUrl, template);
  }

  getPdfTemplates(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.pdfTemplateUrl, { params });
  }

  getPdfTemplate(id: string): Observable<PdfTemplate> {
    return this.http.get<PdfTemplate>(`${this.pdfTemplateUrl}/${id}`);
  }

  createPdfTemplate(template: Partial<PdfTemplate>): Observable<PdfTemplate> {
    return this.http.post<PdfTemplate>(this.pdfTemplateUrl, template);
  }
}
