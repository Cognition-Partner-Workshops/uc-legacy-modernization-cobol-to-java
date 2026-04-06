import { Component, OnInit } from '@angular/core';
import { TargetListService, TargetList } from '../../../services/target-list.service';

@Component({
  selector: 'app-target-list-list',
  template: `
    <div class="target-list-container">
      <div class="toolbar">
        <h2>Target Lists</h2>
        <div class="actions">
          <input type="text" placeholder="Search lists..." (input)="onSearch($event)" />
          <button class="btn-primary">Create Target List</button>
        </div>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>Name</th><th>Type</th><th>Domain</th><th>Entries</th><th>Actions</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let list of targetLists" [routerLink]="[list.id]">
            <td>{{ list.name }}</td>
            <td><span class="badge">{{ list.listType }}</span></td>
            <td>{{ list.domainName }}</td>
            <td>{{ list.entryCount }}</td>
            <td>
              <button (click)="deleteList(list.id, $event)" class="btn-danger btn-sm">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class TargetListListComponent implements OnInit {
  targetLists: TargetList[] = [];

  constructor(private targetListService: TargetListService) {}

  ngOnInit(): void {
    this.loadLists();
  }

  loadLists(): void {
    this.targetListService.getTargetLists().subscribe(data => this.targetLists = data.content || []);
  }

  deleteList(id: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this target list?')) {
      this.targetListService.deleteTargetList(id).subscribe(() => this.loadLists());
    }
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    if (query.length >= 3) {
      this.targetListService.searchTargetLists(query).subscribe(data => this.targetLists = data.content || []);
    } else if (query.length === 0) {
      this.loadLists();
    }
  }
}
