import {IBasemap} from '../IBasemap';
import {IMapDefinition} from '../IMapDefinition';
import {ILabelLayerStyleMetadata, IStyle, IStyleLayer, IStyleMetadata} from '../IStyle';
import {IMapLayer} from '../IMapLayer';
import {ITomboloDataset} from '../ITomboloDataset';
import * as d3scale from 'd3-scale';

import * as uuid from 'uuid/v4';


import {clone} from './clone';
import {IPalette} from '../IPalette';

export const DATA_LAYER_ID = 'data';
export const DATA_LAYER_PREFIX = 'datalayer-';
export const LABEL_LAYER_PREFIX = 'labellayer-';

export const MIN_POINT_RADIUS = 3;

export class StyleGenerator {

  constructor(private mapDefinition: IMapDefinition) {}

  generateMapStyle(basemap: IBasemap): IStyle {

    let style: IStyle = clone(basemap.style);

    style.metadata = style.metadata || {} as IStyleMetadata;
    style.metadata.mapDefinition = this.mapDefinition;
    style.zoom = this.mapDefinition.zoom || style.zoom;
    style.center = this.mapDefinition.center || style.center;
    style.sources = {...style['sources'], ...this.generateSources(this.mapDefinition)};
    style.sources = this.expandTileSources(this.mapDefinition.tileUrl, style.sources);
    style.glyphs = this.expandRelativeURL(this.mapDefinition.mapAssetsUrl, style.glyphs);
    style.sprite = this.expandRelativeURL(this.mapDefinition.mapAssetsUrl, style.sprite);

    this.applyBasemapDetail(style);

    // Find layer indices of insertion points
    let insertionPoints = style.metadata.insertionPoints || {};

    // Create and insert map layers - done in reverse order
    // first layer in array is top-most on map
    const reversedLayers = [...this.mapDefinition.layers].reverse();
    reversedLayers.forEach(layer => {
      const layerStyle = this.generateMapLayer(layer);
      const insertionPoint = insertionPoints[layer.layerType];
      this.insertMapLayer(insertionPoint, style, layerStyle);
    });

    // Create and insert label layers
    const labelAttributeStyle = style.metadata.labelLayerStyle;
    if (!labelAttributeStyle) {
      throw new Error(`No label layer style for basemap ${basemap.name}`);
    }
    else {
      reversedLayers.filter(layer => layer.labelAttribute !== null).forEach(layer => {
        const labelLayerStyle = this.generateLabelLayer(layer, labelAttributeStyle);
        const insertionPoint = insertionPoints['label'];
        this.insertMapLayer(insertionPoint, style, labelLayerStyle);
      });
    }

    return style;
  }

  layoutStyleForLayer(layer: IMapLayer): object {
    return {
      'visibility': layer.visible ? 'visible' : 'none'
    };
  }

  paintStyleForLayer(layer: IMapLayer): object {

    const dataset = this.datasetForLayer(layer);

    if (layer.layerType === 'fill') {
      return {
        'fill-color': this.colorRampForLayer(layer),
        'fill-outline-color': {stops: [[8, 'rgba(255,255,255,0)'], [9, 'rgba(255,255,255,1)']]},
        'fill-opacity': ['interpolate', ['linear'], ['zoom'],
          dataset.minZoom, 0,
          dataset.minZoom + 0.5, layer.opacity || 1
        ]
      };
    }
    else if (layer.layerType === 'circle') {
      return {
        'circle-color': this.colorRampForLayer(layer),
        'circle-radius': this.radiusRampForLayer(layer),
        'circle-opacity': ['interpolate', ['linear'], ['zoom'],
          dataset.minZoom, 0,
          dataset.minZoom + 0.5, layer.opacity || 1
        ]
      };
    }
    else if (layer.layerType === 'line') {
      return {
        'line-color': this.colorRampForLayer(layer),
        'line-width': {
          base: 1.3,
          stops: [[10, 2], [20, 20]]
        },
        'line-opacity': ['interpolate', ['linear'], ['zoom'],
          dataset.minZoom, 0,
          dataset.minZoom + 0.5, layer.opacity || 1
        ]
      };
    }
  }

