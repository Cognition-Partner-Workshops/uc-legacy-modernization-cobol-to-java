import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { EmailTemplateListComponent } from './email-template-list/email-template-list.component';
import { PdfTemplateListComponent } from './pdf-template-list/pdf-template-list.component';

const routes: Routes = [
  { path: '', redirectTo: 'email', pathMatch: 'full' },
  { path: 'email', component: EmailTemplateListComponent },
  { path: 'pdf', component: PdfTemplateListComponent }
];

@NgModule({
  declarations: [EmailTemplateListComponent, PdfTemplateListComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class TemplatesModule {}
