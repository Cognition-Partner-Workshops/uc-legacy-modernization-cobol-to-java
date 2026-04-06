import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { AlertListComponent } from './alert-list/alert-list.component';
import { FavoritesListComponent } from './favorites-list/favorites-list.component';

const routes: Routes = [
  { path: '', redirectTo: 'alerts', pathMatch: 'full' },
  { path: 'alerts', component: AlertListComponent },
  { path: 'favorites', component: FavoritesListComponent }
];

@NgModule({
  declarations: [AlertListComponent, FavoritesListComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class NotificationsModule {}
