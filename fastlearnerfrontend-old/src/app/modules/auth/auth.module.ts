import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
import { SignInComponent } from './sign-in/sign-in.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { ForgetPasswordComponent } from './forget-password/forget-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SubscriptionPlanComponent } from './subscription-plan/subscription-plan.component';
import { SubscriptionComponent } from './subscription/subscription.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { GoogleSigninButtonModule } from '@abacritt/angularx-social-login';
import { PrivacyPolicyComponent } from './privacy-policy/privacy-policy.component';
import { WelcomePageInstructorComponent } from './welcome-page-instructor/welcome-page-instructor.component';
import { GetStartedComponent } from './get-started/get-started.component';
import { PaymentConfirmationComponent } from './payment-confirmation/payment-confirmation.component';
import { WithdrawalSubscriptionComponent } from './withdrawal-subscription/withdrawal-subscription.component';
import { LottieModule } from 'ngx-lottie';
import player from 'lottie-web';
import { TransactionInvoiceComponent } from './transaction-invoice/transaction-invoice.component';
import { TermsAndConditionsComponent } from './terms-and-conditions/terms-and-conditions.component';
import { BecomeInstructorComponent } from '../pages/become-instructor/become-instructor.component';
import { ContentTypeComponent } from './content-type/content-type.component';
import { PaymentMethodComponent } from './payment-method/payment-method.component';


export function playerFactory() {
  return player;
}

@NgModule({
  declarations: [
    AuthComponent,
    SignInComponent,
    SignUpComponent,
    ForgetPasswordComponent,
    ResetPasswordComponent,
    SubscriptionPlanComponent,
    SubscriptionComponent,
    PrivacyPolicyComponent,
    WelcomePageInstructorComponent,
    GetStartedComponent,
    PaymentConfirmationComponent,
    WithdrawalSubscriptionComponent,
    PaymentMethodComponent,
    TransactionInvoiceComponent,
    TermsAndConditionsComponent,
    BecomeInstructorComponent,
    ContentTypeComponent,
  ],
  imports: [
    CommonModule,
    AuthRoutingModule,
    AntDesignModule,
    GoogleSigninButtonModule,
    LottieModule.forRoot({ player: playerFactory })
  ],
})
export class AuthModule {}
