import { Component, OnInit } from '@angular/core';
import { EmailService, Email } from '../../../services/email.service';

@Component({
  selector: 'app-email-list',
  template: `
    <div class="email-list-container">
      <div class="toolbar">
        <h2>Emails</h2>
        <div class="actions">
          <input type="text" placeholder="Search emails..." (input)="onSearch($event)" />
          <button routerLink="compose" class="btn-primary">Compose</button>
        </div>
      </div>
      <div class="email-tabs">
        <button (click)="loadMailbox('inbox')" [class.active]="currentMailbox === 'inbox'">Inbox</button>
        <button (click)="loadMailbox('sent')" [class.active]="currentMailbox === 'sent'">Sent</button>
        <button (click)="loadMailbox('draft')" [class.active]="currentMailbox === 'draft'">Drafts</button>
        <button (click)="loadFlagged()" [class.active]="currentMailbox === 'flagged'">Flagged</button>
      </div>
      <table class="data-table">
        <thead>
          <tr>
            <th>Flag</th><th>From</th><th>Subject</th><th>Date</th><th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let email of emails" (click)="selectEmail(email)">
            <td><span (click)="toggleFlag(email, $event)" class="flag-icon">{{ email.flagged ? '⚑' : '⚐' }}</span></td>
            <td>{{ email.fromAddr }}</td>
            <td>{{ email.name }}</td>
            <td>{{ email.dateSent | date:'short' }}</td>
            <td><span class="badge" [ngClass]="email.status">{{ email.status }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class EmailListComponent implements OnInit {
  emails: Email[] = [];
  currentMailbox = 'inbox';

  constructor(private emailService: EmailService) {}

  ngOnInit(): void {
    this.loadEmails();
  }

  loadEmails(): void {
    this.emailService.getMyEmails().subscribe(data => this.emails = data.content || []);
  }

  loadMailbox(mailbox: string): void {
    this.currentMailbox = mailbox;
    this.loadEmails();
  }

  loadFlagged(): void {
    this.currentMailbox = 'flagged';
    this.loadEmails();
  }

  selectEmail(email: Email): void {
    // Navigate to detail view
  }

  toggleFlag(email: Email, event: Event): void {
    event.stopPropagation();
    this.emailService.toggleFlag(email.id).subscribe(updated => {
      email.flagged = !email.flagged;
    });
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    if (query.length >= 3) {
      this.emailService.searchEmails(query).subscribe(data => this.emails = data.content || []);
    } else if (query.length === 0) {
      this.loadEmails();
    }
  }
}
