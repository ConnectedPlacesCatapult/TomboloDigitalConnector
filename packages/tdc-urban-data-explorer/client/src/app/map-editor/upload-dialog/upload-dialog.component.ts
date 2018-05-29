import {MAT_DIALOG_DATA, MatDialogRef, MatHorizontalStepper, MatStep} from '@angular/material';
import {
  AfterViewInit,
  Component,
  EventEmitter,
  Inject,
  OnDestroy,
  OnInit,
  QueryList, ViewChild,
  ViewChildren
} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {UploadInput, UploadOutput} from 'ngx-uploader';
import {Subscription} from 'rxjs/Subscription';
import * as Debug from 'debug';
import {Subject} from 'rxjs/Subject';
import {IFileUpload} from '../../../../../src/shared/IFileUpload';

const debug = Debug('tombolo:upload-dialog');

export interface SubStep {
  text: string;
  status: 'pending' | 'inprogress' | 'done' | 'error';
}

export class UploadDialogContext  {

  constructor (private parentDialog: UploadDialogComponent) {}

  private _cancel$ = new Subject<number>();
  private _next$ = new Subject<number>();
  private _enter$ = new Subject<number>();

  uploadInput$: EventEmitter<UploadInput>;
  uploadOutput$: Observable<UploadOutput>;
  file: IFileUpload;
  dataset: object;
  openInMap: boolean;

  // Observable to signal page should cancel pending operations
  get cancel$(): Observable<number> {
    return this._cancel$.asObservable();
  }

  // Observable to signal page that next was pressed
  get next$(): Observable<number> {
    return this._next$.asObservable();
  }

  // Observable to signal page was entered
  get enter$(): Observable<number> {
    return this._enter$.asObservable();
  }

  setNextEnabled(page: number, enabled: boolean = true) {
    this.parentDialog.setNextEnabled(page, enabled);
  }

  // Notify pages to cancel pending operations
  cancel(pageIndex: number) {
    this._cancel$.next(pageIndex);
  }

  // Notify page that next was clicked
  next(pageIndex: number) {
    this._next$.next(pageIndex);
  }

  // Notify that page was entered
  enter(pageIndex: number) {
    this._enter$.next(pageIndex);
  }
}

@Component({
  selector: 'upload-dialog',
  templateUrl: './upload-dialog.html',
  styleUrls: ['./upload-dialog.scss']
})
export class UploadDialogComponent implements OnInit, OnDestroy, AfterViewInit {

  private _subs: Subscription[] = [];

  context: UploadDialogContext = new UploadDialogContext(this);

  @ViewChild(MatHorizontalStepper) private stepper: MatHorizontalStepper;
  @ViewChildren(MatStep) private pagesQuery: QueryList<MatStep>;
  pages: MatStep[];

  constructor(
    public dialogRef: MatDialogRef<UploadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data: any) {
    this.context.uploadInput$ = data.uploadInput$;
    this.context.uploadOutput$ = data.uploadOutput$;
  }

  ngOnInit() {
  }

  ngOnDestroy() {
  }

  ngAfterViewInit() {
    this.pages = this.pagesQuery.toArray();
  }

  setNextEnabled(index: number, enabled: boolean = true): void {
    if (this.pages) this.pages[index].completed = enabled;
  }

  pageCompleted(index: number): boolean {
    return (this.pages)? this.pages[index].completed : false;
  }

  cancel() {
    this.context.cancel(this.stepper.selectedIndex);
    this.dialogRef.close();
  }

  next() {
    this.context.next(this.stepper.selectedIndex);
    this.stepper.next();
    this.context.enter(this.stepper.selectedIndex);
  }

  finish() {
    this.context.next(this.stepper.selectedIndex);
    this.dialogRef.close(this.context);
  }
}
