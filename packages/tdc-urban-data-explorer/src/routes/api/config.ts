/**
 * Config Route - exposes config options to client side
 *
 * @module Routes
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import * as express from 'express';
import * as config from 'config';
import * as version from '../../version';

const router = express.Router();

/**
 * Get client config
 */
router.get('/', (req, res) => {
  res.send({...config.get('client'), maxUploadSize: config.get('fileUpload.maxFileSize'), version});
});

export default router;
