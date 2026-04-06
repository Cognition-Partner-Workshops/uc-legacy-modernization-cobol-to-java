import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountService } from '../../../core/services/account.service';

@Component({
  selector: 'app-account-detail',
  template: `
    <div class="detail-container" *ngIf="account">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ account.name }}</mat-card-title>
          <mat-card-subtitle>{{ account.accountType }} | {{ account.industry }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="detail-grid">
            <div class="detail-section">
              <h3>Overview</h3>
              <div class="field"><label>Name:</label><span>{{ account.name }}</span></div>
              <div class="field"><label>Type:</label><span>{{ account.accountType }}</span></div>
              <div class="field"><label>Industry:</label><span>{{ account.industry }}</span></div>
              <div class="field"><label>Annual Revenue:</label><span>{{ account.annualRevenue | currency }}</span></div>
              <div class="field"><label>Employees:</label><span>{{ account.employees }}</span></div>
              <div class="field"><label>Rating:</label><span>{{ account.rating }}</span></div>
              <div class="field"><label>Website:</label><span>{{ account.website }}</span></div>
              <div class="field"><label>Phone:</label><span>{{ account.phoneOffice }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Address</h3>
              <div class="field"><label>Street:</label><span>{{ account.billingAddressStreet }}</span></div>
              <div class="field"><label>City:</label><span>{{ account.billingAddressCity }}</span></div>
              <div class="field"><label>State:</label><span>{{ account.billingAddressState }}</span></div>
              <div class="field"><label>Postal Code:</label><span>{{ account.billingAddressPostalCode }}</span></div>
              <div class="field"><label>Country:</label><span>{{ account.billingAddressCountry }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Description</h3>
              <p>{{ account.description }}</p>
            </div>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="editAccount()">Edit</button>
          <button mat-raised-button color="warn" (click)="deleteAccount()">Delete</button>
          <button mat-button (click)="goBack()">Back to List</button>
        </mat-card-actions>
      </mat-card>

      <mat-card class="related-card">
        <mat-card-header>
          <mat-card-title>Contacts</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <mat-table [dataSource]="relatedContacts" *ngIf="relatedContacts.length > 0">
            <ng-container matColumnDef="name">
              <mat-header-cell *matHeaderCellDef>Name</mat-header-cell>
              <mat-cell *matCellDef="let contact">{{ contact.firstName }} {{ contact.lastName }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="email">
              <mat-header-cell *matHeaderCellDef>Email</mat-header-cell>
              <mat-cell *matCellDef="let contact">{{ contact.email }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="phone">
              <mat-header-cell *matHeaderCellDef>Phone</mat-header-cell>
              <mat-cell *matCellDef="let contact">{{ contact.phoneWork }}</mat-cell>
            </ng-container>
            <mat-header-row *matHeaderRowDef="['name', 'email', 'phone']"></mat-header-row>
            <mat-row *matRowDef="let row; columns: ['name', 'email', 'phone']"></mat-row>
          </mat-table>
          <p *ngIf="relatedContacts.length === 0">No related contacts found.</p>
        </mat-card-content>
      </mat-card>

      <mat-card class="related-card">
        <mat-card-header>
          <mat-card-title>Opportunities</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <mat-table [dataSource]="relatedOpportunities" *ngIf="relatedOpportunities.length > 0">
            <ng-container matColumnDef="name">
              <mat-header-cell *matHeaderCellDef>Name</mat-header-cell>
              <mat-cell *matCellDef="let opp">{{ opp.name }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="salesStage">
              <mat-header-cell *matHeaderCellDef>Stage</mat-header-cell>
              <mat-cell *matCellDef="let opp">{{ opp.salesStage }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="amount">
              <mat-header-cell *matHeaderCellDef>Amount</mat-header-cell>
              <mat-cell *matCellDef="let opp">{{ opp.amount | currency }}</mat-cell>
            </ng-container>
            <mat-header-row *matHeaderRowDef="['name', 'salesStage', 'amount']"></mat-header-row>
            <mat-row *matRowDef="let row; columns: ['name', 'salesStage', 'amount']"></mat-row>
          </mat-table>
          <p *ngIf="relatedOpportunities.length === 0">No related opportunities found.</p>
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
    .related-card { margin-top: 20px; }
    mat-card-actions { padding: 16px; }
  `]
})
export class AccountDetailComponent implements OnInit {
  account: any;
  relatedContacts: any[] = [];
  relatedOpportunities: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.accountService.getAccount(id).subscribe(account => {
        this.account = account;
      });
    }
  }

  editAccount(): void {
    this.router.navigate(['/accounts', this.account.id, 'edit']);
  }

  deleteAccount(): void {
    if (confirm('Are you sure you want to delete this account?')) {
      this.accountService.deleteAccount(this.account.id).subscribe(() => {
        this.router.navigate(['/accounts']);
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/accounts']);
  }
}
