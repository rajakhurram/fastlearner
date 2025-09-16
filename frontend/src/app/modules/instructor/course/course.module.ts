import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CourseRoutingModule } from './course-routing.module';
import { CourseComponent } from './course.component';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import { CourseInformationComponent } from './course-information/course-information.component';
import { AddSectionComponent } from './add-section/add-section.component';
import { PreviewComponent } from './preview/preview.component';
import {CdkDragDrop, CdkDropList, CdkDrag, moveItemInArray, CdkDragHandle} from '@angular/cdk/drag-drop';
import { AngularEditorModule } from '@kolkov/angular-editor';


@NgModule({
  declarations: [
    CourseComponent,
    CourseInformationComponent,
    AddSectionComponent,
    PreviewComponent,
  ],
  imports: [
    CdkDropList,
    CdkDrag,
    CdkDragHandle,
    CommonModule,
    CourseRoutingModule,
    AntDesignModule,
    SharedModule,
    AngularEditorModule
  ], 
  providers: [
  ],
})
export class CourseModule { }
