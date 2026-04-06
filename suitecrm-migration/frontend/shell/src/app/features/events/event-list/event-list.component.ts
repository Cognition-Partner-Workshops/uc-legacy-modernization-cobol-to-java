import { Component, OnInit } from '@angular/core';
import { EventService, FPEvent } from '../../../services/event.service';

@Component({
  selector: 'app-event-list',
  template: `
    <div class="event-list-container">
      <div class="toolbar">
        <h2>Events</h2>
        <button class="btn-primary">Create Event</button>
      </div>
      <div class="view-toggle">
        <button (click)="viewMode = 'list'" [class.active]="viewMode === 'list'">List</button>
        <button (click)="viewMode = 'upcoming'" [class.active]="viewMode === 'upcoming'">Upcoming</button>
      </div>
      <table class="data-table" *ngIf="viewMode === 'list'">
        <thead>
          <tr><th>Name</th><th>Start Date</th><th>End Date</th><th>Status</th><th>Registrations</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let event of events" [routerLink]="[event.id]">
            <td>{{ event.name }}</td>
            <td>{{ event.dateStart | date:'medium' }}</td>
            <td>{{ event.dateEnd | date:'medium' }}</td>
            <td><span class="badge" [ngClass]="event.status">{{ event.status }}</span></td>
            <td>{{ event.registrationCount || 0 }}</td>
          </tr>
        </tbody>
      </table>
      <div *ngIf="viewMode === 'upcoming'" class="upcoming-events">
        <div *ngFor="let event of upcomingEvents" class="event-card" [routerLink]="[event.id]">
          <h3>{{ event.name }}</h3>
          <p>{{ event.dateStart | date:'fullDate' }}</p>
          <span class="badge" [ngClass]="event.status">{{ event.status }}</span>
        </div>
      </div>
    </div>
  `
})
export class EventListComponent implements OnInit {
  events: FPEvent[] = [];
  upcomingEvents: FPEvent[] = [];
  viewMode = 'list';

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.eventService.getEvents().subscribe(data => this.events = data.content || []);
    this.eventService.getUpcomingEvents().subscribe(data => this.upcomingEvents = data);
  }
}
