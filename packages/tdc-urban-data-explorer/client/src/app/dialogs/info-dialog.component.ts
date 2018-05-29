/**
 * General purpose information dialog
 */

import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {Component, Inject} from '@angular/core';

@Component({
  selector: 'information-dialog',
  template: `
    <h1 mat-dialog-title>{{ data.title }}</h1>
    <div mat-dialog-content>
        <p [innerHTML]="data.message"></p>
    </div>
    <div mat-dialog-actions fxLayoutAlign="end">
        <button type="button" cdk-focus-start mat-raised-button color="accent" (click)="dialogRef.close(true)">{{data.okButtonText}}</button>
    </div>
    `,
})
export class InformationDialog {

  constructor(
    public dialogRef: MatDialogRef<InformationDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any) {}
}
