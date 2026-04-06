import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

interface DashboardCard {
  title: string;
  value: string;
  icon: string;
  color: string;
  route: string;
}

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="page-header">
      <h1>Dashboard</h1>
      <p>Welcome back, {{ authService.currentUser?.fullName }}</p>
    </div>

    <div class="dashboard-cards">
      <div class="dashboard-card" *ngFor="let card of cards"
           [style.border-left-color]="card.color"
           [routerLink]="card.route">
        <div class="card-content">
          <div class="card-info">
            <span class="card-title">{{ card.title }}</span>
            <span class="card-value">{{ card.value }}</span>
          </div>
          <mat-icon [style.color]="card.color">{{ card.icon }}</mat-icon>
        </div>
      </div>
    </div>

    <div class="dashboard-widgets">
      <div class="widget">
        <h3>Recent Activities</h3>
        <mat-list>
          <mat-list-item>
            <mat-icon matListItemIcon>phone</mat-icon>
            <span matListItemTitle>Call with Acme Corp</span>
            <span matListItemLine>Today at 2:00 PM</span>
          </mat-list-item>
          <mat-list-item>
            <mat-icon matListItemIcon>meeting_room</mat-icon>
            <span matListItemTitle>Sales Review Meeting</span>
            <span matListItemLine>Tomorrow at 10:00 AM</span>
          </mat-list-item>
          <mat-list-item>
            <mat-icon matListItemIcon>task</mat-icon>
            <span matListItemTitle>Follow up with Lead - John Doe</span>
            <span matListItemLine>Due in 2 days</span>
          </mat-list-item>
        </mat-list>
      </div>

      <div class="widget">
        <h3>Pipeline Summary</h3>
        <div class="pipeline-stages">
          <div class="stage" *ngFor="let stage of pipelineStages">
            <span class="stage-name">{{ stage.name }}</span>
            <div class="stage-bar">
              <div class="stage-fill" [style.width]="stage.percentage + '%'" [style.background]="stage.color"></div>
            </div>
            <span class="stage-value">{{ stage.value }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .dashboard-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      border-left: 4px solid;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      cursor: pointer;
      transition: box-shadow 0.2s;
      &:hover { box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    }
    .card-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .card-info { display: flex; flex-direction: column; }
    .card-title { font-size: 14px; color: #666; }
    .card-value { font-size: 28px; font-weight: 500; margin-top: 4px; }
    .dashboard-widgets {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    .widget {
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      h3 { margin: 0 0 16px; font-weight: 500; }
    }
    .pipeline-stages { display: flex; flex-direction: column; gap: 12px; }
    .stage {
      display: grid;
      grid-template-columns: 120px 1fr 60px;
      align-items: center;
      gap: 12px;
    }
    .stage-name { font-size: 13px; color: #666; }
    .stage-bar { height: 8px; background: #e0e0e0; border-radius: 4px; overflow: hidden; }
    .stage-fill { height: 100%; border-radius: 4px; transition: width 0.3s; }
    .stage-value { font-size: 13px; font-weight: 500; text-align: right; }
  `]
})
export class DashboardComponent implements OnInit {
  cards: DashboardCard[] = [
    { title: 'Open Opportunities', value: '24', icon: 'trending_up', color: '#4CAF50', route: '/opportunities' },
    { title: 'Active Cases', value: '12', icon: 'support_agent', color: '#FF9800', route: '/cases' },
    { title: 'New Leads', value: '38', icon: 'person_add', color: '#2196F3', route: '/contacts/leads' },
    { title: 'Active Campaigns', value: '5', icon: 'campaign', color: '#9C27B0', route: '/campaigns' }
  ];

  pipelineStages = [
    { name: 'Prospecting', percentage: 80, value: '$120K', color: '#2196F3' },
    { name: 'Qualification', percentage: 65, value: '$95K', color: '#4CAF50' },
    { name: 'Proposal', percentage: 40, value: '$68K', color: '#FF9800' },
    { name: 'Negotiation', percentage: 25, value: '$42K', color: '#F44336' },
    { name: 'Closed Won', percentage: 15, value: '$28K', color: '#9C27B0' }
  ];

  constructor(public authService: AuthService) {}

  ngOnInit(): void {}
}
