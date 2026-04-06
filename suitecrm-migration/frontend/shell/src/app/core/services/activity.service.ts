import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Call {
  id: string;
  name: string;
  direction: string;
  status: string;
  dateStart: string;
  dateEnd: string;
  durationHours: number;
  durationMinutes: number;
  description: string;
  parentType: string;
  parentId: string;
  assignedUserId: string;
}

export interface Meeting {
  id: string;
  name: string;
  status: string;
  dateStart: string;
  dateEnd: string;
  durationHours: number;
  durationMinutes: number;
  location: string;
  description: string;
  parentType: string;
  parentId: string;
  assignedUserId: string;
}

export interface Task {
  id: string;
  name: string;
  status: string;
  priority: string;
  dateDue: string;
  dateStart: string;
  description: string;
  parentType: string;
  parentId: string;
  contactId: string;
  assignedUserId: string;
}

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private callUrl = `${environment.apiGatewayUrl}/api/v1/calls`;
  private meetingUrl = `${environment.apiGatewayUrl}/api/v1/meetings`;
  private taskUrl = `${environment.apiGatewayUrl}/api/v1/tasks`;

  constructor(private http: HttpClient) {}

  getCalls(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.callUrl, { params });
  }

  getCall(id: string): Observable<Call> {
    return this.http.get<Call>(`${this.callUrl}/${id}`);
  }

  createCall(call: Partial<Call>): Observable<Call> {
    return this.http.post<Call>(this.callUrl, call);
  }

  deleteCall(id: string): Observable<void> {
    return this.http.delete<void>(`${this.callUrl}/${id}`);
  }

  getMeetings(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.meetingUrl, { params });
  }

  getMeeting(id: string): Observable<Meeting> {
    return this.http.get<Meeting>(`${this.meetingUrl}/${id}`);
  }

  createMeeting(meeting: Partial<Meeting>): Observable<Meeting> {
    return this.http.post<Meeting>(this.meetingUrl, meeting);
  }

  deleteMeeting(id: string): Observable<void> {
    return this.http.delete<void>(`${this.meetingUrl}/${id}`);
  }

  getTasks(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.taskUrl, { params });
  }

  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.taskUrl}/${id}`);
  }

  createTask(task: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(this.taskUrl, task);
  }

  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`${this.taskUrl}/${id}`);
  }
}
