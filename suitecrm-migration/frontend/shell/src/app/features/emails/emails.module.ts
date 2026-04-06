import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { EmailListComponent } from './email-list/email-list.component';
import { EmailComposeComponent } from './email-compose/email-compose.component';

const routes: Routes = [
  { path: '', component: EmailListComponent },
  { path: 'compose', component: EmailComposeComponent }
];

@NgModule({
  declarations: [EmailListComponent, EmailComposeComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class EmailsModule {}
