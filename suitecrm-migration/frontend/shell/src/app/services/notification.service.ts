import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Alert {
  id: string;
  name: string;
  alertType: string;
  urlRedirect: string;
  targetModule: string;
  description: string;
  isRead: boolean;
  dateEntered: string;
}

export interface Favorite {
  id: string;
  moduleName: string;
  recordId: string;
  assignedUserId: string;
  dateEntered: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = '/api/notifications';

  constructor(private http: HttpClient) {}

  getAlerts(userId: string, page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}/alerts?userId=${userId}&page=${page}&size=${size}`); }
  getUnreadAlerts(userId: string): Observable<Alert[]> { return this.http.get<Alert[]>(`${this.apiUrl}/alerts/unread?userId=${userId}`); }
  markAlertRead(id: string): Observable<void> { return this.http.put<void>(`${this.apiUrl}/alerts/${id}/read`, {}); }
  getFavorites(userId: string): Observable<Favorite[]> { return this.http.get<Favorite[]>(`${this.apiUrl}/favorites?userId=${userId}`); }
  addFavorite(userId: string, moduleName: string, recordId: string): Observable<Favorite> {
    return this.http.post<Favorite>(`${this.apiUrl}/favorites?userId=${userId}&moduleName=${moduleName}&recordId=${recordId}`, {});
  }
}
