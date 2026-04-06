import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-contact-detail',
  template: `
    <div class="page-header">
      <h1>{{ contact?.firstName }} {{ contact?.lastName }}</h1>
      <div class="action-buttons">
        <button mat-raised-button color="primary"><mat-icon>edit</mat-icon> Edit</button>
        <button mat-raised-button><mat-icon>email</mat-icon> Send Email</button>
      </div>
    </div>

    <div class="detail-grid" *ngIf="contact">
      <mat-card>
        <mat-card-header><mat-card-title>Contact Information</mat-card-title></mat-card-header>
        <mat-card-content>
          <div class="detail-row"><span class="label">Name</span><span>{{ contact.salutation }} {{ contact.firstName }} {{ contact.lastName }}</span></div>
          <div class="detail-row"><span class="label">Title</span><span>{{ contact.title }}</span></div>
          <div class="detail-row"><span class="label">Department</span><span>{{ contact.department }}</span></div>
          <div class="detail-row"><span class="label">Email</span><span>{{ contact.emailPrimary }}</span></div>
          <div class="detail-row"><span class="label">Phone (Work)</span><span>{{ contact.phoneWork }}</span></div>
          <div class="detail-row"><span class="label">Phone (Mobile)</span><span>{{ contact.phoneMobile }}</span></div>
          <div class="detail-row"><span class="label">Lead Source</span><span>{{ contact.leadSource }}</span></div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-header><mat-card-title>Address</mat-card-title></mat-card-header>
        <mat-card-content>
          <div class="detail-row"><span class="label">Street</span><span>{{ contact.primaryAddressStreet }}</span></div>
          <div class="detail-row"><span class="label">City</span><span>{{ contact.primaryAddressCity }}</span></div>
          <div class="detail-row"><span class="label">State</span><span>{{ contact.primaryAddressState }}</span></div>
          <div class="detail-row"><span class="label">Postal Code</span><span>{{ contact.primaryAddressPostalcode }}</span></div>
          <div class="detail-row"><span class="label">Country</span><span>{{ contact.primaryAddressCountry }}</span></div>
        </mat-card-content>
      </mat-card>

      <mat-card class="full-width">
        <mat-card-header><mat-card-title>Description</mat-card-title></mat-card-header>
        <mat-card-content>
          <p>{{ contact.description || 'No description provided.' }}</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .detail-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    .full-width { grid-column: 1 / -1; }
    .detail-row {
      display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }
    .label { width: 140px; color: #666; font-weight: 500; }
  `]
})
export class ContactDetailComponent implements OnInit {
  contact: any = null;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.http.get(`${environment.apiUrl}/contacts/${id}`).subscribe({
        next: (data) => this.contact = data
      });
    }
  }
}
