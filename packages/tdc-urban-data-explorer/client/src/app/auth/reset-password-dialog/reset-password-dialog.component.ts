import {Component, HostBinding, OnInit} from '@angular/core';
import * as Debug from 'debug';

import {Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Angulartics2} from 'angulartics2';

const debug = Debug('tombolo:password-reset-dialog');

@Component({
  selector: 'reset-password-component',
  templateUrl: './reset-password.html',
  styleUrls: ['../auth-panel.scss']
})
export class ResetPasswordDialogComponent implements OnInit {

  @HostBinding('class.auth-panel-component') authPanelComponentClass = true;

  constructor(
    private router: Router,
    private authService: AuthService,
    private analytics: Angulartics2) {}

  resetPasswordForm: FormGroup;
  passwordReset = false;
  errorMessage: string;
  showProgress = false;

  private _subs: Subscription[] = [];

  ngOnInit() {
    this.resetPasswordForm = new FormGroup({
      email: new FormControl('', Validators.required)
    });

    this._subs.push(this.resetPasswordForm.valueChanges.subscribe(() => {
      this.passwordReset = false;
      this.errorMessage = null;
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  close() {
    this.router.navigate([{outlets: {'loginBox': null}}]);
  }

  resetPassword() {

    this.passwordReset = false;

    const email = this.resetPasswordForm.get('email').value;
    this.showProgress = true;
    this.authService.resetPassword(email)
      .then(user => {
        this.passwordReset = true;
        this.showProgress = false;
        this.analytics.eventTrack.next({
          action: 'ResetPassword',
          properties: {
            category: 'Account',
            label: this.resetPasswordForm.get('email').value
          },
        });
      })
      .catch(e => {
        this.errorMessage = 'Invalid email or password';
        this.showProgress = false;
        this.analytics.eventTrack.next({
          action: 'ResetPasswordFail',
          properties: {
            category: 'Account',
            label: e.message
          },
        });
      });
  }
}
