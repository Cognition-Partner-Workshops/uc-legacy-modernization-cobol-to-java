import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditTrailComponent } from './audit-trail.component';
import { AuditService } from './audit.service';

@NgModule({
  declarations: [AuditTrailComponent],
  imports: [CommonModule],
  exports: [AuditTrailComponent],
  providers: [AuditService]
})
export class AuditModule {}
