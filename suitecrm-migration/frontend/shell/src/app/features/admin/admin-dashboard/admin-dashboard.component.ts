import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard',
  template: `
    <div class="page-header">
      <h1>Administration</h1>
    </div>
    <div class="admin-grid">
      <mat-card routerLink="users" class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">people</mat-icon>
          <h3>User Management</h3>
          <p>Manage users, passwords, and user profiles</p>
        </mat-card-content>
      </mat-card>
      <mat-card routerLink="roles" class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">security</mat-icon>
          <h3>Role Management</h3>
          <p>Configure roles and module-level permissions</p>
        </mat-card-content>
      </mat-card>
      <mat-card class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">settings</mat-icon>
          <h3>System Settings</h3>
          <p>Configure system-wide settings and preferences</p>
        </mat-card-content>
      </mat-card>
      <mat-card class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">email</mat-icon>
          <h3>Email Settings</h3>
          <p>Configure outbound email and SMTP settings</p>
        </mat-card-content>
      </mat-card>
      <mat-card class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">schema</mat-icon>
          <h3>Workflow Management</h3>
          <p>Create and manage automated workflows</p>
        </mat-card-content>
      </mat-card>
      <mat-card class="admin-card">
        <mat-card-content>
          <mat-icon class="admin-icon" color="primary">backup</mat-icon>
          <h3>Backup & Restore</h3>
          <p>System backup and data recovery options</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .admin-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
    .admin-card {
      cursor: pointer;
      transition: box-shadow 0.2s;
      &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
      mat-card-content { text-align: center; padding: 24px; }
      h3 { margin: 12px 0 4px; }
      p { color: #666; font-size: 13px; margin: 0; }
    }
    .admin-icon { font-size: 48px; width: 48px; height: 48px; }
  `]
})
export class AdminDashboardComponent {}
