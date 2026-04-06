import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { SurveyListComponent } from './survey-list/survey-list.component';
import { SurveyDetailComponent } from './survey-detail/survey-detail.component';

const routes: Routes = [
  { path: '', component: SurveyListComponent },
  { path: ':id', component: SurveyDetailComponent }
];

@NgModule({
  declarations: [SurveyListComponent, SurveyDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class SurveysModule {}
