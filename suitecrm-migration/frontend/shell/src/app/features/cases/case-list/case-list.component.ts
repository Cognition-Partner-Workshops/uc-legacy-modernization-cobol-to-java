import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-case-list',
  template: `
    <div class="page-header">
      <h1>Cases</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search cases</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Subject, number...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary"><mat-icon>add</mat-icon> New Case</button>
      </div>
    </div>
    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource">
        <ng-container matColumnDef="caseNumber"><th mat-header-cell *matHeaderCellDef>Case #</th><td mat-cell *matCellDef="let row">{{ row.caseNumber }}</td></ng-container>
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Subject</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row"><span class="status-badge" [ngClass]="row.status?.toLowerCase().replace(' ', '-')">{{ row.status }}</span></td></ng-container>
        <ng-container matColumnDef="priority"><th mat-header-cell *matHeaderCellDef>Priority</th><td mat-cell *matCellDef="let row"><span class="priority-badge" [ngClass]="row.priority?.toLowerCase()">{{ row.priority }}</span></td></ng-container>
        <ng-container matColumnDef="type"><th mat-header-cell *matHeaderCellDef>Type</th><td mat-cell *matCellDef="let row">{{ row.type }}</td></ng-container>
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
    .status-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; background: #e3f2fd; color: #1565c0; }
    .priority-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    .high { background: #fce4ec; color: #c62828; }
    .medium { background: #fff3e0; color: #e65100; }
    .low { background: #e8f5e9; color: #2e7d32; }
  `]
})
export class CaseListComponent implements OnInit {
  displayedColumns = ['caseNumber', 'name', 'status', 'priority', 'type', 'actions'];
  dataSource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/cases`).subscribe({
      next: (res) => this.dataSource.data = res.content || [],
      error: () => this.dataSource.data = []
    });
  }

  ngAfterViewInit(): void { this.dataSource.paginator = this.paginator; }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }
}
