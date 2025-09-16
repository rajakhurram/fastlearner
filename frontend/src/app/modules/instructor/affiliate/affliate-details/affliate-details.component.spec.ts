import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AffliateDetailsComponent } from './affliate-details.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AffliateDetailsComponent', () => {
  let component: AffliateDetailsComponent;
  let fixture: ComponentFixture<AffliateDetailsComponent>;
  let affiliateService: jasmine.SpyObj<AffiliateService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let modalService: jasmine.SpyObj<NzModalService>;

  beforeEach(async () => {
    const affiliateServiceSpy = jasmine.createSpyObj('AffiliateService', [
      'getAffiliate',
      'getCourseByAffiliate',
      'assignAffiliateCourse',
      'deleteAffiliateCourse',
      'deleteAffiliate',
      'getCoursesWithReward',
      'resendLink',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

    await TestBed.configureTestingModule({
      declarations: [AffliateDetailsComponent],
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [
        { provide: AffiliateService, useValue: affiliateServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '123' } } },
        },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AffliateDetailsComponent);
    component = fixture.componentInstance;
    affiliateService = TestBed.inject(
      AffiliateService
    ) as jasmine.SpyObj<AffiliateService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with affiliate data', () => {
    const mockAffiliate = { affiliateId: 123 };
    affiliateService.getAffiliate.and.returnValue(
      of({ status: 200, data: mockAffiliate })
    );
    affiliateService.getCourseByAffiliate.and.returnValue(
      of({ status: 200, data: { content: [] } })
    );
    affiliateService.getCoursesWithReward.and.returnValue(
      of({ status: 200, data: [] })
    );

    component.ngOnInit();

    expect(affiliateService.getAffiliate).toHaveBeenCalledWith({
      instructorAffliateId: 123,
    });
    expect(affiliateService.getCourseByAffiliate).toHaveBeenCalled();
    expect(affiliateService.getCoursesWithReward).toHaveBeenCalledWith({
      affiliateId: 123,
    });
  });

  it('should add a course and refresh affiliate courses', () => {
    const addedCourse = { courseId: 1 };
    affiliateService.assignAffiliateCourse.and.returnValue(of({ status: 200 }));
    spyOn(component, 'getCourseByAffiliate');

    component.onAddCourse(addedCourse);

    expect(affiliateService.assignAffiliateCourse).toHaveBeenCalledWith([
      { courseId: 1, affiliateId: undefined },
    ]);
    expect(component.getCourseByAffiliate).toHaveBeenCalled();
  });

  it('should open the delete modal for assigned course', () => {
    const mockData = { courseId: 1 };
    component.deleteAssignCourse(mockData);

    expect(modalService.create).toHaveBeenCalled();
  });

  it('should copy the URL to clipboard successfully', () => {
    spyOn(document, 'execCommand').and.returnValue(true);
    spyOn(document.body, 'appendChild');
    spyOn(document.body, 'removeChild');

    component.copyURL({ url: 'test-url' });

    expect(document.execCommand).toHaveBeenCalledWith('copy');
    expect(messageService.success).toHaveBeenCalledWith(
      'URL copied to clipboard'
    );
  });

  it('should show error message if no URL is provided', () => {
    component.copyURL({});
    expect(messageService.error).not.toHaveBeenCalled();
  });
  it('should resend link successfully', () => {
    affiliateService.resendLink.and.returnValue(of({ status: 200 }));
    spyOn(component, 'openSuccesModal');

    component.resendLink('test@example.com');

    expect(affiliateService.resendLink).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(component.openSuccesModal).toHaveBeenCalled();
  });
  it('should fetch courses and update tableConfig', () => {
    const mockResponse = {
      status: 200,
      data: {
        content: [{ courseTitle: 'Test' }],
        pageable: { pageNo: 0 },
        totalElements: 1,
      },
    };
    affiliateService.getCourseByAffiliate.and.returnValue(of(mockResponse));

    component.getCourseByAffiliate();

    expect(component.tableConfig.rowData).toEqual([
      { courseTitle: 'Test', showCopyIcon: true, showDeleteIcon: true },
    ]);
  });

  it('should handle error when fetching courses fails', () => {
    affiliateService.getCourseByAffiliate.and.returnValue(of({ status: 500 }));

    component.getCourseByAffiliate();

    expect(component.tableConfig.rowData).toEqual([]);
  });
});
