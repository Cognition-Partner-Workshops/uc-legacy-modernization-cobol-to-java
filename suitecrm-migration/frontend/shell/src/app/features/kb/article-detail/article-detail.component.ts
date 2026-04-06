import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KBService, KBArticle } from '../../../services/kb.service';

@Component({
  selector: 'app-kb-article-detail',
  template: `
    <div class="article-detail" *ngIf="article">
      <div class="article-header">
        <h1>{{ article.name }}</h1>
        <div class="meta">
          <span class="badge" [ngClass]="article.status">{{ article.status }}</span>
          <span>Views: {{ article.viewCount }}</span>
          <span>Revision: {{ article.revision }}</span>
        </div>
      </div>
      <div class="article-body" [innerHTML]="article.body"></div>
      <div class="article-footer">
        <p>Was this article helpful?</p>
        <button (click)="rate(true)" class="btn-success">Yes</button>
        <button (click)="rate(false)" class="btn-danger">No</button>
        <span *ngIf="article.helpfulCount">{{ article.helpfulCount }} found this helpful</span>
      </div>
    </div>
  `
})
export class KBArticleDetailComponent implements OnInit {
  article: KBArticle | null = null;

  constructor(private route: ActivatedRoute, private kbService: KBService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.kbService.getArticle(id).subscribe(article => this.article = article);
    }
  }

  rate(helpful: boolean): void {
    if (this.article) {
      this.kbService.rateArticle(this.article.id, helpful).subscribe();
    }
  }
}
