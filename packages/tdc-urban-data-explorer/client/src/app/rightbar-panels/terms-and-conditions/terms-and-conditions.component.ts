import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';

const debug = Debug('tombolo:terms-and-conditions');

@Component({
  selector: 'terms-and-conditions',
  templateUrl: './terms-and-conditions.html',
  styleUrls: ['./terms-and-conditions.scss']
})
export class TermsAndConditionsComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  constructor() {}

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
