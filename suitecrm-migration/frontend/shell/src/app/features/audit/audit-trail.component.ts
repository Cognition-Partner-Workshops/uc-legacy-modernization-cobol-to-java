import { Component, Input, OnInit } from '@angular/core';
import { AuditService, AuditEntry } from './audit.service';

@Component({
  selector: 'app-audit-trail',
  template: `
    <div class="audit-trail" *ngIf="entries.length > 0">
      <h3>Change History</h3>
      <div class="audit-entry" *ngFor="let entry of entries">
        <div class="audit-header">
          <span class="field-name">{{ entry.fieldName }}</span>
          <span class="audit-date">{{ entry.dateCreated | date:'medium' }}</span>
        </div>
        <div class="audit-values">
          <span class="old-value" *ngIf="entry.beforeValueString">{{ entry.beforeValueString }}</span>
          <span class="arrow" *ngIf="entry.beforeValueString">&rarr;</span>
          <span class="new-value">{{ entry.afterValueString }}</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .audit-trail { padding: 16px; border: 1px solid #e0e0e0; border-radius: 8px; margin-top: 16px; }
    .audit-entry { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
    .audit-header { display: flex; justify-content: space-between; }
    .field-name { font-weight: 600; }
    .audit-date { color: #999; font-size: 12px; }
    .old-value { color: #f44336; text-decoration: line-through; }
    .new-value { color: #4caf50; }
    .arrow { margin: 0 8px; color: #999; }
  `]
})
export class AuditTrailComponent implements OnInit {
  @Input() module = '';
  @Input() recordId = '';
  entries: AuditEntry[] = [];

  constructor(private auditService: AuditService) {}

  ngOnInit() {
    if (this.module && this.recordId) {
      this.auditService.getAuditTrail(this.module, this.recordId).subscribe(e => this.entries = e);
    }
  }
}
