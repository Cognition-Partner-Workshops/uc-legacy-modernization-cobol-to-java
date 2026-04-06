import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Connector {
  id: string;
  name: string;
  description: string;
  connectorType: string;
  sourceModule: string;
  baseUrl: string;
  authType: string;
  status: string;
  isEnabled: boolean;
}

export interface ExternalAccount {
  id: string;
  name: string;
  connectorId: string;
  userId: string;
  externalId: string;
  application: string;
}

export interface OAuthKey {
  id: string;
  name: string;
  connectorId: string;
  oauthType: string;
  tokenUrl: string;
  authorizeUrl: string;
  scope: string;
}

@Injectable({ providedIn: 'root' })
export class ConnectorService {
  private apiUrl = `${environment.apiGatewayUrl}/connector-service/api/v1/connectors`;

  constructor(private http: HttpClient) {}

  getConnectors(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getEnabledConnectors(): Observable<Connector[]> {
    return this.http.get<Connector[]>(`${this.apiUrl}/enabled`);
  }

  getConnector(id: string): Observable<Connector> {
    return this.http.get<Connector>(`${this.apiUrl}/${id}`);
  }

  createConnector(connector: Partial<Connector>): Observable<Connector> {
    return this.http.post<Connector>(this.apiUrl, connector);
  }

  updateConnector(id: string, connector: Partial<Connector>): Observable<Connector> {
    return this.http.put<Connector>(`${this.apiUrl}/${id}`, connector);
  }

  deleteConnector(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleConnector(id: string): Observable<Connector> {
    return this.http.put<Connector>(`${this.apiUrl}/${id}/toggle`, {});
  }

  getExternalAccounts(connectorId: string): Observable<ExternalAccount[]> {
    return this.http.get<ExternalAccount[]>(`${this.apiUrl}/${connectorId}/accounts`);
  }

  getMyAccounts(): Observable<ExternalAccount[]> {
    return this.http.get<ExternalAccount[]>(`${this.apiUrl}/accounts/my`);
  }

  getOAuthKeys(): Observable<OAuthKey[]> {
    return this.http.get<OAuthKey[]>(`${this.apiUrl}/oauth-keys`);
  }

  getConnectorLogs(connectorId: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/${connectorId}/logs`, { params });
  }

  getMappings(connectorId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${connectorId}/mappings`);
  }
}
