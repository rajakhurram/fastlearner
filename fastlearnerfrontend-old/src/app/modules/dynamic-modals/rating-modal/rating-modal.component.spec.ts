import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RatingModalComponent } from './rating-modal.component';
import { CourseService } from 'src/app/core/services/course.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SectionReview } from 'src/app/core/models/section-review.model';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';

describe('RatingModalComponent', () => {
  let component: RatingModalComponent;
  let fixture: ComponentFixture<RatingModalComponent>;
  let courseService: jasmine.SpyObj<CourseService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  const mockHttpConstants = new HttpConstants();

  beforeEach(async () => {
    const courseServiceSpy = jasmine.createSpyObj('CourseService', [
      'getSectionRatingAndReview',
      'rateAndReviewSection',
    ]);
    const sharedServiceSpy = jasmine.createSpyObj('SharedService', [
      'updateSectionRatingAndReviews',
    ]);
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', [
      'closeAll',
    ]);

    await TestBed.configureTestingModule({
      declarations: [RatingModalComponent],
      providers: [
        { provide: CourseService, useValue: courseServiceSpy },
        { provide: SharedService, useValue: sharedServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        NzMessageService,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(RatingModalComponent);
    component = fixture.componentInstance;
    courseService = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    sharedService = TestBed.inject(
      SharedService
    ) as jasmine.SpyObj<SharedService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getSectionRatingAndReviews on init', () => {
    const getSectionRatingAndReviewSpy =
      courseService.getSectionRatingAndReview.and.returnValue(
        of({
          status: mockHttpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
          data: new SectionReview(),
        })
      );

    component.ngOnInit();

    expect(getSectionRatingAndReviewSpy).toHaveBeenCalled();
  });

  it('should set sectionReview on successful data fetch', () => {
    const mockResponse = {
      status: mockHttpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: {
        comment: 'Great section!',
        value: '',

        courseId: '',
        totalReviews: '',
        sectionId: '',
      },
    };
    courseService.getSectionRatingAndReview.and.returnValue(of(mockResponse));

    component.ngOnInit();

    expect(component.sectionReview).toEqual(mockResponse.data);
    expect(component.sectionReview.comment).toBe('');
  });

  it('should handle error in getSectionRatingAndReviews', () => {
    courseService.getSectionRatingAndReview.and.returnValue(
      throwError('Error')
    );

    component.ngOnInit();

    // Add any necessary expectations for error handling
  });

  it('should call submitReview and handle success', () => {
    component.data = { courseId: 1, sectionId: 2 };
    component.sectionReview = { comment: 'Excellent!' } as SectionReview;

    courseService.rateAndReviewSection.and.returnValue(
      of({ status: mockHttpConstants.REQUEST_STATUS.SUCCESS_200.CODE })
    );

    component.submitReview();

    expect(courseService.rateAndReviewSection).toHaveBeenCalledWith(
      component.sectionReview
    );
    expect(sharedService.updateSectionRatingAndReviews).toHaveBeenCalled();
    expect(modalService.closeAll).toHaveBeenCalled();
  });

  it('should handle error in submitReview', () => {
    component.data = { courseId: 1, sectionId: 2 };
    component.sectionReview = { comment: 'Not good!' } as SectionReview;

    courseService.rateAndReviewSection.and.returnValue(
      throwError('Submission Error')
    );

    component.submitReview();

  });

});
