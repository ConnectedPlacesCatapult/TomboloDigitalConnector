import * as path from 'path';
import {Container} from 'typedi';
import * as LRU from 'lru-cache';
import * as SphericalMercator from '@mapbox/sphericalmercator';
import * as Handlebars from 'handlebars';
import * as mapnik from 'mapnik';
import * as moment from 'moment';

import {startTimer} from '../utils';

import {Logger} from '../logger';
import {Dataset} from '../../db/models/Dataset';
import {TileRenderer} from './tile-renderer-service';
import {DB} from '../../db/index';
import {DATA_LAYER_ID} from '../../shared/style-generator/style-generator';
import * as sequelize from 'sequelize';

const db = Container.get(DB);

// Layer id to use for the single tile layer supported by this renderer


mapnik.register_datasource(path.join(mapnik.settings.paths.input_plugins, 'geojson.input'));

interface TileInfo {
  x: number;
  y: number;
  z: number;
  bounds: number[];
  bbox: string;
  bbox_4326: string;
  bbox_3857: string;
  queryParams: object;
}

/**
 * PostgisTileRenderer
 *
 * Renders Mapbox Vector Tiles from PostGIS data source
 */
export class PostgisTileRenderer implements TileRenderer {

  private sqlTemplateCache: LRU.Cache<string, HandlebarsTemplateDelegate>;
  private projection = new SphericalMercator({size: 256});

  constructor(private logger: Logger, private mapnikOptions: object) {
    this.sqlTemplateCache = new LRU<string, HandlebarsTemplateDelegate>(50);
  }

  /**
   * Render a vector tile for the given dataset, zoom, x and y coords
   */
  async renderTile(dataset: Dataset, z: number, x: number, y: number): Promise<Buffer> {

    const tileInfo = this.getTileInfo(z, x, y);
    const query = this.generateQuery(dataset);
    const expandedQuery = this.expandTemplateSql(query, tileInfo);
    const features = await this.runQuery(expandedQuery);

    return this.generateMVT(tileInfo, features);
  }

  /**
   * Return standard tileJSON format fot the specified dataset
   */
  async getTileJson(dataset: Dataset): Promise<object> {

    let fields = {};
    dataset.dataAttributes.forEach(d => fields[d.field] = d.type);

    // Currently only a single layer per dataset is supported. It has a fixed name of 'data'.
    const vectorLayers = [{
      id: DATA_LAYER_ID,
      description: dataset.description,
      minZoom: dataset.minZoom,
      maxZoom: dataset.maxZoom,
      fields
    }];

    const center = [(dataset.extent[0] + dataset.extent[2]) / 2, (dataset.extent[1] + dataset.extent[3]) / 2, 4];
    return {
      tilejson: '2.0.0',
      scheme: 'tms',
      format: 'pbf',
      vector_layers: vectorLayers,
      minzoom: dataset.minZoom,
      maxzoom: dataset.maxZoom,
      center: center,
      id: dataset.id,
      name: dataset.name,
      description: dataset.description,
      attribution: dataset.attribution,
      bounds: dataset.extent
    };
  }

  /**
   * Cleanup the tile renderer
   */
  close() {
    this.sqlTemplateCache.reset();
  }

  /**
   * Generate SQL to query the dataset source table
   */
  private generateQuery(dataset: Dataset): string {

    // A dataset can specify an SQL query directly (for complex joins etc.)
    if (dataset.sourceType === 'sql') return dataset.source;

    // Otherwise the table name and associated data attributes are used to create the query

    let fieldList = dataset.dataAttributes.map(d => `${d.sqlSafeField()} as "${d.field}"`);

    fieldList.push(`"${dataset.geometryColumn}" as "geometry"`);

    return `SELECT ${fieldList.join(',')} from "${dataset.source}" where "${dataset.geometryColumn}" && {{bbox}}`;
  }

