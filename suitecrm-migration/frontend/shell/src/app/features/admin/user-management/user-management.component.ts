import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-management',
  template: `
    <div class="page-header">
      <h1>User Management</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search users</mat-label>
          <input matInput (keyup)="applyFilter($event)">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary"><mat-icon>person_add</mat-icon> New User</button>
      </div>
    </div>
    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource">
        <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>Username</th><td mat-cell *matCellDef="let row">{{ row.username }}</td></ng-container>
        <ng-container matColumnDef="fullName"><th mat-header-cell *matHeaderCellDef>Full Name</th><td mat-cell *matCellDef="let row">{{ row.firstName }} {{ row.lastName }}</td></ng-container>
        <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>Email</th><td mat-cell *matCellDef="let row">{{ row.email }}</td></ng-container>
        <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Role</th><td mat-cell *matCellDef="let row"><span class="role-badge">{{ row.role }}</span></td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row">
          <mat-slide-toggle [checked]="row.active" color="primary" (change)="toggleUser(row)"></mat-slide-toggle>
        </td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
          <button mat-icon-button color="primary"><mat-icon>edit</mat-icon></button>
          <button mat-icon-button><mat-icon>vpn_key</mat-icon></button>
          <button mat-icon-button color="warn"><mat-icon>delete</mat-icon></button>
        </td></ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons></mat-paginator>
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
    .action-buttons { display: flex; gap: 12px; align-items: center; }
    .search-bar { width: 300px; }
    .data-table-container { background: white; border-radius: 8px; overflow: hidden; table { width: 100%; } }
    .role-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; background: #e3f2fd; color: #1565c0; }
  `]
})
export class UserManagementComponent implements OnInit {
  displayedColumns = ['username', 'fullName', 'email', 'role', 'status', 'actions'];
  dataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/auth/users`).subscribe({
      next: (res) => this.dataSource.data = res.content || res || [],
      error: () => this.dataSource.data = []
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }

  toggleUser(user: any): void {
    user.active = !user.active;
  }
}
