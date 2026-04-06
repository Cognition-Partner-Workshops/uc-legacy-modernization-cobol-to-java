import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GlobalSearchComponent } from './global-search.component';
import { SearchService } from './search.service';

const routes: Routes = [
  { path: '', component: GlobalSearchComponent }
];

@NgModule({
  declarations: [GlobalSearchComponent],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule],
  providers: [SearchService]
})
export class SearchModule {}
