/**
 * Notification service - App-wide snack bar notifications
 */

import {Injectable} from "@angular/core";
import {MatSnackBar, MatSnackBarRef} from '@angular/material';
import {DialogsService} from "./dialogs.service";

// Default snack bar duration before auto-dismiss
const NOTIFICATION_DURATION = 2000;
const INFO_CLASS = 'info';
const ERROR_CLASS = 'error';

@Injectable()
export class NotificationService {

  constructor(private snackBar: MatSnackBar, private dialogsService: DialogsService) {}

  show(message: string): MatSnackBarRef<any> {
    return this.snackBar.open(message);
  }

  info(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      extraClasses: [INFO_CLASS],
      duration: NOTIFICATION_DURATION
    })
  }

  error(e: Error): void {
    this.snackBar.open(e.message, 'Details', {
      extraClasses: [ERROR_CLASS],
      duration: NOTIFICATION_DURATION * 2
    }).onAction().subscribe(() => {
        this.dialogsService.information(e.message, JSON.stringify(e));
    });
  }
}
