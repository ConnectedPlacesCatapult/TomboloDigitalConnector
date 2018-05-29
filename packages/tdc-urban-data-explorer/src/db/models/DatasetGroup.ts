import {Column, DataType, HasMany, Model, Scopes, Table} from 'sequelize-typescript';
import {Dataset} from './Dataset';
import {IDatasetGroup} from '../../shared/IDatasetGroup';

@Table({
  tableName: 'dataset_groups'
})

@Scopes({
  full: {
    order: [['order', 'asc nulls last'], ['datasets', 'order', 'asc nulls last']],
    include: [() => Dataset]
  }
})

export class DatasetGroup extends Model<DatasetGroup> implements IDatasetGroup {

  @Column({
    type: DataType.TEXT,
    primaryKey: true,
  })
  id: string;

  @Column({
    type: DataType.TEXT
  })
  name: string;

  @Column({
    type: DataType.INTEGER()
  })
  order: number;

  @HasMany(() => Dataset)
  datasets: Dataset[];
}
