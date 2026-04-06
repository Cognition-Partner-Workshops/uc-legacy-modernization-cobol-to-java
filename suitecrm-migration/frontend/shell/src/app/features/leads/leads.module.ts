import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { LeadListComponent } from './lead-list/lead-list.component';
import { LeadDetailComponent } from './lead-detail/lead-detail.component';

const routes: Routes = [
  { path: '', component: LeadListComponent },
  { path: ':id', component: LeadDetailComponent }
];

@NgModule({
  declarations: [LeadListComponent, LeadDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class LeadsModule {}
