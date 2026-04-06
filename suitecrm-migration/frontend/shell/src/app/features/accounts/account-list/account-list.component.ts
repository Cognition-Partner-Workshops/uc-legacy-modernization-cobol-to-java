import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-account-list',
  template: `
    <div class="page-header">
      <h1>Accounts</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search accounts</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Name, industry...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary"><mat-icon>add</mat-icon> New Account</button>
      </div>
    </div>
    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource">
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
        <ng-container matColumnDef="accountType"><th mat-header-cell *matHeaderCellDef>Type</th><td mat-cell *matCellDef="let row">{{ row.accountType }}</td></ng-container>
        <ng-container matColumnDef="industry"><th mat-header-cell *matHeaderCellDef>Industry</th><td mat-cell *matCellDef="let row">{{ row.industry }}</td></ng-container>
        <ng-container matColumnDef="phoneOffice"><th mat-header-cell *matHeaderCellDef>Phone</th><td mat-cell *matCellDef="let row">{{ row.phoneOffice }}</td></ng-container>
        <ng-container matColumnDef="website"><th mat-header-cell *matHeaderCellDef>Website</th><td mat-cell *matCellDef="let row">{{ row.website }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
          <button mat-icon-button><mat-icon>visibility</mat-icon></button>
          <button mat-icon-button color="primary"><mat-icon>edit</mat-icon></button>
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
  `]
})
export class AccountListComponent implements OnInit {
  displayedColumns = ['name', 'accountType', 'industry', 'phoneOffice', 'website', 'actions'];
  dataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/accounts`).subscribe({
      next: (res) => this.dataSource.data = res.content || [],
      error: () => this.dataSource.data = []
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }
}
