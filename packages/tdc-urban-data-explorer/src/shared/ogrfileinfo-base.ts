export interface OgrFileInfoBase {
  id: string;
  path: string;
  name?: string;
  description?: string;
  attribution?: string;
  removed?: boolean;
  driver: string;
  geometryType: string;
  featureCount: number;
  srs: string;
  attributes: OgrAttributeBase[];
}

export interface OgrAttributeBase {
  id: string;
  type: string;
  precision?: number;
  name?: string;
  description?: string;
  unit?: string;
  removed?: boolean;
}
