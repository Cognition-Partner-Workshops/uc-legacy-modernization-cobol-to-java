import { Component } from '@angular/core';
import { SearchService, SearchResult } from './search.service';

@Component({
  selector: 'app-global-search',
  template: `
    <div class="search-container">
      <div class="search-bar">
        <input type="text" [(ngModel)]="query" (keyup.enter)="search()"
          placeholder="Search across all modules..." class="search-input">
        <select [(ngModel)]="selectedModule">
          <option value="">All Modules</option>
          <option *ngFor="let m of modules" [value]="m">{{ m }}</option>
        </select>
        <button (click)="search()">Search</button>
      </div>
      <div class="search-results" *ngIf="results.length > 0">
        <div class="result-card" *ngFor="let result of results">
          <div class="result-header">
            <span class="module-badge">{{ result._source?.module || 'Unknown' }}</span>
            <h3>{{ result._source?.name || 'Untitled' }}</h3>
          </div>
          <p class="result-snippet" [innerHTML]="getHighlight(result)"></p>
        </div>
      </div>
      <div *ngIf="searched && results.length === 0" class="no-results">
        No results found for "{{ query }}"
      </div>
    </div>
  `,
  styles: [`
    .search-container { max-width: 900px; margin: 0 auto; padding: 20px; }
    .search-bar { display: flex; gap: 8px; margin-bottom: 20px; }
    .search-input { flex: 1; padding: 12px; font-size: 16px; border: 2px solid #1976d2; border-radius: 4px; }
    select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
    button { padding: 12px 24px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
    .result-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
    .result-header { display: flex; align-items: center; gap: 12px; }
    .module-badge { background: #e3f2fd; color: #1976d2; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
    .result-snippet { color: #666; margin-top: 8px; }
    .no-results { text-align: center; padding: 40px; color: #666; }
  `]
})
export class GlobalSearchComponent {
  query = '';
  selectedModule = '';
  results: any[] = [];
  searched = false;
  modules = ['Accounts', 'Contacts', 'Leads', 'Opportunities', 'Cases', 'Campaigns', 'Emails', 'Documents'];

  constructor(private searchService: SearchService) {}

  search() {
    if (!this.query.trim()) return;
    this.searched = true;
    this.searchService.search(this.query, this.selectedModule).subscribe(response => {
      this.results = response?.hits?.hits || [];
    });
  }

  getHighlight(result: any): string {
    const highlights = result.highlight;
    if (!highlights) return result._source?.description || '';
    const firstField = Object.keys(highlights)[0];
    return highlights[firstField]?.[0] || '';
  }
}
