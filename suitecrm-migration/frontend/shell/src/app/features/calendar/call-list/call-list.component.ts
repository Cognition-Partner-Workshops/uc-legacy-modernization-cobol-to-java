import { Component, OnInit } from '@angular/core';
import { CalendarService, Call } from '../../../services/calendar.service';

@Component({
  selector: 'app-call-list',
  template: `
    <div class="container">
      <h2>Calls</h2>
      <div class="toolbar">
        <a routerLink="../meetings" class="btn-secondary">View Meetings</a>
      </div>
      <table class="data-table">
        <thead><tr><th>Subject</th><th>Direction</th><th>Start</th><th>Status</th><th>Duration</th></tr></thead>
        <tbody>
          <tr *ngFor="let c of calls">
            <td>{{c.name}}</td><td>{{c.direction}}</td><td>{{c.dateStart}}</td>
            <td>{{c.status}}</td><td>{{c.durationHours}}h {{c.durationMinutes}}m</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class CallListComponent implements OnInit {
  calls: Call[] = [];
  constructor(private calendarService: CalendarService) {}
  ngOnInit() { this.calendarService.getCalls().subscribe((res: any) => this.calls = res.content || []); }
}
