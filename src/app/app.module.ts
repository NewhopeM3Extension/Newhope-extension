import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { M3OdinModule } from '@infor-up/m3-odin-angular';
import { MaterialModule } from './material/material.module'
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-router.module';

@NgModule({
   declarations: [
      AppComponent
   ],
   imports: [
      BrowserModule,
      RouterModule,
      BrowserAnimationsModule,
      FormsModule,
      M3OdinModule,
      AppRoutingModule,
      MaterialModule,

   ],
   exports:[
      RouterModule
   ],
   providers: [],
   bootstrap: [AppComponent]
})
export class AppModule { }
