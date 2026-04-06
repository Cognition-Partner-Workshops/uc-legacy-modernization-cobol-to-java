import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { ConnectorListComponent } from './connector-list/connector-list.component';
import { ConnectorDetailComponent } from './connector-detail/connector-detail.component';

const routes: Routes = [
  { path: '', component: ConnectorListComponent },
  { path: ':id', component: ConnectorDetailComponent }
];

@NgModule({
  declarations: [ConnectorListComponent, ConnectorDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class ConnectorsModule {}
