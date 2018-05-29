import {IUser} from '../../../../src/shared/IUser';

export class User implements IUser {
  id: string;
  email: string;
  name: string;
  facebookId: string;
  twitterId: string;
  roles: string[];

  constructor(aUser: IUser) {
    this.id = aUser.id;
    this.email = aUser.email;
    this.name = aUser.name;
    this.facebookId = aUser.facebookId;
    this.twitterId = aUser.twitterId;
    this.roles = [...aUser.roles];
  }

  hasRole(role: 'editor' | 'admin'): boolean {
    return this.roles.indexOf(role) > -1;
  }
}
