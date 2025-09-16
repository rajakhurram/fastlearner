import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AssignCourseModalComponent } from './assign-course-modal.component';
import { NzModalService, NzModalRef } from 'ng-zorro-antd/modal';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AssignCourseModalComponent', () => {
  let component: AssignCourseModalComponent;
  let fixture: ComponentFixture<AssignCourseModalComponent>;
  let mockAffiliateService: jasmine.SpyObj<AffiliateService>;
  let mockMessageService: jasmine.SpyObj<MessageService>;
  let mockModalRef: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    mockAffiliateService = jasmine.createSpyObj('AffiliateService', [
      'assignAffiliateCourse',
    ]);
    mockMessageService = jasmine.createSpyObj('MessageService', ['error']);
    mockModalRef = jasmine.createSpyObj('NzModalRef', ['close']);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

    await TestBed.configureTestingModule({
      declarations: [AssignCourseModalComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AffiliateService, useValue: mockAffiliateService },
        { provide: MessageService, useValue: mockMessageService },
        { provide: NzModalRef, useValue: mockModalRef },
        { provide: NzModalService, useValue: modalServiceSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignCourseModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should close the modal on cancel', () => {
    component.cancel();
    expect(mockModalRef.close).toHaveBeenCalled();
  });

  it('should add an affiliate and update table data', () => {
    const affiliate = {
      affiliateName: 'Test Affiliate',
      url: '/test',
    };
    component.courseUrl = 'http://example.com';
    component.onAffiliateAdd(affiliate);

    expect(component.affiliateData.length).toBe(1);
    expect(component.tableConfig.rowData.length).toBe(1);
    expect(component.tableConfig.rowData[0].url).toBe(
      'http://example.com/test'
    );
  });

  it('should not add affiliate if affiliateName is missing', () => {
    const affiliate = {
      url: '/test',
    };
    component.onAffiliateAdd(affiliate);

    expect(component.affiliateData.length).toBe(0);
    expect(component.tableConfig.rowData.length).toBe(0);
  });

  it('should handle table delete action and update data', () => {
    const affiliate = {
      affiliateId: 1,
      affiliateName: 'Test Affiliate',
      url: '/test',
    };
    component.affiliateData = [affiliate];
    component.tableConfig.rowData = [affiliate];

    component.deleteElement(affiliate);

    expect(component.affiliateData.length).toBe(0);
    expect(component.tableConfig.rowData.length).toBe(0);
  });

  it('should assign affiliates and close modal on success', () => {
    const affiliate = {
      courseId: 1,
      affiliateId: 2,
      reward: 10,
    };
    component.tableConfig.rowData = [affiliate];
    mockAffiliateService.assignAffiliateCourse.and.returnValue(
      of({ status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE })
    );

    component.assignAffiliates();

    expect(mockAffiliateService.assignAffiliateCourse).toHaveBeenCalledWith([
      { courseId: 1, affiliateId: 2, reward: 10 },
    ]);
    expect(mockModalRef.close).toHaveBeenCalled();
  });

  it('should show an error message on assign affiliates failure', () => {
    const affiliate = {
      courseId: 1,
      affiliateId: 2,
      reward: 10,
    };
    component.tableConfig.rowData = [affiliate];
    mockAffiliateService.assignAffiliateCourse.and.returnValue(
      throwError({ error: { message: 'Error occurred' } })
    );

    component.assignAffiliates();

    expect(mockAffiliateService.assignAffiliateCourse).toHaveBeenCalled();
    expect(mockMessageService.error).toHaveBeenCalledWith('Error occurred');
  });

  it('should handle table actions correctly', () => {
    const affiliate = {
      affiliateId: 1,
      affiliateName: 'Test Affiliate',
    };

    const editEvent = { action: 'edit', row: affiliate, event: {} };
    component.handleTableAction(editEvent);
    // Expect nothing to happen as 'edit' action is not implemented

    const deleteEvent = {
      action: 'delete',
      row: affiliate,
      event: { stopPropagation: jasmine.createSpy() },
    };
    component.handleTableAction(deleteEvent);

    expect(deleteEvent.event.stopPropagation).toHaveBeenCalled();
  });
});
