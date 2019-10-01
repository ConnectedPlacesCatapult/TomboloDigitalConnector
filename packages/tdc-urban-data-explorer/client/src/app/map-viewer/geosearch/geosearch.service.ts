import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import * as Debug from 'debug';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';
import {APP_CONFIG, AppConfig} from '../../config.service';

const debug = Debug('tombolo:maps-demo');

export interface GeosearchItem {
  displayName: string;
  boundingBox?: number[][];
}

@Injectable()
export class GeosearchService {

  constructor(
    private http: HttpClient,
    @Inject(APP_CONFIG) private config: AppConfig) {}

  search(searchTerms: Subject<string>): Observable<GeosearchItem[]> {
    return searchTerms
      .debounceTime(300)
      .switchMap(term => term
        ? this.geolookup(term)
        : Observable.of<GeosearchItem[]>([]))
      .catch(error => {
        debug(error);
        return Observable.of<GeosearchItem[]>([]);
      });
  }

  private geolookup(term: string): Observable<GeosearchItem[]> {
    return this.http.get<any[]>(`${this.config.nominatimUrl}/search?q=${term}&format=json&countrycodes=GB`)
      .map(response => response.map(this.objectToGeosearchItem));
  }

  private objectToGeosearchItem(obj): GeosearchItem {
    return {
      displayName: obj.display_name,
      boundingBox: [[+obj.boundingbox[2], +obj.boundingbox[0]], [+obj.boundingbox[3], +obj.boundingbox[1]]]
    };
  }
}
