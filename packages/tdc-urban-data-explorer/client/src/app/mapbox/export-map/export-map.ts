/**
 * Export Map Class
 */

import 'rxjs/add/operator/map';
import * as jsPDF from 'jspdf';
import {saveAs} from 'file-saver';
import {Map} from 'mapbox-gl';
import * as webglToCanvas2d from 'webgl-to-canvas2d';
import {MapDom} from "./map-dom";
import * as Debug from 'debug';

const debug = Debug('tombolo:map-export');

export class ExportMap {

  renderMap: Map;
  nativeDPI: number;

  dimensionsLimit = 1200; // Max width and height in mm
  dpiLimit = 300;

  mapDom = new MapDom();

  constructor() {
  }

  /**
   * Download the map canvas as a PNG or PDF file. Width and height should be in mm.
   *
   * @param {Object} options
   * @param {string} name
   * @param {number} width
   * @param {number} height
   * @param {number} dpi
   * @param {string} format
   * @param {(ctx, callback) => void} drawOverlay
   * @returns {Promise<string>}
   */
  downloadCanvas(options: object,
                 name: string,
                 width: number,
                 height: number,
                 dpi: number,
                 format: string,
                 drawOverlay: (ctx, width, height, callback) => void): Promise<string> {

    // Handle errors
    const errors = this.getErrors(width, height, dpi, format);

    if (errors !== '') {
      return new Promise((resolve,reject) => {
        reject(new Error(errors));
      });
    }

    const containerWidth = (width/25.4)*96, containerHeight = (height/25.4)*96;
    this.nativeDPI = window.devicePixelRatio * 96;
    this.setCanvasDPI(dpi);

    const container = this.mapDom.createContainerDiv(containerWidth, containerHeight);

    // Temporary "copy" of the orginal Map object which will be used for exporting.
    this.renderMap = new Map({
      ...options,
      container: container,
      interactive: false,
      attributionControl: false,
      preserveDrawingBuffer: true
    });

    return new Promise((resolve,reject) => {

      this.renderMap.on('load', () => {
        const fileName = `${name}.${format}`;

        setTimeout(() => {
          let renderMapCanvas = this.renderMap.getCanvas();

          let overlayCanvas = this.mapDom.createColourScaleCanvas(renderMapCanvas.width, renderMapCanvas.height);
          let overlayContext = overlayCanvas.getContext("2d");

          // Callback function suppplied to client to add an overlay to the export
          // Calling this function finalizes the export
          let drawOverlayCallback = (err = null) => {

            if (err) {
              // overlay callback reported an error
              this.revertChanges();
              reject(err);
            }
            else {
              debug('Done called. Finalising download');

              let canvas2d = webglToCanvas2d(renderMapCanvas);
              let mapCtx = canvas2d.getContext("2d");
              mapCtx.drawImage(overlayCanvas, 0, 0);

              switch (format) {
                case'png':
                  canvas2d.toBlob(blob => saveAs(blob, fileName));
                  break;
                case 'pdf':
                  this.buildPdf(canvas2d, fileName, width, height);
                  break;
              }

              this.revertChanges();
              resolve(fileName);
            }
          };

          // Draw overlay if required
          if (drawOverlay !== null) {
            debug('Calling out to overlay handler');
            drawOverlay(overlayContext,renderMapCanvas.width, renderMapCanvas.height, drawOverlayCallback);
          }
          else {
            // Finish without calling out to overlay handlerd
            drawOverlayCallback();
          }

        }, 500);
      });
    });
  }

  private buildPdf(canvas, name: string, width: number, height: number): void {
    let pdf = new jsPDF({
      orientation: width > height ? 'l' : 'p',
      unit: 'mm',
      format: [width, height],
      compress: true
    });

    pdf.addImage(canvas.toDataURL('image/png'),
      'png', 0, 0, width, height, null, 'FAST');

    pdf.save(name);
  }

  private setCanvasDPI(dpi: number): void {
    Object.defineProperty(window, 'devicePixelRatio', {
      get: () => dpi/96
    });
  }

  // Remove temporary DOM elements and map copy,
  // and change the DPI back to its native setting.
  private revertChanges(): void {
    this.renderMap.remove();
    this.mapDom.revertDomChanges();
    this.setCanvasDPI(this.nativeDPI);
  }

  private getErrors(width: number, height: number, dpi: number, format: string): string {
    let errors = [];

    if (width > this.dimensionsLimit || width < 0 || height > this.dimensionsLimit || height < 0) {
      errors.push(`Width and height be between 0 and ${this.dimensionsLimit}mm.`);
    }

    if (isNaN(dpi) || isNaN(width) || isNaN(height) ) {
      errors.push(`Width, height and DPI must be numbers.`);
    }

    if (dpi > this.dpiLimit || dpi < 0) {
      errors.push(`DPI must be between 0 and ${this.dpiLimit}.`);
    }

    if (format !== 'png' && format !== 'pdf') {
      errors.push(`Format must be 'png' or 'pdf'.`);
    }

    return errors.join(' ');
  }
}
