import { Component, OnInit } from '@angular/core';
import { AdminService, AdminSetting } from '../admin.service';

@Component({
  selector: 'app-system-settings',
  template: `
    <h2>System Settings</h2>
    <div class="settings-grid">
      <div class="setting-card" *ngFor="let category of categories">
        <h3>{{ category }}</h3>
        <div *ngFor="let setting of getSettingsByCategory(category)" class="setting-row">
          <label>{{ setting.name }}</label>
          <input [(ngModel)]="setting.value" (blur)="saveSetting(setting)">
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 16px; }
    .setting-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 16px; }
    .setting-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
    .setting-row label { font-weight: 500; }
    .setting-row input { padding: 4px 8px; border: 1px solid #ccc; border-radius: 4px; width: 200px; }
  `]
})
export class SystemSettingsComponent implements OnInit {
  settings: AdminSetting[] = [];
  categories: string[] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.adminService.getAllSettings().subscribe(settings => {
      this.settings = settings;
      this.categories = [...new Set(settings.map(s => s.category))];
    });
  }

  getSettingsByCategory(category: string): AdminSetting[] {
    return this.settings.filter(s => s.category === category);
  }

  saveSetting(setting: AdminSetting) {
    this.adminService.setSetting(setting.category, setting.name, setting.value).subscribe();
  }
}
