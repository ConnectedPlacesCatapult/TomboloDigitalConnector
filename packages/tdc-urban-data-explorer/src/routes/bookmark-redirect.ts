import * as express from 'express';
import {Container} from 'typedi';
import {LoggerService} from '../lib/logger';
import {Bookmark} from '../db/models/Bookmark';

const logger = Container.get(LoggerService);
const router = express.Router();

//////////////////////
// Routes

/**
 * Redirect from a bookmark
 */
router.get('/:base58Id', async (req, res, next) => {
  try {
    const bookmark = await Bookmark.findByShortId(req.params.base58Id);

    if (!bookmark) {
      return next({status: 404, message: 'Bookmark not found'});
    }

    res.redirect(bookmark.url);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

export default router;
