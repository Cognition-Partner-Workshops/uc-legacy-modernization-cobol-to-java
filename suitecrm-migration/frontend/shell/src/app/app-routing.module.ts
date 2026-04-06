import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LoginComponent } from './features/login/login.component';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  {
    path: 'contacts',
    loadChildren: () => import('./features/contacts/contacts.module').then(m => m.ContactsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'accounts',
    loadChildren: () => import('./features/accounts/accounts.module').then(m => m.AccountsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'opportunities',
    loadChildren: () => import('./features/opportunities/opportunities.module').then(m => m.OpportunitiesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'cases',
    loadChildren: () => import('./features/cases/cases.module').then(m => m.CasesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'campaigns',
    loadChildren: () => import('./features/campaigns/campaigns.module').then(m => m.CampaignsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'activities',
    loadChildren: () => import('./features/activities/activities.module').then(m => m.ActivitiesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'reports',
    loadChildren: () => import('./features/reports/reports.module').then(m => m.ReportsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
