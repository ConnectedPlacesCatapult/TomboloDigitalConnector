import {Component, HostBinding, Inject, OnInit} from '@angular/core';
import * as Debug from 'debug';

import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Angulartics2} from 'angulartics2';
import {FocusTrapFactory} from '@angular/cdk/a11y';
import {APP_CONFIG, AppConfig} from '../../config.service';
import {DialogsService} from '../../dialogs/dialogs.service';

const debug = Debug('tombolo:login-dialog');

@Component({
  selector: 'login-component',
  templateUrl: './login.html',
  styleUrls: ['../auth-panel.scss']
})
export class LoginDialogComponent implements OnInit {

  @HostBinding('class.auth-panel-component') authPanelComponentClass = true;

  constructor(
    private router: Router,
    private authService: AuthService,
    private dialogsService: DialogsService,
    private angulartics2: Angulartics2,
    @Inject(APP_CONFIG) public config: AppConfig) {}

  loginForm: FormGroup;
  errorMessage: string;

  private _subs: Subscription[] = [];

  ngOnInit() {
    this.loginForm = new FormGroup({
      email: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required)
    });

    this._subs.push(this.loginForm.valueChanges.subscribe(() => this.errorMessage = null));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  signup() {
    if (!this.config.createAccountEnabled) {
      this.dialogsService.information('Sign Up Disabled', 'Signing up for a new account is currently disbaled.').
        subscribe();
    }
    else {
      this.router.navigate([{outlets: {'loginBox': 'signup'}}]);
    }
  }

  close() {
    this.router.navigate([{outlets: {loginBox: null}}]);
  }

  login() {
    this.authService.login(this.loginForm.get('email').value, this.loginForm.get('password').value)
      .then(user => {
        this.angulartics2.eventTrack.next({
          action: 'Login',
          properties: {
            category: 'Account',
            label: this.loginForm.get('email').value
          }
        });
        this.router.navigate([{outlets: {'loginBox': null}}]);
      })
      .catch(e => {
        this.errorMessage = e.message;
        this.angulartics2.eventTrack.next({
          action: 'LoginFail',
          properties: {
            category: 'Account',
            label: this.loginForm.get('email').value
          }
        });
      });
  }
}
