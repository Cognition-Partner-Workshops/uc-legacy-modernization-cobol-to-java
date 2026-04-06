import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Campaign {
  id: string;
  name: string;
  status: string;
  campaignType: string;
  startDate: string;
  endDate: string;
  budget: number;
  actualCost: number;
  expectedRevenue: number;
  expectedCost: number;
  impressions: number;
  description: string;
  assignedUserId: string;
  dateEntered: string;
  dateModified: string;
}

export interface CampaignLog {
  id: string;
  campaignId: string;
  activityType: string;
  activityDate: string;
  targetId: string;
  targetType: string;
  hits: number;
}

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private apiUrl = `${environment.apiGatewayUrl}/api/v1/campaigns`;

  constructor(private http: HttpClient) {}

  getCampaigns(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get(this.apiUrl, { params });
  }

  getCampaign(id: string): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.apiUrl}/${id}`);
  }

  createCampaign(campaign: Partial<Campaign>): Observable<Campaign> {
    return this.http.post<Campaign>(this.apiUrl, campaign);
  }

  updateCampaign(id: string, campaign: Partial<Campaign>): Observable<Campaign> {
    return this.http.put<Campaign>(`${this.apiUrl}/${id}`, campaign);
  }

  deleteCampaign(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCampaignLogs(campaignId: string): Observable<CampaignLog[]> {
    return this.http.get<CampaignLog[]>(`${this.apiUrl}/${campaignId}/logs`);
  }

  getCampaignTrackers(campaignId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${campaignId}/trackers`);
  }
}
