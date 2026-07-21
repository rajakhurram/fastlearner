import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminComponent } from './admin.component';
import { UsersComponent } from './users/users.component';
import { CoursesComponent } from './courses/courses.component';
import { PaymentsComponent } from './payments/payments.component';
import { SettingsComponent } from './settings/settings.component';
import { EnrollmentsComponent } from './enrollments/enrollments.component';
import { PromocodeComponent } from './promocode/promocode.component';
import { PayoutsComponent } from './payouts/payouts.component';
import { SubscriptionComponent } from './subscription/subscription.component';
import { InvoicesComponent } from './invoices/invoices.component';

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    children: [
      { path: '', redirectTo: 'users', pathMatch: 'full' },
      { path: 'users', component: UsersComponent, data: { title: 'Users' } },
      {
        path: 'courses',
        component: CoursesComponent,
        data: { title: 'Courses' },
      },
      {
        path: 'payments',
        component: PaymentsComponent,
        data: { title: 'Payments' },
      },
      {
        path: 'settings',
        component: SettingsComponent,
        data: { title: 'Settings' },
      },
      {
        path: 'enrollments',
        component: EnrollmentsComponent,
        data: { title: 'Enrollments' },
      },
      {
        path: 'promo-codes',
        component: PromocodeComponent,
        data: { title: 'Promo Codes' },
      },
      {
        path: 'payouts',
        component: PayoutsComponent,
        data: { title: 'Payouts' },
      },
      {
        path: 'subscription',
        component: SubscriptionComponent,
        data: { title: 'Subscription' },
      },
      {
        path: 'invoices/:subscriptionId',
        component: InvoicesComponent,
        data: { title: 'Invoices' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
