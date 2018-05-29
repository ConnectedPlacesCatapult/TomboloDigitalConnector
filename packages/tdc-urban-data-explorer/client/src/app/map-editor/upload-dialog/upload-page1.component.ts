import {Component, HostBinding, Inject, Input, OnDestroy, OnInit} from '@angular/core';
import {UploadOutput} from 'ngx-uploader';
import {Subscription} from 'rxjs/Subscription';
import * as Debug from 'debug';
import {MapService} from '../../services/map-service/map.service';
import {SubStep, UploadDialogContext} from './upload-dialog.component';
import {IFileUpload} from '../../../../../src/shared/IFileUpload';
import {Angulartics2} from 'angulartics2';
import {APP_CONFIG, AppConfig} from '../../config.service';

const debug = Debug('tombolo:upload-page1');

const INGEST_POLL_INTERVAL = 2000;

@Component({
  selector: 'upload-page1',
  templateUrl: './upload-page1.html',
  styleUrls: ['./upload-dialog.scss']
})
export class UploadPage1Component implements OnInit, OnDestroy {

  @Input() context: UploadDialogContext;
  @HostBinding('class.wizard-page-component') wizardPageClass = true;

  private _subs: Subscription[] = [];

  currentStepIndex = -1;
  steps: SubStep[] = [
    {
      text: 'Uploading dataset',
      status: 'pending'
    },
    {
      text: 'Validating data format',
      status: 'pending'
    },
    {
      text: 'Processing dataset',
      status: 'pending'
    },
    {
      text: 'Upload done!',
      status: 'pending'
    }
  ];

  progressMode = 'determinate';
  progressValue = 0;
  uploadID: string;
  errorMessage: string = null;
  successMessage: string = null;

  private ingestPollTimer: any;

  constructor(private mapService: MapService,
              private angulartics2: Angulartics2,
              @Inject(APP_CONFIG) private config: AppConfig) {}

  ngOnInit() {
    this._subs.push(this.context.cancel$.subscribe(pageIndex => this.cancel(pageIndex)));
    this._subs.push(this.context.uploadOutput$.subscribe(event => this.handleUploadEvent(event)));
    this.setStep(0);
  }

  ngOnDestroy() {
    debug('Destroying page 1');
    clearTimeout(this.ingestPollTimer);
    this._subs.forEach(sub => sub.unsubscribe());
  }

  iconForStep(step: SubStep) {
    switch (step.status) {
      case 'pending':
        return 'tick-inactive';
      case 'inprogress':
        return 'tick-inactive';
      case 'done':
        return 'tick-active';
      case 'error':
        return 'info';
    }
  }

  private setStep(stepIndex: number) {

    for (let i = 0; i < stepIndex; i++) {
      this.steps[i].status = 'done';
    }

    debug(`Running step: ${stepIndex}`);

    this.currentStepIndex = stepIndex;

    this.steps[stepIndex].status = 'inprogress';

    switch (stepIndex) {
      // Uploading
      case 0:
        this.progressMode = 'determinate';
        this.progressValue = 0;
        break;

      case 1:
        // Validating
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.startIngestPolling();
        break;

      case 2:
        // Processing
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.startIngestPolling();
        break;

      case 3:
        // Done
        this.progressMode = 'determinate';
        this.progressValue = 0;
        break;
    }
  }

  private handleUploadEvent(event: UploadOutput): void {

    console.log(event);

    if (event.type === 'start') {
      debug('starting upload');
      this.progressValue = event.file.progress.data.percentage;
    }
    else if (event.type === 'uploading') {
      this.progressValue = event.file.progress.data.percentage;
    }
    else if (event.type === 'done') {

      const size = this.config.maxUploadSize / 1024 / 1024;
      const fileTooLargeMessage = `The file is too large. You can upload a maximum of ${size}MB.`;

      if (event.file.responseStatus === 200) {
        // Success

        this.uploadID = event.file.response['id'];
        this.setStep(1);
      }
      else if (event.file.responseStatus === 500) {
        // Server error
        let err = event.file.response;

        debug('File upload error', err);

        // Improve the 'File too large' error message with size limits
        if (err.error.code === 'LIMIT_FILE_SIZE') {
          err.message = fileTooLargeMessage;
        }
        this.finish(err);
      }
      else if (event.file.responseStatus === 401) {
        // Server error
        let err = event.file.response;

        debug('File upload error', err);
        err.message = 'You have exceeded your quota for uploading data.';

        this.finish(err);
      }
      else if (event.file.responseStatus === 0) {
        // Connection dropped - NGINX probably blocked a large file
        debug('File upload error', event.file.response);
        this.finish({message: fileTooLargeMessage});
      }
    }
    else if (event.type === 'rejected') {
      debug('rejected');
      this.finish(event.file.response);
    }
  }

  private startIngestPolling(): void {
    this.ingestPollTimer = setTimeout(() => this.pollIngest(), INGEST_POLL_INTERVAL);
  }

  private pollIngest(): void {
    this.mapService.pollIngest(this.uploadID).subscribe(fileUpload => {

      debug('file upload:', fileUpload);

      if (fileUpload.status === 'validating') {
        this.setStep(1);
      }
      else if (fileUpload.status === 'ingesting') {
        this.setStep(2);
      }
      else if (fileUpload.status === 'done') {
        // Finished this page
        this.finish(null, fileUpload);
      }
      else {
        // error
        this.finish({message: fileUpload.error});
      }
    });
  }

  private finish(error: any, fileUpload?: IFileUpload) {

    if (error) {
      this.steps[this.currentStepIndex].status = 'error';
      this.errorMessage = error.message || error.toString();

      this.angulartics2.eventTrack.next({
        action: 'UploadDatasetFail',
        properties: { category: 'UploadDataset', label: this.errorMessage }
      });
    }
    else {

      this.steps.forEach(step => step.status = 'done');
      this.successMessage = `<p>Your dataset has been uploaded successfully. ${fileUpload.ogrInfo.featureCount} features were found.</p>`;
      this.context.file = fileUpload;

      this.angulartics2.eventTrack.next({
        action: 'UploadDatasetSuccess',
        properties: { category: 'UploadDataset', label: fileUpload.size}
      });

      this.context.setNextEnabled(0);
    }
  }

  private cancel(pageIndex: number) {
    if (pageIndex === 0) {
      debug('Cancelling Page 1');
      this.context.uploadInput$.next({type: 'cancel'});
    }
  }
}
