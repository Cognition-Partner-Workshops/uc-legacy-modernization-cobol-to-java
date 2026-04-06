import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OpportunityService } from '../../../core/services/opportunity.service';

@Component({
  selector: 'app-opportunity-detail',
  template: `
    <div class="detail-container" *ngIf="opportunity">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ opportunity.name }}</mat-card-title>
          <mat-card-subtitle>{{ opportunity.salesStage }} | {{ opportunity.amount | currency }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="detail-grid">
            <div class="detail-section">
              <h3>Overview</h3>
              <div class="field"><label>Name:</label><span>{{ opportunity.name }}</span></div>
              <div class="field"><label>Amount:</label><span>{{ opportunity.amount | currency }}</span></div>
              <div class="field"><label>Sales Stage:</label><span>{{ opportunity.salesStage }}</span></div>
              <div class="field"><label>Probability:</label><span>{{ opportunity.probability }}%</span></div>
              <div class="field"><label>Close Date:</label><span>{{ opportunity.dateClosed }}</span></div>
              <div class="field"><label>Type:</label><span>{{ opportunity.opportunityType }}</span></div>
              <div class="field"><label>Lead Source:</label><span>{{ opportunity.leadSource }}</span></div>
              <div class="field"><label>Next Step:</label><span>{{ opportunity.nextStep }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Related</h3>
              <div class="field"><label>Account:</label><span>{{ opportunity.accountName }}</span></div>
            </div>
            <div class="detail-section full-width">
              <h3>Description</h3>
              <p>{{ opportunity.description }}</p>
            </div>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="editOpportunity()">Edit</button>
          <button mat-raised-button color="warn" (click)="deleteOpportunity()">Delete</button>
          <button mat-button (click)="goBack()">Back to List</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .detail-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .detail-section h3 { color: #1976d2; border-bottom: 1px solid #e0e0e0; padding-bottom: 8px; }
    .field { display: flex; padding: 4px 0; }
    .field label { font-weight: 500; width: 150px; color: #666; }
    .full-width { grid-column: span 2; }
  `]
})
export class OpportunityDetailComponent implements OnInit {
  opportunity: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private opportunityService: OpportunityService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.opportunityService.getOpportunity(id).subscribe(opp => {
        this.opportunity = opp;
      });
    }
  }

  editOpportunity(): void {
    this.router.navigate(['/opportunities', this.opportunity.id, 'edit']);
  }

  deleteOpportunity(): void {
    if (confirm('Are you sure you want to delete this opportunity?')) {
      this.opportunityService.deleteOpportunity(this.opportunity.id).subscribe(() => {
        this.router.navigate(['/opportunities']);
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/opportunities']);
  }
}
