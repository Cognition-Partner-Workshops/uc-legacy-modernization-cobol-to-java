import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ProjectService, Project } from '../../../core/services/project.service';

@Component({
  selector: 'app-project-list',
  template: `
    <div class="list-container">
      <div class="list-header">
        <h2>Projects</h2>
        <button mat-raised-button color="primary" (click)="createProject()">
          <mat-icon>add</mat-icon> New Project
        </button>
      </div>

      <mat-form-field appearance="outline" class="search-field">
        <mat-label>Search Projects</mat-label>
        <input matInput (keyup)="applyFilter($event)" placeholder="Search by name...">
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <mat-table [dataSource]="projects" matSort>
        <ng-container matColumnDef="name">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Name</mat-header-cell>
          <mat-cell *matCellDef="let project">
            <a (click)="viewProject(project.id)" class="link">{{ project.name }}</a>
          </mat-cell>
        </ng-container>

        <ng-container matColumnDef="status">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Status</mat-header-cell>
          <mat-cell *matCellDef="let project">
            <span class="status-chip">{{ project.status }}</span>
          </mat-cell>
        </ng-container>

        <ng-container matColumnDef="priority">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Priority</mat-header-cell>
          <mat-cell *matCellDef="let project">{{ project.priority }}</mat-cell>
        </ng-container>

        <ng-container matColumnDef="estimatedEndDate">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Due Date</mat-header-cell>
          <mat-cell *matCellDef="let project">{{ project.estimatedEndDate | date }}</mat-cell>
        </ng-container>

        <ng-container matColumnDef="completionPercentage">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Progress</mat-header-cell>
          <mat-cell *matCellDef="let project">
            <mat-progress-bar mode="determinate" [value]="project.completionPercentage || 0"></mat-progress-bar>
            <span class="progress-text">{{ (project.completionPercentage || 0) | number:'1.0-0' }}%</span>
          </mat-cell>
        </ng-container>

        <ng-container matColumnDef="taskCount">
          <mat-header-cell *matHeaderCellDef>Tasks</mat-header-cell>
          <mat-cell *matCellDef="let project">{{ project.taskCount || 0 }}</mat-cell>
        </ng-container>

        <mat-header-row *matHeaderRowDef="displayedColumns"></mat-header-row>
        <mat-row *matRowDef="let row; columns: displayedColumns;" (click)="viewProject(row.id)"></mat-row>
      </mat-table>

      <mat-paginator [pageSizeOptions]="[10, 25, 50]" showFirstLastButtons></mat-paginator>
    </div>
  `,
  styles: [`
    .list-container { padding: 20px; }
    .list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .search-field { width: 400px; }
    .link { color: #1976d2; cursor: pointer; text-decoration: none; }
    .link:hover { text-decoration: underline; }
    mat-row { cursor: pointer; }
    mat-row:hover { background-color: #f5f5f5; }
    .progress-text { margin-left: 8px; font-size: 0.85em; }
    .status-chip { padding: 2px 8px; border-radius: 4px; font-size: 0.85em; }
  `]
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  displayedColumns = ['name', 'status', 'priority', 'estimatedEndDate', 'completionPercentage', 'taskCount'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private projectService: ProjectService, private router: Router) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getProjects().subscribe((response: any) => {
      this.projects = response.content || response;
    });
  }

  viewProject(id: string): void {
    this.router.navigate(['/projects', id]);
  }

  createProject(): void {
    this.router.navigate(['/projects', 'new']);
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    // Implement client-side filtering or server-side search
  }
}
