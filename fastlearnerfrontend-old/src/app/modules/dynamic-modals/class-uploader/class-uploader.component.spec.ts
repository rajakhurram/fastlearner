import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassUploaderComponent } from './class-uploader.component';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { AiGraderService } from 'src/app/core/services/ai-grader.service';

describe('ClassUploaderComponent', () => {
  let component: ClassUploaderComponent;
  let fixture: ComponentFixture<ClassUploaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClassUploaderComponent ],
      imports: [],
      providers: [
        {
          provide: NzModalRef,
          useValue: {},
        },
        {
          provide: AiGraderService,
          useValue: {},
        },
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
