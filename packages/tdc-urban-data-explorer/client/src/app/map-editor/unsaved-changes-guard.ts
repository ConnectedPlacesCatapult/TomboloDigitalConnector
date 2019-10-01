import {Injectable} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot} from '@angular/router';
import {DialogsService} from "../dialogs/dialogs.service";
import {MapEditorComponent} from "./map-editor.component";
import {MapRegistry} from '../mapbox/map-registry.service';
import {TomboloMapboxMap} from '../mapbox/tombolo-mapbox-map';


@Injectable()
export class EditorDeactivateGuard implements CanDeactivate<MapEditorComponent> {

  constructor(private dialogsService: DialogsService, private mapRegistry: MapRegistry) {
  }

  canDeactivate(): Promise<boolean> {

    return this.mapRegistry.getMap<TomboloMapboxMap>('main-map').then(map => {
      if (map.modified) {
        return this.dialogsService.confirm('Unsaved Changes',
          'You have unsaved changes to your map.<p>Are you sure you want to navigate away?').toPromise();
      }
      else {
        return Promise.resolve(true);
      }
    });
  }
}
