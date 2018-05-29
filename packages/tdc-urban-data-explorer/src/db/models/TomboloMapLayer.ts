import {BelongsTo, Column, DataType, ForeignKey, Model, Table} from 'sequelize-typescript';
import {Dataset} from './Dataset';
import {TomboloMap} from './TomboloMap';
import {Palette} from './Palette';
import * as sequelize from 'sequelize';
import {IMapLayer} from '../../shared/IMapLayer';
import {IMapFilter} from '../../shared/IMapFilter';

@Table({
  tableName: 'map_layers',
  timestamps: true,
  version: true
})
export class TomboloMapLayer extends Model<TomboloMapLayer> implements IMapLayer {

  @ForeignKey(() => TomboloMap)
  @Column({
    type: DataType.UUID,
    field: 'map_id',

    primaryKey: true
  })
  mapId: string;

  @Column({
    type: DataType.TEXT,
    primaryKey: true,
    field: 'layer_id',
    defaultValue: sequelize.literal('uuid_generate_v4()')
  })
  layerId: string;

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
    type: DataType.TEXT,
    field: 'layer_type'
  })
  layerType: 'fill' | 'line' | 'circle';

  @ForeignKey(() => Palette)
  @Column({
    type: DataType.TEXT,
    field: 'palette_id'
  })
  paletteId: string;

  @Column({
    type: DataType.BOOLEAN,
    field: 'palette_inverted',
    defaultValue: false
  })
  paletteInverted: boolean;

  @ForeignKey(() => Dataset)
  @Column({
    type: DataType.UUID,
    field: 'dataset_id'
  })
  datasetId: string;

  @Column({
    type: DataType.TEXT,
    field: 'color_mode',
    defaultValue: 'fixed'
  })
  colorMode: 'fixed' | 'attribute';

  // TODO rename colorAttribute
  @Column({
    type: DataType.TEXT,
    field: 'color_attribute'
  })
  colorAttribute: string;

  @Column({
    type: DataType.TEXT,
    field: 'fixed_color',
    defaultValue: '#888888'
  })
  fixedColor: string;

  @Column({
    type: DataType.TEXT,
    field: 'size_attribute'
  })
  sizeAttribute: string;

  @Column({
    type: DataType.FLOAT,
    field: 'fixed_size',
    defaultValue: 10
  })
  fixedSize: number;

  @Column({
    type: DataType.TEXT,
    field: 'size_mode',
    defaultValue: 'fixed'
  })
  sizeMode: 'fixed' | 'attribute';

  @Column({
    type: DataType.TEXT,
    field: 'label_attribute'
  })
  labelAttribute: string;

  @Column({
    type: DataType.FLOAT,
    defaultValue: 10
  })
  opacity: number;

  @Column({
    type: DataType.JSON
  })
  filters: IMapFilter[];

  @Column({
    type: DataType.FLOAT,
  })
  order: number;

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: true,
  })
  visible: boolean;

  @BelongsTo(() => Dataset, {onDelete: 'CASCADE'})
  dataset: Dataset;

  @BelongsTo(() => Palette, {onDelete: 'SET NULL'})
  palette: Palette;

  @BelongsTo(() => TomboloMap, {onDelete: 'CASCADE'})
  map: TomboloMap;
}

