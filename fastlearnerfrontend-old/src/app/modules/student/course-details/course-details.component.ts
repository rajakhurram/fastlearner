import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { CourseDetails } from 'src/app/core/models/course-details';
import { CourseReview } from 'src/app/core/models/course-review.model';
import { RelatedCourse } from 'src/app/core/models/related-courses.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { environment } from 'src/environments/environment.development';
import { ShareModalComponent } from '../../dynamic-modals/share-modal/share-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { SharedService } from 'src/app/core/services/shared.service';
import { Subscription } from 'rxjs';
import { ViewportScroller } from '@angular/common';
import { CacheService } from 'src/app/core/services/cache.service';
import { Meta, Title } from '@angular/platform-browser';
import { CourseType } from 'src/app/core/enums/course-status';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';

@Component({
  selector: 'app-course-details',
  templateUrl: './course-details.component.html',
  styleUrls: ['./course-details.component.scss'],
})
export class CourseDetailsComponent
  implements OnInit, OnDestroy, AfterViewInit
{
  getCourseByTitle(getCourseByTitle: any) {
    throw new Error('Method not implemented.');
  }
  imageUrl = environment.imageUrl;
  courseDetails!: CourseDetails;
  relatedCourse: RelatedCourse = new RelatedCourse();
  _httpConstants: HttpConstants = new HttpConstants();
  courseReview: CourseReview;
  courseId: any;
  courseTitle: any;
  courseType = CourseType;
  courseUrl: any;
  testTotalQuestions: any = 0;
  isLoading: boolean = true; // Add a loading flag
  isLoggedIn: any;
  courseButtonName: string = 'Start Now';
  expandButtonName: string = 'Expand All Sections';
  expand: boolean = false;
  totalSections: any = 0;
  totalTopics: any = 0;
  totalSectionTimeInSeconds: any = 0;
  showDescription: boolean = false;
  isInView: boolean = false;
  showBuyButton: boolean = false;
  likeCIP: boolean = false;
  courseList: Array<any> = [];
  noOfCount: any;
  showMore?: boolean = false;
  courseOutcomeLength?: number;
  cousrseOutcomeDefaultLength?: number = 4;
  routeSubscription: Subscription;
  fullWidth: boolean = true;
  @ViewChild('courseContent') specificDiv: ElementRef;
  @ViewChild('videoPlayerElement', { static: true }) videoPlayerElement: any;
  isTooltipVisible: boolean = true;
  currentSelectedTopic?: any;
  affiliateUUID?: string;
  applicationCourseDetailsUrl?: string;
  activeCourseUrl?: string;
  currentPage = 0; 
  hasMoreComments = true; 
  isLoadingComments = false; 
  pageSize = 15;
  isFirstLoad = true;
  sectionPanel = [
    {
      active: true,
      name: '',
      topicDetails: [
        {
          topicName: '',
          topicType: '',
          topicDuration: '',
        },
      ],
    },
  ];
  courseContentType = CourseContentType;

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth > 768) {
      this.fullWidth = true;
    } else {
      this.fullWidth = false;
    }
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkTooltipVisibility();
  }

  cardElement!: HTMLElement;
  footerElement!: HTMLElement;
  mainContentElement!: HTMLElement;

  constructor(
    private _courseService: CourseService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _messageService: MessageService,
    private _authService: AuthService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _sharedService: SharedService,
    private route: ActivatedRoute,
    private elementRef: ElementRef,
    private viewportScroller: ViewportScroller,
    private _cacheService: CacheService,
    private el: ElementRef,
    private renderer: Renderer2,
    private metaService: Meta,
    private titleService: Title,
    private loader: NgxUiLoaderService
  ) {
    this.applicationCourseDetailsUrl = environment.applicationCourseDetailsUrl;
  }

  ngOnInit(): void {
    this._activatedRoute.queryParamMap.subscribe(
      (queryParams: ParamMap) => {
        this.affiliateUUID = queryParams.get('affiliate');
        if(this.affiliateUUID){
        this._cacheService.saveInCache('affiliate', this.affiliateUUID);
        }
      }
    );

    this.routeSubscription = this._activatedRoute.paramMap.subscribe(
      (params: ParamMap) => {
        this.showBuyButton = false;
        this.courseUrl = params.get('courseUrl');
        if (this.courseUrl) {
          this.getCourseByUrl(this.courseUrl);
        } else {
          // If no courseUrl is provided, navigate to the home page
          this._router.navigate(['']);
        }
      }
    );
  }

  getCourseByUrl(courseUrl?: any) {
    this.isLoading = true;
    this._courseService?.getCourseByUrl(courseUrl)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE &&
          response?.data?.course
        ) {
          this.isLoading = false;  
          this.courseId = response.data?.course?.id;
          this.courseTitle = response?.data?.course?.title;
          this.activeCourseUrl = response?.data?.activeUrl;
          this.titleService.setTitle(
            `${this.courseTitle?.replace(/\b\w/g, (char) =>
              char.toUpperCase()
            )} | Overview | Fastlearner.ai`
          );
          // Data is now loaded
          if (this.courseId) {
            this.getCourseDetails();
          }
        } else {
          this._router.navigate(['']);
        }
      },
      error: (error: any) => {
        if(error?.error?.status != this._httpConstants.REQUEST_STATUS.UNAUTHORIZED_401.CODE){
          this.isLoading = false;
          this._router.navigate(['']);
        }
      },
    });
  }

  checkTooltipVisibility() {
    const rect =
      this.videoPlayerElement?.media?.nativeElement.getBoundingClientRect();
    const tooltipHeight = rect?.height;

    // Calculate the visible height of the tooltip
    const visibleTop = Math.max(rect?.top, 0);
    const visibleBottom = Math.min(rect?.bottom, window.innerHeight);
    const visibleHeight = visibleBottom - visibleTop;

    // Check if at least half of the tooltip is visible
    this.isTooltipVisible = visibleHeight >= tooltipHeight / 2.5;
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.route.fragment?.subscribe((fragment) => {
        if (fragment === 'course-content') {
          this.scrollToCourseContent();
        } else {
          this.viewportScroller.scrollToPosition([0, 0]);
        }
      });
    }, 900);

    setTimeout(() => {
      this.cardElement = this.el.nativeElement.querySelector('.sticky');
      this.footerElement = document.querySelector('app-footer')!;
      this.mainContentElement = document.querySelector('#main-content')!;
      this.onWindowScrollCard(); // Initial check
    }, 1300);
  }

  ngOnDestroy() {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    this.showBuyButton = false;
  }

  getCourseIdFromRoute() {
    this.courseId = this._activatedRoute?.snapshot?.paramMap?.get('courseId');
    if (this.courseId) {
      this.getCourseDetails();
    }
  }

  getCourseDetails(stopLoader?) {
    this._courseService?.getCourseDetails(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseDetails = response?.data;
          this.isUserLoggedIn();
          if (
            this.courseDetails.courseType == CourseType.PREMIUM &&
            !this.courseDetails.isEnrolled
          ) {
            this.showBuyButton = true;
          }
          this.isLoading = false;
          this.titleService.setTitle(
            `${
              response?.data?.metaTitle ??
              this.courseTitle?.replace(/\b\w/g, (char) => char.toUpperCase())
            } `
          );
          this.metaService.updateTag({
            name: 'description',
            content: `${
              response?.data?.metaDescription ?? response?.data?.about
            }`,
          });
          this.courseDetails.courseDuration =
            this.convertSecondsToHoursAndMinutes(
              Number(this.courseDetails.courseDuration)
            );
          this.courseOutcomeLength = this.courseDetails?.courseOutcome?.length;
          if (
            this.courseDetails?.courseOutcome?.length >
            this.cousrseOutcomeDefaultLength
          ) {
            this.showMore = true;
            this.courseOutcomeLength = this.cousrseOutcomeDefaultLength;
          }
          this.currentSelectedTopic = {
            videoUrl: this.courseDetails.previewVideoUrl,
            vttContent: this.courseDetails.previewVideoVttContent,
          };
          this.testTotalQuestions = response?.data?.testTotalQuestion;
          // this._courseService.passPreviewVideoToVideoPlayer(
          //   this.courseDetails.previewVideoUrl
          // );
          this.totalSections = response?.data?.sectionDetails?.length;
          response?.data?.sectionDetails?.forEach((section: any) => {
            section.totalTime = 0;
            section.totalLectures = section?.topicDetails?.length;
            this.totalTopics += section?.topicDetails.length;
            section?.topicDetails.forEach((topic: any) => {
              if (this.likeCIP) {
                return;
              }
              this.totalSectionTimeInSeconds += topic?.topicDuration * 1000;
              topic.topicDuration = topic?.topicDuration * 1000;
              section.totalTime += topic?.topicDuration;
            });
          });
          this.getRelatedCourses(true, stopLoader);
          this.fetchMoreComments();
        }
      },
      error: (error: any) => {},
    });
  }

  

  fetchComments(currentPage: number, pageSize: number) {
    this.isLoadingComments = true; 
    this._courseService.getComments(this.courseId, currentPage, pageSize).subscribe({
      next: (response: any) => {
        this.isLoadingComments = false;
  
        if (response?.data?.feedback?.feedbackComments?.length) {
          if (!this.courseDetails?.courseFeedback) {
            this.courseDetails.courseFeedback = {
              feedbackComments: [],
              rating1: 0,
              rating2: 0,
              rating3: 0,
              rating4: 0,
              rating5: 0,
            };
          }
  
          this.courseDetails.courseFeedback = {
            ...this.courseDetails.courseFeedback,
            rating1: response.data.feedback.rating1,
            rating2: response.data.feedback.rating2,
            rating3: response.data.feedback.rating3,
            rating4: response.data.feedback.rating4,
            rating5: response.data.feedback.rating5,
          };
          if (this.isFirstLoad){
            this.courseDetails.courseFeedback.feedbackComments = [];
            this.isFirstLoad = false;
          }
          this.courseDetails.courseFeedback.feedbackComments.push(
            ...response.data.feedback.feedbackComments
          );
  
          this.hasMoreComments = response.data.totalPages > currentPage+1;
      } 
      },
      error: (error: any) => {
        this.isLoadingComments = false;
      },
    });
  }

  fetchMoreComments() {
    if (this.hasMoreComments) {
      this.fetchComments(this.currentPage, this.pageSize);
      this.currentPage += 1;

    }
  }

  commentActions(action, reviewId) {
    if (this.likeCIP) {
      return;
    }
    this.likeCIP = true;
    if (this.isLoggedIn) {
      let reviewActionPayload = {
        reviewId: reviewId,
        action: action,
      };
      this._courseService
        ?.likeAndDislikeReviewSection(reviewActionPayload)
        ?.subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this.getCourseDetails(true);
              // this._messageService.success(`Review ${action} successfully`);
              setTimeout(() => {
                this.likeCIP = false;
              }, 1000);
            }
          },
          error: (error: any) => {
            // this._messageService.error(error?.error?.message);
            setTimeout(() => {
              this.likeCIP = false;
            }, 1000);
          },
        });
    } else {
      let path = 'student/course-details/' + this.courseUrl;
      this._cacheService.saveInCache('redirectUrl', path);
      this._router.navigate(['auth/sign-in']);
    }
  }

  getRelatedCourses(isFirst: any, stopLoader?) {
    if (stopLoader) {
      return;
    }
    this.relatedCourse.courseId = this.courseId;
    this._courseService?.getRelatedCourses(this.relatedCourse)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (!isFirst) {
            response?.data?.courses?.forEach((x: any) => {
              this.courseList.push(x);
            });
          }
          if (isFirst) {
            this.courseList = response?.data?.courses;
            this.courseList?.forEach((res: any) => {
              res.courseDuration = this.convertSecondsToHoursAndMinutes(
                res.courseDuration
              );
            });
            this.noOfCount = response?.data?.pageSize;
          }
        }
      },
      error: (error: any) => {},
    });
  }

  @HostListener('window:scroll', [])
  onWindowScrollCard() {
    const footerRect = this.footerElement?.getBoundingClientRect();
    const cardRect = this.cardElement?.getBoundingClientRect();
    const mainContentRect = this.mainContentElement?.getBoundingClientRect();

    if (footerRect?.top <= window.innerHeight / 4 + cardRect?.height / 4) {
      this.renderer?.removeClass(this.cardElement, 'sticky');
      this.renderer?.addClass(this.cardElement, 'sticky-bottom');
    } else if (
      mainContentRect?.bottom >
      window.innerHeight / 2 + cardRect?.height / 2
    ) {
      this.renderer?.addClass(this.cardElement, 'sticky');
      this.renderer?.removeClass(this.cardElement, 'sticky-top');
      this.renderer?.removeClass(this.cardElement, 'sticky-bottom');
    } else {
      this.renderer?.removeClass(this.cardElement, 'sticky');
      this.renderer?.removeClass(this.cardElement, 'sticky-top');
      this.renderer?.addClass(this.cardElement, 'sticky-bottom');
    }
  }

  nextPageOfRelatedCourse() {
    this.relatedCourse.pageNo = this.relatedCourse.pageNo + 1;
    this.noOfCount = this.noOfCount - 1;
    this.getRelatedCourses(false, false);
  }

  getCourseCompleteReview() {
    let courseReviewPayLoad = {
      courseId: this.courseId,
      pageNo: 0,
      pageSize: 20,
    };
    this._courseService
      .getCourseRatingReviewAndFeedback(courseReviewPayLoad)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.courseReview = response?.data?.feedback;
          }
        },
        error: (error: any) => {},
      });
  }

  toggleSectionPanel(event: any, section: any) {
    section.panelOpen = event;
  }

  toggleAllSection() {
    this.expand = !this.expand;
    if (this.expand) {
      this.expandButtonName = 'Collapse All Sections';
      this.courseDetails?.sectionDetails?.forEach((x: any) => {
        if (!x.panelOpen) {
          x.panelOpen = true;
        }
      });
    } else {
      this.expandButtonName = 'Expand All Sections';
      this.courseDetails?.sectionDetails?.forEach((x: any) => {
        if (x.panelOpen) {
          x.panelOpen = false;
        }
      });
    }
  }

  routeToCourseDetails(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl]);
    this.getCourseIdFromRoute();
  }

  routeToCourseDetailsContent(courseUrl: any) {
    this._router.navigate(['student/course-details', courseUrl], {
      fragment: 'course-content',
    });
  }

  isUserLoggedIn() {
    this.isLoggedIn = this._authService.isLoggedIn();
    this.courseButtonName = this.isLoggedIn
    ? 'Start ' + (this.courseDetails?.contentType?.toLowerCase() === this.courseContentType.TEST ? 'Test' : 'Learning')
    : 'Start Now';
  
  }

  toggleFavoriteCourse() {
    this._courseService?.addOrRemoveCourseToFavorite(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseDetails.isFavourite = !this.courseDetails.isFavourite;
          this._sharedService.updateFavCourseMenu();
        }
      },
      error: (error: any) => {},
    });
  }

  startCouseLearning() {
    if (this.showBuyButton) {
      this._router.navigate(['payment-method'], {
        queryParams: {
          courseId: this.courseId,
          courseUrl: this.courseUrl,
          price: this.courseDetails.price,
          premium: true,
          affiliate: this.affiliateUUID
        },
      });
      return;
    }
    this._router.navigate(['student/course-content', this.courseUrl]);
  }

  openShareCourseModal() {
    const modal = this._modal.create({
      nzContent: ShareModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: this.courseId,
        url: this.applicationCourseDetailsUrl + this.activeCourseUrl,
        title: 'Share this Course',
        label: 'Share the Course URL',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  }

  showCourseOutcome(flag?: any) {
    this.courseOutcomeLength = flag
      ? this.cousrseOutcomeDefaultLength
      : this.courseDetails.courseOutcome.length;
  }

  routeToInsructorProfile(event) {
    this._router.navigate(['user/profile'], {
      queryParams: {
        url: event?.profileUrl ? event?.profileUrl : event,
      },
    });
    if (event?.event) {
      event.event.stopPropagation();
      return;
    }
    if (typeof event?.stopPropagation === 'function') {
      event.stopPropagation();
    }
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours === 0) {
      return `${minutes} minutes`;
    } else if (minutes === 0) {
      return `${hours} hours`;
    } else {
      return `${hours} hours ${minutes} minutes`;
    }
  }

  scrollToCourseContent() {
    document.getElementById('course-content')?.scrollIntoView();
  }

  routeToCourseDetailsWithIds(sectionId, topicId) {
    this._router.navigate(['student/course-content', this.courseUrl], {
      queryParams: { sectionId: sectionId, topicId: topicId },
    });
  }

  scrollToFeedback(): void {
    const element = document.getElementById('feedback');
    const offset = 90;
    if (element) {
      const elementPosition =
        element.getBoundingClientRect().top + window.pageYOffset;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth',
      });
    }
  }
}
