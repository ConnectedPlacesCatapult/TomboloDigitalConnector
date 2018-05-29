import {Component, EventEmitter, HostBinding, Input, OnDestroy, OnInit, Output} from '@angular/core';
import * as Debug from 'debug';
import {MapService} from '../../services/map-service/map.service';
import {SubStep, UploadDialogContext} from './upload-dialog.component';
import {Subscription} from 'rxjs/Subscription';

const debug = Debug('tombolo:upload-page2');

@Component({
  selector: 'upload-page4',
  templateUrl: './upload-page4.html',
  styleUrls: ['./upload-dialog.scss']
})
export class UploadPage4Component implements OnInit, OnDestroy {

  @Input() context: UploadDialogContext;
  @HostBinding('class.wizard-page-component') wizardPageClass = true;

  private _subs: Subscription[] = [];

  constructor(private mapService: MapService) {}

  currentStepIndex = -1;
  steps: SubStep[] = [
    {
      text: 'Configuring dataset',
      status: 'pending'
    },
    {
      text: 'Analysing dataset',
      status: 'pending'
    }
  ];

  openInMap = true;
  progressMode = 'indeterminate';
  progressValue = 0;
  errorMessage: string = null;
  successMessage: string = null;

  ngOnInit() {
    this._subs.push(this.context.enter$.subscribe(page => {
      if (page === 3) {
        this.setStep(0);
      }
    }));

    this._subs.push(this.context.next$.subscribe(page => {
      if (page === 3) {
        this.context.openInMap = this.openInMap;
      }
    }));
  }

  ngOnDestroy() {
    debug('Destroying page 3');
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
      // Configuring
      case 0:
        this.progressMode = 'indeterminate';
        this.finalizeUpload();
        break;

      // Analyzing
      case 1:
        this.progressMode = 'indeterminate';
        this.createDataset();
        break;
    }
  }

  private finish(error: any = null) {

    if (error) {
      this.steps[this.currentStepIndex].status = 'error';
      this.errorMessage = error.toString();
    }
    else {
      this.steps.forEach(step => step.status = 'done');
      this.successMessage = `<p>Congratulations! Your dataset is now ready for use.</p> `;
      this.context.setNextEnabled(3);
    }
  }

  private finalizeUpload() {

    debug('Finalizeing upload', this.context.file.dbAttributes);

    this.context.file.dbAttributes.forEach(attr => {
      // Default name to field if user hasn't entered a name
      if (!attr.name) attr.name = attr.field;
    });

    this.mapService.finalizeIngest(this.context.file).subscribe(fileUpload => {
      this.setStep(1);
    });
  }

  private createDataset() {
    this.mapService.createDataset(this.context.file.id).subscribe(dataset => {
      this.context.dataset = dataset;
      debug(dataset);
      this.finish();
    });
  }


}
