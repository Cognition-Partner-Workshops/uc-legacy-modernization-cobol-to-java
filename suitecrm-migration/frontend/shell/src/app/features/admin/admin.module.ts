import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdminPanelComponent } from './admin-panel/admin-panel.component';
import { SystemSettingsComponent } from './system-settings/system-settings.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { RoleManagementComponent } from './role-management/role-management.component';
import { ModuleConfigComponent } from './module-config/module-config.component';
import { AdminService } from './admin.service';

const routes: Routes = [
  { path: '', component: AdminPanelComponent, children: [
    { path: 'settings', component: SystemSettingsComponent },
    { path: 'users', component: UserManagementComponent },
    { path: 'roles', component: RoleManagementComponent },
    { path: 'modules', component: ModuleConfigComponent },
    { path: '', redirectTo: 'settings', pathMatch: 'full' }
  ]}
];

@NgModule({
  declarations: [AdminPanelComponent, SystemSettingsComponent, UserManagementComponent, RoleManagementComponent, ModuleConfigComponent],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, ReactiveFormsModule],
  providers: [AdminService]
})
export class AdminModule {}
