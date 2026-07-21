import { Component, Input, OnInit } from '@angular/core';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';  // Import NzMessageService
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseReview } from 'src/app/core/models/course-review.model';
import { CourseService } from 'src/app/core/services/course.service';

@Component({
  selector: 'app-review-modal',
  templateUrl: './review-modal.component.html',
  styleUrls: ['./review-modal.component.scss'],
})
export class ReviewModalComponent implements OnInit {
  @Input() data?: any;
  @Input() title?: any;
  wordCount: number = 0;
  maxWordLimit: number = 499;

  courseReview: CourseReview = new CourseReview();
  _httpConstants: HttpConstants = new HttpConstants();
  component: { comment: string; value: any; };

  constructor(
    private _courseService: CourseService,
    private _messageService: NzMessageService,
    private _modal: NzModalService,
    private _modalRef: NzModalRef
  ) {}

  ngOnInit(): void {}

  checkWordCount() {
    this.wordCount = this.courseReview.comment
      ? this.courseReview.comment.trim().split(/\s+/).length
      : 0;
  }

  submitReview() {
    this.checkWordCount(); 
  
    if (!this.courseReview.value) {
      this._messageService.error('Please provide a rating before submitting your review.');
      return; 
    }
  
    if (!this.courseReview.comment) {
      this._messageService.error('Please provide a review comment before submitting.');
      return; 
    }
  
    if (this.wordCount > this.maxWordLimit) {
      this._messageService.error(
        `Please shorten your review. The maximum allowed word count is ${this.maxWordLimit}.`
      );
      return; 
    }
  
    this.courseReview.courseId = this.data;
  
    this._courseService.rateAndReviewCourse(this.courseReview).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._messageService.success('Review submitted successfully.');
          this._modalRef.close(response);
        }
      },
      error: (error: any) => {
        this._messageService.error(
          'There was an error submitting your review. Please try again.'
        );
      },
    });
  }
  
}
