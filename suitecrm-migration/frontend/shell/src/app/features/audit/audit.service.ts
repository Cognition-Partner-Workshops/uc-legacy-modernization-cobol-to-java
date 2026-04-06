import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AuditEntry {
  id: string;
  parentId: string;
  parentModule: string;
  fieldName: string;
  beforeValueString: string;
  afterValueString: string;
  changedBy: string;
  dateCreated: string;
}

@Injectable({ providedIn: 'root' })
export class AuditService {
  private apiUrl = `${environment.apiGatewayUrl}/api/audit`;

  constructor(private http: HttpClient) {}

  getAuditTrail(module: string, recordId: string): Observable<AuditEntry[]> {
    return this.http.get<AuditEntry[]>(`${this.apiUrl}/${module}/${recordId}`);
  }
}
