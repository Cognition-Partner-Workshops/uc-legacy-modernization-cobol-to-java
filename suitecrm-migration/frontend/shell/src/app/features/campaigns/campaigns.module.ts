import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';

import { CampaignListComponent } from './campaign-list/campaign-list.component';

const routes: Routes = [
  { path: '', component: CampaignListComponent }
];

@NgModule({
  declarations: [CampaignListComponent],
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule.forChild(routes),
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatChipsModule
  ]
})
export class CampaignsModule { }
