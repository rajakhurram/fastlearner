import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminComponent } from './admin.component';
import { UsersComponent } from './users/users.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../shared/shared.module';
import { SubscriptionComponent } from './subscription/subscription.component';
import { CoursesComponent } from './courses/courses.component';
import { EnrollmentsComponent } from './enrollments/enrollments.component';
import { PaymentsComponent } from './payments/payments.component';
import { PromocodeComponent } from './promocode/promocode.component';
import { PayoutsComponent } from './payouts/payouts.component';
import { SettingsComponent } from './settings/settings.component';
import { InvoicesComponent } from './invoices/invoices.component';
import { AdminService } from './admin.service';

@NgModule({
  declarations: [
    AdminComponent,
    UsersComponent,
    SubscriptionComponent,
    CoursesComponent,
    EnrollmentsComponent,
    PaymentsComponent,
    PromocodeComponent,
    PayoutsComponent,
    SettingsComponent,
    InvoicesComponent,
  ],
  imports: [CommonModule, AdminRoutingModule, AntDesignModule, SharedModule],
  providers: [AdminService],
})
export class AdminModule {}
