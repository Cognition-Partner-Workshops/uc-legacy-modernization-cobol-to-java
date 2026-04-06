import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { KBArticleListComponent } from './article-list/article-list.component';
import { KBArticleDetailComponent } from './article-detail/article-detail.component';

const routes: Routes = [
  { path: '', component: KBArticleListComponent },
  { path: ':id', component: KBArticleDetailComponent }
];

@NgModule({
  declarations: [KBArticleListComponent, KBArticleDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class KBModule {}
