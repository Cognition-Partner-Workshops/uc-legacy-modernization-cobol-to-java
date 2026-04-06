import { Component, OnInit } from '@angular/core';
import { QuoteService, Quote } from '../../../services/quote.service';

@Component({
  selector: 'app-quote-list',
  template: `
    <div class="container">
      <h2>Quotes</h2>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Stage</th><th>Total</th><th>Valid Until</th></tr></thead>
        <tbody>
          <tr *ngFor="let q of quotes" [routerLink]="['/quotes', q.id]">
            <td>{{q.name}}</td><td>{{q.quoteStage}}</td>
            <td>{{q.totalAmount | currency}}</td><td>{{q.validUntil}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class QuoteListComponent implements OnInit {
  quotes: Quote[] = [];
  constructor(private quoteService: QuoteService) {}
  ngOnInit() { this.quoteService.getQuotes().subscribe((res: any) => this.quotes = res.content || []); }
}
