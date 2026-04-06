import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Role {
  id: string;
  name: string;
  description: string;
  permissions: { [module: string]: string[] };
}

@Component({
  selector: 'app-role-management',
  template: `
    <div class="page-header">
      <h1>Role Management</h1>
      <button mat-raised-button color="primary"><mat-icon>add</mat-icon> New Role</button>
    </div>

    <div class="roles-grid">
      <mat-card *ngFor="let role of roles" class="role-card">
        <mat-card-header>
          <mat-card-title>{{ role.name }}</mat-card-title>
          <mat-card-subtitle>{{ role.description }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <h4>Module Permissions</h4>
          <mat-list dense>
            <mat-list-item *ngFor="let module of getModules(role)">
              <span matListItemTitle>{{ module }}</span>
              <span matListItemLine>{{ role.permissions[module].join(', ') }}</span>
            </mat-list-item>
          </mat-list>
        </mat-card-content>
        <mat-card-actions>
          <button mat-button color="primary"><mat-icon>edit</mat-icon> Edit</button>
          <button mat-button color="warn"><mat-icon>delete</mat-icon> Delete</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .roles-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 16px; }
    .role-card { h4 { margin: 16px 0 8px; font-weight: 500; } }
  `]
})
export class RoleManagementComponent implements OnInit {
  roles: Role[] = [
    {
      id: '1', name: 'Administrator', description: 'Full system access',
      permissions: {
        Contacts: ['View', 'Create', 'Edit', 'Delete', 'Export', 'Import'],
        Accounts: ['View', 'Create', 'Edit', 'Delete', 'Export', 'Import'],
        Opportunities: ['View', 'Create', 'Edit', 'Delete', 'Export'],
        Cases: ['View', 'Create', 'Edit', 'Delete'],
        Campaigns: ['View', 'Create', 'Edit', 'Delete'],
        Reports: ['View', 'Create', 'Edit', 'Delete'],
        Admin: ['View', 'Configure']
      }
    },
    {
      id: '2', name: 'Sales Manager', description: 'Sales team management',
      permissions: {
        Contacts: ['View', 'Create', 'Edit'],
        Accounts: ['View', 'Create', 'Edit'],
        Opportunities: ['View', 'Create', 'Edit', 'Delete'],
        Reports: ['View', 'Create']
      }
    },
    {
      id: '3', name: 'Sales Representative', description: 'Standard sales access',
      permissions: {
        Contacts: ['View', 'Create', 'Edit'],
        Accounts: ['View'],
        Opportunities: ['View', 'Create', 'Edit']
      }
    },
    {
      id: '4', name: 'Support Agent', description: 'Customer support access',
      permissions: {
        Contacts: ['View'],
        Cases: ['View', 'Create', 'Edit'],
        'Knowledge Base': ['View']
      }
    },
    {
      id: '5', name: 'Marketing', description: 'Marketing campaign access',
      permissions: {
        Contacts: ['View'],
        Campaigns: ['View', 'Create', 'Edit', 'Delete'],
        Reports: ['View']
      }
    }
  ];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {}

  getModules(role: Role): string[] {
    return Object.keys(role.permissions);
  }
}
