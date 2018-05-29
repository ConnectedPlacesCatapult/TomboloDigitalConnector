import {ITomboloDataset} from './ITomboloDataset';

export interface IDatasetGroup {
  id: string;
  name: string;
  order: number;
  datasets: ITomboloDataset[];
}