  generateMapLayer(layer: IMapLayer): IStyleLayer {

    const dataset = this.datasetForLayer(layer);

    return {
      id: layer.layerId,
      source: layer.datasetId,
      'source-layer':  DATA_LAYER_ID,
      type: layer.layerType,
      minzoom: dataset.minZoom,
      maxzoom: dataset.maxZoom,
      layout: this.layoutStyleForLayer(layer),
      paint: this.paintStyleForLayer(layer),
      filter: this.filtersForLayerId(layer.layerId)
    };
  }

  generateLabelLayer(layer: IMapLayer, labelAttributeStyle: ILabelLayerStyleMetadata): IStyleLayer {

    // Do nothing of no label attribute
    if (!layer.labelAttribute) return null;

    const dataset = this.datasetForLayer(layer);

    if (!dataset) {
      throw new Error(`Layer'${layer.layerId} has no dataset`);
    }

    const labelAttribute = dataset.dataAttributes.find(d => d.field === layer.labelAttribute);
    const labelText = `{${labelAttribute.field}} ${labelAttribute.unit ? labelAttribute.unit : ''}`;

    let layout = {
      ...labelAttributeStyle.layout,
      'visibility': layer.visible ? 'visible' : 'none',

      // TODO - shouldn't need to override these - change the basemap label style
      'text-anchor': 'top',
      'text-justify': 'center',
      'text-offset': [0, 0],
      'text-field': labelText
    };

    let paint = {...labelAttributeStyle.paint};

    switch (layer.layerType) {
      case 'circle':
        paint['text-translate'] = [0, layer.fixedSize + 4];
        break;
      case 'line':
        layout['symbol-placement'] = 'line';
        break;
    }

    return {
      id: LABEL_LAYER_PREFIX + layer.originalLayerId,
      type: 'symbol',
      source: layer.datasetId,
      'source-layer': DATA_LAYER_ID,
      layout: layout,
      paint: paint,
      filter: [...this.filtersForLayerId(layer.layerId), ['has', layer.labelAttribute]]
    };
  }

  insertMapLayer(insertionPoint: string, style: IStyle, layer: IStyleLayer): void {
    const index = style.layers.findIndex(l => l['id'] === insertionPoint);
    style['layers'].splice(index, 0, layer);
  }


  /**
   * Generate a default map layer for the given dataset
   *
   * @param {ITomboloDataset} dataset
   */
  generateDefaultDataLayer(dataset: ITomboloDataset, defaultPalette: IPalette): IMapLayer {

    const newId = uuid();

    let mapLayer: IMapLayer = {
      layerId: DATA_LAYER_PREFIX + newId,
      originalLayerId: newId,
      datasetId: dataset.id,
      name: dataset.name,
      description: dataset.description,
      visible: true,
      opacity: 1,
      layerType: this.layerTypeForGeometryType(dataset.geometryType),
      palette: defaultPalette,
      paletteId: defaultPalette.id,
      paletteInverted: false,
      colorAttribute: null,
      fixedColor: '#888',
      colorMode: 'fixed',
      sizeAttribute: null,
      fixedSize: 10,
      sizeMode: 'fixed',
      labelAttribute: null,
      order: null
    };

    // Sort attributes with 'number' attributes first and then by name
    const sortedAttributes = dataset.dataAttributes.sort((a, b) => {
      if (a.type === b.type) return (a.name < b.name) ? -1 : 1;
      if (a.type === 'number') return -1;
      if (b.type === 'number') return 1;

      return (a.name < b.name) ? -1 : 1;
    });

    if (sortedAttributes.length > 0 && sortedAttributes[0].type === 'number') {
      // Select first numeric attribute for color
      mapLayer.colorAttribute = sortedAttributes[0].field;
      mapLayer.colorMode = 'attribute';
    }

    return mapLayer;
  }

