import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountService } from '../../../core/services/account.service';

@Component({
  selector: 'app-account-form',
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ isEdit ? 'Edit Account' : 'Create Account' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="accountForm" (ngSubmit)="onSubmit()">
            <div class="form-grid">
              <mat-form-field appearance="outline">
                <mat-label>Account Name</mat-label>
                <input matInput formControlName="name" required>
                <mat-error *ngIf="accountForm.get('name')?.hasError('required')">Name is required</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Account Type</mat-label>
                <mat-select formControlName="accountType">
                  <mat-option *ngFor="let type of accountTypes" [value]="type">{{ type }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Industry</mat-label>
                <mat-select formControlName="industry">
                  <mat-option *ngFor="let ind of industries" [value]="ind">{{ ind }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Phone</mat-label>
                <input matInput formControlName="phoneOffice">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Website</mat-label>
                <input matInput formControlName="website">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Annual Revenue</mat-label>
                <input matInput type="number" formControlName="annualRevenue">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Employees</mat-label>
                <input matInput type="number" formControlName="employees">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Rating</mat-label>
                <mat-select formControlName="rating">
                  <mat-option value="Hot">Hot</mat-option>
                  <mat-option value="Warm">Warm</mat-option>
                  <mat-option value="Cold">Cold</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Billing Street</mat-label>
                <input matInput formControlName="billingAddressStreet">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Billing City</mat-label>
                <input matInput formControlName="billingAddressCity">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Billing State</mat-label>
                <input matInput formControlName="billingAddressState">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Billing Postal Code</mat-label>
                <input matInput formControlName="billingAddressPostalCode">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Billing Country</mat-label>
                <input matInput formControlName="billingAddressCountry">
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="4"></textarea>
              </mat-form-field>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" [disabled]="accountForm.invalid">
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
export class AccountFormComponent implements OnInit {
  accountForm!: FormGroup;
  isEdit = false;
  accountId: string | null = null;

  accountTypes = ['Analyst', 'Competitor', 'Customer', 'Integrator', 'Investor', 'Partner', 'Press', 'Prospect', 'Reseller', 'Other'];
  industries = ['Apparel', 'Banking', 'Biotechnology', 'Chemicals', 'Communications', 'Construction', 'Consulting', 'Education', 'Electronics', 'Energy', 'Engineering', 'Entertainment', 'Finance', 'Government', 'Healthcare', 'Hospitality', 'Insurance', 'Machinery', 'Manufacturing', 'Media', 'Not For Profit', 'Recreation', 'Retail', 'Shipping', 'Technology', 'Telecommunications', 'Transportation', 'Utilities', 'Other'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.accountForm = this.fb.group({
      name: ['', Validators.required],
      accountType: [''],
      industry: [''],
      phoneOffice: [''],
      website: [''],
      annualRevenue: [null],
      employees: [null],
      rating: [''],
      billingAddressStreet: [''],
      billingAddressCity: [''],
      billingAddressState: [''],
      billingAddressPostalCode: [''],
      billingAddressCountry: [''],
      description: ['']
    });

    this.accountId = this.route.snapshot.paramMap.get('id');
    if (this.accountId) {
      this.isEdit = true;
      this.accountService.getAccount(this.accountId).subscribe(account => {
        this.accountForm.patchValue(account);
      });
    }
  }

  onSubmit(): void {
    if (this.accountForm.valid) {
      const data = this.accountForm.value;
      if (this.isEdit) {
        this.accountService.updateAccount(this.accountId!, data).subscribe(() => {
          this.router.navigate(['/accounts', this.accountId]);
        });
      } else {
        this.accountService.createAccount(data).subscribe(account => {
          this.router.navigate(['/accounts', account.id]);
        });
      }
    }
  }

  cancel(): void {
    if (this.isEdit) {
      this.router.navigate(['/accounts', this.accountId]);
    } else {
      this.router.navigate(['/accounts']);
    }
  }
}
