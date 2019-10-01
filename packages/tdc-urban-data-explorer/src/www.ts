/**
 * Server Entrypoint
 *
 * @module WWW
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import * as app from './app';
import * as http from 'http';
import {LoggerService} from './lib/logger';
import {Container} from 'typedi';

let logger = Container.get(LoggerService);

/**
 * Get port from environment and store in Express.
 */

let port = normalizePort(process.env['PORT'] || '3000');
app.set('port', port);

/**
 * Create HTTP server.
 */

let server = http.createServer(app);

/**
 * Listen on provided port, on all network interfaces.
 */

server.listen(port);
server.on('error', onError);
server.on('listening', onListening);

/**
 * Normalize a port into a number, string, or false.
 */

function normalizePort(val) {
  let port = parseInt(val, 10);

  if (isNaN(port)) {
    // named pipe
    return val;
  }

  if (port >= 0) {
    // port number
    return port;
  }

  return false;
}

/**
 * Event listener for HTTP server "error" event.
 */

function onError(error) {
  if (error.syscall !== 'listen') {
    throw error;
  }

  let bind = typeof port === 'string'
    ? 'Pipe ' + port
    : 'Port ' + port;

  // handle specific listen errors with friendly messages
  switch (error.code) {
    case 'EACCES':
      logger.error(bind + ' requires elevated privileges');
      process.exit(1);
      break;
    case 'EADDRINUSE':
      logger.error(bind + ' is already in use');
      process.exit(1);
      break;
    default:
      throw error;
  }
}

/**
 * Event listener for HTTP server "listening" event.
 */

function onListening() {
  let address = server.address();
  let bind = typeof address === 'string'
    ? 'pipe ' + address
    : 'port ' + address.port;
  logger.info('Listening on %s', bind);
}
