import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';

const debug = Debug('tombolo:privacy-policy');

@Component({
  selector: 'privacy-policy',
  templateUrl: './privacy-policy.html',
  styleUrls: ['./privacy-policy.scss']
})
export class PrivacyPolicyComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  constructor() {}

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
