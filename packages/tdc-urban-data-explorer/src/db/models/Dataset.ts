import {
  BelongsTo, Column, DataType, DefaultScope, ForeignKey, HasMany, Model, Scopes,
  Table
} from 'sequelize-typescript';
import {User} from './User';
import {DataAttribute} from './DataAttribute';
import {DatasetGroup} from './DatasetGroup';
import * as sequelize from 'sequelize';
import {QueryInterface} from 'sequelize';
import {ITomboloDataset} from '../../shared/ITomboloDataset';
import {ITomboloDatasetAttribute} from '../../shared/ITomboloDatasetAttribute';

type SourceType = 'table' | 'sql' | 'tilelive';

const CATEGORY_MAX_COUNT = 30;

@Table({
  tableName: 'datasets',
  timestamps: true,
  version: true
})
@Scopes({
  withAttributes: {
    order: [['dataAttributes', 'order']],
    include: [() => DataAttribute]
  }
})
export class Dataset extends Model<Dataset> implements ITomboloDataset {

  @Column({
    type: DataType.UUID,
    defaultValue: DataType.UUIDV4,
    primaryKey: true
  })
  id: string;

  @Column({
    type: DataType.TEXT,
    allowNull: false
  })
  name: string;

  @Column({
    type: DataType.TEXT
  })
  description: string;

  @Column({
    type: DataType.TEXT
  })
  attribution: string;

  @Column({
    type: DataType.TEXT,
    validate: {
      isIn: [['table', 'sql', 'tilelive']]
    }
  })
  sourceType: SourceType;

  @Column(DataType.TEXT)
  source: string;

  @Column({
    type: DataType.TEXT,
    defaultValue: 'geometry',
    field: 'geometry_column'
  })
  geometryColumn: string;

