import {ChangeDetectionStrategy, Component, forwardRef, HostBinding, Input, ViewEncapsulation} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'color-well',
  templateUrl: './color-well.html',
  styleUrls: ['./color-well.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ColorWellComponent), multi: true }
  ]
})
export class ColorWellComponent implements ControlValueAccessor {

  @HostBinding('class.color-well') colorWellClass = true;

  @Input() value: string;

  propagateChange = (_: any) => {};

  constructor() {}

  writeValue(value: any) {
    if (value !== undefined) {
      this.value = value;
    }
  }

  registerOnChange(fn) {
    this.propagateChange = fn;
  }

  registerOnTouched() {}
}
