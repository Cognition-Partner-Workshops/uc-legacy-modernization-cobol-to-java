import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ContactService } from '../../../core/services/contact.service';

@Component({
  selector: 'app-contact-form',
  template: `
    <div class="form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ isEdit ? 'Edit Contact' : 'Create Contact' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="contactForm" (ngSubmit)="onSubmit()">
            <div class="form-grid">
              <mat-form-field appearance="outline">
                <mat-label>First Name</mat-label>
                <input matInput formControlName="firstName">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Last Name</mat-label>
                <input matInput formControlName="lastName" required>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Title</mat-label>
                <input matInput formControlName="title">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Department</mat-label>
                <input matInput formControlName="department">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Email</mat-label>
                <input matInput type="email" formControlName="email">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Phone (Work)</mat-label>
                <input matInput formControlName="phoneWork">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Phone (Mobile)</mat-label>
                <input matInput formControlName="phoneMobile">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Lead Source</mat-label>
                <mat-select formControlName="leadSource">
                  <mat-option *ngFor="let src of leadSources" [value]="src">{{ src }}</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Street</mat-label>
                <input matInput formControlName="primaryAddressStreet">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>City</mat-label>
                <input matInput formControlName="primaryAddressCity">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>State</mat-label>
                <input matInput formControlName="primaryAddressState">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Postal Code</mat-label>
                <input matInput formControlName="primaryAddressPostalCode">
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Country</mat-label>
                <input matInput formControlName="primaryAddressCountry">
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="4"></textarea>
              </mat-form-field>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" [disabled]="contactForm.invalid">
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
export class ContactFormComponent implements OnInit {
  contactForm!: FormGroup;
  isEdit = false;
  contactId: string | null = null;

  leadSources = ['Cold Call', 'Existing Customer', 'Self Generated', 'Employee', 'Partner', 'Public Relations', 'Direct Mail', 'Conference', 'Trade Show', 'Web Site', 'Word of mouth', 'Email', 'Campaign', 'Other'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private contactService: ContactService
  ) {}

  ngOnInit(): void {
    this.contactForm = this.fb.group({
      firstName: [''],
      lastName: ['', Validators.required],
      title: [''],
      department: [''],
      email: ['', Validators.email],
      phoneWork: [''],
      phoneMobile: [''],
      leadSource: [''],
      primaryAddressStreet: [''],
      primaryAddressCity: [''],
      primaryAddressState: [''],
      primaryAddressPostalCode: [''],
      primaryAddressCountry: [''],
      description: ['']
    });

    this.contactId = this.route.snapshot.paramMap.get('id');
    if (this.contactId) {
      this.isEdit = true;
      this.contactService.getContact(this.contactId).subscribe(contact => {
        this.contactForm.patchValue(contact);
      });
    }
  }

  onSubmit(): void {
    if (this.contactForm.valid) {
      const data = this.contactForm.value;
      if (this.isEdit) {
        this.contactService.updateContact(this.contactId!, data).subscribe(() => {
          this.router.navigate(['/contacts', this.contactId]);
        });
      } else {
        this.contactService.createContact(data).subscribe(contact => {
          this.router.navigate(['/contacts', contact.id]);
        });
      }
    }
  }

  cancel(): void {
    this.router.navigate(['/contacts']);
  }
}
