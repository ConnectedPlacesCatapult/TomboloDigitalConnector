import {IStyle} from './IStyle';

export interface IBasemap {
  id: string;
  name: string;
  description: string;
  isDefault: boolean;
  style: IStyle;
}
