import { NgModule } from '@angular/core';

import {CdkTableModule} from "@angular/cdk/table";

import {
  MatButtonModule,
  MatCheckboxModule,
  MatInputModule,
  MatIconModule,
  MatMenuModule,
  MatListModule,
  MatSidenavModule,
  MatRadioModule,
  MatProgressSpinnerModule,
  MatDialogModule,
  MatGridListModule,
  MatSnackBarModule,
  MatTabsModule,
  MatTooltipModule,
  MatSelectModule,
  MatTableModule,
  MatProgressBarModule,
  MatSliderModule,
  MatStepperModule,
  MatExpansionModule,
  MatAutocompleteModule,
  MatButtonToggleModule
} from '@angular/material';

const materialModules = [
  MatButtonModule,
  MatCheckboxModule,
  MatInputModule,
  MatMenuModule,
  MatListModule,
  MatIconModule,
  MatSidenavModule,
  MatRadioModule,
  MatProgressSpinnerModule,
  MatProgressBarModule,
  MatDialogModule,
  MatGridListModule,
  MatSnackBarModule,
  MatTabsModule,
  MatTooltipModule,
  MatSelectModule,
  MatTableModule,
  CdkTableModule,
  MatSliderModule,
  MatStepperModule,
  MatExpansionModule,
  MatAutocompleteModule,
  MatButtonToggleModule
];

@NgModule({
  imports: materialModules,
  exports: materialModules,
})
export class MaterialModule { }
