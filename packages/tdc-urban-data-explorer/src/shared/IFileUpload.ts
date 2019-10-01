
import {OgrFileInfoBase} from './ogrfileinfo-base';
import {IDBAttribute} from './IDBAttribute';
import {IUser} from './IUser';
import {ITomboloDataset} from './ITomboloDataset';
import {ITomboloMap} from './ITomboloMap';

export interface IFileUpload {
  id?: string;
  originalName?: string;
  name?: string;
  description?: string;
  attribution?: string;
  mimeType?: string;
  size?: number;
  path: string;
  status?: 'uploaded' | 'validating' | 'ingesting' | 'done' | 'error';
  ogrInfo?: OgrFileInfoBase;
  dbAttributes?: IDBAttribute[];
  error?: string;
  ownerId?: string;
  datasetId?: string;
  mapId?: string;
}
