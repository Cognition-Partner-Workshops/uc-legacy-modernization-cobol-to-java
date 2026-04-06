import { Component, OnInit } from '@angular/core';
import { MapsService, Marker } from '../../../services/maps.service';

@Component({
  selector: 'app-marker-list',
  template: `
    <div class="container">
      <h2>Markers</h2>
      <table class="data-table">
        <thead><tr><th>Name</th><th>City</th><th>Country</th><th>Lat</th><th>Lng</th><th>Type</th></tr></thead>
        <tbody>
          <tr *ngFor="let m of markers">
            <td>{{m.name}}</td><td>{{m.city}}</td><td>{{m.country}}</td>
            <td>{{m.latitude}}</td><td>{{m.longitude}}</td><td>{{m.markerType}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class MarkerListComponent implements OnInit {
  markers: Marker[] = [];
  constructor(private mapsService: MapsService) {}
  ngOnInit() { this.mapsService.getMarkers().subscribe((res: any) => this.markers = res.content || []); }
}
