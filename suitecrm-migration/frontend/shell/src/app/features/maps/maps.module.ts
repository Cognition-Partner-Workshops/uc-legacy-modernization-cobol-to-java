import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MapViewComponent } from './map-view/map-view.component';
import { MarkerListComponent } from './marker-list/marker-list.component';

const routes: Routes = [
  { path: '', component: MapViewComponent },
  { path: 'markers', component: MarkerListComponent }
];

@NgModule({
  declarations: [MapViewComponent, MarkerListComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class MapsModule {}
