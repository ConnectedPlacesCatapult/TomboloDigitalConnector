export interface IDBAttribute {
  field: string;
  type: string;
  allowNull: boolean;
  defaultValue: any;
  special: any[];
  primaryKey: boolean;
  name?: string;
  description?: string;
  removed?: boolean;
  unit?: string;
}
