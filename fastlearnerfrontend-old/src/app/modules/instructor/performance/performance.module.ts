import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

import { PerformanceRoutingModule } from './performance-routing.module';
import { PerformanceComponent } from './performance.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { HighchartsChartModule } from 'highcharts-angular';
import { SharedModule } from '../../shared/shared.module';


@NgModule({
  declarations: [
    PerformanceComponent
  ],
  imports: [
    CommonModule,
    PerformanceRoutingModule,
    AntDesignModule,
    HighchartsChartModule,
    SharedModule
  ],
  providers: [
    DatePipe,
  ],
})
export class PerformanceModule { }
