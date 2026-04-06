import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { QuoteListComponent } from './quote-list/quote-list.component';
import { QuoteDetailComponent } from './quote-detail/quote-detail.component';

const routes: Routes = [
  { path: '', component: QuoteListComponent },
  { path: ':id', component: QuoteDetailComponent }
];

@NgModule({
  declarations: [QuoteListComponent, QuoteDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class QuotesModule {}
