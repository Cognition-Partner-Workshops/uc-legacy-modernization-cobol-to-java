import { Component, OnInit } from '@angular/core';
import { CalendarService, Meeting } from '../../../services/calendar.service';

@Component({
  selector: 'app-meeting-list',
  template: `
    <div class="container">
      <h2>Meetings</h2>
      <div class="toolbar">
        <a routerLink="../calls" class="btn-secondary">View Calls</a>
      </div>
      <table class="data-table">
        <thead><tr><th>Subject</th><th>Location</th><th>Start</th><th>Status</th><th>Type</th></tr></thead>
        <tbody>
          <tr *ngFor="let m of meetings">
            <td>{{m.name}}</td><td>{{m.location}}</td><td>{{m.dateStart}}</td>
            <td>{{m.status}}</td><td>{{m.type}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class MeetingListComponent implements OnInit {
  meetings: Meeting[] = [];
  constructor(private calendarService: CalendarService) {}
  ngOnInit() { this.calendarService.getMeetings().subscribe((res: any) => this.meetings = res.content || []); }
}
