import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ImportExportService {
  private apiUrl = `${environment.apiGatewayUrl}/api/import-export`;

  constructor(private http: HttpClient) {}

  importCsv(file: File, module: string, fieldMappings: string[]): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('module', module);
    formData.append('fieldMappings', JSON.stringify(fieldMappings));
    return this.http.post(`${this.apiUrl}/import`, formData);
  }

  exportCsv(module: string, filters?: any): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/export`, { module, filters }, { responseType: 'blob' });
  }
}
