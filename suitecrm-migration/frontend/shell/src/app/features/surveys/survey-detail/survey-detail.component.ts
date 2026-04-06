import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SurveyService, Survey, SurveyQuestion } from '../../../services/survey.service';

@Component({
  selector: 'app-survey-detail',
  template: `
    <div class="survey-detail" *ngIf="survey">
      <h1>{{ survey.name }}</h1>
      <div class="survey-meta">
        <span class="badge" [ngClass]="survey.status">{{ survey.status }}</span>
        <span>Type: {{ survey.surveyType }}</span>
        <span>Responses: {{ survey.responseCount }}</span>
        <span *ngIf="averageHappiness !== null">Avg Happiness: {{ averageHappiness | number:'1.1-1' }}/5</span>
      </div>
      <p>{{ survey.description }}</p>
      <h3>Questions ({{ questions.length }})</h3>
      <div *ngFor="let q of questions; let i = index" class="question-card">
        <h4>Q{{ i + 1 }}: {{ q.name }}</h4>
        <span class="question-type">{{ q.questionType }}</span>
        <span *ngIf="q.isRequired" class="required-badge">Required</span>
        <div *ngIf="q.options?.length" class="options-list">
          <div *ngFor="let opt of q.options">- {{ opt.name }}</div>
        </div>
      </div>
    </div>
  `
})
export class SurveyDetailComponent implements OnInit {
  survey: Survey | null = null;
  questions: SurveyQuestion[] = [];
  averageHappiness: number | null = null;

  constructor(private route: ActivatedRoute, private surveyService: SurveyService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.surveyService.getSurvey(id).subscribe(s => this.survey = s);
      this.surveyService.getQuestions(id).subscribe(qs => this.questions = qs);
      this.surveyService.getAverageHappiness(id).subscribe(h => this.averageHappiness = h);
    }
  }
}
