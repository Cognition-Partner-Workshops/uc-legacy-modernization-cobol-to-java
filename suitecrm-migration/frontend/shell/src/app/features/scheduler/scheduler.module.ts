import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SchedulerListComponent } from './scheduler-list/scheduler-list.component';
import { JobListComponent } from './job-list/job-list.component';

const routes: Routes = [
  { path: '', component: SchedulerListComponent },
  { path: 'jobs', component: JobListComponent }
];

@NgModule({
  declarations: [SchedulerListComponent, JobListComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class SchedulerModule {}
