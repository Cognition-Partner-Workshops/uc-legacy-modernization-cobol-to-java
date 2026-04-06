import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  template: `
    <mat-toolbar color="primary" class="app-header">
      <button mat-icon-button (click)="toggleSidenav.emit()">
        <mat-icon>menu</mat-icon>
      </button>
      <span class="app-title">SuiteCRM</span>
      <span class="spacer"></span>

      <button mat-icon-button matBadge="3" matBadgeColor="warn">
        <mat-icon>notifications</mat-icon>
      </button>

      <button mat-button [matMenuTriggerFor]="userMenu" class="user-menu-btn">
        <mat-icon>account_circle</mat-icon>
        <span class="user-name">{{ user?.fullName || 'User' }}</span>
        <mat-icon>arrow_drop_down</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu">
        <button mat-menu-item>
          <mat-icon>person</mat-icon>
          <span>Profile</span>
        </button>
        <button mat-menu-item>
          <mat-icon>settings</mat-icon>
          <span>Settings</span>
        </button>
        <mat-divider></mat-divider>
        <button mat-menu-item (click)="logout.emit()">
          <mat-icon>exit_to_app</mat-icon>
          <span>Logout</span>
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .app-header {
      position: sticky;
      top: 0;
      z-index: 1000;
    }
    .app-title {
      margin-left: 8px;
      font-size: 20px;
      font-weight: 500;
    }
    .spacer { flex: 1 1 auto; }
    .user-menu-btn {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .user-name {
      margin: 0 4px;
      font-size: 14px;
    }
  `]
})
export class HeaderComponent {
  @Input() user: User | null = null;
  @Output() toggleSidenav = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
}
