import {Component, HostBinding, OnInit, ViewEncapsulation} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../../mapbox/tombolo-mapbox-map';
import {MapService} from '../../services/map-service/map.service';
import {Subscription} from 'rxjs/Subscription';
import {MapRegistry} from '../../mapbox/map-registry.service';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {FormControl, FormGroup} from '@angular/forms';
import {IMapFilter} from '../../../../../src/shared/IMapFilter';
import {IMapLayer} from '../../../../../src/shared/IMapLayer';

const debug = Debug('tombolo:access-to');

@Component({
  selector: 'access-to',
  templateUrl: './accessto.html',
  styleUrls: ['./accessto.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: [
    trigger('expandedState', [
      state('expanded', style({
        height: '*'
      })),
      state('collapsed',   style({
        height: '146px'
      })),
      transition('collapsed <=> expanded', animate('100ms ease-in-out'))
    ])
  ]
})
export class AccesstoRightBarComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;
  @HostBinding('class.access-to') accessToClass = true;

  constructor(private mapService: MapService,
              private mapRegistry: MapRegistry) {
    this.form = new FormGroup({
      transportMode: new FormControl(),
      journeyTime: new FormControl(),
      satisfaction: new FormControl(),
      gpRatio: new FormControl()
    });
  }

  map: TomboloMapboxMap;
  descriptionExpanded: 'expanded' | 'collapsed' = 'collapsed';
  form: FormGroup;

  numberOfGPsLayer: IMapLayer;
  gpLocationsLayer: IMapLayer;
  satisfactionFilter: IMapFilter;
  gpRatioFilter: IMapFilter;
  prescSetFilter: IMapFilter;

  private _subs: Subscription[] = [];

  ngOnInit() {

    // Initial setting of name and description
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      if (map.mapLoaded) {

        this.map = map;
        this.updateFormFromMap(map);
      }
    });

    // Update name and description when map is loaded
    this._subs.push(this.mapService.mapLoaded$().subscribe(map => {
      this.map = map;
      this.updateFormFromMap(map);
    }));

    this._subs.push(this.mapService.mapLoading$().subscribe(map => {
      this.map = null;
      this.numberOfGPsLayer = null;
      this.satisfactionFilter = null;
      this.gpRatioFilter = null;
      this.prescSetFilter = null;
    }));

    this._subs.push(this.form.get('transportMode').valueChanges.subscribe(val => {
      if (val && this.form.get('journeyTime').value) {
        this.map.setDataLayerColorAttribute(this.numberOfGPsLayer.layerId, val + this.form.get('journeyTime').value);
      }
    }));

    this._subs.push(this.form.get('journeyTime').valueChanges.subscribe(val => {
      if (val && this.form.get('transportMode').value) {
        this.map.setDataLayerColorAttribute(this.numberOfGPsLayer.layerId, this.form.get('transportMode').value + val);
      }
    }));

    this._subs.push(this.form.get('satisfaction').valueChanges.subscribe(val => {
      this.satisfactionFilter.value = val;
      this.satisfactionFilter.enabled = val > 0;
      this.map.updateFilter(0, this.satisfactionFilter);
    }));

    this._subs.push(this.form.get('gpRatio').valueChanges.subscribe(val => {
      this.gpRatioFilter.value = val;
      this.map.updateFilter(1, this.gpRatioFilter);
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  toggleDescriptionExpanded() {
    switch (this.descriptionExpanded) {
      case 'collapsed':
        this.descriptionExpanded = 'expanded';
        break;
      case 'expanded':
        this.descriptionExpanded = 'collapsed';
        break;
    }
  }

  expandButtonText(): string {
    return (this.descriptionExpanded === 'expanded') ? 'Less...' : 'More...';
  }

  // Update UI from map after load
  private updateFormFromMap(map: TomboloMapboxMap): void {

    this.numberOfGPsLayer = map.dataLayers.find(l => l.name === 'Number of GPs');
    this.gpLocationsLayer = map.dataLayers.find(l => l.name === 'GP Locations');
    this.satisfactionFilter = map.filters.find(f => f.attribute === 'phe_survey');
    this.gpRatioFilter = map.filters.find(f => f.attribute === 'reg_pat_gp');
    this.prescSetFilter = map.filters.find(f => f.attribute === 'prescr_set');

    if (!this.numberOfGPsLayer || !this.satisfactionFilter || !this.gpRatioFilter || !this.prescSetFilter) {
      // Badly formed access-to map
      throw new Error('Access-to map is missing required layers or filters');
    }

    let transportMode = 'wlk';
    let journeyTime = 5;
    let satisfaction: number = 0;
    let gpRatio: number = 0;

    if (this.numberOfGPsLayer) {
      transportMode = this.numberOfGPsLayer.colorAttribute.substring(0, 3);
      journeyTime = +this.numberOfGPsLayer.colorAttribute.substring(3);
    }

    if (this.satisfactionFilter) {
      satisfaction = this.satisfactionFilter.value;
    }

    if (this.gpRatioFilter) {
      gpRatio = this.gpRatioFilter.value;
    }

    const formVals = {
      transportMode,
      journeyTime,
      satisfaction,
      gpRatio
    };

    this.form.setValue(formVals);
  }
}
