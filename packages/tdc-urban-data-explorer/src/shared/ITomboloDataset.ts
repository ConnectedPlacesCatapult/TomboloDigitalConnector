
import {ITomboloDatasetAttribute} from './ITomboloDatasetAttribute';

export interface ITomboloDataset {
  id: string;
  name: string;
  description: string;
  geometryType: string;
  dataAttributes: any[];
  minZoom: number;
  maxZoom: number;
  extent: number[];
}
