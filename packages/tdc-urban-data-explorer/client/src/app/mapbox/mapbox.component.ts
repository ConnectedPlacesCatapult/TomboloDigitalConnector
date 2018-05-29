import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewEncapsulation} from '@angular/core';
import * as Debug from 'debug';
import 'rxjs/add/operator/auditTime';
import {animate, state, style, transition, trigger} from "@angular/animations";
import {MapRegistry} from "./map-registry.service";
import {Map, LngLat, MapMouseEvent, AttributionControl} from 'mapbox-gl';
import MapDataEvent = mapboxgl.MapDataEvent;
import {ExportMap} from "./export-map/export-map";

const debug = Debug('tombolo:mapboxgl');

export class EmuMapboxMap extends Map {

  exportMap = new ExportMap();

  export(name, width, height, dpi, format, drawOverlay: (ctx: CanvasRenderingContext2D, width: number, height: number, callback) => void = null): Promise<string> {
    const options = {
      center: this.getCenter(),
      zoom: this.getZoom(),
      style: {...this.getStyle(), transition: {delay: 0, duration: 0}},
      bearing: this.getBearing(),
      pitch: this.getPitch(),
    };

    return this.exportMap.downloadCanvas(options, name, width, height, dpi, format, drawOverlay);
  }

  // Update a source as smoothly as possible. All layers using that source are duplicated while the updated source
  // is loading. Note that this does not work perfectly with transparent layers
  updateSource(sourceId: string, source: object): Promise<boolean> {

    return this.duplicateSource(sourceId).then(duplicatedId => {

      debug('Source duplicated:', duplicatedId);

      // Update the source
      const style = this.getStyle();
      style.sources[sourceId] = source;
      this.setStyle(style);

      // Wait for source to load and then remove all temporary layers
      return this.whenSourceLoaded(sourceId).then((loaded) => {
        debug('Source re-loaded:', loaded);

        const style = this.getStyle();
        const tempLayers = style.layers.filter(layer => layer.id.startsWith(duplicatedId) && layer.type !== 'symbol');

        // Fade out temp layers
        tempLayers.forEach(layer => {
          this.setPaintProperty(layer.id, `${layer.type}-opacity`, 0);
        });

        // Remove temporary layers when fade has finished
        setTimeout(() => {
          tempLayers.forEach(layer => this.removeLayer(layer.id));
          this.removeSource(duplicatedId);
          debug('Temporary layers removed: ', duplicatedId);
        }, 500);

        return loaded;
      });
    })
  }

  // Returns a promise that resolves when the specified source has fully loaded
  // The promise resolves as 'true' if the source loads before the maximum poll period (5s)
  // The promise resolves as 'false' if source fails to load within the maximum poll period
  // (The promise is never rejected)
  whenSourceLoaded(id: string): Promise<boolean> {
    return new Promise((resolve, reject) => {

      // Poll for source to be loaded
      let pollInterval = 100; // ms
      let maxPolls = 5000 / pollInterval; // 5 seconds maximum

      const poller = () => {
        if (this.isSourceLoaded(id)) {
          return resolve(true);
        }

        // Timed out before source loaded
        if (--maxPolls === 0) {
          return resolve(false);
        }

        // Poll again after timeout
        setTimeout(poller, pollInterval);
      };

      // kick off
      poller();
    });
  }

  // Creates a copy of a layer and its associated source and places the layer directly above the original
  // Returns a promise that resolves as the unique id of the copied layer (and copied source)
  // The promise is resolved once the new layer has fully loaded
  private duplicateLayer(id: string): Promise<string> {

    // Add a unique suffix to the layer id (used for both the copied layer and source)
    const uniqueId = id + '-' + Math.random().toString(36).substr(2, 9);

    const s = this.getStyle();

    // Get the layer to copy
    const layer = s.layers.find(l => l.id === id);
    if (!layer) throw new Error('No layer with id ' + id);

    const layerIndex = s.layers.indexOf(layer);
    const nextLayerId = layerIndex < s.layers.length - 2 ? s.layers[layerIndex + 1].id : null;

    // Get the associated source to copy
    const source = s.sources[layer.source as string];
    if (!source) throw new Error('No source for layer with id ' + id);

    // Add the copied source and layer
    this.addSource(uniqueId, source);
    layer.id = uniqueId;
    layer.source = uniqueId;
    this.addLayer(layer, nextLayerId);

    return this.whenSourceLoaded(uniqueId).then(loaded => uniqueId);
  }

