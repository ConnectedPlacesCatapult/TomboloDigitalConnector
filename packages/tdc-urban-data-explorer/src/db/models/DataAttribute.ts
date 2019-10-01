
import {BelongsTo, Column, DataType, ForeignKey, HasMany, Model, Scopes, Table} from 'sequelize-typescript';
import {Dataset} from './Dataset';
import {ITomboloDatasetAttribute} from '../../shared/ITomboloDatasetAttribute';

@Table({
  tableName: 'data_attributes',
  timestamps: true,
  version: true
})
@Scopes({
  default: {
    order: [['order', 'ASC']]
  }
})
export class DataAttribute extends Model<DataAttribute> implements ITomboloDatasetAttribute {

  @ForeignKey(() => Dataset)
  @Column({
    type: DataType.UUID,
    field: 'dataset_id',
    primaryKey: true
  })
  datasetId: string;

  @Column({
    type: DataType.TEXT,
    primaryKey: true
  })
  field: string;

  @Column({
    type: DataType.TEXT,
    field: 'field_sql'
  })
  fieldSql: string;

  @Column({
    type: DataType.TEXT,
    allowNull: false
  })
  type: 'number' | 'string' | 'datetime';

  @Column({
    type: DataType.TEXT
  })
  name: string;

  @Column({
    type: DataType.TEXT
  })
  description: string;

  @Column({
    type: DataType.TEXT
  })
  unit: string;

  @Column({
    type: DataType.DOUBLE,
    field: 'min'
  })
  minValue: number;

  @Column({
    type: DataType.DOUBLE,
    field: 'max'
  })
  maxValue: number;

  @Column({
    type: DataType.ARRAY(DataType.DOUBLE),
    field: 'quantiles_5'
  })
  quantiles5: number[];

  @Column({
    type: DataType.ARRAY(DataType.DOUBLE),
    field: 'quantiles_10'
  })
  quantiles10: number[];

  @Column({
    type: DataType.ARRAY(DataType.TEXT),
  })
  categories: string[];

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: false,
    field: 'is_categorical'
  })
  isCategorical: boolean;

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: false,
    field: 'is_log'
  })
  isLog: boolean;

  @Column(DataType.INTEGER)
  order: number;

  @BelongsTo(() => Dataset, {onDelete: 'CASCADE'})
  dataset: Dataset;

  // Instance methods
  //
  sqlSafeField(): string {
    if (this.fieldSql) {
      return this.fieldSql;
    }
    else {
      return this.sequelize.getQueryInterface().quoteIdentifier(this.field, true);
    }
  }
}
