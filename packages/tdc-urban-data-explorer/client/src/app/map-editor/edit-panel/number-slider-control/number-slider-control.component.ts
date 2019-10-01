import {
  ChangeDetectionStrategy, ChangeDetectorRef, Component, forwardRef, HostBinding, Input,
  ViewEncapsulation
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'number-slider-control',
  templateUrl: './number-slider-control.html',
  styleUrls: ['./number-slider-control.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => NumberSliderControlComponent), multi: true }
  ]
})
export class NumberSliderControlComponent implements ControlValueAccessor {

  @HostBinding('class.number-slider-component') numberSliderComponentClass = true;

  @Input() min: number = 0;
  @Input() max: number = 100;
  @Input() step: number = 1;
  @Input() unit: string;
  @Input() value: number = 0;
  @Input('tick-interval') tickInterval: number | 'auto';

  propagateChange = (_: any) => {};

  constructor(private cd: ChangeDetectorRef) {}

  setValueFromSlider(val) {
    this.value = Number.parseFloat(val.toPrecision(3));
    this.propagateChange(val);
  }

  writeValue(value: any) {
    if (value !== undefined) {
      this.value = value;
      this.cd.markForCheck();
    }
  }

  registerOnChange(fn) {
    this.propagateChange = fn;
  }

  registerOnTouched() {}
}
