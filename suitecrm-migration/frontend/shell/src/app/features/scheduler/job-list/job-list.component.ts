import { Component, OnInit } from '@angular/core';
import { SchedulerService, SchedulerJob } from '../../../services/scheduler.service';

@Component({
  selector: 'app-job-list',
  template: `
    <div class="container">
      <h2>Scheduler Jobs</h2>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Execute Time</th><th>Status</th><th>Resolution</th><th>Message</th></tr></thead>
        <tbody>
          <tr *ngFor="let j of jobs">
            <td>{{j.name}}</td><td>{{j.executeTime}}</td><td>{{j.status}}</td>
            <td>{{j.resolution}}</td><td>{{j.message}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class JobListComponent implements OnInit {
  jobs: SchedulerJob[] = [];
  constructor(private schedulerService: SchedulerService) {}
  ngOnInit() { this.schedulerService.getJobs().subscribe((res: any) => this.jobs = res.content || []); }
}
