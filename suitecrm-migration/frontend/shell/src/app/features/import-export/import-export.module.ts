import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ImportWizardComponent } from './import-wizard/import-wizard.component';
import { ImportExportService } from './import-export.service';

const routes: Routes = [
  { path: 'import', component: ImportWizardComponent }
];

@NgModule({
  declarations: [ImportWizardComponent],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, ReactiveFormsModule],
  providers: [ImportExportService]
})
export class ImportExportModule {}
