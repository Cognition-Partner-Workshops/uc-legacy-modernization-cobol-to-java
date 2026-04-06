import { Component, OnInit } from '@angular/core';
import { LeadService, Lead } from '../../../services/lead.service';

@Component({
  selector: 'app-lead-list',
  template: `
    <div class="container">
      <h2>Leads</h2>
      <div class="toolbar">
        <button routerLink="/leads/new" class="btn-primary">New Lead</button>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>Name</th><th>Company</th><th>Email</th><th>Phone</th>
            <th>Status</th><th>Source</th><th>Assigned To</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let lead of leads" [routerLink]="['/leads', lead.id]">
            <td>{{lead.firstName}} {{lead.lastName}}</td>
            <td>{{lead.accountName}}</td><td>{{lead.primaryEmail}}</td>
            <td>{{lead.phoneWork}}</td><td>{{lead.status}}</td>
            <td>{{lead.leadSource}}</td><td>{{lead.assignedUserId}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class LeadListComponent implements OnInit {
  leads: Lead[] = [];
  constructor(private leadService: LeadService) {}
  ngOnInit() { this.leadService.getLeads().subscribe((res: any) => this.leads = res.content || []); }
}
