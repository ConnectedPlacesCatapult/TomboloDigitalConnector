import {ITomboloDataset} from './ITomboloDataset';
import {IMapLayer} from './IMapLayer';
import {IMapFilter} from './IMapFilter';

// Map definition used for style generation

export interface IMapDefinition {
  id: string;
  name: string;
  description: string;
  isPrivate: boolean;
  zoom: number;
  center: number[];
  datasets: ITomboloDataset[];
  layers: IMapLayer[];
  recipe: string;
  basemapId: string;
  basemapDetailLevel: number;
  tileUrl: string;
  mapAssetsUrl: string;
  ownerId: string;
  filters: IMapFilter[];
  ui: object;
}
