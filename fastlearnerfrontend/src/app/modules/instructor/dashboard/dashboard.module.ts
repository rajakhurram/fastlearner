import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardComponent } from './dashboard.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    DashboardComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    DashboardRoutingModule,
    AntDesignModule,
  ]
})
export class DashboardModule { }
