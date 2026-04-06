import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { TargetListListComponent } from './target-list-list/target-list-list.component';
import { TargetListDetailComponent } from './target-list-detail/target-list-detail.component';

const routes: Routes = [
  { path: '', component: TargetListListComponent },
  { path: ':id', component: TargetListDetailComponent }
];

@NgModule({
  declarations: [TargetListListComponent, TargetListDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class TargetListsModule {}
