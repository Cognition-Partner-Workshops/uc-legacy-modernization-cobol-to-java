import { Component, OnInit } from '@angular/core';
import { MapsService, GeoMap } from '../../../services/maps.service';

@Component({
  selector: 'app-map-view',
  template: `
    <div class="container">
      <h2>Maps</h2>
      <div class="toolbar"><a routerLink="markers" class="btn-secondary">View Markers</a></div>
      <table class="data-table">
        <thead><tr><th>Name</th><th>Type</th><th>Module</th><th>Center</th><th>Zoom</th></tr></thead>
        <tbody>
          <tr *ngFor="let m of maps">
            <td>{{m.name}}</td><td>{{m.mapType}}</td><td>{{m.moduleType}}</td>
            <td>{{m.centerLat}}, {{m.centerLng}}</td><td>{{m.zoomLevel}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class MapViewComponent implements OnInit {
  maps: GeoMap[] = [];
  constructor(private mapsService: MapsService) {}
  ngOnInit() { this.mapsService.getMaps().subscribe((res: any) => this.maps = res.content || []); }
}
