/**
 * General purpose confirmation dialog
 */

import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {Component, Inject} from '@angular/core';

@Component({
  selector: 'confirm-dialog',
  template: `
    <h1 mat-dialog-title>{{ data.title }}</h1>
    <div mat-dialog-content>
        <p [innerHTML]="data.message"></p>
    </div>
    <div mat-dialog-actions fxLayoutAlign="end">
      <button type="button" cdk-focus-start mat-raised-button (click)="dialogRef.close(false)">Cancel</button>
        <button type="button" mat-raised-button color="accent" (click)="dialogRef.close(true)">{{data.okButtonText}}</button>
    </div>
    `,
})
export class ConfirmDialog {

  constructor(
    public dialogRef: MatDialogRef<ConfirmDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any) {

  }
}
