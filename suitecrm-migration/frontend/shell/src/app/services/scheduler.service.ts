import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Scheduler {
  id: string;
  name: string;
  job: string;
  jobInterval: string;
  lastRun: string;
  status: string;
}

export interface SchedulerJob {
  id: string;
  name: string;
  schedulerId: string;
  executeTime: string;
  status: string;
  resolution: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class SchedulerService {
  private apiUrl = '/api/schedulers';

  constructor(private http: HttpClient) {}

  getSchedulers(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}?page=${page}&size=${size}`); }
  getSchedulerById(id: string): Observable<Scheduler> { return this.http.get<Scheduler>(`${this.apiUrl}/${id}`); }
  getJobs(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}/jobs?page=${page}&size=${size}`); }
}
