/**
 * Tombolo Viewer App
 *
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import 'reflect-metadata';
import * as express from 'express';
import * as morgan from 'morgan';
import * as cookieParser from 'cookie-parser';
import * as bodyParser from 'body-parser';
import * as boolParser from 'express-query-boolean';
import * as jwt from 'express-jwt';
import * as config from 'config';
import * as exphbs from 'express-handlebars';
import * as path from 'path';
import * as cors from 'cors';
import * as compression from 'compression';
import * as session from 'express-session';

// initalize sequelize with session store
var SequelizeStore = require('connect-session-sequelize')(session.Store);

import {Container} from 'typedi';
import {LoggerService} from './lib/logger';
import {DB} from './db';

// Router imports
import ConfigRouter from './routes/api/config';
import UploadsRouter from './routes/api/uploads';
import TilesRouter from './routes/tiles';
import MapsRouter from './routes/maps';
import DatasetsRouter from './routes/api/datasets';
import BookmarksRouter from './routes/api/bookmarks';
import BookmarkRedirectRouter from './routes/bookmark-redirect';
import AuthRouter from './routes/api/auth';
import BasemapsRouter from './routes/api/basemaps';
import PalettesRouter from './routes/api/palettes';

import {TileRendererService} from './lib/tile-renderers/tile-renderer-service';
import {TileliveTileRenderer} from './lib/tile-renderers/tilelive-tile-renderer';
import {PostgisTileRenderer} from './lib/tile-renderers/postgis-tile-renderer';
import {AuthService} from './lib/auth';
import {Mailer} from './lib/mailer';

const logger = Container.get(LoggerService);
const tileRendererService = Container.get(TileRendererService);
const auth = Container.get(AuthService);
const db = Container.get(DB);
const mailer = Container.get(Mailer);

const app = express();

// Configure Handlebars views
app.engine('handlebars', exphbs({defaultLayout: 'main'}));
app.set('view engine', 'handlebars');

//////////////////////////////////////////////////////////////////////////
// Configure Morgan http request logger
// Only used for dev - in production, NGINX logging is used

if (app.get('env') === 'development') {
  app.use(morgan('dev'));
}

//////////////////////////////////////////////////////////////////////////
// Register other middleware
app.use(cors(config.get('cors')));
app.use(express.static(path.join(__dirname, '../client-dist')));
app.use(bodyParser.json({limit: '1mb'}));
app.use(bodyParser.urlencoded({extended: false}));
app.use(boolParser());
app.use(cookieParser());
app.use(compression());

app.use(jwt({
  secret: config.get('jwt.secret'),
  credentialsRequired: false
}));

app.use(session({
  secret: config.get('auth.sessionSecret'),
  store: new SequelizeStore({
    db: db.sequelize
  }),
  resave: false, // we support the touch method so per the express-session docs this should be set to false
  proxy: true // if you do SSL outside of node.
}));

//////////////////////////////////////////////////////////////////////////
// Initialise Authentication
auth.init(app);

//////////////////////////////////////////////////////////////////////////
// Register Routes
app.use('/api/v1/config', ConfigRouter);
app.use('/api/v1/datasets', DatasetsRouter);
app.use('/api/v1/bookmarks', BookmarksRouter);
app.use('/api/v1/uploads', UploadsRouter);
app.use('/api/v1/auth', AuthRouter);
app.use('/api/v1/basemaps', BasemapsRouter);
app.use('/api/v1/palettes', PalettesRouter);
app.use('/tiles', TilesRouter);
app.use('/maps', MapsRouter);
app.use('/b', BookmarkRedirectRouter);

// Redirect to index.html for Angular routes
app.get('/[^\.]+$', function(req, res){
  let indexFile = path.join(__dirname, '../client-dist/index.html');
  res.set('Content-Type', 'text/html').sendFile(indexFile);
});

// catch not handled and return 404
app.use((req, res, next) => next({
  message: 'Not Found',
  status: 404,
  stack: (new Error()).stack
}));

///////////////////////////////////////////////////////////
// Error handlers

// development error handler - will print stacktrace
if (app.get('env') === 'development') {
  app.use((err, req, res, next) => {

    if (res.headersSent) {
      // Don't attempt to respond with error if headers already sent
      // Happens if a slow db query returns an error after a connection timeout
      // has already been sent
      logger.error('Error thrown after headers sent', err);
      return;
    }

    res.status(err.status || 500);
    if (isApi(req)) {
      res.json({success: false, message: err.message, error: err});
    } else {
      res.render('error', {message: err.message, error: err, layout: false});
    }
  });
}

// production error handler - no stack-traces leaked to user
app.use((err, req, res, next) => {
  if (res.headersSent) {
    // Don't attempt to respond with error if headers already sent
    // Happens if a slow db query returns an error after a connection timeout
    // has already been sent
    logger.error('Error thrown after headers sent', err);
    return;
  }

  res.status(err.status || 500);
  if (isApi(req)) {
    res.json({success: false,  message: err.message, error: err});
  } else {
    res.render('error', {message: err.message, error: {}, layout: false});
  }
});

function isApi(req) {
  return req.url.indexOf('/api/v1/') === 0;
}

export = app;

//////////////////////////////////////////////////////////////////////////
// Register Tile Renderers
const postgisTileRenderer = new PostgisTileRenderer(logger, config.get('mapnik'));
const tileliveTileRenderer = new TileliveTileRenderer(logger);
tileRendererService.registerRenderer(['table', 'sql'], postgisTileRenderer);
tileRendererService.registerRenderer(['tilelive'], tileliveTileRenderer);

//////////////////////////////////////////////////////////////////////////
// Check DB
db.checkConnection()
  .catch(e => {
    logger.error('Could connect to database', e);
    process.exit(1);
  });

//////////////////////////////////////////////////////////////////////////
// Check Mailer
mailer.checkConnection()
  .catch (e => process.exit(1));

//////////////////////////////////////////////////////////////////////////
// SIGINT handler - exit cleanly
process.on('SIGINT',  function() {
  logger.info('Received SIGINT - shutting down');
  try {
    Container.get(DB).close();
    tileRendererService.close();
    setTimeout(() => process.exit(0), 500);
  }
  catch (e) {
    logger.error('Error on shutdown', e);
    process.exit(1);
  }
});
