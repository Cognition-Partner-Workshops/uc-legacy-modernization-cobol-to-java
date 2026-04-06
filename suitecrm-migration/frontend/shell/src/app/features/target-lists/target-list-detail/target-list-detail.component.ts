import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TargetListService, TargetList, TargetListMember } from '../../../services/target-list.service';

@Component({
  selector: 'app-target-list-detail',
  template: `
    <div class="target-list-detail" *ngIf="targetList">
      <h1>{{ targetList.name }}</h1>
      <div class="detail-meta">
        <span class="badge">{{ targetList.listType }}</span>
        <span>Entries: {{ targetList.entryCount }}</span>
        <span *ngIf="targetList.domainName">Domain: {{ targetList.domainName }}</span>
      </div>
      <p>{{ targetList.description }}</p>
      <h3>Members ({{ members.length }})</h3>
      <table class="data-table">
        <thead>
          <tr><th>Related ID</th><th>Type</th><th>Actions</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let member of members">
            <td>{{ member.relatedId }}</td>
            <td><span class="badge">{{ member.relatedType }}</span></td>
            <td>
              <button (click)="removeMember(member.id)" class="btn-danger btn-sm">Remove</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class TargetListDetailComponent implements OnInit {
  targetList: TargetList | null = null;
  members: TargetListMember[] = [];

  constructor(private route: ActivatedRoute, private targetListService: TargetListService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.targetListService.getTargetList(id).subscribe(list => this.targetList = list);
      this.targetListService.getMembers(id).subscribe(members => this.members = members);
    }
  }

  removeMember(memberId: string): void {
    this.targetListService.removeMember(memberId).subscribe(() => {
      this.members = this.members.filter(m => m.id !== memberId);
    });
  }
}
