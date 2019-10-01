import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostBinding,
  Input,
  OnChanges,
  OnInit,
  Output,
  ViewEncapsulation
} from '@angular/core';
import * as Debug from 'debug';
import {TomboloMapboxMap} from '../../mapbox/tombolo-mapbox-map';
import {FormControl, FormGroup} from '@angular/forms';
import {Subscription} from 'rxjs/Subscription';
import {ITomboloDatasetAttribute} from '../../../../../src/shared/ITomboloDatasetAttribute';
import {IMapFilter} from '../../../../../src/shared/IMapFilter';

const debug = Debug('tombolo:filter-editor');

@Component({
  selector: 'filter-editor',
  templateUrl: './filter-editor.html',
  styleUrls: ['./filter-editor.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class FilterEditorComponent implements OnInit, OnChanges {

  @HostBinding('class.filter-editor') layerEditorClass = true;

  @Input() map: TomboloMapboxMap;
  @Input() filter: IMapFilter;

  @Output() filterChange = new EventEmitter<IMapFilter>();

  form: FormGroup;
  mode: 'number' | 'string' = 'number';
  selectedAttribute: ITomboloDatasetAttribute;

  _subs: Subscription[] = [];

  constructor() {
    this.form = new FormGroup({
      dataLayerId: new FormControl(),
      dataAttribute: new FormControl(),
      operator: new FormControl(),
      value: new FormControl()
    });
  }

  ngOnInit() {
    // Save form changes to map as user changes controls
    this._subs.push(this.form.get('dataLayerId').valueChanges.subscribe(val => {
      this.filter.datalayerId = val;
      this.emitFilterChanged();
    }));

    this._subs.push(this.form.get('dataAttribute').valueChanges.subscribe(val => {

      this.filter.attribute = val;

      // Cache selected data attribute
      const dataLayerId = this.form.get('dataLayerId').value;
      if (dataLayerId && val) {
        this.selectedAttribute = this.map.getDataAttributeForLayer(this.form.get('dataLayerId').value, val);

        // Ensure slide value is within range of new attribute
        if (this.selectedAttribute.type === 'number') {
          const currentValue = this.form.get('value').value;
          if (currentValue > this.selectedAttribute.maxValue) {
            this.form.patchValue({value: this.selectedAttribute.maxValue});
          }
          else if (currentValue < this.selectedAttribute.minValue) {
            this.form.patchValue({value: this.selectedAttribute.minValue});
          }
        }
      }
      else {
        this.selectedAttribute = null;
      }

      this.mode = this.selectedAttribute ? (this.selectedAttribute.type as 'number' | 'string') : 'number';
      this.emitFilterChanged();
    }));

    this._subs.push(this.form.get('operator').valueChanges.subscribe(val => {
      this.filter.operator = val;
      this.emitFilterChanged();
    }));

    this._subs.push(this.form.get('value').valueChanges.subscribe(val => {
      this.filter.value = val;
      this.emitFilterChanged();
    }));
  }

  ngOnDestroy() {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  ngOnChanges(changes) {

    // Transfer values to form
    if ((changes.map || changes.filter) && this.map) {

      const filter = this.filter;

      if (filter.datalayerId && filter.attribute) {
        this.selectedAttribute = this.map.getDataAttributeForLayer(filter.datalayerId, filter.attribute);
      }
      else {
        this.selectedAttribute = null;
      }

      this.form.setValue({
        dataLayerId: filter.datalayerId,
        dataAttribute: filter.attribute,
        operator: filter.operator,
        value: filter.value
      });
    }
  }

  attributesForSelectedLayer(): ITomboloDatasetAttribute[] {
    const seletedLayerId = this.form.get('dataLayerId').value;
    if (!seletedLayerId) return [];

    return this.map.getDataAttributesForLayer(seletedLayerId);
  }

  attributeSliderStep(): number {
    if (this.selectedAttribute) {
      let stepSize = (this.selectedAttribute.maxValue - this.selectedAttribute.minValue) / 100;
      // Round to a power of ten
      stepSize = Math.pow(10, Math.round(Math.log10(stepSize)));
      return stepSize;
    }
    else {
      return 1;
    }
  }

  private emitFilterChanged() {
    this.filterChange.emit(this.filter);
  }

}
