import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-opportunity-list',
  template: `
    <div class="page-header">
      <h1>Opportunities</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search opportunities</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Name, account...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary"><mat-icon>add</mat-icon> New Opportunity</button>
      </div>
    </div>
    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource">
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
        <ng-container matColumnDef="accountName"><th mat-header-cell *matHeaderCellDef>Account</th><td mat-cell *matCellDef="let row">{{ row.accountName }}</td></ng-container>
        <ng-container matColumnDef="amount"><th mat-header-cell *matHeaderCellDef>Amount</th><td mat-cell *matCellDef="let row">{{ row.amount | currency }}</td></ng-container>
        <ng-container matColumnDef="salesStage"><th mat-header-cell *matHeaderCellDef>Stage</th><td mat-cell *matCellDef="let row"><span class="stage-badge">{{ row.salesStage }}</span></td></ng-container>
        <ng-container matColumnDef="probability"><th mat-header-cell *matHeaderCellDef>Probability</th><td mat-cell *matCellDef="let row">{{ row.probability }}%</td></ng-container>
        <ng-container matColumnDef="dateClosed"><th mat-header-cell *matHeaderCellDef>Close Date</th><td mat-cell *matCellDef="let row">{{ row.dateClosed | date }}</td></ng-container>
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
    .stage-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; background: #e3f2fd; color: #1565c0; }
  `]
})
export class OpportunityListComponent implements OnInit {
  displayedColumns = ['name', 'accountName', 'amount', 'salesStage', 'probability', 'dateClosed', 'actions'];
  dataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/opportunities`).subscribe({
      next: (res) => this.dataSource.data = res.content || [],
      error: () => this.dataSource.data = []
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }
}
