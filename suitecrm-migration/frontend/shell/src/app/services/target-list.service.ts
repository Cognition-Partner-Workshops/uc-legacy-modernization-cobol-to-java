import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TargetList {
  id: string;
  name: string;
  description: string;
  listType: string;
  domainName: string;
  entryCount: number;
  assignedUserId: string;
}

export interface Target {
  id: string;
  firstName: string;
  lastName: string;
  title: string;
  emailAddress: string;
  phoneWork: string;
  accountName: string;
  doNotCall: boolean;
}

export interface TargetListMember {
  id: string;
  prospectListId: string;
  relatedId: string;
  relatedType: string;
}

@Injectable({ providedIn: 'root' })
export class TargetListService {
  private apiUrl = `${environment.apiGatewayUrl}/target-list-service/api/v1/target-lists`;

  constructor(private http: HttpClient) {}

  getTargetLists(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getTargetList(id: string): Observable<TargetList> {
    return this.http.get<TargetList>(`${this.apiUrl}/${id}`);
  }

  createTargetList(list: Partial<TargetList>): Observable<TargetList> {
    return this.http.post<TargetList>(this.apiUrl, list);
  }

  updateTargetList(id: string, list: Partial<TargetList>): Observable<TargetList> {
    return this.http.put<TargetList>(`${this.apiUrl}/${id}`, list);
  }

  deleteTargetList(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchTargetLists(query: string): Observable<any> {
    const params = new HttpParams().set('q', query);
    return this.http.get(`${this.apiUrl}/search`, { params });
  }

  getMembers(listId: string): Observable<TargetListMember[]> {
    return this.http.get<TargetListMember[]>(`${this.apiUrl}/${listId}/members`);
  }

  addMember(listId: string, member: Partial<TargetListMember>): Observable<TargetListMember> {
    return this.http.post<TargetListMember>(`${this.apiUrl}/${listId}/members`, member);
  }

  removeMember(memberId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/members/${memberId}`);
  }

  getTargets(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/targets`, { params });
  }

  searchTargets(query: string): Observable<any> {
    const params = new HttpParams().set('q', query);
    return this.http.get(`${this.apiUrl}/targets/search`, { params });
  }
}
