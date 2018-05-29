import {Component, HostBinding, Inject, OnDestroy, OnInit} from '@angular/core';
import {animate, state, style, transition, trigger} from '@angular/animations';
import * as Debug from 'debug';
import {MapRegistry} from '../mapbox/map-registry.service';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Style} from 'mapbox-gl';
import {MapService} from '../services/map-service/map.service';
import {TomboloMapboxMap} from '../mapbox/tombolo-mapbox-map';
import {Angulartics2} from 'angulartics2';
import {IMapGroup} from '../../../../src/shared/IMapGroup';
import {GeosearchItem} from './geosearch/geosearch.service';
import LngLatBoundsLike = mapboxgl.LngLatBoundsLike;
import {Subscription} from 'rxjs/Subscription';
import {AuthService} from '../auth/auth.service';
import {APP_CONFIG, AppConfig} from '../config.service';
import {ITomboloMap} from '../../../../src/shared/ITomboloMap';

const debug = Debug('tombolo:map-viewer');

@Component({
  selector: 'maps-viewer',
  templateUrl: './map-viewer.html',
  styles: []
})
export class MapViewerComponent implements OnInit, OnDestroy {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  private _subs: Subscription[] = [];

  mapGroups$: Observable<IMapGroup[]> = null;


  constructor(private mapRegistry: MapRegistry,
              private activatedRoute: ActivatedRoute,
              private mapService: MapService,
              private router: Router,
              private authService: AuthService,
              @Inject(APP_CONFIG) private config: AppConfig) {}

  ngOnInit() {

    // Load maps whenever user changes
    this._subs.push(this.authService.user$.subscribe(user => {
      this.mapGroups$ = this.mapService.loadMapGroups();
    }));

    this.activatedRoute.params.subscribe(params => {

      const mapId = params.mapID || null;

      if (mapId === null) {
        // Redirect to default map
        this.router.navigate(['/', {outlets: {
          primary: ['view', this.config.defaultMap],
          loginBar: null,
          rightBar: ['appinfo']}}], {
            queryParamsHandling: 'preserve'
        });
        return
      }

      this.loadMap(mapId);
    });
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  loadMap(mapID: string) {
    debug('mapID:', mapID);
    if (!mapID) return;

    this.mapService.loadMap(mapID);
  }

  geosearchSelected(item: GeosearchItem) {
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      map.fitBounds(item.boundingBox, {padding: 30, maxZoom: 13});
    });
  }

  gotoPlayground() {
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {

      let route;

      if (map.id) {
        route = ['/', {outlets: {
          primary: ['edit', map.id],
          loginBar: null,
          rightBar: ['editpanel']}}]
      }
      else {
        route = ['/', {outlets: {
          primary: ['edit'],
          loginBar: null,
          rightBar: ['editinfo']}}]
      }

      this.router.navigate(route,{
        queryParamsHandling: 'merge'
      });
    });
  }

  // Return rightbar route to use for the specified map
  rightBarRoute(map: ITomboloMap) {
    return (map.ui && map.ui['rightBar']) || 'mapinfo';
  }

}