  // Creates a copy of a source and its associated layers
  // Returns a promise that resolves once the source is fully loaded
  // The resolved value is the new unique id of the duplicated source
  private duplicateSource(sourceId: string): Promise<string> {

    const uniqueId = sourceId + '-' + Math.random().toString(36).substr(2, 9);
    const style = this.getStyle();
    const source = style.sources[sourceId];

    // Add duplicate source
    this.addSource(uniqueId, source);
    debug('Copied source ' + uniqueId);

    // Get layers using this source
    const layers = style.layers.filter(layer => layer.source === sourceId);

    layers.forEach(layer => {
      const copiedLayerId = uniqueId + '-' + layer.id;
      const layerIndex = style.layers.indexOf(layer);
      const nextLayerId = layerIndex < style.layers.length - 2 ? style.layers[layerIndex + 1].id : null;
      layer.id = copiedLayerId;
      layer.source = uniqueId;
      this.addLayer(layer, nextLayerId);
      debug('Copied layer ' + layer.id);
    });

    return this.whenSourceLoaded(uniqueId).then(loaded => uniqueId);
  }
}

export class MapboxState  {
  constructor(public center: LngLat, public zoom: number) {}
}

@Component({
  selector: 'tombolo-mapbox',
  templateUrl: './mapbox.component.html',
  styleUrls: ['./mapbox.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: [
    trigger('mapLoading' ,[
      state('*', style({opacity: 1})),
      state('void', style({opacity: 0})),
      transition(':enter', [
        animate('200ms 600ms'),
      ]),
      transition(':leave', [
        animate('200ms')
      ])
    ])
  ]
})
export class MapboxComponent implements OnInit {

  private mouseMove$ = new EventEmitter<MapboxState>();

  map: EmuMapboxMap;
  pendingStyle: any;
  loading = true;
  mapOpacity = 0;

  @Input() mapClass: typeof EmuMapboxMap = EmuMapboxMap;
  @Input() showHover: boolean;
  @Input() id: string;
  @ViewChild('mapContainer') mapContainer;

  @Output() mapLoaded = new EventEmitter<EmuMapboxMap>();
  @Output() mapMoveEnd = new EventEmitter<MapboxState>();
  @Output() mapClick = new EventEmitter<MapMouseEvent>();
  @Output() mouseMove = this.mouseMove$.auditTime(200);

  constructor(private registry: MapRegistry) { }

  ngOnInit() {
    const options = {
      container: this.mapContainer.nativeElement,
      attributionControl: false
    };

    this.map = new this.mapClass(options);
    this.map.addControl(new AttributionControl(), 'bottom-left');

    if (this.pendingStyle) {
      this.map.setStyle(this.pendingStyle);
      this.pendingStyle = null;
    }

    this.map.on('load', this.onLoadHandler.bind(this));

    this.registry.registerMap(this.id, this.map);
  }

  @Input()
  set style(style: any) {
    if (this.map) {
      this.map.setStyle(style);
    } else {
      this.pendingStyle = style;
    }
  }

  get style(): any {
    return this.map.getStyle();
  }

  onLoadHandler() {
    this.map.resize();
    this.loading = false;
    this.mapOpacity = 1;
    this.mapLoaded.emit(this.map);

    this.map.on('moveend', this.onMoveEndHandler.bind(this));
    this.map.on('click', this.onClickHandler.bind(this));
    this.map.on('mousemove', this.onMouseMoveHandler.bind(this));
    this.map.on('data', this.onMapDataHandler.bind(this));
  }

  onMoveEndHandler() {
    this.mapMoveEnd.emit(new MapboxState(this.map.getCenter(), this.map.getZoom()));
  }

  onClickHandler(ev) {
    this.mapClick.emit(ev);
  }

  onMouseMoveHandler(ev) {
    this.mouseMove$.emit(ev);
  }

  onMapDataHandler(ev: MapDataEvent) {
    if (ev.dataType === 'source') {
      this.loading = !ev.isSourceLoaded;
    }
  }

  resize(delay: number) {
    setTimeout(this.map.resize.bind(this.map), delay);
  }

  zoom(increment) {
    this.map.zoomTo(this.map.getZoom() + increment);
  }
}


