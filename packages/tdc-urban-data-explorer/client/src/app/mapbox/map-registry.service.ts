/**
 * MapRegistry - service to register and retrieve MapBoxGL maps
 */

import {Injectable} from "@angular/core";
import * as Debug from 'debug';
import {EmuMapboxMap} from "./mapbox.component";

const debug = Debug('tombolo:MapRegistry');

@Injectable()
export class MapRegistry {

  private maps: Map<string, Promise<EmuMapboxMap>> = new Map<string, Promise<EmuMapboxMap>>();

  registerMap(id: string, map: EmuMapboxMap): void {

    debug(`Registering map '${id}'`);

    this.maps.set(id, new Promise((resolve, reject) => {
      map.once('load', () => {
        debug(`Registered map '${id}'`);
        resolve(map);
      });
    }));
  }

  getMap<T extends EmuMapboxMap = EmuMapboxMap>(id: string): Promise<T> {
    const mapPromise = this.maps.get(id);
    if (!mapPromise) {
      return Promise.reject(new Error(`Map '${id}' not registered`));
    }

    return mapPromise as Promise<T>;
  }
}
