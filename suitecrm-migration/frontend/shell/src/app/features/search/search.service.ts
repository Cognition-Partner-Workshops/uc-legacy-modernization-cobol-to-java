import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface SearchResult {
  hits: { hits: any[]; total: { value: number } };
}

@Injectable({ providedIn: 'root' })
export class SearchService {
  private apiUrl = `${environment.apiGatewayUrl}/api/search`;

  constructor(private http: HttpClient) {}

  search(query: string, module?: string, from = 0, size = 20): Observable<any> {
    let params = new HttpParams().set('q', query).set('from', from.toString()).set('size', size.toString());
    if (module) params = params.set('module', module);
    return this.http.get<any>(this.apiUrl, { params });
  }
}
