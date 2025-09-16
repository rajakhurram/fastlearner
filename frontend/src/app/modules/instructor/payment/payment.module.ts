import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaymentComponent } from './payment.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import { PaymentRoutingModule } from './payment-routing.module';



@NgModule({
  declarations: [
    PaymentComponent
  ],
  imports: [
    CommonModule,
    AntDesignModule,
    SharedModule,
    PaymentRoutingModule
  ]
})
export class PaymentModule { }