  @Column({
    type: DataType.TEXT,
    field: 'geometry_type'
  })
  geometryType: string;

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: false,
    field: 'is_private'
  })
  isPrivate: boolean;

  @Column({
    type: DataType.INTEGER,
    defaultValue: 0,
    allowNull: false,
    validate: {
      isInt: true
    }
  })
  minZoom: number;

  @Column({
    type: DataType.INTEGER,
    defaultValue: 20,
    allowNull: false,
    validate: {
      isInt: true
    }
  })
  maxZoom: number;

  @Column({
    type: DataType.ARRAY(DataType.DOUBLE),
    defaultValue: [-180, -90, 180, 90]
  })
  extent: number[];

  @Column(DataType.JSON)
  headers: object;

  @Column({
    type: DataType.INTEGER,
    field: 'original_bytes'
  })
  originalBytes: number;

  @Column({
    type: DataType.INTEGER,
    field: 'db_bytes'
  })
  dbBytes: number;

  @Column({
    type: DataType.INTEGER()
  })
  order: number;

  @ForeignKey(() => User)
  @Column({
    type: DataType.UUID,
    field: 'owner_id'
  })
  ownerId: string;

  @ForeignKey(() => DatasetGroup)
  @Column({
    type: DataType.TEXT,
    field: 'dataset_group_id'
  })
  datasetGroupId: string;


  @BelongsTo(() => User, {onDelete: 'CASCADE'})
  owner: User;

  @HasMany(() => DataAttribute)
  dataAttributes: ITomboloDatasetAttribute[];

  // Find all datasets by userId
  static findByUserId(userId: string) {
    return Dataset.findAll<Dataset>({
      where: { ownerId: userId},
      order: ['name']
    });
  }

  // Find datasets by full-text query
  static findByFullTextQuery(query: string) {

    const queryInterface: QueryInterface = (this as any).QueryInterface;
    const sqlSafeQuery = queryInterface.escape(query);

    const fullTextQuery =
      `to_tsvector('english', name || ' ' || coalesce(description, '')) @@ plainto_tsquery('english', ${sqlSafeQuery}) and is_private=false`;

    return Dataset.findAll<Dataset>({
      where: {
        query: sequelize.literal(fullTextQuery)
      } as any,
      order: ['name']
    });
  }

  /**
   * Adjust duplicate quantiles generated for sparse data e.g [0, 0, 0, 2, 3]. MapboxGl can't interpolate
   * these so adjust to interpolate duplicates. e.g. [0, 0.666, 1.333, 2, 3]
   *
   * @param {number[]} quantiles
   * @returns {number[]}
   */
  static adjustDuplicateQuantiles(quantiles: number[]): number[] {

    let startIndex = -1;
    let duplicatesFound = false;
    let currentVal = -9999999999;
    let results = [...quantiles];

    for (let i = 0; i < quantiles.length; i++) {

      if (quantiles[i] !== currentVal && !duplicatesFound) {
        // Possibly starting a new run of duplicates
        currentVal = results[i];
        startIndex = i;
      }
      else {
        duplicatesFound = true;
      }

      // Tests for end of a run
      if (quantiles[i] !== currentVal && duplicatesFound) {
        // At end of a run of duplicates before end of quantiles
        // Interpolate duplicate quantiles up to next value
        //e.g [0, 0, 0, 0, 1, 2] -> [0, 0.25, 0.5, 0.75, 1, 2]

        const stepPerQuantile = (quantiles[i] - quantiles[startIndex]) / (i - startIndex);
        for (let j = startIndex; j < i; j++) {
          results[j] = currentVal + stepPerQuantile * (j - startIndex);
        }

        // Carry on and look for another run
        duplicatesFound = false;
        currentVal = quantiles[i];
        startIndex = i;
      }
      else if (duplicatesFound && i === quantiles.length - 1) {
        // Terminal run of duplicates found
        // Interpolate duplicate quantiles down to previous value
        // e.g. [0, 1, 2, 2, 2] -> [0, 1, 1.333. 1.666, 2]
        if (startIndex > 0) {
          const numDuplicates = i - startIndex + 1;
          const stepPerQuantile = (quantiles[i] - quantiles[startIndex - 1]) / numDuplicates;
          for (let j = 0; j < numDuplicates; j++) {
            results[j + startIndex] = quantiles[startIndex - 1] + stepPerQuantile * (j + 1);
          }
        }
        else {
          // Whole set of quantiles is a duplicate e.g. [0, 0, 0, 0, 0]
          // Just set arbitrary values above initial value e.g. [0, 1, 2, 3, 4]
          for (let j  = 1; j < quantiles.length; j++) {
            results[j] = quantiles[0] + j;
          }
        }
      }
    }

    return results;
  }

  async calculateDataAttributeStats(): Promise<void> {
    // Calculating attribute stats is only supported for 'table' and 'sql' type datasets
    if (this.sourceType !== 'table' && this.sourceType !== 'sql') return;

    const dataAttributes = await this.$get('dataAttributes') as DataAttribute[];

    await Promise.all(dataAttributes.map(attribute => {
      if (attribute.type === 'number') {
        return this.updateNumericAttribute(attribute);
      }
      else if (attribute.type === 'string') {
        return this.updateTextualAttribute(attribute);
      }
    }));
  }

  async calculateGeometryExtent(): Promise<void> {

    const extentSql = `
      select btrim(replace(st_extent(${this.sqlSafeGeometryColumn()})::text, ' ', ','), '(BOX()') as extent 
      from ${this.sqlSafeSource()}`;

    const result = await this.sequelize.query(extentSql, {type: sequelize.QueryTypes.SELECT});

    this.extent = result[0]['extent'].split(',').map(val => +val);

    await this.save();
  }

  async calculateDatasetBytes(): Promise<void> {

    if (this.sourceType !== 'table') return;

    const bytesSql = `SELECT pg_total_relation_size('${this.sqlSafeSource()}') as bytes`;

    const result = await this.sequelize.query(bytesSql, {type: sequelize.QueryTypes.SELECT});

    this.dbBytes = result[0]['bytes'];

    await this.save();
  }

  sqlSafeGeometryColumn() {
    return this.sequelize.getQueryInterface().quoteIdentifier(this.geometryColumn, true);
  }

  sqlSafeSource() {
    if (this.sourceType === 'table')
      return this.sequelize.getQueryInterface().quoteIdentifier(this.source, true);
    else
      return this.source;
  }

  private async updateNumericAttribute(attribute: DataAttribute): Promise<void> {

    attribute.isCategorical = false;

    // Min and max values
    const minmaxSql = `select min(${attribute.sqlSafeField()}) as min, 
      max(${attribute.sqlSafeField()}) as max 
      from ${this.sqlSafeSource()}`;

    const minmax = await this.sequelize.query(minmaxSql, {type: sequelize.QueryTypes.SELECT});
    attribute.minValue = minmax[0].min;
    attribute.maxValue = minmax[0].max;

    const ntileSql = `select min(val), max(val), ntile from (
          select ${attribute.sqlSafeField()} as val, ntile($1) OVER(order by ${attribute.sqlSafeField()}) 
          from ${this.sqlSafeSource()} 
          where ${attribute.sqlSafeField()} notnull) as subquery
          group by ntile order by ntile;`;

    // Quintiles
    const quintiles = await this.sequelize.query(ntileSql, {type: sequelize.QueryTypes.SELECT, bind: [4]});
    attribute.quantiles5 = Dataset.adjustDuplicateQuantiles([...quintiles.map(d => d.min), attribute.maxValue]);

    // Dectiles
    const dectiles = await this.sequelize.query(ntileSql, {type: sequelize.QueryTypes.SELECT, bind: [9]});
    attribute.quantiles10 = Dataset.adjustDuplicateQuantiles([...dectiles.map(d => d.min), attribute.maxValue]);

    await attribute.save();
  }

  private async updateTextualAttribute(attribute: DataAttribute): Promise<void> {
    attribute.minValue = null;
    attribute.maxValue = null;
    attribute.quantiles5 = null;
    attribute.quantiles10 = null;

    // See if field is categorical (i.e. has distinct values less than CATEGORY_MAX_COUNT)
    const categoriesSql = `select distinct ${attribute.sqlSafeField()} as val 
      from ${this.sqlSafeSource()} where ${attribute.sqlSafeField()} notnull order by 1 limit ${CATEGORY_MAX_COUNT + 1}`;

    const results = await this.sequelize.query(categoriesSql, {type: sequelize.QueryTypes.SELECT});

    if (results.length > 1 && results.length <= CATEGORY_MAX_COUNT) {
      attribute.isCategorical = true;
      attribute.categories = results.map(d => d.val);
    }
    else {
      attribute.isCategorical = false;
    }

    await attribute.save();
  }
}
