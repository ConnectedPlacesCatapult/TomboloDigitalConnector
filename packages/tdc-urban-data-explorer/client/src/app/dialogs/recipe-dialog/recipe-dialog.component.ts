/**
 * Recipe dialog
 */

import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {Component, Inject} from '@angular/core';

@Component({
  selector: 'recipe-dialog',
  templateUrl: './recipe-dialog.html',
  styleUrls: ['./recipe-dialog.scss']
})

export class RecipeDialog {

  codemirrorConfig = {
    mode: 'application/json',
    autoCloseBrackets: true,
    theme: 'dracula',
    readOnly: true
  };

  public recipe: string;

  constructor(public dialogRef: MatDialogRef<RecipeDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.recipe = data.recipe;
  }

  close(): void {
    this.dialogRef.close();
  }


}
