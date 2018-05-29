import {
  ChangeDetectionStrategy, Component, HostBinding, Input, OnChanges, OnInit,
  ViewEncapsulation
} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../../../mapbox/tombolo-mapbox-map';
import {FormControl, FormGroup} from '@angular/forms';
import {Subscription} from 'rxjs/Subscription';
import {IPalette} from '../../../../../../src/shared/IPalette';
import {ITomboloDatasetAttribute} from '../../../../../../src/shared/ITomboloDatasetAttribute';
import {IMapLayer} from '../../../../../../src/shared/IMapLayer';
import {MIN_POINT_RADIUS} from '../../../../../../src/shared/style-generator/style-generator';

const debug = Debug('tombolo:map-layer-editor');

@Component({
  selector: 'map-layer-editor',
  templateUrl: './map-layer-editor.html',
  styleUrls: ['./map-layer-editor.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class MapLayerEditorComponent implements OnInit, OnChanges {

  @HostBinding('class.layer-editor') layerEditorClass = true;

  @Input() map: TomboloMapboxMap;
  @Input() layer: IMapLayer;
  @Input() palettes: IPalette[];

  form: FormGroup;
  allAttributes: ITomboloDatasetAttribute[];
  rampAttributes: ITomboloDatasetAttribute[];
  mode: 'fill' | 'line' | 'circle' = 'fill';
  minPointSize = MIN_POINT_RADIUS;

  _subs: Subscription[] = [];

  constructor() {
    this.form = new FormGroup({
      colorRadio: new FormControl('fixed'),
      fixedColor: new FormControl('#bbb'),
      colorAttribute: new FormControl(),
      palette: new FormControl(),
      paletteInverted: new FormControl(),
      size: new FormControl(5),
      sizeRadio: new FormControl('fixed'),
      sizeAttribute: new FormControl(),
      labelAttribute: new FormControl(),
      opacity: new FormControl(100)
    });
  }

  ngOnInit() {
    // Save form changes to map as user changes controls

    this._subs.push(this.form.get('colorRadio').valueChanges.subscribe(val => {
      this.map.setDataLayerColorMode(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('fixedColor').valueChanges.subscribe(val => {
      this.map.setDataLayerFixedColor(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('colorAttribute').valueChanges.subscribe(val => {
      this.map.setDataLayerColorAttribute(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('palette').valueChanges.subscribe(val => {
      this.map.setDataLayerPalette(this.layer.layerId, this.palettes.find(p => p.id === val));
    }));

    this._subs.push(this.form.get('paletteInverted').valueChanges.subscribe(val => {
      this.map.setDataLayerPaletteInverted(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('size').valueChanges.subscribe(val => {
      this.map.setDataLayerFixedSize(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('sizeRadio').valueChanges.subscribe(val => {
      this.map.setDataLayerSizeMode(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('sizeAttribute').valueChanges.subscribe(val => {
      this.map.setDataLayerSizeAttribute(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('labelAttribute').valueChanges.subscribe(val => {
      this.map.setDataLayerLabelAttribute(this.layer.layerId, val);
    }));

    this._subs.push(this.form.get('opacity').valueChanges.subscribe(val => {
      this.map.setDataLayerOpacity(this.layer.layerId, val / 100);
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  ngOnChanges(changes) {
    // Transfer values to form
    if ((changes.map || changes.layerId) && this.map) {
      const map = this.map;
      const layer = this.layer;

      this.allAttributes = map.getDataAttributesForLayer(this.layer.layerId);
      this.rampAttributes = this.allAttributes.filter(a => a.type === 'number' || a.categories);
      this.mode = layer.layerType;

      this.form.setValue({
        colorRadio: layer.colorMode || 'fixed',
        fixedColor: layer.fixedColor || '#ddd',
        colorAttribute: layer.colorAttribute,
        palette: layer.palette.id,
        paletteInverted: layer.paletteInverted,
        size: layer.fixedSize || 10,
        sizeRadio: layer.sizeMode || 'fixed',
        sizeAttribute: layer.sizeAttribute,
        labelAttribute: layer.labelAttribute,
        opacity: layer.opacity * 100
      });
    }
  }
}
