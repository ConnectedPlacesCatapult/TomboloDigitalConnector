import {Container, Service} from 'typedi';
import {Logger, LoggerService} from '../logger';
import {FileUpload} from '../../db/models/FileUpload';
import * as config from 'config';
import * as path from 'path';
import * as fs from 'fs';
import {Dataset} from '../../db/models/Dataset';
import {DataAttribute} from '../../db/models/DataAttribute';
import {TomboloMap} from '../../db/models/TomboloMap';
import {BaseMap} from '../../db/models/BaseMap';
import {TomboloMapLayer} from '../../db/models/TomboloMapLayer';
import {Palette} from '../../db/models/Palette';
import * as sequelize from 'sequelize';
import {IFileUpload} from '../../shared/IFileUpload';
import {StyleGenerator} from '../../shared/style-generator/style-generator';
import {DB} from '../../db/index';
import {IDBAttribute} from '../../shared/IDBAttribute';

const exec = require('child_process').exec;

export const DATATABLE_SUFFIX = '_data';

const OGRINFO_OPTIONS = '-ro -al -so -oo FLATTEN_NESTED_ATTRIBUTES=yes';
const OGR2OGR_OPTIONS = '-t_srs EPSG:4326 -oo FLATTEN_NESTED_ATTRIBUTES=yes -nlt PROMOTE_TO_MULTI -lco PRECISION=NO';
const DEFAULT_TILE_HEADERS = {'Cache-Control': 'public, max-age=86400'};
const DEFAULT_MIN_ZOOM = 7;
const DEFAULT_MAX_ZOOM = 20;

export interface OgrFileInfo {
  id: string;
  path: string;
  name?: string;
  description?: string;
  attribution?: string;
  removed?: boolean;
  driver: string;
  geometryType: string;
  featureCount: number;
  srs: string;
  attributes: OgrAttribute[];
}

export interface OgrAttribute {
  id: string;
  type: string;
  precision?: number;
  name?: string;
  description?: string;
  unit?: string;
  removed?: boolean;
}

/**
 * DI factory function to create service.
 */
function ServiceFactory() {
  let logger = Container.get(LoggerService);
  let db = Container.get(DB);
  return new FileIngester(logger, db, config.get('db'));
}

@Service({factory: ServiceFactory})
export class FileIngester {

  constructor(private logger: Logger, private db: DB, private dbConfig: object) {}

  async processFile(file: FileUpload): Promise<OgrFileInfo> {

    if (!file) throw new Error('file required');

    try {
      // Handle zip file uploads using GDAL virtual file system
      const ext = path.extname(file.originalName);
      if (ext === '.zip') {
        const newPath = file.path + '.zip';
        fs.renameSync(file.path, newPath);
        file.path = '/vsizip/' + newPath;
      }

      // Validate file
      file.status = 'validating';
      await file.save();
      const fileInfo = await this.validateFile(file);

      // Ingest file
      await file.update({status: 'ingesting', ogrInfo: fileInfo});
      await this.ingestFile(file);

      file.status = 'done';
      await file.save();

      return fileInfo;
    }
    catch (e) {
      this.logger.error(e.message);
      await file.update({status: 'error', error: e.message});

      // Do not return a rejected promise because no-one is listening
      // This is running detached and an unhandled rejected promise will
      // be fatal in future versions of node.
      throw e;
    }
  }

