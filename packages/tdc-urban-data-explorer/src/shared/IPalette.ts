export interface IPalette {
  id: string;
  description: string;
  colorStops: string[];
  isDefault?: boolean;
  groupId?: string;
  order?: number;
}
