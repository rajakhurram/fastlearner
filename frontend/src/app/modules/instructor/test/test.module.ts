import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TestRoutingModule } from './test-routing.module';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { SharedModule } from '../../shared/shared.module';
import { CdkDropList, CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { TestInformationComponent } from './test-information/test-information.component';
import { TestAddSectionComponent } from './test-add-section/test-add-section.component';
import { TestPreviewComponent } from './test-preview/test-preview.component';
import { TestComponent } from './test.component';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { FormsModule } from '@angular/forms';
import { InputComponent } from '../../shared/input/input.component';

@NgModule({
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [
    TestComponent,
    TestInformationComponent,
    TestAddSectionComponent,
    TestPreviewComponent,
  ],
  imports: [
    CdkDropList,
    CdkDrag,
    CdkDragHandle,
    CommonModule,
    TestRoutingModule,
    AntDesignModule,
    SharedModule,
    AngularEditorModule,
    NzTagModule,
    NzIconModule,
    FormsModule,
  ],
  providers: [],
})
export class TestModule {}
