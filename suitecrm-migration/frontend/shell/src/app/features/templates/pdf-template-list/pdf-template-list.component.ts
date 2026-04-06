import { Component, OnInit } from '@angular/core';
import { TemplateService, PdfTemplate } from '../../../services/template.service';

@Component({
  selector: 'app-pdf-template-list',
  template: `
    <div class="container">
      <h2>PDF Templates</h2>
      <div class="toolbar"><a routerLink="../email" class="btn-secondary">Email Templates</a></div>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Type</th><th>Module</th><th>Page Size</th></tr></thead>
        <tbody>
          <tr *ngFor="let t of templates"><td>{{t.name}}</td><td>{{t.type}}</td><td>{{t.moduleName}}</td><td>{{t.pageSize}}</td></tr>
        </tbody>
      </table>
    </div>
  `
})
export class PdfTemplateListComponent implements OnInit {
  templates: PdfTemplate[] = [];
  constructor(private templateService: TemplateService) {}
  ngOnInit() { this.templateService.getPdfTemplates().subscribe((res: any) => this.templates = res.content || []); }
}
