import { Component, OnInit } from '@angular/core';
import { ConnectorService, Connector } from '../../../services/connector.service';

@Component({
  selector: 'app-connector-list',
  template: `
    <div class="connector-list-container">
      <div class="toolbar">
        <h2>Connectors & Integrations</h2>
        <button class="btn-primary">Add Connector</button>
      </div>
      <table class="data-table">
        <thead>
          <tr><th>Name</th><th>Type</th><th>Auth</th><th>Status</th><th>Enabled</th><th>Actions</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let connector of connectors" [routerLink]="[connector.id]">
            <td>{{ connector.name }}</td>
            <td>{{ connector.connectorType }}</td>
            <td>{{ connector.authType }}</td>
            <td><span class="badge" [ngClass]="connector.status">{{ connector.status }}</span></td>
            <td>
              <span class="toggle" [class.enabled]="connector.isEnabled" (click)="toggle(connector, $event)">
                {{ connector.isEnabled ? 'ON' : 'OFF' }}
              </span>
            </td>
            <td>
              <button (click)="deleteConnector(connector.id, $event)" class="btn-danger btn-sm">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class ConnectorListComponent implements OnInit {
  connectors: Connector[] = [];

  constructor(private connectorService: ConnectorService) {}

  ngOnInit(): void {
    this.loadConnectors();
  }

  loadConnectors(): void {
    this.connectorService.getConnectors().subscribe(data => this.connectors = data.content || []);
  }

  toggle(connector: Connector, event: Event): void {
    event.stopPropagation();
    this.connectorService.toggleConnector(connector.id).subscribe(updated => {
      connector.isEnabled = updated.isEnabled;
    });
  }

  deleteConnector(id: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure?')) {
      this.connectorService.deleteConnector(id).subscribe(() => this.loadConnectors());
    }
  }
}
