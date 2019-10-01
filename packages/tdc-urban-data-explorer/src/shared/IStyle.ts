import {IMapDefinition} from './IMapDefinition';

export interface IStyleLayer {
  id: string;
  source: string;
  'source-layer': string;
  type: 'fill' | 'circle' | 'line' | 'symbol';
  paint?: object;
  layout?: object;
  filter?: any[];
  minzoom?: number;
  maxzoom?: number;
}

export interface IStyle {
  zoom: number;
  center: number[];
  glyphs: string;
  sprite: string;
  sources: object;
  layers: IStyleLayer[];
  metadata: IStyleMetadata;
}

export interface IStyleMetadata {
  mapDefinition: IMapDefinition;
  insertionPoints: {[key: string]: string};
  basemapDetail: IBasemapDetailMetadata;
  labelLayerStyle: ILabelLayerStyleMetadata;
}

export interface IBasemapDetailMetadata {
  defaultDetailLevel: number;
  layers: {[key: string]: number};
}

export interface ILabelLayerStyleMetadata {
  paint: {[key: string]: any};
  layout: {[key: string]: any};
}
