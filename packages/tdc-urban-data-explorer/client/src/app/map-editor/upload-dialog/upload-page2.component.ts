import {Component, EventEmitter, HostBinding, Input, OnDestroy, OnInit, Output} from '@angular/core';
import * as Debug from 'debug';
import {MapService} from '../../services/map-service/map.service';
import {UploadDialogContext} from './upload-dialog.component';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Subscription} from 'rxjs/Subscription';

const debug = Debug('tombolo:upload-page2');

@Component({
  selector: 'upload-page2',
  templateUrl: './upload-page2.html',
  styleUrls: ['./upload-dialog.scss']
})
export class UploadPage2Component implements OnInit, OnDestroy {

  @Input() context: UploadDialogContext;
  @HostBinding('class.wizard-page-component') wizardPageClass = true;

  form: FormGroup;

  private _subs: Subscription[] = [];

  constructor(private mapService: MapService) {}

  ngOnInit() {
    this.form = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl(''),
      attribution: new FormControl('')
    });

    this._subs.push(this.form.statusChanges.subscribe(() => {
      this.context.setNextEnabled(1, this.form.valid)
    }));

    this._subs.push(this.context.next$.subscribe(pageIndex => {
      if (pageIndex === 1) {
        this.context.file.name = this.form.get('name').value;
        this.context.file.description  = this.form.get('description').value;
        this.context.file.attribution  = this.form.get('attribution').value;
      }
    }));
  }

  ngOnDestroy() {
    debug('Destroying page 2');
    this._subs.forEach(sub => sub.unsubscribe());
  }
}
