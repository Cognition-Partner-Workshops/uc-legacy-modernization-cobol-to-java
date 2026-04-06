import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { QuoteService, Quote } from '../../../services/quote.service';

@Component({
  selector: 'app-quote-detail',
  template: `
    <div class="container" *ngIf="quote">
      <h2>{{quote.name}}</h2>
      <div class="detail-grid">
        <div><label>Quote #:</label><span>{{quote.quoteNum}}</span></div>
        <div><label>Stage:</label><span>{{quote.quoteStage}}</span></div>
        <div><label>Total:</label><span>{{quote.totalAmount | currency}}</span></div>
        <div><label>Valid Until:</label><span>{{quote.validUntil}}</span></div>
        <div><label>Payment Terms:</label><span>{{quote.paymentTerms}}</span></div>
      </div>
    </div>
  `
})
export class QuoteDetailComponent implements OnInit {
  quote: Quote | null = null;
  constructor(private route: ActivatedRoute, private quoteService: QuoteService) {}
  ngOnInit() { this.route.params.subscribe(p => this.quoteService.getQuoteById(p['id']).subscribe(q => this.quote = q)); }
}
