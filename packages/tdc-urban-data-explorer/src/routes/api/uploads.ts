/**
 * Uploads Route - file upload handling
 *
 * @module Routes
 */

/**
 * Copyright © 2018 Emu Analytics
 */

import * as express from 'express';
import * as multer from 'multer';
import * as config from 'config';
import {Container} from 'typedi';
import {LoggerService} from '../../lib/logger';
import {FileUpload} from '../../db/models/FileUpload';
import {FileIngester} from '../../lib/file-ingester/file-ingester';
import {Dataset} from '../../db/models/Dataset';
import {TomboloMap} from '../../db/models/TomboloMap';
import {DataAttribute} from '../../db/models/DataAttribute';
import {isAuthenticated} from '../../lib/utils';
import {IFileUpload} from '../../shared/IFileUpload';

const logger = Container.get(LoggerService);
const fileUploader = Container.get(FileIngester);

const router = express.Router();

const upload = multer({
  dest: config.get('fileUpload.uploadPath'),
  limits: {
    files: 1,
    fileSize: config.get('fileUpload.maxFileSize')
  }
} as any);

/**
 * Get client config
 */
router.get('/:uploadId', isAuthenticated, async (req, res, next) => {
  try {
    const upload = await FileUpload.findById<FileUpload>(req.params.uploadId);

    if (!upload) {
      return next({status: 404, message: 'Upload not found'});
    }

    res.json(upload);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

router.post('/', isAuthenticated, upload.single('file'), async (req, res, next) => {

 try {
    const file = req.file;

    const usage = await req.user.calculateUsage();

    console.log(usage);
   console.log(file.size);

    if (usage.used.datasets >= usage.limit.datasets || usage.used.totalStorage + file.size > usage.limit.totalStorage) {
      return next({status: 401, message: 'Quota exceeded'});
    }

    const fileUpload = await FileUpload.create<FileUpload>({
      id: file.filename,
      mimeType: file.mimetype,
      originalName: file.originalname,
      size: file.size,
      path: file.path,
      status: 'uploaded',
      ownerId: req.user.id
    });

    // Do not wait for processing to finish.
    // Processing will continue after this route returns
    fileUploader.processFile(fileUpload);

    res.json(fileUpload);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

router.post('/:uploadId', isAuthenticated, async (req, res, next) => {
  try {
    let fileUpload = await FileUpload.findById<FileUpload>(req.params.uploadId);

    if (!fileUpload) {
      return next({status: 404, message: 'Upload not found'});
    }

    const updatedFile: IFileUpload = req.body;
    await fileUploader.finalizeUpload(fileUpload, updatedFile);

    fileUpload = await fileUpload.update(updatedFile, {fields: ['name', 'description', 'attribution', 'dbAttributes']});

    res.json(fileUpload);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

router.get('/:uploadId/dataset', isAuthenticated, async (req, res, next) => {
  try {
    let fileUpload = await FileUpload.findById<FileUpload>(req.params.uploadId, {include: [Dataset]});

    if (!fileUpload) {
      return next({status: 404, message: 'Upload not found'});
    }

    // Dataset already generated for this upload. Return it directly
    if (fileUpload.dataset) {
      return res.json(fileUpload.dataset);
    }

    // Generate a new dataset from this upload
    const dataset = await fileUploader.generateDataset(fileUpload);

    res.json(dataset);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

router.get('/:uploadId/map', isAuthenticated, async (req, res, next) => {
  try {
    let fileUpload = await FileUpload.findById<FileUpload>(req.params.uploadId, {
      include: [{model: Dataset, include: [{model: DataAttribute}]}, TomboloMap]
    });

    if (!fileUpload) {
      return next({status: 404, message: 'Upload not found'});
    }

    if (!fileUpload.dataset) {
      return next({status: 404, message: 'Dataset not found for upload'});
    }

    // Map already generated for this upload. Return it directly
    if (fileUpload.map) {
      return res.json(fileUpload.map);
    }

    const map = await fileUploader.generateMap(fileUpload);

    res.json(map);
  }
  catch (e) {
    logger.error(e);
    next(e);
  }
});

export default router;
