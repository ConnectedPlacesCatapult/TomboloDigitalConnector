import {Container} from 'typedi';
import {DB} from '../src/db/index';

describe("connect DB", () => {

  it('should connect to db', (done) => {
    Container.get(DB).checkConnection(true).then( () => {
      done();
    });
  });
});
