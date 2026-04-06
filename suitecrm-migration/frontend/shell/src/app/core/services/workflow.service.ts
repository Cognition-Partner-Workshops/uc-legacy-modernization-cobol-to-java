import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Workflow {
  id: string;
  name: string;
  flowModule: string;
  status: string;
  runWhen: string;
  repeatedRuns: boolean;
  description: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface WorkflowAction {
  id: string;
  workflowId: string;
  name: string;
  actionType: string;
  actionModule: string;
  fieldName: string;
  fieldValue: string;
  parameters: string;
}

export interface WorkflowCondition {
  id: string;
  workflowId: string;
  fieldName: string;
  operator: string;
  value: string;
  valueType: string;
}

@Injectable({ providedIn: 'root' })
export class WorkflowService {
  private apiUrl = `${environment.apiGatewayUrl}/api/v1/workflows`;

  constructor(private http: HttpClient) {}

  getWorkflows(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.apiUrl, { params });
  }

  getWorkflow(id: string): Observable<Workflow> {
    return this.http.get<Workflow>(`${this.apiUrl}/${id}`);
  }

  createWorkflow(workflow: Partial<Workflow>): Observable<Workflow> {
    return this.http.post<Workflow>(this.apiUrl, workflow);
  }

  updateWorkflow(id: string, workflow: Partial<Workflow>): Observable<Workflow> {
    return this.http.put<Workflow>(`${this.apiUrl}/${id}`, workflow);
  }

  deleteWorkflow(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getWorkflowActions(workflowId: string): Observable<WorkflowAction[]> {
    return this.http.get<WorkflowAction[]>(`${this.apiUrl}/${workflowId}/actions`);
  }

  getWorkflowConditions(workflowId: string): Observable<WorkflowCondition[]> {
    return this.http.get<WorkflowCondition[]>(`${this.apiUrl}/${workflowId}/conditions`);
  }
}
