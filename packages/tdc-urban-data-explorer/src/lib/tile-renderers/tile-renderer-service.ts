import {Container, Service} from 'typedi';
import {Dataset} from '../../db/models/Dataset';
import {Logger, LoggerService} from '../logger';

export interface TileRenderer {

  /**
   * Render a vector tile for the given source, zoom, x and y coords
   */
  renderTile(dataset: Dataset, z: number, x: number, y: number);

  /**
   * Return tileJSON for tile source
   *
   * https://github.com/mapbox/tilejson-spec
   */
  getTileJson(dataset: Dataset): Promise<object>;

  /**
   * Close any persistent resources (e.g. DB connections)
   *
   * Called prior to graceful exit
   */
  close();
}

/**
 * DI factory function to create service.
 */
function ServiceFactory() {
  let logger = Container.get(LoggerService);
  return new TileRendererService(logger);
}

@Service({factory: ServiceFactory})
export class TileRendererService {

  private renderers: TileRenderer[] = [];
  private rendererIndex = new Map<string, TileRenderer>();

  constructor(private logger: Logger) {}

  registerRenderer(types: string[], renderer: TileRenderer): void {
    this.renderers.push(renderer);
    types.forEach(type =>  {
      this.rendererIndex.set(type, renderer);
      this.logger.info(`Registered tile renderer for '${type}`);
    });
  }

  rendererForType(type: string): TileRenderer {
    return this.rendererIndex.get(type);
  }

  close() {
    this.rendererIndex.forEach(renderer => renderer.close());
  }
}
