import * as express from 'express';
import * as LRU from 'lru-cache';
import * as config from 'config';

import {Container} from 'typedi';
import {LoggerService} from '../lib/logger';
import {Dataset} from '../db/models/Dataset';
import {startTimer} from '../lib/utils';
import {DataAttribute} from '../db/models/DataAttribute';
import {TileRenderer, TileRendererService} from '../lib/tile-renderers/tile-renderer-service';

const logger = Container.get(LoggerService);
const router = express.Router();
const tileRendererService = Container.get(TileRendererService);

// Cache of dataset instances
const datasetCache = new LRU<string, Dataset>(50);

// Tile server config options
const baseUrl = config.get('server.baseUrl');
const datasetCaching = config.get('server.datasetCaching');
const slowTileLimit = config.get('server.slowTileLimit');
const largeTileLimit = config.get('server.largeTileLimit');


//////////////////////
// Routes

/**
 * Get a map tile
 */
router.get('/:datasetId/:z(\\d+)/:x(\\d+)/:y(\\d+).:ext(pbf|mvt)', async (req, res, next) => {

  try {

    const timer = startTimer();

    const x = Math.floor(req.params.x);
    const y = Math.floor(req.params.y);
    const z = Math.floor(req.params.z);

    const dataset = await loadDataset(req.params.datasetId);

    if (z < dataset.minZoom || z > dataset.maxZoom) {
      // Outside of dataset zoom range
      return res.status(404).send();
    }

    // Generate Tile
    //
    const renderer = tileRendererForDataset(dataset);
    const tile = await renderer.renderTile(dataset, z, x, y);
    const renderTime = timer().toFixed(2);

    // Set response headers
    //
    res.set('Content-Type', 'application/x-protobuf');
    res.set('Content-Encoding', 'gzip');
    res.set('X-Render-Time', `${renderTime} ms`);

    // Add dataset-specific headers if any
    if (dataset.headers) {
      Object.keys(dataset.headers).forEach(key => res.set(key, dataset.headers[key]));
    }

    // Warnings for slow or large tiles
    if (renderTime > slowTileLimit) {
      logger.warn(`Slow tile: ${req.url} [${renderTime} ms]`);
    }

    if (tile.length > largeTileLimit) {
      logger.warn(`Large tile: ${req.url} [${tile.length} bytes]`);
    }

    if (tile.length === 0) {
      // Mapbox GL expects 204 for an empty tile
      res.status(204).send();
    }
    else {
      res.send(tile);
    }

  } catch (e) {
    logger.error(e);
    next(e);
  }
});

/**
 * Get tileJSON for a dataset
 */
router.get('/:datasetId/index.json', async (req, res, next) => {
  try {
    const dataset = await loadDataset(req.params.datasetId);
    const renderer = tileRendererForDataset(dataset);
    let info  = await renderer.getTileJson(dataset);

    // Add tile url to tileJSON
    info['tiles'] = [
      `${baseUrl}/tiles/${dataset.id}/{z}/{x}/{y}.pbf`
    ];

    res.json(info);

  } catch (e) {
    logger.error(e);
    next(e);
  }
});

router.get('/:datasetId/calculatestats', async (req, res, next) => {
  try {
    const dataset = await loadDataset(req.params.datasetId);
    await dataset.calculateDataAttributeStats();
    await dataset.calculateGeometryExtent();
    await dataset.calculateDatasetBytes();
    await dataset.reload({include: [DataAttribute]});
    res.json(dataset);
  } catch (e) {
    logger.error(e);
    next(e);
  }
});

///////////////////////////
// Route internal functions

/**
 * Load a dataset from DB (or return cached value)
 */
async function loadDataset(datasetId: string): Promise<Dataset> {


  if (!datasetCaching || !datasetCache.has(datasetId)) {
    const dataset = await Dataset.findById<Dataset>(datasetId, {include: [DataAttribute]});

    if (!dataset) {
      return Promise.reject({status: 404, message: 'Dataset not found'});
    }

    if (datasetCaching) {
      logger.debug(`Caching dataset '${dataset.id}'`);
      datasetCache.set(dataset.id, dataset);
    }

    return dataset;
  }

  return datasetCache.get(datasetId);
}

/**
 * Return tile renderer for given tilesource type
 */
function tileRendererForDataset(dataset: Dataset): TileRenderer {

  return tileRendererService.rendererForType(dataset.sourceType);

}

export default router;
