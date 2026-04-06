import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService, FPEvent, EventRegistration } from '../../../services/event.service';

@Component({
  selector: 'app-event-detail',
  template: `
    <div class="event-detail" *ngIf="event">
      <h1>{{ event.name }}</h1>
      <div class="event-info">
        <p><strong>Date:</strong> {{ event.dateStart | date:'fullDate' }} - {{ event.dateEnd | date:'fullDate' }}</p>
        <p><strong>Status:</strong> <span class="badge" [ngClass]="event.status">{{ event.status }}</span></p>
        <p><strong>Budget:</strong> {{ event.budget | currency }}</p>
        <p><strong>Registrations:</strong> {{ event.registrationCount }} ({{ event.acceptedCount }} accepted)</p>
      </div>
      <p>{{ event.description }}</p>
      <h3>Registrations</h3>
      <table class="data-table">
        <thead>
          <tr><th>Name</th><th>Email</th><th>Company</th><th>Status</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let reg of registrations">
            <td>{{ reg.firstName }} {{ reg.lastName }}</td>
            <td>{{ reg.email }}</td>
            <td></td>
            <td><span class="badge">{{ reg.acceptStatus }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class EventDetailComponent implements OnInit {
  event: FPEvent | null = null;
  registrations: EventRegistration[] = [];

  constructor(private route: ActivatedRoute, private eventService: EventService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.eventService.getEvent(id).subscribe(event => this.event = event);
      this.eventService.getRegistrations(id).subscribe(regs => this.registrations = regs);
    }
  }
}
