import { Component, OnInit } from '@angular/core';
import { AdminService } from '../admin.service';

@Component({
  selector: 'app-user-management',
  template: `
    <h2>User Management</h2>
    <div class="toolbar">
      <input type="text" placeholder="Search users..." [(ngModel)]="searchTerm" (input)="filterUsers()">
      <button (click)="createUser()">Create User</button>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>Username</th>
          <th>Full Name</th>
          <th>Email</th>
          <th>Status</th>
          <th>Role</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let user of filteredUsers">
          <td>{{ user.userName }}</td>
          <td>{{ user.firstName }} {{ user.lastName }}</td>
          <td>{{ user.email }}</td>
          <td><span [class]="'status-' + user.status">{{ user.status }}</span></td>
          <td>{{ user.role }}</td>
          <td><button (click)="editUser(user)">Edit</button> <button (click)="deactivateUser(user)">Deactivate</button></td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`
    .toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
    .toolbar input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; width: 300px; }
    .toolbar button { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .data-table { width: 100%; border-collapse: collapse; }
    .data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #e0e0e0; }
    .data-table th { background: #f5f5f5; font-weight: 600; }
    .status-Active { color: #4caf50; }
    .status-Inactive { color: #f44336; }
    td button { margin-right: 8px; padding: 4px 8px; background: #f5f5f5; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; }
  `]
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  searchTerm = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() { this.loadUsers(); }

  loadUsers() {
    this.adminService.getUsers().subscribe(users => {
      this.users = users;
      this.filteredUsers = users;
    });
  }

  filterUsers() {
    const term = this.searchTerm.toLowerCase();
    this.filteredUsers = this.users.filter(u =>
      u.userName?.toLowerCase().includes(term) || u.email?.toLowerCase().includes(term) ||
      u.firstName?.toLowerCase().includes(term) || u.lastName?.toLowerCase().includes(term)
    );
  }

  createUser() { /* Open create user dialog */ }
  editUser(user: any) { /* Open edit user dialog */ }
  deactivateUser(user: any) { /* Deactivate user */ }
}
