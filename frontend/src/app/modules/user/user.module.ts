import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserRoutingModule } from './user-routing.module';
import { UserComponent } from './user.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { ChangeUserPasswordComponent } from './change-user-password/change-user-password.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { EditorModule, TINYMCE_SCRIPT_SRC } from '@tinymce/tinymce-angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { StudentNotificationsComponent } from './student-notifications/student-notifications.component';
import { NotificationsModule } from '../instructor/notifications/notifications.module';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { UserProfileViewComponent } from './user-profile-view/user-profile-view.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    UserComponent,
    UserProfileComponent,
    ChangeUserPasswordComponent,
    StudentNotificationsComponent,
    UserProfileViewComponent,
  ],
  imports: [
    NotificationsModule,
    CommonModule,
    UserRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    AntDesignModule,
    EditorModule,
    AngularEditorModule,
    SharedModule,
  ],
  exports: [UserProfileViewComponent],
  providers: [],
})
export class UserModule {}
