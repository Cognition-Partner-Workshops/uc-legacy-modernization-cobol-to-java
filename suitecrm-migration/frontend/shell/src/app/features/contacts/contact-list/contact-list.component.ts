import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-contact-list',
  template: `
    <div class="page-header">
      <h1>Contacts</h1>
      <div class="action-buttons">
        <mat-form-field appearance="outline" class="search-bar">
          <mat-label>Search contacts</mat-label>
          <input matInput (keyup)="applyFilter($event)" placeholder="Name, email, phone...">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
        <button mat-raised-button color="primary">
          <mat-icon>add</mat-icon> New Contact
        </button>
      </div>
    </div>

    <div class="data-table-container mat-elevation-z2">
      <table mat-table [dataSource]="dataSource" matSort>
        <ng-container matColumnDef="firstName">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>First Name</th>
          <td mat-cell *matCellDef="let row">{{ row.firstName }}</td>
        </ng-container>
        <ng-container matColumnDef="lastName">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Last Name</th>
          <td mat-cell *matCellDef="let row">{{ row.lastName }}</td>
        </ng-container>
        <ng-container matColumnDef="emailPrimary">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Email</th>
          <td mat-cell *matCellDef="let row">{{ row.emailPrimary }}</td>
        </ng-container>
        <ng-container matColumnDef="phoneWork">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Phone</th>
          <td mat-cell *matCellDef="let row">{{ row.phoneWork }}</td>
        </ng-container>
        <ng-container matColumnDef="title">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>Title</th>
          <td mat-cell *matCellDef="let row">{{ row.title }}</td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let row">
            <button mat-icon-button [routerLink]="['/contacts', row.id]"><mat-icon>visibility</mat-icon></button>
            <button mat-icon-button color="primary"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button color="warn"><mat-icon>delete</mat-icon></button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons></mat-paginator>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 16px;
    }
    .action-buttons { display: flex; gap: 12px; align-items: center; }
    .search-bar { width: 300px; }
    .data-table-container {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      table { width: 100%; }
    }
  `]
})
export class ContactListComponent implements OnInit {
  displayedColumns = ['firstName', 'lastName', 'emailPrimary', 'phoneWork', 'title', 'actions'];
  dataSource = new MatTableDataSource<any>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadContacts();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadContacts(): void {
    this.http.get<any>(`${environment.apiUrl}/contacts`).subscribe({
      next: (response) => {
        this.dataSource.data = response.content || [];
      },
      error: () => {
        this.dataSource.data = [];
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }
}
