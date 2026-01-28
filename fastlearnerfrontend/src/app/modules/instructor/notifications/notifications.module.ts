import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificationsRoutingModule } from './notifications-routing.module';
import { NotificationsComponent } from './notifications.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';


@NgModule({
  declarations: [
    NotificationsComponent
  ],
  imports: [
    CommonModule,
    NotificationsRoutingModule,
    AntDesignModule,
    SharedModule
  ],
  exports :[
    NotificationsComponent
  ]
})
export class NotificationsModule { }
  