  /**
   * Run an SQL select query
   */
  private async runQuery(sql: string): Promise<any[]> {

    const timer = startTimer();
    const results = await db.sequelize.query(sql, {type: sequelize.QueryTypes.SELECT});
    this.logger.debug(`Query returned ${results.length} features in ${timer()} ms`);

    return results;
  }

  /**
   * Expand the SQL query using Handlebars syntax and a tile info object as context
   *
   * e.g. {{bbox}} in the query will be expanded to the tile bounding box
   */
  private expandTemplateSql(sql: string, tileInfo: TileInfo): string {
    if (!this.sqlTemplateCache.has(sql)) {
      this.logger.debug(`Compiling template for sql: ${sql}`);
      this.sqlTemplateCache.set(sql, Handlebars.compile(sql));
    }

    return this.sqlTemplateCache.get(sql)(tileInfo);
  }

  /**
   * Return tile info for use as Handlebars context during expansion of SQL
   */
  private getTileInfo(z: number, x: number, y: number): TileInfo {

    let tile: any = {};

    tile.x = x;
    tile.y = y;
    tile.z = z;
    tile.bounds = this.projection.bbox(x, y, z, false);

    tile.bbox_4326 =
      `ST_SetSRID(
        ST_MakeBox2D(
          ST_MakePoint(${tile.bounds[0]}, ${tile.bounds[1]}),
          ST_MakePoint(${tile.bounds[2]}, ${tile.bounds[3]})
        ), 4326)`;

    tile.bbox_3857 = `ST_Transform(${tile.bbox_4326}, 3857)`;
    tile.bbox = tile.bbox_4326;

    // Calculate pixel width and height in lat/lng degrees (useful for PostGIS functions like ST_Simplify that
    // take a tolerance in coordinate units
    const tileSize = this.projection.size;
    // Pixel coords of tile centre
    const pixelCenter = [x * tileSize + tileSize / 2, y * tileSize + tileSize / 2];
    // lng/lat of tile centre
    const latLngCenter = this.projection.ll(pixelCenter, z);
    // lng/lat of tile centre + one pixel
    const latLngCenterPlusOne = this.projection.ll([pixelCenter[0] + 1, pixelCenter[1] + 1], z);

    tile.pixelWidth = latLngCenterPlusOne[0] - latLngCenter[0];
    tile.pixelHeight = latLngCenter[1] - latLngCenterPlusOne[1];

    return tile as TileInfo;
  }

  /**
   * Generate a Mapbox Vector Tile from query results
   */
  private generateMVT(tileInfo: TileInfo, tileQueryResults: any[]): Promise<Buffer> {

    return new Promise((resolve, reject) => {
      const vtile = new mapnik.VectorTile(tileInfo.z, tileInfo.x, tileInfo.y);
      const geojson = this.generateGeojson(tileQueryResults);

      this.logger.silly('Geojson', geojson);

      vtile.addGeoJSON(JSON.stringify(geojson), DATA_LAYER_ID, this.mapnikOptions);

      if (!vtile.painted()) {
        // Empty tile
        return resolve(new Buffer(''));
      }

      vtile.getData({
        compression: 'gzip',
        level: 9,
        strategy: 'FILTERED'
      }, (err, data) => {
        if (err) return reject(err);
        resolve(data);
      });
    });
  }

  /**
   * Convert query results to a GeoJSON FeatureCollection
   */
  private generateGeojson( tileQueryResults: any[]): GeoJSON.FeatureCollection<GeoJSON.GeometryObject> {

    let features = tileQueryResults.map(feature => {

      let geom = feature['geometry'];
      if (!geom) throw new Error('No geometry field on feature');

      Object.keys(feature).forEach(key => {
        if (feature[key] instanceof Date) {
          feature[key] = moment(feature[key]).valueOf();
        }
      });

      let geojsonFeature = {
        type: 'Feature',
        geometry: geom,
        properties: feature
      };

      delete geojsonFeature.properties['geometry'];

      return geojsonFeature;
    });

    return {
      type: 'FeatureCollection',
      features: features
    } as GeoJSON.FeatureCollection<GeoJSON.GeometryObject>;
  }
}
