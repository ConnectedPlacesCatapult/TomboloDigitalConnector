/**
 * App-wide logger service
 *
 * @module Logger
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import * as winston from 'winston';
import * as config from 'config';

import {Service, Token} from 'typedi';

/**
 * DI token to retrieve logger service
 */
export const LoggerService = new Token<Logger>();

/**
 * Generic Logger interface
 */
export interface Logger {
  log(level: string, msg: string, ...meta: any[]): void;
  silly(msg: string, ...meta: any[]): void;
  debug(msg: string, ...meta: any[]): void;
  info(msg: string, ...meta: any[]): void;
  warn(msg: string, ...meta: any[]): void;
  error(msg: string, ...meta: any[]): void;
}

/**
 * Default Logger service that uses Winston as logging backend
 */
@Service(LoggerService)
class WinstonLogger implements Logger {

  logger: winston.LoggerInstance;

  constructor() {
    this.logger = new (winston.Logger)({
      transports: [
        new (winston.transports.Console)({
          timestamp: true,
          colorize: process.env.NODE_ENV !== 'production',
          level: config.get('logger.level'),
          label: config.get('logger.label')
        })
    ]});
  }

  log(level: string, msg: string, ...meta: any[]): void {
    this.logger.log(level, msg, ...meta);
  }

  silly(msg: string, ...meta: any[]): void {
    this.logger.log('silly', msg, ...meta);
  }

  debug(msg: string, ...meta: any[]): void {
    this.logger.log('debug', msg, ...meta);
  }

  info(msg: string, ...meta: any[]): void {
    this.logger.log('info', msg, ...meta);
  }

  warn(msg: string, ...meta: any[]): void {
    this.logger.log('warn', msg, ...meta);
  }

  error(msg: string, ...meta: any[]): void {
    this.logger.log('error', msg, ...meta);
  }
}
