import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestInformationComponent } from './test-information.component';
import { TestInformationDropdownsService } from 'src/app/core/services/test-information-dropdowns.service';
import { CourseService } from 'src/app/core/services/course.service';
import { ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NzIconModule, NzIconService } from 'ng-zorro-antd/icon';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { SharedModule } from 'src/app/modules/shared/shared.module';
import { FormsModule } from '@angular/forms';
import { CdkDropList, CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { AntDesignModule } from 'src/app/ui-library/ant-design/ant-design.module';
import { TestRoutingModule } from '../test-routing.module';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('TestInformationComponent', () => {
  let component: TestInformationComponent;
  let fixture: ComponentFixture<TestInformationComponent>;
  let testInformationDropdownServiceSpy: jasmine.SpyObj<TestInformationDropdownsService>;
  let nzIconServiceSpy: jasmine.SpyObj<NzIconService>;

  beforeEach(async () => {
    testInformationDropdownServiceSpy = jasmine.createSpyObj<TestInformationDropdownsService>('TestInformationDropdownsService', ['getDropdownData']);
    nzIconServiceSpy = jasmine.createSpyObj<NzIconService>('NzIconService', ['fetchFromIconfont', 'addIcon']);
    testInformationDropdownServiceSpy.getDropdownData.and.returnValues(of({
      categories: [],
      levels: []
    }));

    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [TestInformationComponent],
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
        BrowserAnimationsModule
      ],
      providers: [
        { provide: TestInformationDropdownsService, useValue: testInformationDropdownServiceSpy },
        { provide: CourseService, useValue: {} },
        { provide: NzIconService, useValue: nzIconServiceSpy },
        { provide: ChangeDetectorRef, useValue: {} },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
