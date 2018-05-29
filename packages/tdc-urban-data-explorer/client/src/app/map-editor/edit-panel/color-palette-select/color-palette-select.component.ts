import {ChangeDetectionStrategy, Component, forwardRef, HostBinding, Input, ViewEncapsulation} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {IPalette} from '../../../../../../src/shared/IPalette';


@Component({
  selector: 'color-palette-select',
  templateUrl: './color-palette-select.html',
  styleUrls: ['./color-palette-select.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ColorPaletteSelectComponent), multi: true }
  ]
})
export class ColorPaletteSelectComponent implements ControlValueAccessor {

  @HostBinding('class.color-palette-select') colorPaletteSelectClass = true;

  @Input() palettes: IPalette[];
  @Input() value: string; // palette id

  propagateChange = (_: any) => {};

  constructor() {}

  paletteForId(id: string) : IPalette {
    return this.palettes.find(p => p.id === id);
  }

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
