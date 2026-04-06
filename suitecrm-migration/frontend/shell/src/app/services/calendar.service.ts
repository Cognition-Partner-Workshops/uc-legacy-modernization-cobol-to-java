import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Call {
  id: string;
  name: string;
  dateStart: string;
  dateEnd: string;
  direction: string;
  status: string;
  description: string;
  durationHours: number;
  durationMinutes: number;
  assignedUserId: string;
}

export interface Meeting {
  id: string;
  name: string;
  dateStart: string;
  dateEnd: string;
  status: string;
  type: string;
  location: string;
  description: string;
  assignedUserId: string;
}

@Injectable({ providedIn: 'root' })
export class CalendarService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  getCalls(page = 0, size = 20): Observable<any> { return this.http.get(`${this.baseUrl}/calls?page=${page}&size=${size}`); }
  getCallById(id: string): Observable<Call> { return this.http.get<Call>(`${this.baseUrl}/calls/${id}`); }
  createCall(call: Partial<Call>): Observable<Call> { return this.http.post<Call>(`${this.baseUrl}/calls`, call); }
  getMeetings(page = 0, size = 20): Observable<any> { return this.http.get(`${this.baseUrl}/meetings?page=${page}&size=${size}`); }
  getMeetingById(id: string): Observable<Meeting> { return this.http.get<Meeting>(`${this.baseUrl}/meetings/${id}`); }
  createMeeting(meeting: Partial<Meeting>): Observable<Meeting> { return this.http.post<Meeting>(`${this.baseUrl}/meetings`, meeting); }
}
