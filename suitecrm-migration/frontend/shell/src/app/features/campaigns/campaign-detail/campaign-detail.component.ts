import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampaignService } from '../../../core/services/campaign.service';

@Component({
  selector: 'app-campaign-detail',
  template: `
    <div class="detail-container" *ngIf="campaign">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ campaign.name }}</mat-card-title>
          <mat-card-subtitle>{{ campaign.campaignType }} | {{ campaign.status }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="detail-grid">
            <div class="detail-section">
              <h3>Overview</h3>
              <div class="field"><label>Name:</label><span>{{ campaign.name }}</span></div>
              <div class="field"><label>Status:</label><span>{{ campaign.status }}</span></div>
              <div class="field"><label>Type:</label><span>{{ campaign.campaignType }}</span></div>
              <div class="field"><label>Start Date:</label><span>{{ campaign.startDate | date }}</span></div>
              <div class="field"><label>End Date:</label><span>{{ campaign.endDate | date }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Financial</h3>
              <div class="field"><label>Budget:</label><span>{{ campaign.budget | currency }}</span></div>
              <div class="field"><label>Actual Cost:</label><span>{{ campaign.actualCost | currency }}</span></div>
              <div class="field"><label>Expected Revenue:</label><span>{{ campaign.expectedRevenue | currency }}</span></div>
              <div class="field"><label>Impressions:</label><span>{{ campaign.impressions }}</span></div>
            </div>
            <div class="detail-section full-width">
              <h3>Description</h3>
              <p>{{ campaign.description }}</p>
            </div>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="editCampaign()">Edit</button>
          <button mat-raised-button color="warn" (click)="deleteCampaign()">Delete</button>
          <button mat-button (click)="goBack()">Back to List</button>
        </mat-card-actions>
      </mat-card>

      <mat-card class="logs-card">
        <mat-card-header>
          <mat-card-title>Campaign Log</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <mat-table [dataSource]="campaignLogs" *ngIf="campaignLogs.length > 0">
            <ng-container matColumnDef="activityType">
              <mat-header-cell *matHeaderCellDef>Activity</mat-header-cell>
              <mat-cell *matCellDef="let log">{{ log.activityType }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="activityDate">
              <mat-header-cell *matHeaderCellDef>Date</mat-header-cell>
              <mat-cell *matCellDef="let log">{{ log.activityDate | date:'medium' }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="targetType">
              <mat-header-cell *matHeaderCellDef>Target Type</mat-header-cell>
              <mat-cell *matCellDef="let log">{{ log.targetType }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="hits">
              <mat-header-cell *matHeaderCellDef>Hits</mat-header-cell>
              <mat-cell *matCellDef="let log">{{ log.hits }}</mat-cell>
            </ng-container>
            <mat-header-row *matHeaderRowDef="logColumns"></mat-header-row>
            <mat-row *matRowDef="let row; columns: logColumns;"></mat-row>
          </mat-table>
          <p *ngIf="campaignLogs.length === 0">No campaign logs yet.</p>
        </mat-card-content>
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
    .logs-card { margin-top: 20px; }
  `]
})
export class CampaignDetailComponent implements OnInit {
  campaign: any;
  campaignLogs: any[] = [];
  logColumns = ['activityType', 'activityDate', 'targetType', 'hits'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private campaignService: CampaignService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.campaignService.getCampaign(id).subscribe(c => this.campaign = c);
      this.campaignService.getCampaignLogs(id).subscribe(logs => this.campaignLogs = logs);
    }
  }

  editCampaign(): void {
    this.router.navigate(['/campaigns', this.campaign.id, 'edit']);
  }

  deleteCampaign(): void {
    if (confirm('Are you sure you want to delete this campaign?')) {
      this.campaignService.deleteCampaign(this.campaign.id).subscribe(() => {
        this.router.navigate(['/campaigns']);
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/campaigns']);
  }
}
