import {Component, HostBinding, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import * as Debug from 'debug';
import {MapRegistry} from '../mapbox/map-registry.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Location} from '@angular/common';
import {NotificationService} from '../dialogs/notification.service';
import {TomboloMapboxMap} from '../mapbox/tombolo-mapbox-map';
import {MapService} from '../services/map-service/map.service';
import {Subscription} from 'rxjs/Subscription';
import * as html2canvas from 'html2canvas';
import {APP_CONFIG, AppConfig} from '../config.service';

const debug = Debug('tombolo:map-export');

@Component({
  selector: 'map-export',
  templateUrl: './map-export.html',
  styleUrls: ['./map-export.scss']
})
export class MapExportComponent implements OnInit, OnDestroy {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;
  @HostBinding('class.map-export') mapExportClass = true;

  @ViewChild('theMapKey') mapKey;

  map: TomboloMapboxMap;
  exporting = false;
  exportForm: FormGroup;
  presets = {
    "a4_150dpi": { width: 297, height: 210, dpi: 150, format: 'png' },
    "a4_300dpi": { width: 297, height: 210, dpi: 300, format: 'png' },
    "a3_150dpi": { width: 420, height: 297, dpi: 150, format: 'png' },
    "a3_300dpi": { width: 420, height: 297, dpi: 300, format: 'png' }
  };

  private _subs: Subscription[] = [];

  constructor(private mapRegistry: MapRegistry,
              private location: Location,
              private notificationService: NotificationService,
              private mapService: MapService,
              @Inject(APP_CONFIG) private config: AppConfig)
  {
    this.exportForm = new FormGroup({
      name: new FormControl('', Validators.required),
      width: new FormControl(this.presets['a4_150dpi'].width, Validators.required),
      height: new FormControl(this.presets['a4_150dpi'].height, Validators.required),
      dpi: new FormControl(this.presets['a4_150dpi'].dpi, Validators.required),
      format: new FormControl(this.presets['a4_150dpi'].format, Validators.required),
      preset: new FormControl('a4_150dpi')
    });
  }

  ngOnInit() {

    // Initial setting of name and description
    this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      if (map.mapLoaded) {
        this.map = map;
        this.updateUI();
      }
    });

    // Update name and description when map is loaded
    this._subs.push(this.mapService.mapLoading$().subscribe(map => {
      this.map = null;
    }));

    // Update name and description when map is loaded
    this._subs.push(this.mapService.mapLoaded$().subscribe(map => {
      this.map = map;
      this.updateUI();
    }));

    this._subs.push(this.exportForm.get('preset').valueChanges.subscribe(this.onPresetChange.bind(this)));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  exportMap(): void {
    this.exporting = true;

    this.map.export(
          this.exportForm.get('name').value,
          this.exportForm.get('width').value,
          this.exportForm.get('height').value,
          this.exportForm.get('dpi').value,
          this.exportForm.get('format').value,
          this.drawOverlays.bind(this))
      .then(name => {
        debug('Downloaded ' + name);
        this.routeBack();
        this.exporting = false;
      })
      .catch(err => {
        this.exporting = false;
        this.notificationService.error(err);
      });
  }

  routeBack(): void {
    this.location.back();
  }

  private formatFileName(name: string): string {
    return name.toLowerCase().replace(/ /g, "_");
  }

  private updateUI() {
    if (this.map) {
      this.exportForm.patchValue({name: this.formatFileName(this.map.name)});
    }
  }

  private onPresetChange(preset) {
    this.exportForm.patchValue({
      width: this.presets[preset].width,
      height: this.presets[preset].height,
      dpi: this.presets[preset].dpi,
      format: this.presets[preset].format
    });
  }

  private drawOverlays(ctx: CanvasRenderingContext2D, canvasWidth: number, canvasHeight: number, done) {

    const attribElement = document.getElementsByClassName('mapboxgl-ctrl-attrib').item(0);

    Promise.all([
      html2canvas(attribElement as any),
      html2canvas(this.mapKey.nativeElement)
    ]).then(([attribCanvas, mapKeyCanvas]) => {

      // Attribution
      ctx.drawImage(attribCanvas, 0, canvasHeight - attribCanvas.height);

      // Map key
      const scaleFactor = 0.75; // Shrink the map key
      const trimTop = 20; // Trim off top of map key
      const brandingHeight = 10;

      const srcX = 0;
      const srcY = trimTop;
      const srcWidth = mapKeyCanvas.width;
      const srcHeight = mapKeyCanvas.height - trimTop;
      const dstWidth = srcWidth * scaleFactor;
      const dstHeight = srcHeight * scaleFactor;
      const dstX = canvasWidth - dstWidth;
      const dstY = canvasHeight - dstHeight - brandingHeight;

      ctx.drawImage(mapKeyCanvas, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);

      // Branding
      ctx.fillStyle = 'white';
      ctx.fillRect(dstX, canvasHeight - brandingHeight, dstWidth, dstHeight);
      ctx.font = '12px Roboto';
      ctx.fillStyle = 'black';
      ctx.textAlign = 'right';
      ctx.fillText(this.config.poweredBy, canvasWidth - 10, canvasHeight - brandingHeight / 2);

      done();
    })
      .catch(e => {
        done(e);
      });
  }
}
