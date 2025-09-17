import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingPageComponent } from './modules/pages/landing-page/landing-page.component';
import { AboutUsComponent } from './modules/pages/about-us/about-us.component';
import { ContactUsComponent } from './modules/pages/contact-us/contact-us.component';
import { AuthGuard } from './core/guards/auth.guard';
import { BecomeInstructorComponent } from './modules/pages/become-instructor/become-instructor.component';

import { MaintenancePageComponent } from './modules/pages/maintenance-page/maintenance-page.component';
import { PaymentConfirmationComponent } from './modules/auth/payment-confirmation/payment-confirmation.component';
import { PrivacyPolicyComponent } from './modules/auth/privacy-policy/privacy-policy.component';
import { SubscriptionPlanComponent } from './modules/auth/subscription-plan/subscription-plan.component';
import { SubscriptionComponent } from './modules/auth/subscription/subscription.component';
import { TermsAndConditionsComponent } from './modules/auth/terms-and-conditions/terms-and-conditions.component';
import { TransactionInvoiceComponent } from './modules/auth/transaction-invoice/transaction-invoice.component';
import { WelcomePageInstructorComponent } from './modules/auth/welcome-page-instructor/welcome-page-instructor.component';
import { WithdrawalSubscriptionComponent } from './modules/auth/withdrawal-subscription/withdrawal-subscription.component';
import { SubscriptionGuard } from './core/guards/subscription.guard';
import { PremiumStudentComponent } from './modules/instructor/premium-student/premium-student.component';
import { FastlearnerLoginComponent } from './fastlearner-login/fastlearner-login.component';
import { ContentTypeComponent } from './modules/auth/content-type/content-type.component';
import { PaymentMethodComponent } from './modules/auth/payment-method/payment-method.component';
const routes: Routes = [
  {
    path: '',
    component: LandingPageComponent,
  },
  {
    path: 'fl-login',
    component: FastlearnerLoginComponent,
    data: { title: 'Fast Learner Sign In' },
  },
  {
    path: 'about-us',
    component: AboutUsComponent,
    data: { title: 'About Us' },
  },
  {
    path: 'contact-us',
    component: ContactUsComponent,
    data: { title: 'Contact Us' },
  },
  {
    path: 'become-instructor',
    component: BecomeInstructorComponent,
    data: { title: 'Become an Instructor' },
  },
  {
    path: 'payment-method',
    component: PaymentMethodComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'transaction-invoice',
    component: TransactionInvoiceComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'subscription',
    component: SubscriptionComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'subscription-plan',
    component: SubscriptionPlanComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'payment-confirmation',
    component: PaymentConfirmationComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'withdrawal-subscription',
    component: WithdrawalSubscriptionComponent,
    data: { title: 'Subscription' },
    canActivate: [SubscriptionGuard],
  },
  {
    path: 'privacy-policy',
    component: PrivacyPolicyComponent,
    data: { title: 'Privacy Policy' },
  },
  {
    path: 'terms-and-conditions',
    component: TermsAndConditionsComponent,
    data: { title: 'Terms & Conditions' },
  },
  {
    path: 'welcome-instructor',
    component: WelcomePageInstructorComponent,
    data: { title: 'Instructor' },
  },

  {
    path: 'content-type',
    component: ContentTypeComponent,
    data: { title: 'Instructor' },
  },

  {
    path: 'auth',
    loadChildren: () =>
      import('./modules/auth/auth.module').then((m) => m.AuthModule),
    canActivate: [AuthGuard], // Protect auth routes
    canLoad: [AuthGuard],
  },
  {
    path: 'instructor',
    loadChildren: () =>
      import('./modules/instructor/instructor.module').then(
        (m) => m.InstructorModule
      ),
    canLoad: [AuthGuard],
  },
  {
    path: 'student',
    loadChildren: () =>
      import('./modules/student/student.module').then((m) => m.StudentModule),
    canLoad: [AuthGuard],
  },
  {
    path: 'user',
    loadChildren: () =>
      import('./modules/user/user.module').then((m) => m.UserModule),
    canLoad: [AuthGuard],
  },
  {
    path: 'maintenance',
    component: MaintenancePageComponent,
  },
  {
    path: 'premium-student',
    component: PremiumStudentComponent,
  },
  {
    path: '**',
    redirectTo: '',
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      useHash: true,
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
