import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GeoMap {
  id: string;
  name: string;
  centerLat: number;
  centerLng: number;
  zoomLevel: number;
  mapType: string;
  moduleType: string;
}

export interface Marker {
  id: string;
  name: string;
  city: string;
  state: string;
  country: string;
  latitude: number;
  longitude: number;
  markerType: string;
  relatedModule: string;
  relatedId: string;
}

@Injectable({ providedIn: 'root' })
export class MapsService {
  private apiUrl = '/api/maps';

  constructor(private http: HttpClient) {}

  getMaps(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}?page=${page}&size=${size}`); }
  getMarkers(page = 0, size = 20): Observable<any> { return this.http.get(`${this.apiUrl}/markers?page=${page}&size=${size}`); }
  getMarkerById(id: string): Observable<Marker> { return this.http.get<Marker>(`${this.apiUrl}/markers/${id}`); }
}
