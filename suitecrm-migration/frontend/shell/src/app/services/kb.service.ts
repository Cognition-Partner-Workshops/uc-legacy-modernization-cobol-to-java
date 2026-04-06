import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface KBArticle {
  id: string;
  name: string;
  body: string;
  summary: string;
  status: string;
  revision: number;
  viewCount: number;
  helpfulCount: number;
  categoryId: string;
  tags: string[];
  dateEntered: string;
}

export interface KBCategory {
  id: string;
  name: string;
  description: string;
  parentId: string;
  children: KBCategory[];
}

@Injectable({ providedIn: 'root' })
export class KBService {
  private apiUrl = `${environment.apiGatewayUrl}/kb-service/api/v1/kb`;

  constructor(private http: HttpClient) {}

  getArticles(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/articles`, { params });
  }

  getArticle(id: string): Observable<KBArticle> {
    return this.http.get<KBArticle>(`${this.apiUrl}/articles/${id}`);
  }

  createArticle(article: Partial<KBArticle>): Observable<KBArticle> {
    return this.http.post<KBArticle>(`${this.apiUrl}/articles`, article);
  }

  updateArticle(id: string, article: Partial<KBArticle>): Observable<KBArticle> {
    return this.http.put<KBArticle>(`${this.apiUrl}/articles/${id}`, article);
  }

  deleteArticle(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/articles/${id}`);
  }

  searchArticles(query: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/articles/search`, { params });
  }

  getMostViewed(limit = 10): Observable<KBArticle[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<KBArticle[]>(`${this.apiUrl}/articles/most-viewed`, { params });
  }

  rateArticle(id: string, helpful: boolean): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/articles/${id}/rate`, { helpful });
  }

  getCategories(): Observable<KBCategory[]> {
    return this.http.get<KBCategory[]>(`${this.apiUrl}/categories`);
  }

  createCategory(category: Partial<KBCategory>): Observable<KBCategory> {
    return this.http.post<KBCategory>(`${this.apiUrl}/categories`, category);
  }

  getTags(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tags`);
  }
}
