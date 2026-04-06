import { Component, Input } from '@angular/core';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidenav',
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav [opened]="opened" mode="side" class="sidenav">
        <mat-nav-list>
          <a mat-list-item *ngFor="let item of filteredNavItems"
             [routerLink]="item.route"
             routerLinkActive="active">
            <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: 100%; }
    .sidenav {
      width: 240px;
      background: #fff;
      border-right: 1px solid #e0e0e0;
    }
    .active {
      background-color: rgba(63, 81, 181, 0.08);
      color: #3f51b5;
    }
  `]
})
export class SidenavComponent {
  @Input() opened = true;
  @Input() userRoles: string[] = [];

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Contacts', icon: 'contacts', route: '/contacts' },
    { label: 'Accounts', icon: 'business', route: '/accounts' },
    { label: 'Leads', icon: 'person_add', route: '/contacts/leads' },
    { label: 'Opportunities', icon: 'trending_up', route: '/opportunities', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SALES'] },
    { label: 'Cases', icon: 'support_agent', route: '/cases', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SUPPORT'] },
    { label: 'Campaigns', icon: 'campaign', route: '/campaigns', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_MARKETING'] },
    { label: 'Activities', icon: 'event', route: '/activities' },
    { label: 'Reports', icon: 'assessment', route: '/reports' },
    { label: 'Admin', icon: 'admin_panel_settings', route: '/admin', roles: ['ROLE_ADMIN'] }
  ];

  get filteredNavItems(): NavItem[] {
    return this.navItems.filter(item => {
      if (!item.roles || item.roles.length === 0) return true;
      return item.roles.some(role => this.userRoles.includes(role));
    });
  }
}
