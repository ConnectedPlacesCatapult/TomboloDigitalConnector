import {Component, HostBinding, Inject, OnInit} from '@angular/core';
import * as Debug from 'debug';
import * as moment from 'moment';
import {APP_CONFIG, AppConfig} from '../../config.service';

const debug = Debug('tombolo:app-info');

@Component({
  selector: 'app-info',
  templateUrl: './app-info.html',
  styleUrls: ['./app-info.scss']
})
export class AppInfoComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  constructor(@Inject(APP_CONFIG) private config: AppConfig) {}

  ngOnInit() {
  }

  ngOnDestroy() {
  }

  get version(): string {
    return `Version: ${this.config.version.tag || 'CI'}.${this.config.version.hash}`;
  }

  get buildTime(): string {
    const buildTime = moment.unix(this.config.version.timestamp).format();
    return `Built at ${buildTime}`;
  }
}
