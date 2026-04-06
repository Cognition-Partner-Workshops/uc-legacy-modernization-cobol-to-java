import { Component, OnInit } from '@angular/core';
import { AdminService } from '../admin.service';

@Component({
  selector: 'app-module-config',
  template: `
    <h2>Module Configuration</h2>
    <p>Configure module visibility, default views, and field layouts.</p>
    <table class="data-table">
      <thead><tr><th>Module</th><th>Visible</th><th>Default View</th><th>Actions</th></tr></thead>
      <tbody>
        <tr *ngFor="let mod of modules">
          <td>{{ mod.name }}</td>
          <td><input type="checkbox" [(ngModel)]="mod.visible"></td>
          <td>
            <select [(ngModel)]="mod.defaultView">
              <option value="list">List</option>
              <option value="kanban">Kanban</option>
              <option value="calendar">Calendar</option>
            </select>
          </td>
          <td><button (click)="configureFields(mod)">Fields</button></td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`
    .data-table { width: 100%; border-collapse: collapse; }
    .data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #e0e0e0; }
    .data-table th { background: #f5f5f5; }
    select { padding: 4px 8px; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: 4px 12px; background: #f5f5f5; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; }
  `]
})
export class ModuleConfigComponent implements OnInit {
  modules: any[] = [];
  constructor(private adminService: AdminService) {}
  ngOnInit() {
    this.modules = [
      { name: 'Accounts', visible: true, defaultView: 'list' },
      { name: 'Contacts', visible: true, defaultView: 'list' },
      { name: 'Leads', visible: true, defaultView: 'list' },
      { name: 'Opportunities', visible: true, defaultView: 'list' },
      { name: 'Cases', visible: true, defaultView: 'list' },
      { name: 'Campaigns', visible: true, defaultView: 'list' },
      { name: 'Calls', visible: true, defaultView: 'list' },
      { name: 'Meetings', visible: true, defaultView: 'calendar' },
      { name: 'Tasks', visible: true, defaultView: 'list' },
      { name: 'Reports', visible: true, defaultView: 'list' },
      { name: 'Documents', visible: true, defaultView: 'list' },
      { name: 'Emails', visible: true, defaultView: 'list' },
      { name: 'Projects', visible: true, defaultView: 'list' },
      { name: 'Knowledge Base', visible: true, defaultView: 'list' },
      { name: 'Events', visible: true, defaultView: 'calendar' },
      { name: 'Surveys', visible: true, defaultView: 'list' },
    ];
  }
  configureFields(mod: any) { /* Open field configuration dialog */ }
}
