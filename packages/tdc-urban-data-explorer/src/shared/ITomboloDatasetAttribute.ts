export interface ITomboloDatasetAttribute {
  datasetId: string;
  name: string;
  description: string | null;
  field: string;
  order: number;
  unit: string | null;
  minValue: number | null;
  maxValue: number | null;
  quantiles5: number[] | null;
  quantiles10: number[] | null;
  type: 'number' | 'string' | 'datetime';
  categories: string[] | null;
  isCategorical: boolean;

  sqlSafeField?: () => any;
}
