/**
 * Service to send email using Handlebar templates
 *
 * @module Mailer
 */

/**
 * Copyright © 2017 Emu Analytics
 */

import * as config from 'config';
import * as nodemailer from 'nodemailer';
import {SentMessageInfo, Transporter} from 'nodemailer';
import * as path from 'path';
import * as Handlebars from 'handlebars';
import {Container, Service} from 'typedi';

import {readFile} from './nodeApi';
import {Logger, LoggerService} from './logger';

/**
 * Mailer service configuration
 */
export interface MailerConfig {
  /** Email address to use as 'from' address **/
  from?: string;
  /** Base path of template directory (can be relative to CWD) **/
  basePath?: string;
}

/**
 * DI factory function to create and configure mailer.
 *
 * Configuration is loaded from the [smtp] section of the config system.
 */
function mailerFactory() {
  let logger = Container.get(LoggerService);
  let transporter = nodemailer.createTransport(config.get('smtp'));
  const baseLinkUrl = config.get('server.baseUrl');
  const mailerConfig = config.get('emailTemplates');
  return new Mailer(transporter, logger, baseLinkUrl, mailerConfig);
}

/**
 * Mailer service class
 */
@Service({factory: mailerFactory})
export class Mailer {

  static defaultConfig: MailerConfig = {
    from: 'info@emu.analytics.com',
    basePath: './email-templates'
  };

  private config: MailerConfig;

  constructor(private transporter: Transporter, private logger: Logger, private baseLinkUrl: string, config: MailerConfig) {
    this.config = {...Mailer.defaultConfig, ...config};
  }

  /**
   * Send email using the specified Handlebars template and context.
   *
   * @param to The 'to' email address
   * @param subject The email subject
   * @param templatePath Path to handlebars template (relative to `templateBasePath`)
   * @param context Context for rendering template
   */
  async sendMail(to: string, subject: string, templatePath: string, context: object): Promise<SentMessageInfo> {

    const template = await this.loadTemplate(templatePath);
    const renderedMessage = Handlebars.compile(template)({baseLinkUrl: this.baseLinkUrl, ...context});

    const messageEnvelope = {
      from: this.config.from,
      to,
      subject,
      html: renderedMessage
    };

    this.logger.info('Sending mail:', messageEnvelope.to, templatePath);
    this.logger.debug('Message Envelope', messageEnvelope);

    try {
      const result = await this.transporter.sendMail(messageEnvelope);
      this.logger.info('Message sent OK:', messageEnvelope.to, templatePath);
      return result;
    } catch (e) {
      this.logger.error('Message failed to send', e);
      return Promise.reject(e);
    }
  }

  /**
   * Check SMTP transport is working
   */
  async checkConnection(): Promise<void> {
    try {
      await this.transporter.verify();
      this.logger.info('Connected to SMTP Server');
    } catch (e) {
      this.logger.error(`Error connecting to SMTP server: ${e.message}`);
      throw e;
    }
  }

  /** Load a template from the specified path (relative to `templateBasePath`) **/
  private async loadTemplate(templatePath: string): Promise<string> {

    templatePath = path.resolve(this.config.basePath, templatePath);
    let template = await readFile(templatePath, 'utf-8');

    return template;
  }
}