  /**
   * Generate filter property for specified layer
   *
   * @param {IMapLayer} layer
   * @returns {any} Mapbox filter property
   */
  filtersForLayerId(layerId: string): any {

    if (this.mapDefinition.filters === null || this.mapDefinition.filters.length === 0) return ['all'];

    // Concat enabled features
    const filters = this.mapDefinition.filters.filter(f =>
      f.enabled &&
      f.datalayerId === layerId &&
      f.attribute &&
      f.operator &&
      f.value !== null
    )
      .map(f => {

        let value = [f.value];

        if (f.operator === 'in' || f.operator === '!in') {
          value = f.value.toString().split(',').map(s => s.trim()).filter(s => s !== '');
        }

        return [f.operator, f.attribute, ...value];
      });

    return ['all', ...filters];
  }

  private generateSources(mapDefinition: IMapDefinition): object {
    return  mapDefinition.layers.reduce((accum, layer) => {
      accum[layer.datasetId] = this.generateMapStyleSource(layer);
      return accum;
    }, {});
  }
  private generateMapStyleSource(layer: IMapLayer): object {
    return {
      type: 'vector',
      url: `${layer.datasetId}/index.json`
    };
  }

  private expandTileSources(baseUrl: string, sources: object): object {
    return Object.keys(sources).reduce((accum, key) => {
      let source = sources[key];

      // For vector source with tileJSON url
      if (source.hasOwnProperty('url')) {
        source = {...source, url: this.expandRelativeURL(baseUrl, source['url'])};
      }

      // For vector sources with inline tiles url
      if (source.hasOwnProperty('tiles')) {
        source = {...source, tiles: source['tiles'].map(tileUrl => this.expandRelativeURL(baseUrl, tileUrl))};
      }

      // For geojson sources
      if (source.hasOwnProperty('data')) {
        source = {...source, data: this.expandRelativeURL(baseUrl, source['data'])};
      }

      accum[key] = source;

      return accum;
    }, {});
  }

  private expandRelativeURL(baseUrl, url: string): string {
    return (url.startsWith('http')) ? url : baseUrl + url;
  }

  private applyBasemapDetail(style: IStyle): void {

    const level = style.metadata.mapDefinition.basemapDetailLevel;

    Object.keys(style.metadata.basemapDetail.layers).forEach(key => {
      const layer = style.layers.find(l => l.id === key);
      if (!layer) throw new Error(`Unknown layer ${key}`);
      const opacity = (style.metadata.basemapDetail.layers[key] <= level) ? 1 : 0;

      switch (layer.type) {
        case 'line':
          layer.paint['line-opacity'] = opacity;
          break;
        case 'symbol':
          layer.paint['text-opacity'] = opacity;
          layer.paint['icon-opacity'] = opacity;
          break;
        case 'fill':
          layer.paint['fill-opacity'] = opacity;
          break;
        default:
          throw new Error(`Unsupported layer type for basemap detail: ${layer.type}`);
      }
    });
  }

  private datasetForLayer(layer: IMapLayer): ITomboloDataset {
    return this.mapDefinition.datasets.find(ds => ds.id === layer.datasetId);
  }

