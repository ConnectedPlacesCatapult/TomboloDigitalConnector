import {
  ChangeDetectionStrategy, Component, HostBinding, OnInit, DoCheck, ChangeDetectorRef
} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../mapbox/tombolo-mapbox-map';
import {MapService} from '../services/map-service/map.service';
import {Subscription} from 'rxjs/Subscription';
import {MapRegistry} from '../mapbox/map-registry.service';
import {ActivatedRoute} from '@angular/router';
import {IMapLayer} from '../../../../src/shared/IMapLayer';
import {DialogsService} from '../dialogs/dialogs.service';
import {DragulaService} from 'ng2-dragula';
import {NotificationService} from '../dialogs/notification.service';
import {IMapFilter} from '../../../../src/shared/IMapFilter';
import {AuthService} from "../auth/auth.service";
import {AppComponent} from "../app.component";

const debug = Debug('tombolo:map-filters-panel');

@Component({
  selector: 'map-filters',
  templateUrl: './map-filters-panel.html',
  styleUrls: ['./map-filters-panel.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MapFiltersPanelComponent implements OnInit, DoCheck {

  @HostBinding('class.map-filters') mapFiltersClass = true;

  constructor(private mapService: MapService,
              private mapRegistry: MapRegistry,
              private dialogsService: DialogsService,
              private activatedRoute: ActivatedRoute,
              private notificationService: NotificationService,
              private cd: ChangeDetectorRef) {}

  _subs: Subscription[] = [];
  map: TomboloMapboxMap;
  filters: IMapFilter[];

  ngOnInit() {

    // Initial setting of map
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      debug('initial settting of map', map.mapLoaded);
      if (map.mapLoaded) {
        this.map = map;
        this.filters = map.filters;
        this.cd.markForCheck();
      }
    });

    this._subs.push(this.mapService.mapLoading$().subscribe(() => {
      debug('Map is loading');
      // Clear map so that child components don't try to access map
      // while it is loading
      this.map = null;
      this.filters = null;
      this.cd.markForCheck();
    }));

    // Update when map loaded
    this._subs.push(this.mapService.mapLoaded$().subscribe(map => {
      debug('Edit panel got map', map.id);
      this.map = map;
      this.filters = map.filters;
      this.cd.markForCheck();
    }));

  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  // Custom change detection to detect filter changes on map
  // due to inserts, deletions etc.
  ngDoCheck() {
    if (this.map && this.map.filters !== this.filters) {
      this.filters = this.map.filters;
      this.cd.markForCheck();
    }
  }

  eyeIconForFilter(filter: IMapFilter): string {
    return filter.enabled ? 'eye' : 'eye-off';
  }

  toggleFilterEnabled(index: number, filter: IMapFilter) {
    filter.enabled = !filter.enabled;
    this.map.updateFilter(index, filter);
  }

  addFilter()  {

    if (this.map.dataLayers.length === 0) {
      this.dialogsService.information('No Data Layers on Map', 'You can only add a filter if the map has one or more data layers.');
      return;
    }

    const defaultLayerId = this.map.dataLayers[0].layerId;

    const defaultAttribute = defaultLayerId ?
      this.map.getDataAttributesForLayer(defaultLayerId).find(attr => attr.type === 'number') : null;

    const filter: IMapFilter = {
      datalayerId: defaultLayerId,
      attribute: defaultAttribute ? defaultAttribute.field : null,
      operator: '',
      value: null,
      enabled: true
    };

    this.map.addFilter(filter);
  }

  deleteFilter(filter: IMapFilter) {
    this.dialogsService
      .confirm('Delete Filter', `Are you sure you want to delete the filter?`)
      .filter(result => result)
      .subscribe(() => {
        this.map.removeFilter(filter);
        this.cd.markForCheck();
      });
  }

  onFilterChange(index: number, newFilter: IMapFilter) {
    this.map.updateFilter(index, newFilter);
  }
}
