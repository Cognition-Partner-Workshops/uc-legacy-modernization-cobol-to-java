import { Component } from '@angular/core';
import { ImportExportService } from '../import-export.service';

@Component({
  selector: 'app-import-wizard',
  template: `
    <div class="import-wizard">
      <h2>Import Data</h2>
      <div class="wizard-steps">
        <div [class.active]="step === 1" class="step">1. Select Module</div>
        <div [class.active]="step === 2" class="step">2. Upload File</div>
        <div [class.active]="step === 3" class="step">3. Map Fields</div>
        <div [class.active]="step === 4" class="step">4. Review & Import</div>
      </div>

      <div *ngIf="step === 1" class="step-content">
        <label>Select Module:</label>
        <select [(ngModel)]="selectedModule">
          <option value="">-- Select --</option>
          <option *ngFor="let m of modules" [value]="m">{{ m }}</option>
        </select>
        <button (click)="step = 2" [disabled]="!selectedModule">Next</button>
      </div>

      <div *ngIf="step === 2" class="step-content">
        <label>Upload CSV File:</label>
        <input type="file" (change)="onFileSelected($event)" accept=".csv">
        <div class="options">
          <label><input type="checkbox" [(ngModel)]="hasHeader"> File has header row</label>
          <label>Delimiter: <input type="text" [(ngModel)]="delimiter" maxlength="1" style="width:30px"></label>
        </div>
        <button (click)="step = 1">Back</button>
        <button (click)="step = 3" [disabled]="!selectedFile">Next</button>
      </div>

      <div *ngIf="step === 3" class="step-content">
        <p>Map CSV columns to module fields</p>
        <table class="mapping-table">
          <thead><tr><th>CSV Column</th><th>Module Field</th></tr></thead>
          <tbody>
            <tr *ngFor="let col of csvColumns; let i = index">
              <td>{{ col }}</td>
              <td><select [(ngModel)]="fieldMappings[i]">
                <option value="">-- Skip --</option>
                <option *ngFor="let f of moduleFields" [value]="f">{{ f }}</option>
              </select></td>
            </tr>
          </tbody>
        </table>
        <button (click)="step = 2">Back</button>
        <button (click)="step = 4">Next</button>
      </div>

      <div *ngIf="step === 4" class="step-content">
        <h3>Review</h3>
        <p>Module: {{ selectedModule }}</p>
        <p>File: {{ selectedFile?.name }}</p>
        <p>Mapped fields: {{ getMappedCount() }}</p>
        <button (click)="step = 3">Back</button>
        <button (click)="importData()" class="primary-btn">Import</button>
        <div *ngIf="importResult" class="import-result">
          <p>Imported: {{ importResult.importedRows }} rows</p>
          <p *ngIf="importResult.errorRows > 0" class="error">Errors: {{ importResult.errorRows }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .import-wizard { max-width: 800px; margin: 0 auto; padding: 20px; }
    .wizard-steps { display: flex; gap: 8px; margin-bottom: 24px; }
    .step { padding: 8px 16px; background: #f5f5f5; border-radius: 20px; font-size: 13px; }
    .step.active { background: #1976d2; color: white; }
    .step-content { padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; }
    select, input[type=text] { padding: 8px; border: 1px solid #ccc; border-radius: 4px; margin: 8px 0; }
    button { padding: 8px 16px; margin: 8px 4px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; }
    .primary-btn { background: #1976d2; color: white; border: none; }
    .mapping-table { width: 100%; border-collapse: collapse; }
    .mapping-table th, .mapping-table td { padding: 8px; border-bottom: 1px solid #e0e0e0; }
    .import-result { margin-top: 16px; padding: 12px; background: #e8f5e9; border-radius: 4px; }
    .error { color: #f44336; }
    .options label { display: block; margin: 8px 0; }
  `]
})
export class ImportWizardComponent {
  step = 1;
  selectedModule = '';
  selectedFile: File | null = null;
  hasHeader = true;
  delimiter = ',';
  csvColumns: string[] = [];
  moduleFields: string[] = [];
  fieldMappings: string[] = [];
  importResult: any = null;
  modules = ['Accounts', 'Contacts', 'Leads', 'Opportunities', 'Cases', 'Campaigns'];

  constructor(private importService: ImportExportService) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    if (this.selectedFile) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const firstLine = e.target.result.split('\n')[0];
        this.csvColumns = firstLine.split(this.delimiter).map((c: string) => c.trim().replace(/"/g, ''));
        this.fieldMappings = new Array(this.csvColumns.length).fill('');
      };
      reader.readAsText(this.selectedFile);
    }
    this.moduleFields = this.getModuleFields(this.selectedModule);
  }

  getModuleFields(module: string): string[] {
    const common = ['name', 'description', 'assigned_user_id', 'email1', 'phone_work'];
    const specific: Record<string, string[]> = {
      'Accounts': ['account_type', 'industry', 'annual_revenue', 'website', 'billing_address_street', 'billing_address_city'],
      'Contacts': ['first_name', 'last_name', 'title', 'department', 'phone_mobile', 'account_name'],
      'Leads': ['first_name', 'last_name', 'status', 'lead_source', 'account_name', 'title'],
    };
    return [...common, ...(specific[module] || [])];
  }

  getMappedCount(): number {
    return this.fieldMappings.filter(m => m !== '').length;
  }

  importData() {
    if (!this.selectedFile) return;
    this.importService.importCsv(this.selectedFile, this.selectedModule, this.fieldMappings).subscribe(result => {
      this.importResult = result;
    });
  }
}
