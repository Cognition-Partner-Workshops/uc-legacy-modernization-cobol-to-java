import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ConnectorService, Connector, ExternalAccount } from '../../../services/connector.service';

@Component({
  selector: 'app-connector-detail',
  template: `
    <div class="connector-detail" *ngIf="connector">
      <h1>{{ connector.name }}</h1>
      <div class="connector-meta">
        <span class="badge" [ngClass]="connector.status">{{ connector.status }}</span>
        <span>Type: {{ connector.connectorType }}</span>
        <span>Auth: {{ connector.authType }}</span>
        <span>Enabled: {{ connector.isEnabled ? 'Yes' : 'No' }}</span>
      </div>
      <p>{{ connector.description }}</p>
      <div class="connector-config">
        <h3>Configuration</h3>
        <p><strong>Base URL:</strong> {{ connector.baseUrl }}</p>
        <p><strong>API Version:</strong> {{ connector.apiVersion }}</p>
        <p><strong>Source Module:</strong> {{ connector.sourceModule }}</p>
      </div>
      <h3>External Accounts ({{ accounts.length }})</h3>
      <table class="data-table" *ngIf="accounts.length">
        <thead>
          <tr><th>Name</th><th>Application</th><th>External ID</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let account of accounts">
            <td>{{ account.name }}</td>
            <td>{{ account.application }}</td>
            <td>{{ account.externalId }}</td>
          </tr>
        </tbody>
      </table>
      <h3>Logs</h3>
      <table class="data-table" *ngIf="logs.length">
        <thead>
          <tr><th>Action</th><th>Status</th><th>Duration</th><th>Date</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let log of logs">
            <td>{{ log.action }}</td>
            <td><span class="badge" [ngClass]="log.status">{{ log.status }}</span></td>
            <td>{{ log.durationMs }}ms</td>
            <td>{{ log.dateEntered | date:'short' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class ConnectorDetailComponent implements OnInit {
  connector: Connector | null = null;
  accounts: ExternalAccount[] = [];
  logs: any[] = [];

  constructor(private route: ActivatedRoute, private connectorService: ConnectorService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.connectorService.getConnector(id).subscribe(c => this.connector = c);
      this.connectorService.getExternalAccounts(id).subscribe(a => this.accounts = a);
      this.connectorService.getConnectorLogs(id).subscribe(data => this.logs = data.content || []);
    }
  }
}
