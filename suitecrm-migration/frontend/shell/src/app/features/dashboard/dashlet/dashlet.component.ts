import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-dashlet',
  template: `
    <div class="dashlet-card">
      <div class="dashlet-header">
        <h3>{{ dashlet.title || dashlet.name }}</h3>
        <div class="dashlet-controls">
          <button (click)="refresh()" title="Refresh">&#x21bb;</button>
          <button (click)="remove.emit(dashlet.id)" title="Remove">&times;</button>
        </div>
      </div>
      <div class="dashlet-body" [ngSwitch]="dashlet.dashletType">
        <div *ngSwitchCase="'chart'" class="chart-placeholder">Chart: {{ dashlet.dashletModule }}</div>
        <div *ngSwitchCase="'list'" class="list-placeholder">Recent {{ dashlet.dashletModule }}</div>
        <div *ngSwitchDefault class="default-content">{{ dashlet.name }}</div>
      </div>
    </div>
  `,
  styles: [`
    .dashlet-card { border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    .dashlet-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f5f5f5; border-bottom: 1px solid #e0e0e0; }
    .dashlet-header h3 { margin: 0; font-size: 14px; }
    .dashlet-controls button { background: none; border: none; cursor: pointer; font-size: 16px; padding: 4px 8px; }
    .dashlet-body { padding: 16px; min-height: 150px; }
    .chart-placeholder, .list-placeholder { color: #666; text-align: center; padding: 40px 0; }
  `]
})
export class DashletComponent {
  @Input() dashlet: any = {};
  @Output() remove = new EventEmitter<string>();

  refresh() { /* Refresh dashlet data */ }
}
