import { Component, OnInit } from '@angular/core';
import { TemplateService, EmailTemplate } from '../../../services/template.service';

@Component({
  selector: 'app-email-template-list',
  template: `
    <div class="container">
      <h2>Email Templates</h2>
      <div class="toolbar"><a routerLink="../pdf" class="btn-secondary">PDF Templates</a></div>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Subject</th><th>Type</th></tr></thead>
        <tbody>
          <tr *ngFor="let t of templates"><td>{{t.name}}</td><td>{{t.subject}}</td><td>{{t.type}}</td></tr>
        </tbody>
      </table>
    </div>
  `
})
export class EmailTemplateListComponent implements OnInit {
  templates: EmailTemplate[] = [];
  constructor(private templateService: TemplateService) {}
  ngOnInit() { this.templateService.getEmailTemplates().subscribe((res: any) => this.templates = res.content || []); }
}
