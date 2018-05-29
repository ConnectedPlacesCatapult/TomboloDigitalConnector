import {BelongsTo, Column, DataType, ForeignKey, Model, Table} from 'sequelize-typescript';
import {User} from './User';
const base58 = require('base58');

@Table({
  tableName: 'bookmarks',
  timestamps: false,
  version: false
})
export class Bookmark extends Model<Bookmark> {

  @Column({
    type: DataType.INTEGER,
    allowNull: false,
    primaryKey: true,
    autoIncrement: true
  })
  id: number;

  @Column({
    type: DataType.TEXT,
    allowNull: false
  })
  url: string;


  @ForeignKey(() => User)
  @Column({
    type: DataType.UUID,
    field: 'owner_id'
  })
  ownerId: string;

  @BelongsTo(() => User, {onDelete: 'CASCADE'})
  owner: User;

  get shortId() {
    return base58.encode(this.id);
  }

  static findByShortId(shortId: number) {
    return Bookmark.findOne<Bookmark>({where: { id: base58.decode(shortId)}});
  }
}
