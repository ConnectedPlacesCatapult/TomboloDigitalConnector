import {
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  Input,
  OnChanges,
  OnInit,
  ViewEncapsulation
} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../../../mapbox/tombolo-mapbox-map';
import {IBasemap} from '../../../../../../src/shared/IBasemap';
import {Subscription} from 'rxjs/Subscription';
import {FormControl, FormGroup} from '@angular/forms';

const debug = Debug('tombolo:map-basemap-editor');

@Component({
  selector: 'map-basemap-editor',
  templateUrl: './map-basemap-editor.html',
  styleUrls: ['./map-basemap-editor.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class MapBasemapEditorComponent implements OnInit, OnChanges {

  @HostBinding('class.basemap-editor') basemapEditorClass = true;

  @Input() map: TomboloMapboxMap;
  @Input() basemaps: IBasemap[];

  form: FormGroup;
  _subs: Subscription[] = [];

  constructor() {
    this.form = new FormGroup({
      basemapId: new FormControl()
    });
  }

  ngOnInit() {
    // Save form changes to map as user types
    this._subs.push(this.form.get('basemapId').valueChanges.subscribe(val => {
      const basemap = this.basemaps.find(b => b.id === val);
      this.map.setBasemap(basemap);
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  ngOnChanges(changes) {
    // Transfer values to form
    if (changes.map && (this.map && this.basemaps)) {
      this.form.setValue({
        basemapId: this.map.basemapId
      });
    }
  }
}
