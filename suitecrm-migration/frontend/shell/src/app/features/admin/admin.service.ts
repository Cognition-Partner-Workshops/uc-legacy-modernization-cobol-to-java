import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AdminSetting {
  id?: string;
  category: string;
  name: string;
  value: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiGatewayUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  getAllSettings(): Observable<AdminSetting[]> {
    return this.http.get<AdminSetting[]>(`${this.apiUrl}/settings/all`);
  }

  getSettings(category: string): Observable<AdminSetting[]> {
    return this.http.get<AdminSetting[]>(`${this.apiUrl}/settings/${category}`);
  }

  setSetting(category: string, name: string, value: string): Observable<AdminSetting> {
    return this.http.post<AdminSetting>(`${this.apiUrl}/settings`, { category, name, value });
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiGatewayUrl}/api/auth/users`);
  }

  getRoles(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiGatewayUrl}/api/auth/roles`);
  }

  saveRole(role: any): Observable<any> {
    return this.http.put(`${environment.apiGatewayUrl}/api/auth/roles/${role.id}`, role);
  }
}
