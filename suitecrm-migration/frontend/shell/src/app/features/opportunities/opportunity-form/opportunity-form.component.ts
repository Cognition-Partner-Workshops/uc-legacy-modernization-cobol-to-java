import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OpportunityService } from '../../../core/services/opportunity.service';

@Component({
  selector: 'app-opportunity-form',
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ isEdit ? 'Edit Opportunity' : 'Create Opportunity' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="oppForm" (ngSubmit)="onSubmit()">
            <div class="form-grid">
              <mat-form-field appearance="outline">
                <mat-label>Name</mat-label>
                <input matInput formControlName="name" required>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Amount</mat-label>
                <input matInput type="number" formControlName="amount">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Sales Stage</mat-label>
                <mat-select formControlName="salesStage">
                  <mat-option *ngFor="let stage of salesStages" [value]="stage">{{ stage }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Probability (%)</mat-label>
                <input matInput type="number" formControlName="probability">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Close Date</mat-label>
                <input matInput [matDatepicker]="picker" formControlName="dateClosed">
                <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                <mat-datepicker #picker></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Type</mat-label>
                <mat-select formControlName="opportunityType">
                  <mat-option value="Existing Business">Existing Business</mat-option>
                  <mat-option value="New Business">New Business</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Lead Source</mat-label>
                <mat-select formControlName="leadSource">
                  <mat-option *ngFor="let src of leadSources" [value]="src">{{ src }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Next Step</mat-label>
                <input matInput formControlName="nextStep">
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="4"></textarea>
              </mat-form-field>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" [disabled]="oppForm.invalid">
                {{ isEdit ? 'Update' : 'Create' }}
              </button>
              <button mat-button type="button" (click)="cancel()">Cancel</button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-container { padding: 20px; max-width: 900px; margin: 0 auto; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .full-width { grid-column: span 2; }
    .form-actions { margin-top: 20px; display: flex; gap: 12px; }
    mat-form-field { width: 100%; }
  `]
})
export class OpportunityFormComponent implements OnInit {
  oppForm!: FormGroup;
  isEdit = false;
  oppId: string | null = null;

  salesStages = ['Prospecting', 'Qualification', 'Needs Analysis', 'Value Proposition', 'Id. Decision Makers', 'Perception Analysis', 'Proposal/Price Quote', 'Negotiation/Review', 'Closed Won', 'Closed Lost'];
  leadSources = ['Cold Call', 'Existing Customer', 'Self Generated', 'Employee', 'Partner', 'Public Relations', 'Direct Mail', 'Conference', 'Trade Show', 'Web Site', 'Word of mouth', 'Email', 'Campaign', 'Other'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private opportunityService: OpportunityService
  ) {}

  ngOnInit(): void {
    this.oppForm = this.fb.group({
      name: ['', Validators.required],
      amount: [null],
      salesStage: ['Prospecting'],
      probability: [10],
      dateClosed: [''],
      opportunityType: [''],
      leadSource: [''],
      nextStep: [''],
      description: ['']
    });

    this.oppId = this.route.snapshot.paramMap.get('id');
    if (this.oppId) {
      this.isEdit = true;
      this.opportunityService.getOpportunity(this.oppId).subscribe(opp => {
        this.oppForm.patchValue(opp);
      });
    }
  }

  onSubmit(): void {
    if (this.oppForm.valid) {
      const data = this.oppForm.value;
      if (this.isEdit) {
        this.opportunityService.updateOpportunity(this.oppId!, data).subscribe(() => {
          this.router.navigate(['/opportunities', this.oppId]);
        });
      } else {
        this.opportunityService.createOpportunity(data).subscribe(opp => {
          this.router.navigate(['/opportunities', opp.id]);
        });
      }
    }
  }

  cancel(): void {
    this.router.navigate(['/opportunities']);
  }
}
