import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { CallListComponent } from './call-list/call-list.component';
import { MeetingListComponent } from './meeting-list/meeting-list.component';

const routes: Routes = [
  { path: '', redirectTo: 'calls', pathMatch: 'full' },
  { path: 'calls', component: CallListComponent },
  { path: 'meetings', component: MeetingListComponent }
];

@NgModule({
  declarations: [CallListComponent, MeetingListComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class CalendarModule {}
