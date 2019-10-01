import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../../mapbox/tombolo-mapbox-map';
import {MapService} from '../../services/map-service/map.service';
import {Subscription} from 'rxjs/Subscription';
import {MapRegistry} from '../../mapbox/map-registry.service';
import {AuthService} from "../../auth/auth.service";

const debug = Debug('tombolo:map-info');

@Component({
  selector: 'map-info',
  templateUrl: './map-info.html',
  styleUrls: ['./map-info.scss']
})
export class MapInfoComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  map: TomboloMapboxMap;

  constructor(private mapService: MapService,
              private mapRegistry: MapRegistry) {}

  private _subs: Subscription[] = [];

  ngOnInit() {

    // Initial setting of name and description
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      if (map.mapLoaded) {
        this.map = map;
      }
    });

    // Update name and description when map is loaded
    this._subs.push(this.mapService.mapLoading$().subscribe(map => {
      this.map = null;
    }));

    // Update name and description when map is loaded
    this._subs.push(this.mapService.mapLoaded$().subscribe(map => {
      this.map = map;
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }
}
