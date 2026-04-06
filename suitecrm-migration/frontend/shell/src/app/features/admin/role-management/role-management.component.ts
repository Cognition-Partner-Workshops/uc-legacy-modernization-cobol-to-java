import { Component, OnInit } from '@angular/core';
import { AdminService } from '../admin.service';

@Component({
  selector: 'app-role-management',
  template: `
    <h2>Role Management</h2>
    <div class="roles-grid">
      <div class="role-card" *ngFor="let role of roles">
        <h3>{{ role.name }}</h3>
        <p>{{ role.description }}</p>
        <div class="permissions">
          <div *ngFor="let perm of role.permissions" class="perm-row">
            <span>{{ perm.module }}</span>
            <div class="perm-toggles">
              <label><input type="checkbox" [(ngModel)]="perm.view"> View</label>
              <label><input type="checkbox" [(ngModel)]="perm.edit"> Edit</label>
              <label><input type="checkbox" [(ngModel)]="perm.delete"> Delete</label>
              <label><input type="checkbox" [(ngModel)]="perm.export"> Export</label>
            </div>
          </div>
        </div>
        <button (click)="saveRole(role)">Save</button>
      </div>
    </div>
  `,
  styles: [`
    .roles-grid { display: grid; gap: 16px; }
    .role-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; }
    .role-card h3 { margin: 0 0 8px; }
    .perm-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
    .perm-toggles label { margin-left: 12px; font-size: 13px; }
    button { margin-top: 12px; padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
  `]
})
export class RoleManagementComponent implements OnInit {
  roles: any[] = [];
  constructor(private adminService: AdminService) {}
  ngOnInit() { this.adminService.getRoles().subscribe(r => this.roles = r); }
  saveRole(role: any) { this.adminService.saveRole(role).subscribe(); }
}
