import * as express from 'express';
import * as config from 'config';
import {Bookmark} from '../../db/models/Bookmark';
import {LoggerService} from '../../lib/logger';
import {Container} from 'typedi';

const router = express.Router();

// Tile server config options
const baseUrl = config.get('server.baseUrl');

const logger = Container.get(LoggerService);

//////////////////////
// Routes

/**
 * Post a bookmark
 */
router.post('/', async (req, res, next) => {
  try {
    const bookmark = await Bookmark.create<Bookmark>({url: req.body.url});
    res.json({shortUrl: `${baseUrl}/b/${bookmark.shortId}`});
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

export default router;
