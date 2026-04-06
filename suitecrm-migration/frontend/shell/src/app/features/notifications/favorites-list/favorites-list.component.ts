import { Component, OnInit } from '@angular/core';
import { NotificationService, Favorite } from '../../../services/notification.service';

@Component({
  selector: 'app-favorites-list',
  template: `
    <div class="container">
      <h2>Favorites</h2>
      <div class="toolbar"><a routerLink="../alerts" class="btn-secondary">Alerts</a></div>
      <table class="data-table">
        <thead><tr><th>Module</th><th>Record ID</th><th>Date Added</th></tr></thead>
        <tbody>
          <tr *ngFor="let f of favorites">
            <td>{{f.moduleName}}</td><td>{{f.recordId}}</td><td>{{f.dateEntered}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class FavoritesListComponent implements OnInit {
  favorites: Favorite[] = [];
  constructor(private notificationService: NotificationService) {}
  ngOnInit() { /* userId would come from auth context */ }
}
