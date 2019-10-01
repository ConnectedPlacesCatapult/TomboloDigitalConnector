import {Component, Inject, OnInit} from '@angular/core';
import * as Debug from 'debug';

import {ActivatedRoute} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {AuthService} from '../auth.service';
import {User} from '../user';
import {APP_CONFIG, AppConfig} from '../../config.service';

const debug = Debug('tombolo:maps-demo');

@Component({
  selector: 'login-controls',
  templateUrl: './login-controls.html',
  styleUrls: ['./login-controls.scss']
})
export class LoginControlsComponent implements OnInit {

  constructor(
    private activatedRoute: ActivatedRoute,
    private authService: AuthService,
    @Inject(APP_CONFIG) public config: AppConfig) {}

  private _subs: Subscription[] = [];

  user: User = null;

  ngOnInit() {

    this._subs.push(this.authService.user$.subscribe(user => {
      this.user = user;
    }));

    this.authService.loadUser();
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  logout() {
    this.authService.logOut();
  }
}
