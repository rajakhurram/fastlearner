import { Component, Input, OnInit } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { SectionReview } from 'src/app/core/models/section-review.model';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';

@Component({
  selector: 'app-rating-modal',
  templateUrl: './rating-modal.component.html',
  styleUrls: ['./rating-modal.component.scss'],
})
export class RatingModalComponent implements OnInit {
  @Input() data?: any;
  @Input() title?: any;

  sectionReview: SectionReview = new SectionReview();
  _httpConstants: HttpConstants = new HttpConstants();

  constructor(
    private _courseService: CourseService,
    private _messageService: MessageService,
    private _modal: NzModalService,
    private _sharedService: SharedService
  ) {}

  ngOnInit(): void {
    this.getSectionRatingAndReviews();
  }

  getSectionRatingAndReviews() {
    this._courseService
      .getSectionRatingAndReview(this.data?.sectionId)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.sectionReview = response?.data;
            this.sectionReview.comment = '';
          }
        },
        error: (error: any) => {},
      });
  }

  submitReview() {
    this.sectionReview.courseId = this.data?.courseId;
    this.sectionReview.sectionId = this.data?.sectionId;

    this._courseService.rateAndReviewSection(this.sectionReview).subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this._sharedService.updateSectionRatingAndReviews();
          this._modal.closeAll();
        }
      },
      error: (error: any) => {},
    });
  }
}
