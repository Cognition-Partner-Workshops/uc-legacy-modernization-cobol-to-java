import { Component, OnInit } from '@angular/core';
import { KBService, KBArticle, KBCategory } from '../../../services/kb.service';

@Component({
  selector: 'app-kb-article-list',
  template: `
    <div class="kb-container">
      <div class="toolbar">
        <h2>Knowledge Base</h2>
        <input type="text" placeholder="Search articles..." (input)="onSearch($event)" />
      </div>
      <div class="kb-layout">
        <aside class="category-sidebar">
          <h3>Categories</h3>
          <ul>
            <li *ngFor="let cat of categories" (click)="filterByCategory(cat.id)" [class.active]="selectedCategory === cat.id">
              {{ cat.name }}
              <ul *ngIf="cat.children?.length">
                <li *ngFor="let child of cat.children" (click)="filterByCategory(child.id); $event.stopPropagation()">{{ child.name }}</li>
              </ul>
            </li>
          </ul>
        </aside>
        <main class="article-list">
          <div *ngFor="let article of articles" class="article-card" [routerLink]="[article.id]">
            <h3>{{ article.name }}</h3>
            <p>{{ article.summary }}</p>
            <div class="meta">
              <span class="badge" [ngClass]="article.status">{{ article.status }}</span>
              <span>Views: {{ article.viewCount }}</span>
              <span>Rev: {{ article.revision }}</span>
            </div>
          </div>
        </main>
      </div>
    </div>
  `
})
export class KBArticleListComponent implements OnInit {
  articles: KBArticle[] = [];
  categories: KBCategory[] = [];
  selectedCategory: string | null = null;

  constructor(private kbService: KBService) {}

  ngOnInit(): void {
    this.loadArticles();
    this.kbService.getCategories().subscribe(cats => this.categories = cats);
  }

  loadArticles(): void {
    this.kbService.getArticles().subscribe(data => this.articles = data.content || []);
  }

  filterByCategory(categoryId: string): void {
    this.selectedCategory = categoryId;
    this.loadArticles();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    if (query.length >= 3) {
      this.kbService.searchArticles(query).subscribe(data => this.articles = data.content || []);
    } else if (query.length === 0) {
      this.loadArticles();
    }
  }
}
