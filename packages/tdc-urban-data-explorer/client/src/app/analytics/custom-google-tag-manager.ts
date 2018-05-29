import {Inject, Injectable} from '@angular/core';

import { Angulartics2, GoogleTagManagerSettings } from 'angulartics2';
import {APP_CONFIG, AppConfig} from "../config.service";

declare var dataLayer: any;
declare var gtag;

export class GoogleTagManagerDefaults implements GoogleTagManagerSettings {
  userId = null;
}

@Injectable()
export class CustomGoogleTagManager {

  constructor(
    protected angulartics2: Angulartics2,
    @Inject(APP_CONFIG) private config: AppConfig
  ) {
    // The dataLayer needs to be initialized
    if (typeof dataLayer !== 'undefined' && dataLayer) {
      dataLayer = (<any>window).dataLayer = (<any>window).dataLayer || [];
    }
    const defaults = new GoogleTagManagerDefaults;
    // Set the default settings for this module
    this.angulartics2.settings.gtm = { ...defaults, ...this.angulartics2.settings.gtm };

    this.angulartics2.pageTrack
      .pipe(this.angulartics2.filterDeveloperMode())
      .subscribe((x) => this.pageTrack(x.path));
    this.angulartics2.eventTrack
      .pipe(this.angulartics2.filterDeveloperMode())
      .subscribe((x) => this.eventTrack(x.action, x.properties));
    this.angulartics2.exceptionTrack
      .pipe(this.angulartics2.filterDeveloperMode())
      .subscribe((x: any) => this.exceptionTrack(x));
    this.angulartics2.setUsername
      .subscribe((x: string) => this.setUsername(x));
  }

  pageTrack(path: string) {
    if (typeof dataLayer !== 'undefined' && dataLayer) {
      gtag('config', this.config.googleAnalyticsUserId, {
        'page_title' : document.title,
        'page_path': path
      });
    }
  }

  /**
   * Send interactions to the dataLayer, i.e. for event tracking in Google Analytics
   *
   * @param action associated with the event
   * @param properties
   * @param {string} properties.category
   * @param {string} [properties.label]
   * @param {number} [properties.value]
   * @param {boolean} [properties.noninteraction]
   */
  eventTrack(action: string, properties: any) {

    // Set a default GTM category
    properties = properties || {};

    if (typeof dataLayer !== 'undefined' && dataLayer) {
      gtag('event', action, {
        'event_category': properties.category || 'Event',
        'event_label': properties.label || "Label",
        ...properties.gtmCustom
      });
    }
  }

  /**
   * Exception Track Event in GTM
   *
   * @param {Object} properties
   * @param {string} properties.appId
   * @param {string} properties.appName
   * @param {string} properties.appVersion
   * @param {string} [properties.description]
   * @param {boolean} [properties.fatal]
   */
  exceptionTrack(properties: any) {
    if (! properties || ! properties.appId || ! properties.appName || ! properties.appVersion) {
      console.error('Must be setted appId, appName and appVersion.');
      return;
    }

    if (properties.fatal === undefined) {
      console.log('No "fatal" provided, sending with fatal=true');
      properties.exFatal = true;
    }

    properties.exDescription = properties.event ? properties.event.stack : properties.description;

    this.eventTrack(`Exception thrown for ${properties.appName} <${properties.appId}@${properties.appVersion}>`, {
      'category': 'Exception',
      'label': properties.exDescription
    });
  }

  /**
   * Set userId for use with Universal Analytics User ID feature
   *
   * @param userId used to identify user cross-device in Google Analytics
   */
  setUsername(userId: string) {
    this.angulartics2.settings.gtm.userId = userId;
  }
}
