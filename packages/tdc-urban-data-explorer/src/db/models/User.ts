import {Column, DataType, HasMany, Model, Table, Unique} from 'sequelize-typescript';
import {Dataset} from './Dataset';
import * as sequelize from 'sequelize';
import {IUser} from '../../shared/IUser';
import {IUsageReport} from '../../shared/IUsage';
import * as config from 'config';

@Table({
  tableName: 'users',
  timestamps: true,
  version: true
})
export class User extends Model<User> implements IUser {

  @Column({
    type: DataType.UUID,
    primaryKey: true,
    defaultValue: sequelize.literal('uuid_generate_v4()')
  })
  id: string;

  @Column({
    type: DataType.TEXT,
    unique: true,
    allowNull: false,
    validate: {
      isEmail: true
    }
  })
  email: string;

  @Column({
    type: DataType.TEXT
  })
  name: string;

  @Column({
    type: DataType.TEXT
  })
  password: string;

  @Column({
    type: DataType.TEXT,
    field: 'facebook_id'
  })
  facebookId: string;

  @Column({
    type: DataType.TEXT,
    field: 'twitter_id'
  })
  twitterId: string;

  @Column({
    type: DataType.ARRAY(DataType.TEXT),
    defaultValue: []
  })
  roles: string[];

  @Column({
    type: DataType.BOOLEAN,
    defaultValue: false
  })
  newsletters: boolean;

  @Column({
    type: DataType.BOOLEAN,
    field: 'email_verified',
    defaultValue: false
  })
  emailVerified: boolean;

  @Column({
    type: DataType.UUID,
    field: 'verification_token'
  })
  verificationToken: string;

  @Column({
    type: DataType.UUID,
    field: 'password_reset_token'
  })
  passwordResetToken: string;

  @HasMany(() => Dataset)
  datasets: Dataset[];

  // Return representation safe for sending to client
  get clientSafeUser() {
    return {
      id: this.id,
      email: this.email,
      name: this.name,
      roles: this.roles
    };
  }

  hasRole(role: 'editor' | 'admin'): boolean {
    return this.roles.indexOf(role) > -1;
  }

  async calculateUsage(): Promise<IUsageReport> {

    try {
      const countMapsSql = `select count(1) as c from maps where owner_id = '${this.id}';`;
      const countMapResult = await this.sequelize.query(countMapsSql, {type: sequelize.QueryTypes.SELECT});
      const countMaps = +countMapResult[0]['c'];

      const datasetsSql = `select count(1) as c, sum(db_bytes) as b from datasets where owner_id = '${this.id}';`;
      const datasetsResult = await this.sequelize.query(datasetsSql, {type: sequelize.QueryTypes.SELECT});
      const countDatasets = +datasetsResult[0]['c'];
      const totalStorage = +datasetsResult[0]['b'];

      return {
        used: {
          maps: countMaps,
          datasets: countDatasets,
          totalStorage: totalStorage
        },
        limit: {
          maps: config.get('quota.maps'),
          datasets: config.get('quota.datasets'),
          totalStorage: config.get('quota.totalStorage')
        }
      };
    }
    catch (e) {
      return Promise.reject(e);
    }
  }
}
