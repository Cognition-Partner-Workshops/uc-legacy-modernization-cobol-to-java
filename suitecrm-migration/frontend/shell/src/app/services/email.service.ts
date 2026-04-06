import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Email {
  id: string;
  name: string;
  fromAddr: string;
  toAddrs: string;
  ccAddrs: string;
  bccAddrs: string;
  description: string;
  descriptionHtml: string;
  status: string;
  type: string;
  flagged: boolean;
  dateSent: string;
  assignedUserId: string;
  attachments: EmailAttachment[];
}

export interface EmailAttachment {
  id: string;
  emailId: string;
  filename: string;
  fileMimeType: string;
  fileSize: number;
}

@Injectable({ providedIn: 'root' })
export class EmailService {
  private apiUrl = `${environment.apiGatewayUrl}/email-service/api/v1/emails`;

  constructor(private http: HttpClient) {}

  getEmails(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getMyEmails(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/my`, { params });
  }

  getEmail(id: string): Observable<Email> {
    return this.http.get<Email>(`${this.apiUrl}/${id}`);
  }

  createEmail(email: Partial<Email>): Observable<Email> {
    return this.http.post<Email>(this.apiUrl, email);
  }

  sendEmail(id: string): Observable<Email> {
    return this.http.post<Email>(`${this.apiUrl}/${id}/send`, {});
  }

  deleteEmail(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchEmails(query: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/search`, { params });
  }

  toggleFlag(id: string): Observable<Email> {
    return this.http.put<Email>(`${this.apiUrl}/${id}/flag`, {});
  }

  getStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`);
  }
}
