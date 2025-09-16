import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserComponent } from './user.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { ChangeUserPasswordComponent } from './change-user-password/change-user-password.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { StudentNotificationsComponent } from './student-notifications/student-notifications.component';
import { UserProfileViewComponent } from './user-profile-view/user-profile-view.component';

const routes: Routes = [
  { path: '', component: UserComponent,
    children : [
      { path: 'update-profile', component: UserProfileComponent, canActivate: [AuthGuard], data: { title: 'Update Profile' }},
      { path: 'change-password', component: ChangeUserPasswordComponent, data: { title: 'Change Password' }},
      { path: 'notifications', component: StudentNotificationsComponent, data: { title: 'Notifications' }},
      { path: 'profile', component: UserProfileViewComponent, data: { title: 'User Profile' }}
    ]
  }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
