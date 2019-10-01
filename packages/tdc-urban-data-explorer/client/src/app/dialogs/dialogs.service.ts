/**
 * Dialog Service - app-wide access to general-purpose dialogs (e.g. confirmation, info)
 */

import { Observable } from 'rxjs/Rx';
import { ConfirmDialog } from './confirm-dialog.component';
import { MatDialogRef, MatDialog, MatDialogConfig } from '@angular/material';
import { Injectable } from '@angular/core';
import {InformationDialog} from "./info-dialog.component";
import {ShareDialog} from './share-dialog/share-dialog.component';
import {RecipeDialog} from './recipe-dialog/recipe-dialog.component';

@Injectable()
export class DialogsService {

  constructor(private dialog: MatDialog) { }

  public confirm(title: string, message: string, okButtonText = 'OK'): Observable<boolean> {

    let dialogRef = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data: {title, message, okButtonText}
    });

    return dialogRef.afterClosed();
  }

  public information(title: string, message: string, okButtonText = 'OK'): Observable<boolean> {

    let dialogRef = this.dialog.open(InformationDialog, {
      width: '400px',
      data: {title, message, okButtonText}
    });

    return dialogRef.afterClosed();
  }

  public share(title: string, url: string): Observable<boolean> {

    let dialogRef = this.dialog.open(ShareDialog, {
      width: '400px',
      data: {title, url}
    });

    return dialogRef.afterClosed();
  }

  public recipe(recipeText: string): Observable<boolean> {

    let dialogRef = this.dialog.open(RecipeDialog, {
      width: '600px',
      data: {
        recipe: recipeText
      }});

    return dialogRef.afterClosed();
  }
}
