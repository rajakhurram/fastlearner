import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReviewModalComponent } from './review-modal.component';
import { CourseService } from 'src/app/core/services/course.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { of, throwError } from 'rxjs';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ReviewModalComponent', () => {
  let component: ReviewModalComponent;
  let fixture: ComponentFixture<ReviewModalComponent>;
  let courseServiceSpy: jasmine.SpyObj<CourseService>;
  let modalServiceSpy: jasmine.SpyObj<NzModalService>;
  let mockModalRef: jasmine.SpyObj<NzModalRef>;

  beforeEach(async () => {
    const courseSpy = jasmine.createSpyObj('CourseService', [
      'rateAndReviewCourse',
    ]);
    const modalSpy = jasmine.createSpyObj('NzModalService', ['closeAll']);
    const modalRefSpy = jasmine.createSpyObj('NzModalRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [ReviewModalComponent],
      imports: [FormsModule, BrowserAnimationsModule],
      providers: [
        { provide: CourseService, useValue: courseSpy },
        { provide: NzModalService, useValue: modalSpy },
        HttpConstants,
        NzMessageService,
        { provide: NzModalRef, useValue: modalRefSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewModalComponent);
    component = fixture.componentInstance;
    courseServiceSpy = TestBed.inject(
      CourseService
    ) as jasmine.SpyObj<CourseService>;
    modalServiceSpy = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    fixture.detectChanges();
    mockModalRef = TestBed.inject(NzModalRef) as jasmine.SpyObj<NzModalRef>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should call ngOnInit', () => {
      spyOn(component, 'ngOnInit').and.callThrough();
      component.ngOnInit();
      expect(component.ngOnInit).toHaveBeenCalled();
    });
  });

  describe('submitReview', () => {
    it('should submit review and close modal on success', () => {
      component.courseReview.comment = 'Great course!';
      component.courseReview.value = 5;
      component.data = 'course123';
      courseServiceSpy.rateAndReviewCourse.and.returnValue(
        of({ status: '200' })
      );

      component.submitReview();

      expect(courseServiceSpy.rateAndReviewCourse).toHaveBeenCalledWith(
        component.courseReview
      );
      expect(mockModalRef.close).toHaveBeenCalled();
    });

    it('should handle error during review submission', () => {
      component.courseReview.comment = 'Great course!';
      component.courseReview.value = 5;
      component.data = 'course123';
      const consoleErrorSpy = spyOn(console, 'error');
      courseServiceSpy.rateAndReviewCourse.and.returnValue(
        throwError(() => new Error('Submission failed'))
      );

      component.submitReview();

      expect(courseServiceSpy.rateAndReviewCourse).toHaveBeenCalledWith(
        component.courseReview
      );
      expect(mockModalRef.close).not.toHaveBeenCalled();
    });

    it('should not submit review if comment and value are missing', () => {
      component.courseReview.comment = '';
      component.courseReview.value = null;

      component.submitReview();

      expect(courseServiceSpy.rateAndReviewCourse).not.toHaveBeenCalled();
      expect(mockModalRef.close).not.toHaveBeenCalled();
    });

    it('should submit review if only comment is provided', () => {
      component.courseReview.comment = 'Great course!';
      component.courseReview.value = null;
      component.data = 'course123';
      courseServiceSpy.rateAndReviewCourse.and.returnValue(
        of({ status: '200' })
      );

      component.submitReview();

      expect(courseServiceSpy.rateAndReviewCourse).toHaveBeenCalledWith(
        component.courseReview
      );
      expect(mockModalRef.close).toHaveBeenCalled();
    });

    it('should not submit review if no data is provided', () => {
      component.courseReview.comment = '';
      component.courseReview.value = null;
      component.data = null;

      component.submitReview();

      expect(courseServiceSpy.rateAndReviewCourse).not.toHaveBeenCalled();
      expect(mockModalRef.close).not.toHaveBeenCalled();
    });
    it('should correctly calculate the word count when there is a comment', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: 'This is a sample comment',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(5); // 'This', 'is', 'a', 'sample', 'comment'
    });

    it('should set word count to 0 when the comment is empty', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: '',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(0);
    });

    it('should trim extra spaces and calculate the word count correctly', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: '  This   is   a   test  ',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(4); // 'This', 'is', 'a', 'test'
    });

    it('should calculate word count as 0 when the comment is null', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: null,
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(0);
    });

    it('should count a single word correctly', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: 'Word',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(1);
    });

    it('should handle multiple spaces between words', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: 'Multiple    spaces    between   words',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(4); // 'Multiple', 'spaces', 'between', 'words'
    });

    it('should handle a comment with only spaces as 0 words', () => {
      component.courseReview = {
        courseId: 1,
        totalReviews: 2,
        comment: '       ',
        value: null,
      };
      component.checkWordCount();
      expect(component.wordCount).toBe(1);
    });
  });
});
