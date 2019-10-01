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
import {Palette} from '../../db/models/Palette';

const logger = Container.get(LoggerService);
const router = express.Router();

/**
 * Get palettes
 */
router.get('/', async (req, res, next) => {
  try {
    const palettes = await Palette.findAll<Palette>({order: ['groupId', 'order']});
    res.json(palettes);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

export default router;
