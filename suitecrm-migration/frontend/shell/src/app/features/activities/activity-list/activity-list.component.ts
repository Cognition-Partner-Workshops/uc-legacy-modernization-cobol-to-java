import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-activity-list',
  template: `
    <div class="page-header">
      <h1>Activities</h1>
      <div class="action-buttons">
        <button mat-raised-button color="primary"><mat-icon>event</mat-icon> New Meeting</button>
        <button mat-raised-button color="accent"><mat-icon>phone</mat-icon> Log Call</button>
        <button mat-raised-button><mat-icon>task</mat-icon> New Task</button>
      </div>
    </div>

    <mat-tab-group>
      <mat-tab label="Meetings">
        <div class="tab-content">
          <table mat-table [dataSource]="meetingsSource" class="full-width">
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Subject</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
            <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row"><span class="status-badge">{{ row.status }}</span></td></ng-container>
            <ng-container matColumnDef="dateStart"><th mat-header-cell *matHeaderCellDef>Start</th><td mat-cell *matCellDef="let row">{{ row.dateStart | date:'medium' }}</td></ng-container>
            <ng-container matColumnDef="location"><th mat-header-cell *matHeaderCellDef>Location</th><td mat-cell *matCellDef="let row">{{ row.location }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
              <button mat-icon-button><mat-icon>visibility</mat-icon></button>
              <button mat-icon-button color="primary"><mat-icon>edit</mat-icon></button>
            </td></ng-container>
            <tr mat-header-row *matHeaderRowDef="meetingColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: meetingColumns;"></tr>
          </table>
        </div>
      </mat-tab>

      <mat-tab label="Calls">
        <div class="tab-content">
          <table mat-table [dataSource]="callsSource" class="full-width">
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Subject</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
            <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row">{{ row.status }}</td></ng-container>
            <ng-container matColumnDef="direction"><th mat-header-cell *matHeaderCellDef>Direction</th><td mat-cell *matCellDef="let row">{{ row.direction }}</td></ng-container>
            <ng-container matColumnDef="dateStart"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let row">{{ row.dateStart | date:'medium' }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
              <button mat-icon-button><mat-icon>visibility</mat-icon></button>
            </td></ng-container>
            <tr mat-header-row *matHeaderRowDef="callColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: callColumns;"></tr>
          </table>
        </div>
      </mat-tab>

      <mat-tab label="Tasks">
        <div class="tab-content">
          <table mat-table [dataSource]="tasksSource" class="full-width">
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Subject</th><td mat-cell *matCellDef="let row">{{ row.name }}</td></ng-container>
            <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let row">{{ row.status }}</td></ng-container>
            <ng-container matColumnDef="priority"><th mat-header-cell *matHeaderCellDef>Priority</th><td mat-cell *matCellDef="let row"><span class="priority-badge" [ngClass]="row.priority?.toLowerCase()">{{ row.priority }}</span></td></ng-container>
            <ng-container matColumnDef="dateDue"><th mat-header-cell *matHeaderCellDef>Due Date</th><td mat-cell *matCellDef="let row">{{ row.dateDue | date }}</td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Actions</th><td mat-cell *matCellDef="let row">
              <button mat-icon-button><mat-icon>visibility</mat-icon></button>
              <button mat-icon-button color="primary"><mat-icon>check_circle</mat-icon></button>
            </td></ng-container>
            <tr mat-header-row *matHeaderRowDef="taskColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: taskColumns;"></tr>
          </table>
        </div>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
    .action-buttons { display: flex; gap: 8px; }
    .tab-content { padding: 16px 0; }
    .full-width { width: 100%; }
    .status-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; background: #e3f2fd; color: #1565c0; }
    .priority-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    .high { background: #fce4ec; color: #c62828; }
    .medium { background: #fff3e0; color: #e65100; }
    .low { background: #e8f5e9; color: #2e7d32; }
  `]
})
export class ActivityListComponent implements OnInit {
  meetingColumns = ['name', 'status', 'dateStart', 'location', 'actions'];
  callColumns = ['name', 'status', 'direction', 'dateStart', 'actions'];
  taskColumns = ['name', 'status', 'priority', 'dateDue', 'actions'];

  meetingsSource = new MatTableDataSource<any>();
  callsSource = new MatTableDataSource<any>();
  tasksSource = new MatTableDataSource<any>();

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/meetings`).subscribe({
      next: (res) => this.meetingsSource.data = res.content || [],
      error: () => this.meetingsSource.data = []
    });
    this.http.get<any>(`${environment.apiUrl}/calls`).subscribe({
      next: (res) => this.callsSource.data = res.content || [],
      error: () => this.callsSource.data = []
    });
    this.http.get<any>(`${environment.apiUrl}/tasks`).subscribe({
      next: (res) => this.tasksSource.data = res.content || [],
      error: () => this.tasksSource.data = []
    });
  }
}
