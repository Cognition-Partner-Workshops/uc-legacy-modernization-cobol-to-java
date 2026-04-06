import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CaseService } from '../../../core/services/case.service';

@Component({
  selector: 'app-case-detail',
  template: `
    <div class="detail-container" *ngIf="caseData">
      <mat-card>
        <mat-card-header>
          <mat-card-title>[{{ caseData.caseNumber }}] {{ caseData.name }}</mat-card-title>
          <mat-card-subtitle>
            <span class="status-badge" [class]="caseData.status?.toLowerCase()">{{ caseData.status }}</span>
            <span class="priority-badge" [class]="caseData.priority?.toLowerCase()">{{ caseData.priority }}</span>
          </mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="detail-grid">
            <div class="detail-section">
              <h3>Overview</h3>
              <div class="field"><label>Case Number:</label><span>{{ caseData.caseNumber }}</span></div>
              <div class="field"><label>Subject:</label><span>{{ caseData.name }}</span></div>
              <div class="field"><label>Status:</label><span>{{ caseData.status }}</span></div>
              <div class="field"><label>Priority:</label><span>{{ caseData.priority }}</span></div>
              <div class="field"><label>Type:</label><span>{{ caseData.type }}</span></div>
              <div class="field"><label>Account:</label><span>{{ caseData.accountName }}</span></div>
              <div class="field"><label>Contact:</label><span>{{ caseData.contactName }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Description</h3>
              <p>{{ caseData.description }}</p>
            </div>
            <div class="detail-section full-width" *ngIf="caseData.resolution">
              <h3>Resolution</h3>
              <p>{{ caseData.resolution }}</p>
            </div>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="editCase()">Edit</button>
          <button mat-raised-button color="warn" (click)="deleteCase()">Delete</button>
          <button mat-button (click)="goBack()">Back to List</button>
        </mat-card-actions>
      </mat-card>

      <mat-card class="updates-card">
        <mat-card-header>
          <mat-card-title>Case Updates</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="update-list" *ngIf="caseUpdates.length > 0">
            <div class="update-item" *ngFor="let update of caseUpdates">
              <div class="update-header">
                <span class="update-name">{{ update.name }}</span>
                <span class="update-date">{{ update.dateEntered | date:'medium' }}</span>
                <mat-chip *ngIf="update.internal">Internal</mat-chip>
              </div>
              <p class="update-description">{{ update.description }}</p>
            </div>
          </div>
          <p *ngIf="caseUpdates.length === 0">No updates yet.</p>
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
    .status-badge { padding: 2px 8px; border-radius: 4px; margin-right: 8px; }
    .priority-badge { padding: 2px 8px; border-radius: 4px; }
    .updates-card { margin-top: 20px; }
    .update-item { border-bottom: 1px solid #e0e0e0; padding: 12px 0; }
    .update-header { display: flex; align-items: center; gap: 12px; }
    .update-name { font-weight: 500; }
    .update-date { color: #666; font-size: 0.9em; }
  `]
})
export class CaseDetailComponent implements OnInit {
  caseData: any;
  caseUpdates: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private caseService: CaseService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.caseService.getCase(id).subscribe(c => {
        this.caseData = c;
      });
      this.caseService.getCaseUpdates(id).subscribe(updates => {
        this.caseUpdates = updates;
      });
    }
  }

  editCase(): void {
    this.router.navigate(['/cases', this.caseData.id, 'edit']);
  }

  deleteCase(): void {
    if (confirm('Are you sure you want to delete this case?')) {
      this.caseService.deleteCase(this.caseData.id).subscribe(() => {
        this.router.navigate(['/cases']);
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/cases']);
  }
}
