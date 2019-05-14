import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { UsermgmtRoutingModule } from './usermgmt-routing.module';
import { UserregistrationComponent } from './userregistration/userregistration.component';
import { MaterialModule } from '../../material.module';
import { RidverificationComponent } from './ridverification/ridverification.component';

@NgModule({
  imports: [
    CommonModule,
    UsermgmtRoutingModule,
    MaterialModule,
    ReactiveFormsModule
  ],
  declarations: [UserregistrationComponent, RidverificationComponent]
})
export class UsermgmtModule { }
