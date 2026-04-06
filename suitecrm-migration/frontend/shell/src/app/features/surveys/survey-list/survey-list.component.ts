import { Component, OnInit } from '@angular/core';
import { SurveyService, Survey } from '../../../services/survey.service';

@Component({
  selector: 'app-survey-list',
  template: `
    <div class="survey-list-container">
      <div class="toolbar">
        <h2>Surveys</h2>
        <button class="btn-primary">Create Survey</button>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>Name</th><th>Type</th><th>Status</th><th>Responses</th><th>Anonymous</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let survey of surveys" [routerLink]="[survey.id]">
            <td>{{ survey.name }}</td>
            <td>{{ survey.surveyType }}</td>
            <td><span class="badge" [ngClass]="survey.status">{{ survey.status }}</span></td>
            <td>{{ survey.responseCount }}</td>
            <td>{{ survey.isAnonymous ? 'Yes' : 'No' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class SurveyListComponent implements OnInit {
  surveys: Survey[] = [];

  constructor(private surveyService: SurveyService) {}

  ngOnInit(): void {
    this.surveyService.getSurveys().subscribe(data => this.surveys = data.content || []);
  }
}
