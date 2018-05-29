import {ChangeDetectorRef, Component, DoCheck, HostBinding, Input, ViewEncapsulation} from '@angular/core';
import {IPalette} from '../../../../../src/shared/IPalette';
import {TomboloMapboxMap} from '../../mapbox/tombolo-mapbox-map';
import {IMapLayer} from '../../../../../src/shared/IMapLayer';
import * as Debug from 'debug';
import {ITomboloDatasetAttribute} from '../../../../../src/shared/ITomboloDatasetAttribute';
import {MIN_POINT_RADIUS} from '../../../../../src/shared/style-generator/style-generator';
import * as d3format from 'd3-format';

const debug = Debug('tombolo:map-key');

interface MapKeyItemData {
  title: string;
  ndColor: string;
  palette: IPalette,
  paletteInverted: boolean,
  values: string[],
  mode: 'square' | 'circle',
  sizes: number[]
}

@Component({
  selector: 'map-key',
  templateUrl: './map-key.html',
  styleUrls: ['./map-key.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class MapKeyComponent implements DoCheck {

  @HostBinding('class.map-key') mapKeyClass = true;

  @Input() map: TomboloMapboxMap;
  dataLayers: IMapLayer[];

  keyItems: MapKeyItemData[];

  constructor(private cd: ChangeDetectorRef) {}

  ngDoCheck() {

    // TODO - map layers are updated in a mutable fashion so the change detection is not as simple as it should be
    if (this.map && this.map.mapDefinition &&  !this.compareLayers(this.dataLayers, this.map.dataLayers)) {
        this.dataLayers = this.copyLayers(this.map.mapDefinition.layers);
        this.updateUi();
    }
  }

  private updateUi() {

    debug('Updating UI');

    let items = [] as MapKeyItemData[];

    this.dataLayers.forEach(layer => {

      if (!layer.visible) return;

      // Color key for layer
      if (layer.colorMode === 'attribute') {
        let itemData = {} as MapKeyItemData;
        const dataAttribute = this.map.getDataAttributeForLayer(layer.layerId, layer.colorAttribute);

        itemData.title = this.formatTitleForAttribute(dataAttribute);
        itemData.palette = layer.palette;
        itemData.paletteInverted = layer.paletteInverted;
        itemData.ndColor = layer.fixedColor;
        itemData.mode = (layer.layerType === 'circle') ? 'circle' : 'square';
        itemData.values = dataAttribute.isCategorical?
          this.formatCategories(dataAttribute.categories)
          : this.formatQuantiles(dataAttribute.quantiles5);
        items.push(itemData);
      }

      // Size key for layer
      if (layer.sizeMode === 'attribute') {
        let itemData = {} as MapKeyItemData;
        const dataAttribute = this.map.getDataAttributeForLayer(layer.layerId, layer.sizeAttribute);

        const radiusRange = layer.fixedSize - MIN_POINT_RADIUS;
        const radiusPerStop = radiusRange / 5;
        let radiusStops = [MIN_POINT_RADIUS]; // Size for ND
        for (let i = 0; i < 5; i++) {
          radiusStops.push(MIN_POINT_RADIUS + radiusPerStop * i);
        }

        itemData.title = this.formatTitleForAttribute(dataAttribute);
        itemData.ndColor = layer.palette.colorStops[2];
        itemData.mode = (layer.layerType === 'circle') ? 'circle' : 'square';
        itemData.values = dataAttribute.isCategorical?
          this.formatCategories(dataAttribute.categories)
          : this.formatQuantiles(dataAttribute.quantiles5);
        itemData.sizes = radiusStops;
        items.push(itemData);
      }
    });

    this.keyItems = items;

    this.cd.markForCheck();
  }

  private copyLayers(layers: IMapLayer[]): IMapLayer[] {

    let layersCopy = [] as IMapLayer[];

    layers.forEach(layer => {
      layersCopy.push({...layer});
    });

    return layersCopy;
  }

  private compareLayers(layers1: IMapLayer[], layers2: IMapLayer[]): boolean {

    if (!layers1 && !layers2) return true;

    if (layers1 && !layers2) return false;

    if (!layers1 && layers2) return false;

    if (layers1.length != layers2.length) return false;

    for (let i = 0; i < layers1.length; i++) {
      if (layers1[i].colorAttribute != layers2[i].colorAttribute) return false;
      if (layers1[i].sizeAttribute != layers2[i].sizeAttribute) return false;
      if (layers1[i].colorMode != layers2[i].colorMode) return false;
      if (layers1[i].sizeMode != layers2[i].sizeMode) return false;
      if (layers1[i].palette != layers2[i].palette) return false;
      if (layers1[i].fixedColor != layers2[i].fixedColor) return false;
      if (layers1[i].fixedSize != layers2[i].fixedSize) return false;
      if (layers1[i].paletteInverted != layers2[i].paletteInverted) return false;
      if (layers1[i].visible != layers2[i].visible) return false;
    }

    return true;
  }

  private formatTitleForAttribute(attribute: ITomboloDatasetAttribute): string {
    return (attribute.unit)? attribute.name + ` (${attribute.unit})` : attribute.name;
  }

  private formatQuantiles(quantiles: number[]): string[] {
    return quantiles.map(q => {
      return d3format.format(.3)(q);
    });
  }

  private formatCategories(categories: string[]): string[] {
    // TODO - work out what to do here - map n categories to 5 labels
    let categoryValues = [];
    categoryValues =  ['', '', '', '', ''];
    return categoryValues;
  }
}
