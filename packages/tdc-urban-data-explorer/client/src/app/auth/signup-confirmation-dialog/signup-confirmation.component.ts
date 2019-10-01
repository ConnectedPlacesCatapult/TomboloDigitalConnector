import {Component, HostBinding, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';

import * as Debug from 'debug';

const debug = Debug('tombolo:signup-confirmation');

@Component({
  selector: 'signup-confirmation',
  templateUrl: './signup-confirmation.html',
  styleUrls: ['../auth-panel.scss']
})
export class SignupConfirmationComponent implements OnInit {

  @HostBinding('class.auth-panel-component') authPanelComponentClass = true;

  constructor(private router: Router) {}

  private _subs: Subscription[] = [];

  ngOnInit() {

  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  login() {
    this.router.navigate([{outlets: {'loginBox': 'login'}}]);
  }

  close() {
    this.router.navigate([{outlets: {'loginBox': null}}]);
  }
}
