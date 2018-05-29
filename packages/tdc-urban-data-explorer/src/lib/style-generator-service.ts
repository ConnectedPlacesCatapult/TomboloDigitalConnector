import {Container, Service} from 'typedi';
import {Logger, LoggerService} from './logger';
import {TomboloMap} from '../db/models/TomboloMap';
import {IMapDefinition} from '../shared/IMapDefinition';
import {ITomboloDataset} from '../shared/ITomboloDataset';
import {IMapLayer} from '../shared/IMapLayer';
import {DATA_LAYER_PREFIX, StyleGenerator} from '../shared/style-generator/style-generator';
import {IStyle} from '../shared/IStyle';
import {IMapFilter} from '../shared/IMapFilter';

function ServiceFactory() {
  let logger = Container.get(LoggerService);
  return new StyleGeneratorService(logger);
}

/**
 * MapboxGL style generator
 *
 * https://www.mapbox.com/mapbox-gl-js/style-spec/
 */
@Service({factory: ServiceFactory})
export class StyleGeneratorService {

  constructor(private logger: Logger) {
  }

  /**
   * Generate a MapboxGL style for the specified map
   *
   * @param {TomboloMap} map
   * @param {string} baseUrl
   * @returns {Object}
   */
  generateMapStyle(map: TomboloMap, tileUrl: string, mapAssetsUrl: string): IStyle {

    const mapDefinition = this.generateMapDefinition(map, tileUrl, mapAssetsUrl);
    const styleGenerator = new StyleGenerator(mapDefinition);
    return styleGenerator.generateMapStyle(map.basemap);
  }

  /**
   * Generate portable map definition for map
   *
   * @param {TomboloMap} map
   * @returns {IMapDefinition}
   */
  generateMapDefinition(map: TomboloMap, tileUrl: string, mapAssetsUrl): IMapDefinition {
    let mapDefinition: IMapDefinition = {
      id: map.id,
      name: map.name,
      description: map.description,
      isPrivate: map.isPrivate,
      zoom: map.zoom,
      center: map.center,
      datasets: this.datasetsForMap(map),
      layers: this.datalayersForMap(map),
      recipe: map.recipe,
      basemapId: map.basemapId,
      basemapDetailLevel: map.basemapDetailLevel,
      tileUrl: tileUrl,
      mapAssetsUrl: mapAssetsUrl,
      ownerId: map.ownerId,
      filters: this.filtersForMap(map),
      ui: map.ui
    };

    return mapDefinition;
  }

  /**
   * Return datasets for map
   *
   * @param {TomboloMap} map
   * @returns {ITomboloDataset[]}
   */
  private datasetsForMap(map: TomboloMap): ITomboloDataset[] {

    // Reduce datasets from all map layers to remove duplicates
    const reducedDatasets: { [key: string]: ITomboloDataset } = map.layers.reduce((accum, layer) => {
      const ds = layer.dataset;
      accum[ds.id] = {
        id: ds.id,
        name: ds.name,
        description: ds.description,
        geometryType: ds.geometryType,
        minZoom: ds.minZoom,
        maxZoom: ds.maxZoom,
        extent: ds.extent,
        dataAttributes: layer.dataset.dataAttributes
          .sort((a, b) => a.order - b.order).map(attr => ({
            field: attr.field,
            name: attr.name,
            description: attr.description,
            unit: attr.unit,
            minValue: attr.minValue,
            maxValue: attr.maxValue,
            quantiles5: attr.quantiles5,
            quantiles10: attr.quantiles10,
            type: attr.type,
            categories: attr.categories,
            isCategorical: attr.isCategorical
          }))
      };

      return accum;
    }, {});

    return Object.keys(reducedDatasets).map(key => reducedDatasets[key]);
  }

  private filtersForMap(map: TomboloMap): IMapFilter[] {
    const combinedFilters = map.layers.map(layer => {
      if (!layer.filters) return [];
      return layer.filters.map(f => ({...f, datalayerId: DATA_LAYER_PREFIX + layer.layerId}));
    });

    // Flatten combined filters into a single array and return
    return [].concat.apply([], combinedFilters);
  }

  /**
   * REturn data layers for map
   *
   * @param {TomboloMap} map
   * @returns {IMapLayer[]}
   */
  private datalayersForMap(map: TomboloMap): IMapLayer[] {

    return map.layers.map(layer => {
      const datalayer: IMapLayer = {
        layerId: DATA_LAYER_PREFIX + layer.layerId,
        originalLayerId: layer.layerId,
        name: layer.name,
        description: layer.description,
        layerType: layer.layerType,
        palette: layer.palette,
        paletteId: layer.palette.id,
        paletteInverted: layer.paletteInverted,
        datasetId: layer.datasetId,
        colorMode: layer.colorMode,
        colorAttribute: layer.colorAttribute,
        fixedColor: layer.fixedColor,
        sizeMode: layer.sizeMode,
        sizeAttribute: layer.sizeAttribute,
        fixedSize: layer.fixedSize,
        labelAttribute: layer.labelAttribute,
        opacity: layer.opacity,
        visible: layer.visible,
        order: layer.order
      };

      return datalayer;
    });
  }
}
