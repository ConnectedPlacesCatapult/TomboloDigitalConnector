import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';

const debug = Debug('tombolo:edit-info');

@Component({
  selector: 'edit-info',
  templateUrl: './edit-info.html',
  styleUrls: ['./edit-info.scss']
})
export class EditInfoComponent implements OnInit {

  @HostBinding('class.sidebar-component') sidebarComponentClass = true;

  constructor() {}

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
