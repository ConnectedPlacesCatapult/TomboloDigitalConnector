import {ITomboloMap} from './ITomboloMap';

export interface IMapGroup {
  id: string;
  name: string;
  order: number;
  maps: ITomboloMap[];
}
