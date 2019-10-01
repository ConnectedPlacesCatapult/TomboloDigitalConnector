import {BelongsTo, Column, DataType, ForeignKey, HasMany, Model, Scopes, Table} from 'sequelize-typescript';
import * as sequelize from 'sequelize';
import {User} from './User';
import {Dataset} from './Dataset';
import {DataAttribute} from './DataAttribute';
import {BaseMap} from './BaseMap';
import {TomboloMapLayer} from './TomboloMapLayer';
import {Palette} from './Palette';
import {MapGroup} from './MapGroup';
import {ITomboloMap} from '../../shared/ITomboloMap';
import {IMapDefinition} from '../../shared/IMapDefinition';
import {LoggerService} from '../../lib/logger';
import {Container} from 'typedi';
import {DATA_LAYER_PREFIX} from '../../shared/style-generator/style-generator';

const logger = Container.get(LoggerService);

@Table({
  tableName: 'maps',
  timestamps: true,
  version: true
})
@Scopes({
  full: {
    order: [['layers', 'order']],
    include: [
      () => User,
      () => BaseMap,
      {
        model: () => TomboloMapLayer,
        include: [() => Palette, {
          model: () => Dataset,
          include: [() => DataAttribute]
        }]
      }]
  }
})
export class TomboloMap extends Model<TomboloMap> implements ITomboloMap {

  @Column({
    type: DataType.UUID,
    primaryKey: true,
    defaultValue: sequelize.literal('uuid_generate_v4()')
  })
  id: string;

  @Column({
    type: DataType.TEXT,
    allowNull: false
  })
  name: string;

  @Column({
    type: DataType.TEXT,
  })
  recipe: string;

  @Column({
    type: DataType.TEXT,
    allowNull: true
  })
  description: string;

  @Column({
    type: DataType.TEXT
  })
  icon: string;

  @Column({
    type: DataType.DOUBLE
  })
  zoom: number;

  @Column({
    type: DataType.ARRAY(DataType.DOUBLE)
  })
  center: number[];

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: true,
    field: 'is_private'
  })
  isPrivate: boolean;

  @Column({
    type: DataType.INTEGER,
    field: 'basemap_detail_level'
  })
  basemapDetailLevel: number;

  @Column({
    type: DataType.JSON
  })
  ui: object;

  @ForeignKey(() => User)
  @Column({
    type: DataType.UUID,
    field: 'owner_id'
  })
  ownerId: string;

  @ForeignKey(() => BaseMap)
  @Column({
    type: DataType.TEXT,
    field: 'basemap_id'
  })
  basemapId: string;

  @ForeignKey(() => MapGroup)
  @Column({
    type: DataType.TEXT,
    field: 'map_group_id'
  })
  mapGroupId: string;

  @Column({
    type: DataType.INTEGER()
  })
  order: number;

  @HasMany(() => TomboloMapLayer)
  layers: TomboloMapLayer[];

  @BelongsTo(() => User, {onDelete: 'CASCADE'})
  owner: User;

  @BelongsTo(() => MapGroup, {onDelete: 'SET NULL'})
  mapGroup: MapGroup;

  @BelongsTo(() => BaseMap, {onDelete: 'SET NULL'})
  basemap: BaseMap;


  static async saveMap(mapDefinition: IMapDefinition): Promise<TomboloMap> {
    try {

      // Could do an upsert here but Sequelize doesn't update version
      let [map, created] = await this.findOrCreate<TomboloMap>({
        where: {id: mapDefinition.id},
        defaults: mapDefinition
      });

      if (!created) {
        logger.debug(`Updating map ${map.id}`);
        await map.update(mapDefinition, {
          fields: ['name', 'description', 'recipe', 'isPrivate', 'zoom', 'center', 'basemapDetailLevel', 'basemapId']
        });
      }
      else {
        logger.debug(`Created map ${map.id}`);
      }

      let mapLayers: TomboloMapLayer[] = await map.$get('layers') as TomboloMapLayer[];

      // Update existing layers and insert new layers
      const updatesAndInserts = mapDefinition.layers.map(defLayer => {
        const mapLayer = mapLayers.find(mapLayer => defLayer.originalLayerId === mapLayer.layerId);

        if (mapLayer) {
          logger.debug(`Updating map layer ${mapLayer.layerId}`);
          return mapLayer.update({...defLayer, palette: defLayer.palette, paletteId: defLayer.palette.id});
        }
        else {
          logger.debug(`Creating new map layer ${defLayer.originalLayerId}`);
          return TomboloMapLayer.create<TomboloMapLayer>({
            ...defLayer,
            layerId: defLayer.originalLayerId,
            mapId: map.id,
            paletteId: defLayer.palette.id
          });
        }
      });

      await Promise.all(updatesAndInserts);

      // Remove layers that have been deleted
      const deletions = mapLayers.map(mapLayer => {
        const found = mapDefinition.layers.find(defLayer => defLayer.originalLayerId === mapLayer.layerId);
        if (found) {
          return Promise.resolve();
        }
        else {
          logger.debug(`Deleting map layer ${mapLayer.layerId}`);
          return mapLayer.destroy();
        }
      });

      await Promise.all(deletions);

      // Save map layer filters
      //

      // Reload layers to pick up insertions and deletions
      mapLayers = await map.$get('layers') as TomboloMapLayer[];

      const filterUpdates = mapLayers.map(layer => {
        const filtersForLayer = mapDefinition.filters.filter(f => f.datalayerId === DATA_LAYER_PREFIX + layer.layerId);
        layer.filters = filtersForLayer.map(filter => {
          // Delete datalayerId from filter
          const filterCopy = {...filter};
          delete filterCopy.datalayerId;
          return filterCopy;
        });

        return layer.save();
      });

      await Promise.all(filterUpdates);

      return map;
    }
    catch (e) {
      throw e;
    }
  }

}
