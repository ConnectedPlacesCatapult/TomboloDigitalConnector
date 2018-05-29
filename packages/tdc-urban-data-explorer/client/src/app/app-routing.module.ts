/**
 * Top-level routing module
 */

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MapViewerComponent} from './map-viewer/map-viewer.component';
import {MapInfoComponent} from './rightbar-panels/map-info/map-info.component';
import {MapExportComponent} from './map-export/map-export.component';
import {MapEditorComponent} from './map-editor/map-editor.component';
import {LoginDialogComponent} from './auth/login-dialog/login-dialog.component';
import {SignupDialogComponent} from './auth/signup-dialog/signup-dialog.component';
import {SignupConfirmationComponent} from './auth/signup-confirmation-dialog/signup-confirmation.component';
import {ChangePasswordDialogComponent} from './auth/change-password-dialog/change-password-dialog.component';
import {ResetPasswordDialogComponent} from './auth/reset-password-dialog/reset-password-dialog.component';
import {AccountInfoComponent} from './rightbar-panels/account-info/account-info.component';
import {TermsAndConditionsComponent} from './rightbar-panels/terms-and-conditions/terms-and-conditions.component';
import {AppInfoComponent} from './rightbar-panels/app-info/app-info.component';
import {MyAccountDialogComponent} from './auth/my-account-dialog/my-account.component';
import {EditPanelComponent} from './map-editor/edit-panel/edit-panel.component';
import {EditInfoComponent} from './rightbar-panels/edit-intro/edit-info.component';
import {AccesstoRightBarComponent} from './rightbar-panels/accessto/accessto-rightbar.component';
import {EditorDeactivateGuard} from "./map-editor/unsaved-changes-guard";
import {PrivacyPolicyComponent} from './rightbar-panels/privacy-policy/privacy-policy.component';

const routes: Routes = [
  {
    path: 'view',
    component: MapViewerComponent
  },
  {
    path: 'view/:mapID',
    component: MapViewerComponent
  },
  {
    path: 'edit/:mapID',
    component: MapEditorComponent,
    canDeactivate: [EditorDeactivateGuard]
  },
  {
    path: 'edit',
    component: MapEditorComponent
  },
  {
    path: 'mapinfo',
    component: MapInfoComponent,
    outlet: 'rightBar'
  },
  {
    path: 'mapexport',
    component: MapExportComponent,
    outlet: 'rightBar'
  },
  {
    path: 'editpanel',
    component: EditPanelComponent,
    outlet: 'rightBar'
  },
  {
    path: 'appinfo',
    component: AppInfoComponent,
    outlet: 'rightBar'
  },
  {
    path: 'editinfo',
    component: EditInfoComponent,
    outlet: 'rightBar'
  },
  {
    path: 'accountinfo',
    component: AccountInfoComponent,
    outlet: 'rightBar'
  },
  {
    path: 'termsandconditions',
    component: TermsAndConditionsComponent,
    outlet: 'rightBar'
  },
  {
    path: 'privacypolicy',
    component: PrivacyPolicyComponent,
    outlet: 'rightBar'
  },
  {
    path: 'accessto',
    component: AccesstoRightBarComponent,
    outlet: 'rightBar'
  },
  {
    path: 'login',
    component: LoginDialogComponent,
    outlet: 'loginBox'
  },
  {
    path: 'signup',
    component: SignupDialogComponent,
    outlet: 'loginBox'
  },
  {
    path: 'resetpassword',
    component: ResetPasswordDialogComponent,
    outlet: 'loginBox'
  },
  {
    path: 'changepassword',
    component: ChangePasswordDialogComponent,
    outlet: 'loginBox'
  },
  {
    path: 'signupconfirm',
    component: SignupConfirmationComponent,
    outlet: 'loginBox'
  },
  {
    path: 'myaccount',
    component: MyAccountDialogComponent,
    outlet: 'loginBox'
  },
  { path: '',
    redirectTo: '/view(rightBar:appinfo)',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
