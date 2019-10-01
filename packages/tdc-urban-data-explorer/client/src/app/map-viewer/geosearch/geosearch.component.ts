import {Component, OnInit, Output, EventEmitter, ViewChild, ElementRef, OnDestroy} from '@angular/core';
import { Observable }        from 'rxjs/Observable';
import { Subject }           from 'rxjs/Subject';
import {GeosearchItem, GeosearchService} from './geosearch.service';

// Observable class extensions
import 'rxjs/add/observable/of';

// Observable operators
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import {Subscription} from 'rxjs/Subscription';
import {Angulartics2} from "angulartics2";

@Component({
  selector: 'geosearch-component',
  templateUrl: './geosearch.component.html',
  styleUrls: ['./geosearch.component.scss']
})

export class GeosearchComponent implements OnInit, OnDestroy {

  private searchTerms$ = new Subject<string>();
  private _subs: Subscription[] = [];

  places: GeosearchItem[] = [];
  found = true;

  @ViewChild('searchBox') searchBox: ElementRef;
  @Output() selectItem: EventEmitter<GeosearchItem> = new EventEmitter<GeosearchItem>();

  constructor(private geosearchService: GeosearchService, private angulartics2: Angulartics2) { }

  ngOnInit(): void {
    this.searchBox.nativeElement.blur();
    this._subs.push(this.geosearchService.search(this.searchTerms$).subscribe(places => {
      this.places = places;
      this.found = places.length > 0;
    }));
  }

  ngOnDestroy(): void {
    this._subs.forEach(sub => sub.unsubscribe());
  }

  clear() {
    this.places = [];
    this.found = true;
  }

  search(term: string): void {
    this.searchTerms$.next(term);

    this.angulartics2.eventTrack.next({
      action: 'SearchLocation',
      properties: { category: 'Geosearch', label: term }
    });
  }

  locationClick(item: GeosearchItem): void {
    if (item.boundingBox) this.selectItem.emit(item);

    this.angulartics2.eventTrack.next({
      action: 'ClickLocation',
      properties: { category: 'Geosearch', label: item.displayName }
    });

    this.clear();
  }
}
