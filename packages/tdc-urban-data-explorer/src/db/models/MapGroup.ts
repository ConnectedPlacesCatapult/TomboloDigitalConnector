import {Column, DataType, HasMany, Model, Scopes, Table} from 'sequelize-typescript';
import {TomboloMap} from './TomboloMap';
import {IMapGroup} from '../../shared/IMapGroup';

@Table({
  tableName: 'map_groups'
})
@Scopes({
  systemMaps: {
    order: [['order', 'asc nulls last'], ['maps', 'order', 'asc nulls last']],
    include: [{model: () => TomboloMap, where: {'ownerId': null}}]
  }
})
export class MapGroup extends Model<MapGroup> implements IMapGroup {

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

  @HasMany(() => TomboloMap)
  maps: TomboloMap[];
}
