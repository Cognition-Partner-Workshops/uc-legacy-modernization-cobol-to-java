import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FPEvent {
  id: string;
  name: string;
  description: string;
  dateStart: string;
  dateEnd: string;
  status: string;
  budget: number;
  locationId: string;
  locationName: string;
  registrationCount: number;
  acceptedCount: number;
  assignedUserId: string;
}

export interface EventRegistration {
  id: string;
  eventId: string;
  contactId: string;
  firstName: string;
  lastName: string;
  email: string;
  status: string;
  acceptStatus: string;
}

export interface EventLocation {
  id: string;
  name: string;
  address: string;
  city: string;
  state: string;
  country: string;
  capacity: number;
}

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = `${environment.apiGatewayUrl}/event-service/api/v1/events`;

  constructor(private http: HttpClient) {}

  getEvents(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getEvent(id: string): Observable<FPEvent> {
    return this.http.get<FPEvent>(`${this.apiUrl}/${id}`);
  }

  createEvent(event: Partial<FPEvent>): Observable<FPEvent> {
    return this.http.post<FPEvent>(this.apiUrl, event);
  }

  updateEvent(id: string, event: Partial<FPEvent>): Observable<FPEvent> {
    return this.http.put<FPEvent>(`${this.apiUrl}/${id}`, event);
  }

  deleteEvent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getUpcomingEvents(): Observable<FPEvent[]> {
    return this.http.get<FPEvent[]>(`${this.apiUrl}/upcoming`);
  }

  getRegistrations(eventId: string): Observable<EventRegistration[]> {
    return this.http.get<EventRegistration[]>(`${this.apiUrl}/${eventId}/registrations`);
  }

  register(eventId: string, registration: Partial<EventRegistration>): Observable<EventRegistration> {
    return this.http.post<EventRegistration>(`${this.apiUrl}/${eventId}/registrations`, registration);
  }

  getLocations(): Observable<EventLocation[]> {
    return this.http.get<EventLocation[]>(`${this.apiUrl}/locations`);
  }

  createLocation(location: Partial<EventLocation>): Observable<EventLocation> {
    return this.http.post<EventLocation>(`${this.apiUrl}/locations`, location);
  }
}
