import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  template: `
    <div class="app-container" *ngIf="authService.isAuthenticated(); else loginTemplate">
      <app-header
        [user]="authService.currentUser"
        (toggleSidenav)="sidenavOpened = !sidenavOpened"
        (logout)="onLogout()">
      </app-header>
      <div class="main-content">
        <app-sidenav
          [opened]="sidenavOpened"
          [userRoles]="authService.getUserRoles()">
        </app-sidenav>
        <div class="sidenav-content">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
    <ng-template #loginTemplate>
      <app-login></app-login>
    </ng-template>
  `,
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
    }
    .main-content {
      display: flex;
      flex: 1;
      overflow: hidden;
    }
    .sidenav-content {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
      background-color: #fafafa;
    }
  `]
})
export class AppComponent implements OnInit {
  sidenavOpened = true;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
