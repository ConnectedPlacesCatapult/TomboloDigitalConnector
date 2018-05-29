/**
 * Render tilelive tiles
 *
 */

/**
 * Copyright © 2017 Emu Analytics
 */

import {Container, Inject, Service} from 'typedi';
import * as LRU from 'lru-cache';

import {Logger, LoggerService} from '../logger';
import {Dataset} from '../../db/models/Dataset';
import {TileRendererService, TileRenderer} from './tile-renderer-service';

// Initialize tilelive
const tilelive = require('@mapbox/tilelive');
require('tilelive-modules/loader')(tilelive);

/**
 * TileliveTileRenderer
 *
 * Renders Mapbox Vector Tiles from a TileLive backend data source
 */
export class TileliveTileRenderer implements TileRenderer {

  private tilesourceCache: LRU.Cache<string, any>;

  constructor(private logger: Logger) {
    this.tilesourceCache = new LRU<string, any>({
      max: 20,
      dispose: (key, tilesource) => {
        this.logger.debug(`Disposing of tilesource for ${key}`);
        tilesource.close();
      }
    });
  }

  /**
   * Render a vector tile using specified source
   */
  async renderTile(dataset: Dataset, z: number, x: number, y: number): Promise<Buffer> {

    try {
      const source = await this.getTileliveSource(dataset);

      return await this.getTile(source, z, x, y);
    }
    catch (e) {
      if (e.message === 'Tile does not exist') return Promise.resolve(new Buffer(''));
      return Promise.reject(e);
    }
  }

  /**
   * Return tileJSON for tile source
   */
  async getTileJson(dataset: Dataset): Promise<object> {

    const source = await this.getTileliveSource(dataset);

    return this.getInfo(source).then(info => {
      info.minzoom = dataset.minZoom;
      info.maxzoom = dataset.maxZoom;
      return info;
    });
  }

  close() {
    this.tilesourceCache.reset();
  }

  /**
   * Load TileLive source or return from cache
   */
  private async getTileliveSource(dataset: Dataset): Promise<any> {

    // tilelive sources are cached based on the connectionUri
    if (!this.tilesourceCache.has(dataset.source)) {
      this.logger.debug(`Acquiring tilelive source for uri: ${dataset.source}`);
      const source = await this.loadTileliveSource(dataset.source);
      this.tilesourceCache.set(dataset.source, source);
    }

    return this.tilesourceCache.get(dataset.source);
  }

  /**
   * Promisified version of TileLive::load
   */
  private loadTileliveSource(uri: string): Promise<any> {
    return new Promise((resolve, reject) => {
      tilelive.load(uri, (err, source) => err ? reject(err) : resolve(source));
    });
  }

  /**
   * Promisified version of TileLive::getTile
   */
  private getTile(source: any, z: number, x: number, y: number): Promise<any> {
    return new Promise((resolve, reject) => {
      source.getTile(z, x, y, (err, tile) => err ? reject(err) : resolve(tile));
    });
  }

  /**
   * Promisified version of TileLive::getInfo
   */
  private getInfo(source: any): Promise<any> {
    return new Promise((resolve, reject) => {
      source.getInfo((err, info) => err ? reject(err) : resolve(info));
    });
  }
}
