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
    path: 'projects',
    loadChildren: () => import('./features/projects/projects.module').then(m => m.ProjectsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'emails',
    loadChildren: () => import('./features/emails/emails.module').then(m => m.EmailsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'kb',
    loadChildren: () => import('./features/kb/kb.module').then(m => m.KBModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'events',
    loadChildren: () => import('./features/events/events.module').then(m => m.EventsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'surveys',
    loadChildren: () => import('./features/surveys/surveys.module').then(m => m.SurveysModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'products',
    loadChildren: () => import('./features/products/products.module').then(m => m.ProductsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'target-lists',
    loadChildren: () => import('./features/target-lists/target-lists.module').then(m => m.TargetListsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'connectors',
    loadChildren: () => import('./features/connectors/connectors.module').then(m => m.ConnectorsModule),
    canActivate: [AuthGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  {
    path: 'leads',
    loadChildren: () => import('./features/leads/leads.module').then(m => m.LeadsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'quotes',
    loadChildren: () => import('./features/quotes/quotes.module').then(m => m.QuotesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'calendar',
    loadChildren: () => import('./features/calendar/calendar.module').then(m => m.CalendarModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'maps',
    loadChildren: () => import('./features/maps/maps.module').then(m => m.MapsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'templates',
    loadChildren: () => import('./features/templates/templates.module').then(m => m.TemplatesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'scheduler',
    loadChildren: () => import('./features/scheduler/scheduler.module').then(m => m.SchedulerModule),
    canActivate: [AuthGuard],
    data: { roles: ['ROLE_ADMIN'] }
  },
  {
    path: 'notifications',
    loadChildren: () => import('./features/notifications/notifications.module').then(m => m.NotificationsModule),
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
