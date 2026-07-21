import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthComponent } from './auth.component';
import { ForgetPasswordComponent } from './forget-password/forget-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SignInComponent } from './sign-in/sign-in.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { GetStartedComponent } from './get-started/get-started.component';

const routes: Routes = [
  {
    path: '',
    component: AuthComponent,
    children: [
      {
        path: 'sign-in',
        component: SignInComponent,
        data: { title: 'Sign In' },
      },
      {
        path: 'sign-up',
        component: SignUpComponent,
        data: { title: 'Sign Up' },
      },
      { path: 'get-started', component: GetStartedComponent },
      {
        path: 'forget-password',
        component: ForgetPasswordComponent,
        data: { title: 'Forget Password' },
      },
      {
        path: 'reset-password',
        component: ResetPasswordComponent,
        data: { title: 'Reset Password' },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {}
