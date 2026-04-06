import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EmailService } from '../../../services/email.service';

@Component({
  selector: 'app-email-compose',
  template: `
    <div class="email-compose-container">
      <h2>Compose Email</h2>
      <form [formGroup]="emailForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label>To</label>
          <input formControlName="toAddrs" type="text" placeholder="recipient@example.com" />
        </div>
        <div class="form-group">
          <label>CC</label>
          <input formControlName="ccAddrs" type="text" />
        </div>
        <div class="form-group">
          <label>Subject</label>
          <input formControlName="name" type="text" />
        </div>
        <div class="form-group">
          <label>Body</label>
          <textarea formControlName="descriptionHtml" rows="12"></textarea>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-primary" [disabled]="!emailForm.valid">Send</button>
          <button type="button" (click)="saveDraft()" class="btn-secondary">Save Draft</button>
          <button type="button" (click)="cancel()" class="btn-cancel">Cancel</button>
        </div>
      </form>
    </div>
  `
})
export class EmailComposeComponent {
  emailForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private emailService: EmailService,
    private router: Router
  ) {
    this.emailForm = this.fb.group({
      toAddrs: ['', Validators.required],
      ccAddrs: [''],
      name: ['', Validators.required],
      descriptionHtml: ['']
    });
  }

  onSubmit(): void {
    if (this.emailForm.valid) {
      this.emailService.createEmail({ ...this.emailForm.value, status: 'sent' }).subscribe(email => {
        this.emailService.sendEmail(email.id).subscribe(() => this.router.navigate(['/emails']));
      });
    }
  }

  saveDraft(): void {
    this.emailService.createEmail({ ...this.emailForm.value, status: 'draft' }).subscribe(() => {
      this.router.navigate(['/emails']);
    });
  }

  cancel(): void {
    this.router.navigate(['/emails']);
  }
}
