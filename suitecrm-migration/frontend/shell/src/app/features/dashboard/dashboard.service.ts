import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Dashboard {
  id: string;
  name: string;
  dashboardModule?: string;
  dashboardType?: string;
  layout?: string;
  assignedUserId?: string;
}

export interface Dashlet {
  id: string;
  dashboardId: string;
  name: string;
  dashletType?: string;
  dashletModule?: string;
  title?: string;
  options?: string;
  position?: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = `${environment.apiGatewayUrl}/api/dashboards`;

  constructor(private http: HttpClient) {}

  getUserDashboards(): Observable<Dashboard[]> {
    return this.http.get<Dashboard[]>(`${this.apiUrl}/user/me`);
  }

  getDashboard(id: string): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.apiUrl}/${id}`);
  }

  createDashboard(dashboard: Partial<Dashboard>): Observable<Dashboard> {
    return this.http.post<Dashboard>(this.apiUrl, dashboard);
  }

  updateDashboard(id: string, dashboard: Partial<Dashboard>): Observable<Dashboard> {
    return this.http.put<Dashboard>(`${this.apiUrl}/${id}`, dashboard);
  }

  deleteDashboard(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
