import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule), canActivate: [AuthGuard] },
  { path: 'accounts', loadChildren: () => import('./features/accounts/accounts.module').then(m => m.AccountsModule), canActivate: [AuthGuard] },
  { path: 'contacts', loadChildren: () => import('./features/contacts/contacts.module').then(m => m.ContactsModule), canActivate: [AuthGuard] },
  { path: 'leads', loadChildren: () => import('./features/leads/leads.module').then(m => m.LeadsModule), canActivate: [AuthGuard] },
  { path: 'opportunities', loadChildren: () => import('./features/opportunities/opportunities.module').then(m => m.OpportunitiesModule), canActivate: [AuthGuard] },
  { path: 'cases', loadChildren: () => import('./features/cases/cases.module').then(m => m.CasesModule), canActivate: [AuthGuard] },
  { path: 'campaigns', loadChildren: () => import('./features/campaigns/campaigns.module').then(m => m.CampaignsModule), canActivate: [AuthGuard] },
  { path: 'activities', loadChildren: () => import('./features/activities/activities.module').then(m => m.ActivitiesModule), canActivate: [AuthGuard] },
  { path: 'reports', loadChildren: () => import('./features/reports/reports.module').then(m => m.ReportsModule), canActivate: [AuthGuard] },
  { path: 'documents', loadChildren: () => import('./features/documents/documents.module').then(m => m.DocumentsModule), canActivate: [AuthGuard] },
  { path: 'emails', loadChildren: () => import('./features/emails/emails.module').then(m => m.EmailsModule), canActivate: [AuthGuard] },
  { path: 'projects', loadChildren: () => import('./features/projects/projects.module').then(m => m.ProjectsModule), canActivate: [AuthGuard] },
  { path: 'knowledge-base', loadChildren: () => import('./features/knowledge-base/knowledge-base.module').then(m => m.KnowledgeBaseModule), canActivate: [AuthGuard] },
  { path: 'events', loadChildren: () => import('./features/events/events.module').then(m => m.EventsModule), canActivate: [AuthGuard] },
  { path: 'surveys', loadChildren: () => import('./features/surveys/surveys.module').then(m => m.SurveysModule), canActivate: [AuthGuard] },
  { path: 'target-lists', loadChildren: () => import('./features/target-lists/target-lists.module').then(m => m.TargetListsModule), canActivate: [AuthGuard] },
  { path: 'products', loadChildren: () => import('./features/products/products.module').then(m => m.ProductsModule), canActivate: [AuthGuard] },
  { path: 'connectors', loadChildren: () => import('./features/connectors/connectors.module').then(m => m.ConnectorsModule), canActivate: [AuthGuard] },
  { path: 'search', loadChildren: () => import('./features/search/search.module').then(m => m.SearchModule), canActivate: [AuthGuard] },
  { path: 'import', loadChildren: () => import('./features/import-export/import-export.module').then(m => m.ImportExportModule), canActivate: [AuthGuard] },
  { path: 'admin', loadChildren: () => import('./features/admin/admin.module').then(m => m.AdminModule), canActivate: [AuthGuard], data: { roles: ['Admin'] } },
  { path: 'login', loadChildren: () => import('./features/login/login.module').then(m => m.LoginModule) },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
