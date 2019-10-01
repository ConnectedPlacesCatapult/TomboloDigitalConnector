import {ChangeDetectionStrategy, Component, HostBinding, Input, OnChanges, ViewEncapsulation} from '@angular/core';
import {IPalette} from '../../../../../../src/shared/IPalette';

const DEFAULT_CHIP_SIZE = 10;

interface MapKeyChip {
  color: string;
  value: number | string;
  radius: number,
  tooltip: string
}

@Component({
  selector: 'map-key-item',
  templateUrl: './map-key-item.html',
  styleUrls: ['./map-key-item.scss'],
  encapsulation: ViewEncapsulation.Emulated,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MapKeyItemComponent implements OnChanges {

  @HostBinding('class.map-key-item') mapKeyItemClass = true;

  @Input() title: string;
  @Input() palette: IPalette; // With 5 color stops to apply to chips
  @Input() paletteInverted: boolean = false;
  @Input() ndColor: string;
  @Input() values: string[]; // Array of 5 values to annotate each chip
  @Input() mode: 'square' | 'circle';
  @Input() radii: number[]; // Array of 5 pixels radii for each chip. Can be null to specify fixed max radius

  chips: MapKeyChip[];

  constructor() {}

  ngOnChanges(changes) {

    // Init chips
    let chips: MapKeyChip[] = [];

    // NoData chip
    chips.push({
      color: this.ndColor,
      value: 'ND',
      radius: ((this.radii)? this.radii[0] : DEFAULT_CHIP_SIZE),
      tooltip: 'No Data'
    });

    for(let i = 0; i < 5; i++) {
      const paletteIndex = (this.paletteInverted)?  4 - i : i;
      chips.push({
        color: (this.palette)? this.palette.colorStops[paletteIndex] : this.ndColor,
        value: this.values[i],
        radius: ((this.radii)? this.radii[i + 1] : DEFAULT_CHIP_SIZE),
        tooltip: null
      })
    }

    this.chips = chips;
  }

}
