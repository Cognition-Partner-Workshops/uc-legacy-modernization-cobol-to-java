import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Project {
  id: string;
  name: string;
  status: string;
  priority: string;
  estimatedStartDate: string;
  estimatedEndDate: string;
  actualStartDate: string;
  actualEndDate: string;
  estimatedCost: number;
  actualCost: number;
  description: string;
  assignedUserId: string;
  taskCount: number;
  completionPercentage: number;
}

export interface ProjectTask {
  id: string;
  projectId: string;
  name: string;
  status: string;
  priority: string;
  percentComplete: number;
  dateDue: string;
  dateStart: string;
  dateFinish: string;
  duration: number;
  durationUnit: string;
  milestoneFlag: boolean;
  description: string;
  assignedUserId: string;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private apiUrl = `${environment.apiGatewayUrl}/api/v1/projects`;

  constructor(private http: HttpClient) {}

  getProjects(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.apiUrl, { params });
  }

  getProject(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  createProject(project: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project);
  }

  updateProject(id: string, project: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project);
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getProjectTasks(projectId: string): Observable<ProjectTask[]> {
    return this.http.get<ProjectTask[]>(`${this.apiUrl}/${projectId}/tasks`);
  }

  createProjectTask(task: Partial<ProjectTask>): Observable<ProjectTask> {
    return this.http.post<ProjectTask>(`${this.apiUrl}/tasks`, task);
  }

  updateProjectTask(taskId: string, task: Partial<ProjectTask>): Observable<ProjectTask> {
    return this.http.put<ProjectTask>(`${this.apiUrl}/tasks/${taskId}`, task);
  }

  deleteProjectTask(taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tasks/${taskId}`);
  }

  getMilestones(projectId: string): Observable<ProjectTask[]> {
    return this.http.get<ProjectTask[]>(`${this.apiUrl}/${projectId}/milestones`);
  }

  getOverdueTasks(): Observable<ProjectTask[]> {
    return this.http.get<ProjectTask[]>(`${this.apiUrl}/tasks/overdue`);
  }
}
