import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LeadService, Lead } from '../../../services/lead.service';

@Component({
  selector: 'app-lead-detail',
  template: `
    <div class="container" *ngIf="lead">
      <h2>{{lead.firstName}} {{lead.lastName}}</h2>
      <div class="detail-grid">
        <div><label>Title:</label><span>{{lead.title}}</span></div>
        <div><label>Company:</label><span>{{lead.accountName}}</span></div>
        <div><label>Email:</label><span>{{lead.primaryEmail}}</span></div>
        <div><label>Phone:</label><span>{{lead.phoneWork}}</span></div>
        <div><label>Status:</label><span>{{lead.status}}</span></div>
        <div><label>Source:</label><span>{{lead.leadSource}}</span></div>
        <div><label>Converted:</label><span>{{lead.converted ? 'Yes' : 'No'}}</span></div>
      </div>
      <div class="actions">
        <button *ngIf="!lead.converted" (click)="convert()" class="btn-primary">Convert Lead</button>
        <button (click)="delete()" class="btn-danger">Delete</button>
      </div>
    </div>
  `
})
export class LeadDetailComponent implements OnInit {
  lead: Lead | null = null;
  constructor(private route: ActivatedRoute, private router: Router, private leadService: LeadService) {}
  ngOnInit() { this.route.params.subscribe(p => this.leadService.getLeadById(p['id']).subscribe(l => this.lead = l)); }
  convert() { if (this.lead) this.leadService.convertLead({ leadId: this.lead.id }).subscribe(() => this.ngOnInit()); }
  delete() { if (this.lead) this.leadService.deleteLead(this.lead.id).subscribe(() => this.router.navigate(['/leads'])); }
}
