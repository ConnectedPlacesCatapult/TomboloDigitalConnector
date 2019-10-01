import * as config from 'config';
import {Logger} from '../../src/lib/logger';
import {Container} from 'typedi';
import {DB} from '../../src/db/index';
import * as sequelize from 'sequelize';
import {AuthService} from '../../src/lib/auth';
import {Mailer} from '../../src/lib/mailer';


describe('Authentication Service', () => {

  let authService: AuthService;
  const db =  Container.get(DB);

  const mockLocker: Logger = {
    log: (level: string, msg: string, ...meta: any[]) => {
    },
    silly: (msg: string, ...meta: any[]) => {
    },
    debug: (msg: string, ...meta: any[]) => {
    },
    info: (msg: string, ...meta: any[]) => {
    },
    warn: (msg: string, ...meta: any[]) => {
    },
    error: (msg: string, ...meta: any[]) => {
    }
  };

  const mockMailer: Mailer = {} as Mailer;

  beforeEach(() => {
    const baseUrl = config.get('server.baseUrl');
    const authConfig = config.get('auth');
    authService = new AuthService(mockLocker, mockMailer, baseUrl, authConfig);
  });

  describe('Authentication Service', () => {

    it('should exist', () => {
      expect(authService).toBeDefined();
    });

    it('should encrypt a password', (done) => {
      authService.encryptPassword('password').then(encryptedPassword => {
        expect(encryptedPassword.length).toBe(76);
        done();
      });
    });

    it('should verify a password', (done) => {
      authService.encryptPassword('password')
        .then(encryptedPassword => {
          return authService.validatePassword('password', encryptedPassword);
        })
        .then(verified => {
          expect(verified).toBeTruthy();
          done();
        });
    });

    it('should not verify an incorrect password', (done) => {
      authService.encryptPassword('password')
        .then(encryptedPassword => {
          return authService.validatePassword('wrongpassword', encryptedPassword);
        })
        .then(verified => {
          expect(verified).toBeFalsy();
          done();
        });
    });

  });
});
