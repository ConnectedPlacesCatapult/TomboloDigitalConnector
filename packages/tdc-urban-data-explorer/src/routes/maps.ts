import * as express from 'express';
import * as config from 'config';
import {Container} from 'typedi';
import {LoggerService} from '../lib/logger';
import {TomboloMap} from '../db/models/TomboloMap';
import {StyleGeneratorService} from '../lib/style-generator-service';
import {MapGroup} from '../db/models/MapGroup';
import {isAuthenticated} from '../lib/utils';
import {IMapDefinition} from '../shared/IMapDefinition';

const logger = Container.get(LoggerService);
const styleGeneratorService = Container.get(StyleGeneratorService);
const router = express.Router();

// Tile server config options
const mapsUrl = config.get('server.baseUrl') + '/maps/';
const tilesUrl = config.get('server.baseUrl') + '/tiles/';
const mapAssetsUrl = config.get('server.mapAssetsUrl') || config.get('server.baseUrl') + '/static/';

//////////////////////
// Routes


// Get maps
router.get('/', async (req, res, next) => {
  try {

    let where: object;

    if (req.query.userId) {

      if (!req.user ||  (req.user.id !== req.query.userId && !req.user.hasRole('editor'))) {
        return next({status: 401, message: 'Not authorized'});
      }

      // Get user's maps
      where = {
        ownerId: req.query.userId
      };
    }
    else {
      // Get all public maps
      where = {
        $or: [{isPrivate: false}, {isPrivate: null}]
      };
    }

    const maps = await TomboloMap.findAll<TomboloMap>({
      where,
      order: ['name'],
      limit: 1000});

    res.json(maps.map(clientSafeMap));
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

// Save a map
router.put('/:mapId', isAuthenticated, async (req, res, next) => {

  try {
    const mapDefinition = req.body as IMapDefinition;

    console.log('map', mapDefinition.id);
    console.log('user', req.user.id);

    if (mapDefinition.ownerId !== req.user.id && !req.user.hasRole('editor')) {
      return next({status: 401, message: 'Not authorized'});
    }

    await TomboloMap.saveMap(mapDefinition);

    // Generate style from updated map and respond
    const map = await TomboloMap.scope('full').findById<TomboloMap>(req.params.mapId);
    res.json(styleGeneratorService.generateMapStyle(map, tilesUrl, mapAssetsUrl));
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});


// Delete a map - user must be logged in and own map
router.delete('/:mapId', isAuthenticated, async (req, res, next) => {

  try {
    const map = await TomboloMap.findById<TomboloMap>(req.params.mapId);

    if (!map) {
      return next({status: 404, message: 'Map not found'});
    }

    if (map.ownerId !== req.user.id && !req.user.hasRole('editor')) {
      return next({status: 401, message: 'Not authorized'});
    }

    await map.destroy();

    res.status(204).send();
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

// Get maps grouped by groupID for populating left hand navigation
router.get('/grouped', async (req, res, next) => {
  try {

    // Get system map groups
    const mapGroups = await MapGroup.scope('systemMaps').findAll<MapGroup>();

    let results = mapGroups.map(group => ({
      id: group.id,
      name: group.name,
      order: group.order,
      maps: group.maps.map(clientSafeMap)
    }));

    // Get user's maps
    if (req.user) {
      const userMaps = await TomboloMap.findAll<TomboloMap>({
        where: {ownerId: req.user.id},
        order: ['name']
      });

      const userGroup = {
        id: 'usergroup',
        name: 'My Maps',
        order: 99,
        maps: userMaps.map(clientSafeMap)
      };

      results.push(userGroup);
    }

    res.json(results);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

/**
 * Get a map style
 */
router.get('/:mapId/style.json', async (req, res, next) => {

  try {
    const map = await TomboloMap.scope('full').findById<TomboloMap>(req.params.mapId);

    if (map.isPrivate && (!req.user || map.ownerId !== req.user.id)) {
      return next({status: 401, message: 'Not authorized'});
    }

    if (!map) {
      return next({status: 404, message: 'Map not found'});
    }

    res.json(styleGeneratorService.generateMapStyle(map, tilesUrl, mapAssetsUrl));
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});


////////////////
// Route helpers

function clientSafeMap(map: TomboloMap): object {
  return {
    id: map.id,
    name: map.name,
    description: map.description,
    isPrivate: map.isPrivate,
    icon: map.icon,
    groupId: map.mapGroupId,
    ownerId: map.ownerId,
    ui: map.ui,
    styleUrl: `${mapsUrl}/${map.id}/style.json`
  };
}

export default router;
