import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Survey {
  id: string;
  name: string;
  description: string;
  status: string;
  surveyType: string;
  isAnonymous: boolean;
  responseCount: number;
  questions: SurveyQuestion[];
}

export interface SurveyQuestion {
  id: string;
  surveyId: string;
  name: string;
  questionType: string;
  sortOrder: number;
  isRequired: boolean;
  options: SurveyQuestionOption[];
}

export interface SurveyQuestionOption {
  id: string;
  questionId: string;
  name: string;
  sortOrder: number;
}

export interface SurveyResponse {
  id: string;
  surveyId: string;
  contactId: string;
  happiness: number;
  questionResponses: any[];
}

@Injectable({ providedIn: 'root' })
export class SurveyService {
  private apiUrl = `${environment.apiGatewayUrl}/survey-service/api/v1/surveys`;

  constructor(private http: HttpClient) {}

  getSurveys(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getSurvey(id: string): Observable<Survey> {
    return this.http.get<Survey>(`${this.apiUrl}/${id}`);
  }

  createSurvey(survey: Partial<Survey>): Observable<Survey> {
    return this.http.post<Survey>(this.apiUrl, survey);
  }

  updateSurvey(id: string, survey: Partial<Survey>): Observable<Survey> {
    return this.http.put<Survey>(`${this.apiUrl}/${id}`, survey);
  }

  deleteSurvey(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchSurveys(query: string): Observable<any> {
    const params = new HttpParams().set('q', query);
    return this.http.get(`${this.apiUrl}/search`, { params });
  }

  getQuestions(surveyId: string): Observable<SurveyQuestion[]> {
    return this.http.get<SurveyQuestion[]>(`${this.apiUrl}/${surveyId}/questions`);
  }

  addQuestion(surveyId: string, question: Partial<SurveyQuestion>): Observable<SurveyQuestion> {
    return this.http.post<SurveyQuestion>(`${this.apiUrl}/${surveyId}/questions`, question);
  }

  submitResponse(surveyId: string, response: Partial<SurveyResponse>): Observable<SurveyResponse> {
    return this.http.post<SurveyResponse>(`${this.apiUrl}/${surveyId}/responses`, response);
  }

  getResponses(surveyId: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/${surveyId}/responses`, { params });
  }

  getAverageHappiness(surveyId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${surveyId}/analytics/happiness`);
  }
}