  private colorRampForLayer(layer: IMapLayer): any {

    // Fixed color
    if (layer.colorMode === 'fixed' || !layer.colorAttribute) {
      return layer.fixedColor || 'black';
    }

    // Data-driven color
    const dataset = this.datasetForLayer(layer);

    if (!dataset) {
      throw new Error(`Layer'${layer.layerId} has no dataset`);
    }

    const dataAttribute = dataset.dataAttributes.find(d => d.field === layer.colorAttribute);

    if (!dataAttribute) {
      throw new Error(`Data attribute '${layer.colorAttribute} not found on dataset`);
    }

    const colorStops = [...layer.palette.colorStops];
    if (layer.paletteInverted) colorStops.reverse();
    const defaultColor = colorStops[0];

    if (dataAttribute.isCategorical) {
      // Categorical data - d3 scale is used to interpolate color stops and map values to colors
      //

      const filteredCategories = dataAttribute.categories.filter(cat => cat !== null);
      const numCategories = filteredCategories.length;

      // Reduce from category index to number between 0 and 4 (number of color stops)
      const reductionScale = d3scale.scaleLinear()
        .domain([0, numCategories])
        .range([0, 4]);

      // Map 0 - 4 to a color from the interpolated palette
      const colorScale = d3scale.scaleLinear()
        .domain([0, 1, 2, 3, 4])
        .range(colorStops);


      let ramp: any[] = ['match', ['to-string', ['get', layer.colorAttribute]], 'null', layer.fixedColor || defaultColor];

      for (let i = 0; i < numCategories; i++) {
        ramp.push(filteredCategories[i]);
        ramp.push(colorScale(reductionScale(i)));
      }

      ramp.push(defaultColor);

      console.log(ramp);

      return ramp;
    }
    else {
      // Continuous numeric data - use a mapboxgl linear interpolation from quantiles to color stops
      //
      const rampStops = dataAttribute.quantiles5.reduce((accum, val, i) => {
        accum.push(val);
        accum.push(colorStops[i]);
        return accum;
      }, []);

      const ramp = ['case',
        ['==', ['get', layer.colorAttribute], null], layer.fixedColor || defaultColor, // Null data
        ['interpolate', ['linear'], ['get', layer.colorAttribute], ...rampStops]
      ];

      return ramp;
    }
  }

  private radiusRampForLayer(layer: IMapLayer): any {

    // Fixed radius
    if (layer.sizeMode === 'fixed' || !layer.sizeAttribute) {
      return layer.fixedSize;
    }

    // Data-driven radius
    const dataset = this.datasetForLayer(layer);
    const dataAttribute = dataset.dataAttributes.find(d => d.field === layer.sizeAttribute);

    if (!dataAttribute) {
      throw new Error(`Data attribute '${layer.sizeAttribute} not found on dataset`);
    }

    if (dataAttribute.isCategorical) {
      // Categorical data - d3 scale is used to map category to size

      const filteredCategories = dataAttribute.categories.filter(cat => cat !== null);
      const numCategories = filteredCategories.length;

      // D3 scale to convert category  index into size
      const sizeScale = d3scale.scaleLinear()
        .domain([0, numCategories])
        .range([MIN_POINT_RADIUS, layer.fixedSize]);

      let ramp: any[] = ['match', ['to-string', ['get', layer.sizeAttribute]], 'null', MIN_POINT_RADIUS];

      for (let i = 0; i < numCategories; i++) {
        ramp.push(filteredCategories[i]);
        ramp.push(sizeScale(i));
      }

      ramp.push(MIN_POINT_RADIUS);

      return ramp;
    }
    else {

      const radiusRange = layer.fixedSize - MIN_POINT_RADIUS;
      const radiusPerStop = radiusRange / 5;

      const stops = dataAttribute.quantiles5.reduce((accum, val, i) => {
        accum.push(val);
        accum.push(MIN_POINT_RADIUS + radiusPerStop * i);
        return accum;
      }, []);

      const ndSize = MIN_POINT_RADIUS;

      return ['case',
        ['==', ['get', layer.sizeAttribute], null], ndSize, // Null data
        ['interpolate', ['linear'], ['get', layer.sizeAttribute], ...stops]
      ];
    }
  }

  private layerTypeForGeometryType(geometryType: string): 'circle' | 'line' | 'fill' {
    switch (geometryType) {
      case 'ST_MultiPoint': return 'circle';
      case 'ST_Point': return 'circle';
      case 'ST_MultiLineString': return 'line';
      case 'ST_LineString': return 'line';
      case 'ST_MultiPolygon': return 'fill';
      case 'ST_Polygon': return 'fill';
    }
  }
}
