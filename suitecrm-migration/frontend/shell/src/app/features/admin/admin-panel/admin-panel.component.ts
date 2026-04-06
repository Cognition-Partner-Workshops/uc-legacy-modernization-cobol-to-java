import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-panel',
  template: `
    <div class="admin-layout">
      <nav class="admin-sidebar">
        <h3>Administration</h3>
        <ul>
          <li><a routerLink="settings" routerLinkActive="active">System Settings</a></li>
          <li><a routerLink="users" routerLinkActive="active">User Management</a></li>
          <li><a routerLink="roles" routerLinkActive="active">Role Management</a></li>
          <li><a routerLink="modules" routerLinkActive="active">Module Configuration</a></li>
        </ul>
      </nav>
      <main class="admin-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .admin-layout { display: flex; min-height: calc(100vh - 64px); }
    .admin-sidebar { width: 240px; background: #263238; color: white; padding: 20px; }
    .admin-sidebar h3 { margin-bottom: 20px; font-size: 18px; }
    .admin-sidebar ul { list-style: none; padding: 0; }
    .admin-sidebar li { margin-bottom: 4px; }
    .admin-sidebar a { color: #b0bec5; text-decoration: none; padding: 8px 12px; display: block; border-radius: 4px; }
    .admin-sidebar a:hover, .admin-sidebar a.active { background: #37474f; color: white; }
    .admin-content { flex: 1; padding: 20px; }
  `]
})
export class AdminPanelComponent {}
