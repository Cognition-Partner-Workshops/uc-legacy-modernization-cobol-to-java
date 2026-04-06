import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService, Project, ProjectTask } from '../../../core/services/project.service';

@Component({
  selector: 'app-project-detail',
  template: `
    <div class="detail-container" *ngIf="project">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ project.name }}</mat-card-title>
          <mat-card-subtitle>
            {{ project.status }} | {{ project.priority }} Priority
          </mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="progress-section">
            <h3>Overall Progress</h3>
            <mat-progress-bar mode="determinate" [value]="project.completionPercentage || 0"></mat-progress-bar>
            <span>{{ (project.completionPercentage || 0) | number:'1.0-0' }}% Complete</span>
          </div>

          <div class="detail-grid">
            <div class="detail-section">
              <h3>Timeline</h3>
              <div class="field"><label>Est. Start:</label><span>{{ project.estimatedStartDate | date }}</span></div>
              <div class="field"><label>Est. End:</label><span>{{ project.estimatedEndDate | date }}</span></div>
              <div class="field"><label>Actual Start:</label><span>{{ project.actualStartDate | date }}</span></div>
              <div class="field"><label>Actual End:</label><span>{{ project.actualEndDate | date }}</span></div>
            </div>
            <div class="detail-section">
              <h3>Cost</h3>
              <div class="field"><label>Estimated:</label><span>{{ project.estimatedCost | currency }}</span></div>
              <div class="field"><label>Actual:</label><span>{{ project.actualCost | currency }}</span></div>
            </div>
          </div>

          <p *ngIf="project.description">{{ project.description }}</p>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="editProject()">Edit</button>
          <button mat-raised-button color="warn" (click)="deleteProject()">Delete</button>
          <button mat-button (click)="goBack()">Back to List</button>
        </mat-card-actions>
      </mat-card>

      <mat-card class="tasks-card">
        <mat-card-header>
          <mat-card-title>Project Tasks ({{ tasks.length }})</mat-card-title>
          <button mat-mini-fab color="primary" (click)="addTask()"><mat-icon>add</mat-icon></button>
        </mat-card-header>
        <mat-card-content>
          <mat-table [dataSource]="tasks" *ngIf="tasks.length > 0">
            <ng-container matColumnDef="name">
              <mat-header-cell *matHeaderCellDef>Task</mat-header-cell>
              <mat-cell *matCellDef="let task">
                <mat-icon *ngIf="task.milestoneFlag" color="accent">flag</mat-icon>
                {{ task.name }}
              </mat-cell>
            </ng-container>
            <ng-container matColumnDef="status">
              <mat-header-cell *matHeaderCellDef>Status</mat-header-cell>
              <mat-cell *matCellDef="let task">{{ task.status }}</mat-cell>
            </ng-container>
            <ng-container matColumnDef="percentComplete">
              <mat-header-cell *matHeaderCellDef>Progress</mat-header-cell>
              <mat-cell *matCellDef="let task">
                <mat-progress-bar mode="determinate" [value]="task.percentComplete"></mat-progress-bar>
                {{ task.percentComplete }}%
              </mat-cell>
            </ng-container>
            <ng-container matColumnDef="dateDue">
              <mat-header-cell *matHeaderCellDef>Due Date</mat-header-cell>
              <mat-cell *matCellDef="let task">{{ task.dateDue | date }}</mat-cell>
            </ng-container>
            <mat-header-row *matHeaderRowDef="taskColumns"></mat-header-row>
            <mat-row *matRowDef="let row; columns: taskColumns;"></mat-row>
          </mat-table>
          <p *ngIf="tasks.length === 0">No tasks yet. Click + to add a task.</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .detail-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 16px; }
    .detail-section h3 { color: #1976d2; border-bottom: 1px solid #e0e0e0; padding-bottom: 8px; }
    .field { display: flex; padding: 4px 0; }
    .field label { font-weight: 500; width: 150px; color: #666; }
    .progress-section { margin: 16px 0; }
    .progress-section mat-progress-bar { margin: 8px 0; }
    .tasks-card { margin-top: 20px; }
    .tasks-card mat-card-header { display: flex; justify-content: space-between; align-items: center; }
  `]
})
export class ProjectDetailComponent implements OnInit {
  project!: Project;
  tasks: ProjectTask[] = [];
  taskColumns = ['name', 'status', 'percentComplete', 'dateDue'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.projectService.getProject(id).subscribe(p => this.project = p);
      this.projectService.getProjectTasks(id).subscribe(tasks => this.tasks = tasks);
    }
  }

  editProject(): void {
    this.router.navigate(['/projects', this.project.id, 'edit']);
  }

  deleteProject(): void {
    if (confirm('Are you sure you want to delete this project?')) {
      this.projectService.deleteProject(this.project.id).subscribe(() => {
        this.router.navigate(['/projects']);
      });
    }
  }

  addTask(): void {
    // Open task creation dialog/form
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }
}
