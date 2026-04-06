import { Component, OnInit } from '@angular/core';
import { DashboardService, Dashboard, Dashlet } from './dashboard.service';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h2>{{ currentDashboard?.name || 'Dashboard' }}</h2>
        <div class="dashboard-actions">
          <select [(ngModel)]="selectedDashboardId" (change)="loadDashboard()">
            <option *ngFor="let d of dashboards" [value]="d.id">{{ d.name }}</option>
          </select>
          <button (click)="addDashlet()">Add Dashlet</button>
        </div>
      </div>
      <div class="dashlet-grid">
        <app-dashlet *ngFor="let dashlet of dashlets"
          [dashlet]="dashlet"
          (remove)="removeDashlet(dashlet.id)">
        </app-dashlet>
      </div>
      <div *ngIf="dashlets.length === 0" class="empty-state">
        <p>No dashlets configured. Click "Add Dashlet" to get started.</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container { padding: 20px; }
    .dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .dashboard-actions { display: flex; gap: 10px; }
    .dashlet-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 16px; }
    .empty-state { text-align: center; padding: 40px; color: #666; }
    select, button { padding: 8px 16px; border-radius: 4px; border: 1px solid #ccc; }
    button { background: #1976d2; color: white; border: none; cursor: pointer; }
  `]
})
export class DashboardComponent implements OnInit {
  dashboards: Dashboard[] = [];
  currentDashboard: Dashboard | null = null;
  dashlets: Dashlet[] = [];
  selectedDashboardId = '';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit() {
    this.dashboardService.getUserDashboards().subscribe(dashboards => {
      this.dashboards = dashboards;
      if (dashboards.length > 0) {
        this.selectedDashboardId = dashboards[0].id;
        this.loadDashboard();
      }
    });
  }

  loadDashboard() {
    if (this.selectedDashboardId) {
      this.dashboardService.getDashboard(this.selectedDashboardId).subscribe(d => {
        this.currentDashboard = d;
      });
    }
  }

  addDashlet() { /* Opens dashlet picker dialog */ }
  removeDashlet(id: string) { /* Removes dashlet */ }
}
