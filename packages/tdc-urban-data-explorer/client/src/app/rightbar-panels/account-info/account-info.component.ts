import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';

const debug = Debug('tombolo:account-info');

@Component({
  selector: 'account-info',
  templateUrl: './account-info.html',
  styleUrls: ['./account-info.scss']
})
export class AccountInfoComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  constructor() {}

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
