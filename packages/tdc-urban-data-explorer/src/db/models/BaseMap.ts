import {BelongsTo, Column, DataType, ForeignKey, HasMany, Model, Scopes, Table} from 'sequelize-typescript';
import * as sequelize from 'sequelize';
import {literal} from 'sequelize';
import {IBasemap} from '../../shared/IBasemap';
import {IStyle} from '../../shared/IStyle';

@Table({
  tableName: 'base_maps',
  timestamps: true,
  version: true
})
export class BaseMap extends Model<BaseMap> implements IBasemap {

  @Column({
    type: DataType.TEXT,
    primaryKey: true
  })
  id: string;

  @Column({
    type: DataType.TEXT,
    allowNull: false
  })
  name: string;

  @Column({
    type: DataType.TEXT,
    allowNull: true
  })
  description: string;

  @Column({
    type: DataType.BOOLEAN,
    field: 'is_default'
  })
  isDefault: boolean;

  @Column({
    type: DataType.JSON,
    allowNull: true
  })
  style: IStyle;

  static getDefault() {
    return BaseMap.findOne<BaseMap>({where: { isDefault: true} as any});
  }

}
