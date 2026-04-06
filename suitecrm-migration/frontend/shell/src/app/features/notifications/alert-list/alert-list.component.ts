import { Component, OnInit } from '@angular/core';
import { NotificationService, Alert } from '../../../services/notification.service';

@Component({
  selector: 'app-alert-list',
  template: `
    <div class="container">
      <h2>Alerts</h2>
      <div class="toolbar"><a routerLink="../favorites" class="btn-secondary">Favorites</a></div>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Type</th><th>Module</th><th>Read</th><th>Date</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let a of alerts" [class.unread]="!a.isRead">
            <td>{{a.name}}</td><td>{{a.alertType}}</td><td>{{a.targetModule}}</td>
            <td>{{a.isRead ? 'Yes' : 'No'}}</td><td>{{a.dateEntered}}</td>
            <td><button *ngIf="!a.isRead" (click)="markRead(a.id)">Mark Read</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class AlertListComponent implements OnInit {
  alerts: Alert[] = [];
  constructor(private notificationService: NotificationService) {}
  ngOnInit() { /* userId would come from auth context */ }
  markRead(id: string) { this.notificationService.markAlertRead(id).subscribe(() => this.ngOnInit()); }
}
