import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Report {
  id: string;
  name: string;
  reportType: string;
  reportModule: string;
  description: string;
  chartType: string;
  content: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface ScheduledReport {
  id: string;
  reportId: string;
  name: string;
  schedule: string;
  nextRun: string;
  active: boolean;
  exportFormat: string;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = `${environment.apiGatewayUrl}/api/v1/reports`;

  constructor(private http: HttpClient) {}

  getReports(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.apiUrl, { params });
  }

  getReport(id: string): Observable<Report> {
    return this.http.get<Report>(`${this.apiUrl}/${id}`);
  }

  createReport(report: Partial<Report>): Observable<Report> {
    return this.http.post<Report>(this.apiUrl, report);
  }

  updateReport(id: string, report: Partial<Report>): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}`, report);
  }

  deleteReport(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getScheduledReports(reportId: string): Observable<ScheduledReport[]> {
    return this.http.get<ScheduledReport[]>(`${this.apiUrl}/${reportId}/scheduled`);
  }

  scheduleReport(reportId: string, schedule: Partial<ScheduledReport>): Observable<ScheduledReport> {
    return this.http.post<ScheduledReport>(`${this.apiUrl}/${reportId}/schedule`, schedule);
  }
}
