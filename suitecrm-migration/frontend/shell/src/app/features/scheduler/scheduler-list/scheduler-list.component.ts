import { Component, OnInit } from '@angular/core';
import { SchedulerService, Scheduler } from '../../../services/scheduler.service';

@Component({
  selector: 'app-scheduler-list',
  template: `
    <div class="container">
      <h2>Schedulers</h2>
      <div class="toolbar"><a routerLink="jobs" class="btn-secondary">View Jobs</a></div>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Job</th><th>Interval</th><th>Last Run</th><th>Status</th></tr></thead>
        <tbody>
          <tr *ngFor="let s of schedulers">
            <td>{{s.name}}</td><td>{{s.job}}</td><td>{{s.jobInterval}}</td>
            <td>{{s.lastRun}}</td><td>{{s.status}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class SchedulerListComponent implements OnInit {
  schedulers: Scheduler[] = [];
  constructor(private schedulerService: SchedulerService) {}
  ngOnInit() { this.schedulerService.getSchedulers().subscribe((res: any) => this.schedulers = res.content || []); }
}
