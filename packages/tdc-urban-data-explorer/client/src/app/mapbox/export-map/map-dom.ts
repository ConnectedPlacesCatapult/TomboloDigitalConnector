/**
 * Map DOM Manipulation class
 *
 * Contains functions used to create the temporary HTML elements needed for
 * map export.
 */

export class MapDom {

  // Create the container div for the temporary renderMap.
  createContainerDiv(width: number, height: number): HTMLDivElement {
    let hidden = this.createHiddenDiv();
    let container = document.createElement('div');
    container.style.width = `${width}px`;
    container.style.height = `${height}px`;
    hidden.appendChild(container);
    return container;
  }

  // Create the hidden div which the renderMap container sits in.
  createHiddenDiv(): HTMLDivElement {
    let hidden = <HTMLDivElement>document.createElement('div');

    hidden.className = 'hidden-map';
    hidden.id = 'hidden-map';
    document.body.appendChild(hidden);
    return hidden;
  }

  // Create the colour-scale canvas which is overlayed with the map canvas.
  createColourScaleCanvas(width, height): HTMLCanvasElement {
    let canvasContainer = document.getElementsByClassName('mapboxgl-canvas-container')[1];
    let colourScaleCanvas = document.createElement('canvas');
    colourScaleCanvas.setAttribute('id', 'colourScaleCanvas');
    colourScaleCanvas.setAttribute('width', width);
    colourScaleCanvas.setAttribute('height', height);
    canvasContainer.appendChild(colourScaleCanvas);
    return colourScaleCanvas;
  }

  revertDomChanges(): void {
    const hidden = document.getElementById('hidden-map');
    hidden.parentNode.removeChild(hidden);
  }
}
