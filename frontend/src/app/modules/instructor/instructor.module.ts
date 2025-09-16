import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InstructorRoutingModule } from './instructor-routing.module';
import { InstructorComponent } from './instructor.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../shared/shared.module';
import { InstructorProfileComponent } from './instructor-profile/instructor-profile.component';

@NgModule({
  declarations: [InstructorComponent, InstructorProfileComponent],

  imports: [
    CommonModule,
    InstructorRoutingModule,
    AntDesignModule,
    SharedModule,
  ],
})
export class InstructorModule {}
