import * as express from 'express';
import * as config from 'config';
import {Container} from 'typedi';
import {LoggerService} from '../../lib/logger';
import {DatasetGroup} from '../../db/models/DatasetGroup';
import {Dataset} from '../../db/models/Dataset';
import {Sequelize} from 'sequelize-typescript';
import {DB} from '../../db';
import {IDatasetGroup} from '../../shared/IDatasetGroup';
import {isAuthenticated} from '../../lib/utils';
import {TomboloMap} from '../../db/models/TomboloMap';
import {TomboloMapLayer} from '../../db/models/TomboloMapLayer';

const logger = Container.get(LoggerService);
const router = express.Router();
const db = Container.get(DB);

// Tile server config options
const baseUrl = config.get('server.baseUrl');

/**
 * Query datasets by userId or full-text search
 */
router.get('/', async (req, res, next) => {
  try {
    if (req.query.userId) {
      res.json(await Dataset.findByUserId(req.query.userId));
    }
    else if (req.query.query) {
      res.json(await Dataset.findByFullTextQuery(req.query.query));
    }
    else {
      next({status: 400, message: 'Must specify userId or query parameter'});
    }
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

///// Dataset groups

/**
 * Load all dataset groups and contained datasets
 */
router.get('/groups', async (req, res, next) => {
  try {
    const datasetGroups = await DatasetGroup.scope('full').findAll<DatasetGroup>();
    res.json(datasetGroups);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

/**
 * Load a dataset group and contained datasets
 */
router.get('/groups/:groupId', async (req, res, next) => {
  try {
    const datasetGroups = await DatasetGroup.scope('full').findById<DatasetGroup>(req.params.groupId);
    res.json(datasetGroups);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

////////// Dataset-specific routes

/**
 * Load a dataset by ID
 */
router.get('/:datasetId', async (req, res, next) => {
  try {
    const dataset = await Dataset.scope('withAttributes').findById<Dataset>(req.params.datasetId);

    if (!dataset) {
      return next({status: 404, message: 'Dataset not found'});
    }

    if (dataset.isPrivate && (!req.user || (dataset.ownerId !== req.user.id && !req.user.hasRole('editor')))) {
      return next({status: 401, message: 'Not authorized'});
    }

    res.json(dataset);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

/**
 * Delete a dataset - user must be logged in and own map
 */
router.delete('/:datasetId', isAuthenticated, async (req, res, next) => {

  try {
    const ds = await Dataset.findById<Dataset>(req.params.datasetId);

    if (!ds) {
      return next({status: 404, message: 'Dataset not found'});
    }

    if (ds.ownerId !== req.user.id && !req.user.hasRole('editor')) {
      return next({status: 401, message: 'Not authorized'});
    }

    if (ds.sourceType === 'table') {
      // Drop the associated data table
      const dropQuery = `DROP TABLE ${ds.sequelize.getQueryInterface().quoteTable(ds.source)}`;
      await ds.sequelize.query(dropQuery);
    }

    await ds.destroy();

    res.status(204).send();
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

/**
 * Load maps that are referencing the specified dataset
 */
router.get('/:datasetId/maps', isAuthenticated, async (req, res, next) => {
  try {

    const ds = await Dataset.findById<Dataset>(req.params.datasetId);

    if (!ds) {
      return next({status: 404, message: 'Dataset not found'});
    }

    if (ds.ownerId !== req.user.id && !req.user.hasRole('editor')) {
      return next({status: 401, message: 'Not authorized'});
    }

    const maps = await TomboloMap.findAll<TomboloMap>({
      include: [{
        model: TomboloMapLayer,
        where: {
          datasetId: req.params.datasetId
        }
      }]
    });

    res.json(maps);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});



export default router;
