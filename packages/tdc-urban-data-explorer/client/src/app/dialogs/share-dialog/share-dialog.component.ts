/**
 * Share dialog
 */

import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import { ShareButtons } from '@ngx-share/core';
import {APP_CONFIG, AppConfig} from '../../config.service';

@Component({
  selector: 'share-dialog',
  templateUrl: './share-dialog.html',
  styleUrls: ['./share-dialog.scss']
})
export class ShareDialog implements AfterViewInit {

  public title: string;
  public url: string;
  public description: string;
  public socialMediaTitle: string;
  public tags: string;

  @ViewChild('urlInput') urlInput;

  constructor(public dialogRef: MatDialogRef<ShareDialog>,
              public share: ShareButtons,
              @Inject(MAT_DIALOG_DATA) public data: any,
              @Inject(APP_CONFIG) private config: AppConfig) {

    this.title = data.title;
    this.url = data.url;
    this.socialMediaTitle = config.socialMediaTitle;
    this.description = config.socialMediaDescription;
    this.tags = config.socialMediaTags;
  }

  ngAfterViewInit() {
    this.urlInput.nativeElement.focus();
    this.urlInput.nativeElement.select();
  }

  copy() {
    document.execCommand('copy');
    this.dialogRef.close(true);
  }

  email() {
    window.location.href = `mailto:?subject=${this.socialMediaTitle}&body=${this.description}%0D%0A%0D%0A${this.url}`;
    return false;
  }
}
