import {Component, HostBinding, Inject, OnInit} from '@angular/core';
import * as Debug from 'debug';

import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs/Subscription';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Angulartics2} from 'angulartics2';
import {APP_CONFIG, AppConfig} from '../../config.service';

const debug = Debug('tombolo:signup-dialog');

@Component({
  selector: 'signup-component',
  templateUrl: './signup.html',
  styleUrls: ['../auth-panel.scss']
})
export class SignupDialogComponent implements OnInit {

  @HostBinding('class.auth-panel-component') authPanelComponentClass = true;

  constructor(
    private router: Router,
    private authService: AuthService,
    private analytics: Angulartics2,
    @Inject(APP_CONFIG) public config: AppConfig) {}

  signupForm: FormGroup;
  errorMessage: string;
  showProgress = false;

  private _subs: Subscription[] = [];

  ngOnInit() {
    this.signupForm = new FormGroup({
      name: new FormControl('', Validators.required),
      acceptToC: new FormControl(false, Validators.requiredTrue),
      email: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required),
      confirmPassword: new FormControl('', Validators.required),
      newsletters: new FormControl(true, Validators.required)
    });

    this._subs.push(this.signupForm.valueChanges.subscribe(() => this.errorMessage = null));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  login() {
    this.router.navigate(['/', {outlets: {loginBox: 'login'}}]);
  }

  close() {
    this.router.navigate([{outlets: {loginBox: null}}]);
  }

  signup() {

    // Check password matches confirmation
    if (this.signupForm.get('password').value !== this.signupForm.get('confirmPassword').value) {
      this.errorMessage = 'Passwords do not match';

      this.analytics.eventTrack.next({
        action: 'SignUpFail',
        properties: {
          category: 'Account',
          label: this.errorMessage
        }
      });

      return;
    }

    this.showProgress = true;

    this.authService.signup(this.signupForm.value)
      .then(user => {
        this.showProgress = false;
        this.router.navigate([{outlets: {'loginBox': 'signupconfirm'}}]);
        this.analytics.eventTrack.next({
          action: 'SignUp',
          properties: {
            category: 'Account',
            label: this.signupForm.get('email').value
          },
        });
      })
      .catch((e) => {
        this.showProgress = false;
        this.errorMessage = e.message;
        this.analytics.eventTrack.next({
          action: 'SignUpFail',
          properties: {
            category: 'Account',
            label: e.message
          },
        });
      });
  }
}
