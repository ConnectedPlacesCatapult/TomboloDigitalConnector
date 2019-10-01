/**
 * Sequelize ORM initialization and configuration
 *
 * @module DB
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import {Sequelize} from 'sequelize-typescript';
import * as config from 'config';
import {Container, Service} from 'typedi';
import {Logger, LoggerService} from '../lib/logger';
import * as path from 'path';

/**
 * DI factory function to create and configure sequelize.
 *
 * Configuration is loaded from the [db] section of the config system.
 */
function DBFactory() {
  let logger = Container.get(LoggerService);
  return new DB(logger, config.get('db'));
}

/**
 * Sequelize wrapper class
 */
@Service({factory: DBFactory})
export class DB {

  sequelize: Sequelize;

  /**
   * DB service constructor
   *
   * @param logger Logger interface
   * @param options Sequelize options
   */
  constructor(private logger: Logger, options: any) {

    // Custom logging setup to use our Logger service
    options.logging = (msg: string, ...args: any[]) => {
      // Filter out options object from args
      args = args.filter(arg => !(typeof arg === 'object' && arg.hasOwnProperty('logging')));
      logger.log(options['loglevel'], msg, ...args);
    };

    this.sequelize = new Sequelize(options);
    this.sequelize.addModels([path.join(__dirname, 'models')]);
  }

  /**
   * Check DB connection is alive and well
   */
  async checkConnection(force: boolean = false): Promise<void> {
    try {
      await this.sequelize.authenticate();

      if (force) {
        await this.sequelize.sync({force: force, match: /test/});
      }
      else {
        await this.sequelize.sync({force: force});
      }

      this.logger.info(`Connected to DB: ${this.sequelize.options.database}`);
    } catch (e) {
      this.logger.error('Error connecting to DB:', e);
      throw e;
    }
  }

  /**
   * Shutdown DB connection
   */
  close() {
    this.logger.info('Closing DB');
    return this.sequelize.close();
  }
}
