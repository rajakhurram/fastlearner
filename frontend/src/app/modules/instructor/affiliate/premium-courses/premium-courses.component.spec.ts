import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PremiumCoursesComponent } from './premium-courses.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AffiliateService } from 'src/app/core/services/affiliate.service';
import { MessageService } from 'src/app/core/services/message.service';
import { of, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('PremiumCoursesComponent', () => {
  let component: PremiumCoursesComponent;
  let fixture: ComponentFixture<PremiumCoursesComponent>;
  let affiliateService: jasmine.SpyObj<AffiliateService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let modalService: jasmine.SpyObj<NzModalService>;

  beforeEach(async () => {
    const affiliateServiceSpy = jasmine.createSpyObj('AffiliateService', [
      'getAffiliates',
      'getPremiumCoursesByInstructor',
      'getPremiumCoursesAffiliates',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      declarations: [PremiumCoursesComponent],
      providers: [
        { provide: AffiliateService, useValue: affiliateServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    affiliateService = TestBed.inject(
      AffiliateService
    ) as jasmine.SpyObj<AffiliateService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;

    fixture = TestBed.createComponent(PremiumCoursesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize and fetch data on ngOnInit', () => {
    affiliateService.getPremiumCoursesByInstructor.and.returnValue(
      of({ status: 200, data: { content: [], totalElements: 0 } })
    );
    affiliateService.getAffiliates.and.returnValue(
      of({ status: 200, data: { affiliates: [] } })
    );

    component.ngOnInit();

    expect(affiliateService.getPremiumCoursesByInstructor).toHaveBeenCalledWith(
      component.instructorPremiumCoursesPayload
    );
    expect(affiliateService.getAffiliates).toHaveBeenCalledWith(
      component.payload
    );
  });

  it('should search premium courses based on the search term', () => {
    affiliateService.getPremiumCoursesByInstructor.and.returnValue(
      of({ status: 200, data: { content: [] } })
    );
    component.searchCallBack('test');

    expect(component.searchTerm).toBe('test');
    expect(affiliateService.getPremiumCoursesByInstructor).toHaveBeenCalled();
  });

  it('should copy URL to clipboard', () => {
    spyOn(document, 'execCommand').and.returnValue(true);
    const data = { url: 'sample-course' };

    component.copyURL(data);

    expect(messageService.success).toHaveBeenCalledWith(
      'URL copied to clipboard'
    );
  });

  it('should show an error if copy fails', () => {
    spyOn(document, 'execCommand').and.returnValue(false);
    const data = { url: 'sample-course' };

    component.copyURL(data);

    expect(messageService.error).toHaveBeenCalledWith(
      'Failed to copy URL to clipboard.'
    );
  });

  it('should fetch affiliates data', () => {
    affiliateService.getAffiliates.and.returnValue(
      of({ status: 200, data: { affiliates: [], totalElements: 0 } })
    );

    component.getAffiliateData(component.payload);

    expect(affiliateService.getAffiliates).toHaveBeenCalledWith(
      component.payload
    );
  });

  it('should expand all panels', () => {
    component.premiumCourses = [{ panelOpen: false }, { panelOpen: false }];

    component.expandCoursesPanel();

    component.premiumCourses.forEach((course) => {
      expect(course.panelOpen).toBeTrue();
    });
  });

  it('should collapse all panels', () => {
    component.premiumCourses = [{ panelOpen: true }, { panelOpen: true }];

    component.collapsePanel();

    component.premiumCourses.forEach((course) => {
      expect(course.panelOpen).toBeFalse();
    });
  });

  it('should handle table action for copy', () => {
    spyOn(component, 'copyURL');
    const event = {
      event: new Event('click'),
      action: 'copy',
      row: { url: 'sample-course' },
    };

    component.handleTableAction(event);

    expect(component.copyURL).toHaveBeenCalledWith(event.row);
  });

  it('should handle modal creation on assign affiliate', () => {
    const event = new Event('click');
    modalService.create.and.returnValue({ afterClose: of() } as any);

    component.assignAffliate(event, 1, 'sample-url');

    expect(modalService.create).toHaveBeenCalled();
  });
});
