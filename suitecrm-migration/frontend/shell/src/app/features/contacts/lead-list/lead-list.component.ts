import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-lead-list',
  template: `
    <div class="page-header">
      <h1>Leads</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search leads</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Name, company, email...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary"><mat-icon>add</mat-icon> New Lead</button>
      </div>
    </div>
    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource">
        <ng-container matColumnDef="firstName"><th mat-header-cell *matHeaderCellDef>First Name</th><td mat-cell *matCellDef="let row">{{ row.firstName }}</td></ng-container>
        <ng-container matColumnDef="lastName"><th mat-header-cell *matHeaderCellDef>Last Name</th><td mat-cell *matCellDef="let row">{{ row.lastName }}</td></ng-container>
        <ng-container matColumnDef="company"><th mat-header-cell *matHeaderCellDef>Company</th><td mat-cell *matCellDef="let row">{{ row.company }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row"><span class="status-badge" [ngClass]="row.status?.toLowerCase()">{{ row.status }}</span></td></ng-container>
        <ng-container matColumnDef="leadSource"><th mat-header-cell *matHeaderCellDef>Source</th><td mat-cell *matCellDef="let row">{{ row.leadSource }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
          <button mat-icon-button><mat-icon>visibility</mat-icon></button>
          <button mat-icon-button color="primary"><mat-icon>swap_horiz</mat-icon></button>
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
    .status-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    .new { background: #e3f2fd; color: #1565c0; }
    .assigned { background: #fff3e0; color: #e65100; }
    .converted { background: #e8f5e9; color: #2e7d32; }
  `]
})
export class LeadListComponent implements OnInit {
  displayedColumns = ['firstName', 'lastName', 'company', 'status', 'leadSource', 'actions'];
  dataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/leads`).subscribe({
      next: (res) => this.dataSource.data = res.content || [],
      error: () => this.dataSource.data = []
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }
}
