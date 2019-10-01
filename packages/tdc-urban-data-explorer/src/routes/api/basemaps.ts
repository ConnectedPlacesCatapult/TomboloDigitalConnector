/**
 * Basemaps Route - get basemaps
 *
 * @module Routes
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import * as express from 'express';
import {LoggerService} from '../../lib/logger';
import {Container} from 'typedi';
import {BaseMap} from '../../db/models/BaseMap';

const logger = Container.get(LoggerService);
const router = express.Router();

/**
 * Get basemaps
 */
router.get('/', async (req, res, next) => {
  try {
    const basemaps = await BaseMap.findAll<BaseMap>();
    res.json(basemaps);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

export default router;