  /**
   * Validate given file is a supported geospatial file using ogrinfo
   *
   * @param {string} path
   */
  validateFile(file: IFileUpload): Promise<OgrFileInfo> {

    if (!file) return Promise.reject(new Error('file required'));

    const cmd = `ogrinfo ${OGRINFO_OPTIONS} ${file.path}`;

    this.logger.info(`Executing ogrinfo for upload: ${file.id}`, cmd);

    return new Promise((resolve, reject) => {
      exec(cmd, (err, stdout) => {
        if (err) {
          // ogrinfo error message is actually returned through stdout
          reject(new Error(this.interpretOgInfo(stdout)));
        }

        let fileInfo: OgrFileInfo = {} as any;

        try {
          // Extract driver type
          const driverRegex = /using driver `(.*)'/;
          const driver = stdout.match(driverRegex);
          fileInfo.driver = driver ? driver[1] : null;

          // Extract key/value pairs
          const keyValueRegex = /^(.*):\s(.*)$/gm;
          let keys = [];
          let keyValues = {};
          let temp;
          while ((temp = keyValueRegex.exec(stdout)) !== null) {
            keys.push(temp[1]);
            keyValues[temp[1]] = temp[2];
          }

          // Extract layer info
          fileInfo.geometryType = keyValues['Geometry'];
          fileInfo.featureCount = +keyValues['Feature Count'];

          let srs = keyValues['Layer SRS WKT'].match(/"(.*)"/);
          if (srs) fileInfo.srs = srs[1];

          // Extract data attributes
          let attributes = [];
          const attributeStartIndex = keys.indexOf('Layer SRS WKT');
          if (attributeStartIndex > -1) {
            for (let i = attributeStartIndex + 1; i < keys.length; i++) {
              const key = keys[i];
              const value = keyValues[key];
              const type = this.convertOgrType(value.match(/^(\w*)/)[1].toLowerCase());
              const precision = +(value.match(/\((.*)\)/)[1]);
              attributes.push({id: this.convertKeyToPostgres(keys[i]), type, precision});
            }
          }

          fileInfo.attributes = attributes;

          resolve(fileInfo);
        }
        catch (e) {
          this.logger.error('Validation error', e);
          reject(e);
        }
      });
    });
  }

  ingestFile(file: FileUpload): Promise<void> {

    if (!file) return Promise.reject(Error('file required'));

    const database = this.dbConfig['database'];
    const host = this.dbConfig['host'];
    const port = this.dbConfig['port'];
    const username = this.dbConfig['username'];
    const password = this.dbConfig['password'];

    const cmd = `ogr2ogr -f "PostgreSQL" PG:"dbname='${database}' host='${host}' port='${port}' user='${username}' password='${password}'" \
    ${file.path} ${OGR2OGR_OPTIONS} -nln ${file.id}${DATATABLE_SUFFIX}`;

    this.logger.info(`Executing ogr2ogr for upload: ${file.id}`, cmd);

    return new Promise((resolve, reject) => {
      let env = {...process.env};
      env['PG_USE_COPY'] = 'YES';
      exec(cmd, {env}, (err, stdout) => {
        if (err) {
          // ogr2ogr error message is actually returned through stdout
          reject(new Error(stdout));
        }

        // Capture attribute type info from newly created table
        this.db.sequelize.getQueryInterface().describeTable(file.tableName()).then((results) => {
          file.dbAttributes = Object.keys(results).map(key => ({...results[key], field: key}));
          resolve();
        })
          .catch(e => {
            this.logger.error(e);
          });
      });
    });
  }

  async finalizeUpload(file: FileUpload, newFile: IFileUpload): Promise<void> {

    // Change column types - update all columns whose type has been changed
    await Promise.all(newFile.dbAttributes.filter(attr => attr.type !== file.attributeType(attr.field))
      .map(attr => {
        const columnTypeSql = `
          ALTER TABLE ${file.sqlSafeTableName()} 
          ALTER COLUMN ${file.sqlSafeAttributeColumn(attr.field)} SET DATA TYPE ${attr.type} 
          USING ${file.sqlSafeAttributeColumn(attr.field)}::${attr.type};`;
        return file.sequelize.query(columnTypeSql);
      }));

    // Remove unwanted columns
    await Promise.all(newFile.dbAttributes.filter(attr => attr.removed).map(attr => {
      const dropColumnSql = `ALTER TABLE ${file.sqlSafeTableName()} DROP COLUMN ${file.sqlSafeAttributeColumn(attr.field)};`;
      return file.sequelize.query(dropColumnSql);
    }));

    return;
  }

  async generateDataset(file: FileUpload): Promise<Dataset> {

    if (!file) return Promise.reject(Error('file required'));

    const geometryType = await this.queryGeometryType(file);

    // Create dataset
    const dataset = await Dataset.create<Dataset>({
      name: file.name,
      description: file.description,
      attribution: file.attribution,
      sourceType: 'table',
      source: file.tableName(),
      geometryColumn: 'wkb_geometry',
      geometryType: geometryType,
      originalBytes: file.size,
      isPrivate: true,
      headers: DEFAULT_TILE_HEADERS,
      minZoom: DEFAULT_MIN_ZOOM,
      maxZoom: DEFAULT_MAX_ZOOM,
      ownerId: file.ownerId
    });

    await file.$set('dataset', dataset);

    // Create data attributes

    this.logger.info('DB Attributes', file.dbAttributes);

    await Promise.all(file.dbAttributes.filter(attr => !attr.removed && attr.field !== 'wkb_geometry').map((attr, index) => {

      // Convert attr type to javascript type
      const datasetType = this.datasetTypeForDbType(attr.type);

      return dataset.$create('dataAttribute', {
        datasetId: dataset.id,
        field: attr.field,
        type: datasetType,
        name: attr.name,
        description: attr.description,
        unit: attr.unit,
        order: index
      });
    }));

    // Calculate dataset stats, extent and dataset radius
    await dataset.calculateDataAttributeStats();
    await dataset.calculateGeometryExtent();
    await dataset.calculateDatasetBytes();
    await dataset.reload({include: [DataAttribute]});

    return dataset;
  }

  async generateMap(file: FileUpload): Promise<TomboloMap> {

    const basemap = await BaseMap.getDefault();
    const palette = await Palette.getDefault();

    const [lng, lat, zoom] = this.centerAndZoomFromExtent(file.dataset.extent);

    // Create map
    const map = await TomboloMap.create<TomboloMap>({
      name: `Map of ${file.dataset.name}`,
      description: `Map of uploaded dataset ${file.dataset.name}`,
      center: [lng, lat],
      zoom: zoom,
      basemapId: (basemap) ? basemap.id : null,
      basemapDetailLevel: 4,
      ownerId: file.ownerId,
      isPrivate: true
    });

    await file.$set('map', map);

    // Style generator is used to create the default map layer.
    // !!!No map definition is given so don't try to call anything other than generateDefaultDataLayer!!!
    const styleGenerator = new StyleGenerator(null);
    let layer = styleGenerator.generateDefaultDataLayer(file.dataset, palette);

    // !!! styleGenerator.generateDefaultDataLayer generates layerId as mapboxGl prefixed layer ID !!!
    layer.layerId = layer.originalLayerId;

    // Create map layer
    await map.$create<TomboloMapLayer>('layer', layer);

    return map;
  }

  private queryGeometryType(file: FileUpload) {
    const geometryTypeSql = `SELECT ST_GeometryType("wkb_geometry") as geometrytype from ${file.sqlSafeTableName()} limit 1;`;
    return file.sequelize.query(geometryTypeSql, {type: sequelize.QueryTypes.SELECT}).then(result => {

      return result[0]['geometrytype'];
    });
  }

  private centerAndZoomFromExtent(extent: number[]): number[] {
    const centerLng = (extent[2] - extent[0]) / 2 + extent[0];
    const centerLat = (extent[3] - extent[1]) / 2 + extent[1];

    // TODO Calculate zoom

    return [centerLng, centerLat, 8];
  }

  private interpretOgInfo(ogInfo) {
    if (ogInfo.search('with the following drivers') !== -1) {
      return 'The file is not in a supported format. You can upload geoJSON or zipped shapefiles.';
    }
    return ogInfo;
  }

  private convertKeyToPostgres(key: string): string {

    // TODO - check what else GDAL laundering does to field names

    return key.toLowerCase().replace(/[-#]/g, '_');
  }

  // Massage type return from OGR
  private convertOgrType(type: string): string {
    if (type === 'integer64') type = 'integer';
    return type;
  }

  private datasetTypeForDbType(type: string): 'string' | 'number' | 'date' | 'datetime' {
    switch (type) {
      case 'CHARACTER VARYING': return 'string';
      case 'INTEGER': return 'number';
      case 'BIGINT': return 'string'; // bigints need to be handled as strings
      case 'DOUBLE PRECISION': return 'number';
      case 'DATE': return 'date';
      case 'TIMESTAMP WITH TIME ZONE': return 'datetime';

      default:
        this.logger.warn(`Unrecognized DB type '${type}'`);
        return 'string';
    }
  }
}
