import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnInit,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CourseService } from 'src/app/core/services/course.service';
import { RatingModalComponent } from '../../dynamic-modals/rating-modal/rating-modal.component';
import { SectionReview } from 'src/app/core/models/section-review.model';
import { MessageService } from 'src/app/core/services/message.service';
import { ShareModalComponent } from '../../dynamic-modals/share-modal/share-modal.component';
import { CourseChat } from 'src/app/core/models/course-chat.model';
import {
  QuestionAnswers,
  QuestionDetail,
} from 'src/app/core/models/course-qa.model';
import { CourseNote } from 'src/app/core/models/course-notes.model';
import { ChatModalComponent } from '../../dynamic-modals/chat-modal/chat-modal.component';
import { ReviewModalComponent } from '../../dynamic-modals/review-modal/review-modal.component';
import { CompleteReview } from 'src/app/core/models/complete-review.model';
import { CacheService } from 'src/app/core/services/cache.service';
import { Subscription } from 'rxjs';
import { SharedService } from 'src/app/core/services/shared.service';
import { CertificateService } from 'src/app/core/services/certificate.service';
import { SubscriptionPlanComponent } from '../../auth/subscription-plan/subscription-plan.component';
import { Meta, Title } from '@angular/platform-browser';
import { ViewportScroller } from '@angular/common';
import { NzDropdownMenuComponent } from 'ng-zorro-antd/dropdown';
import { CourseType } from 'src/app/core/enums/course-status';
import { CourseContentType } from 'src/app/core/enums/course-content-type.enum';

@Component({
  selector: 'app-course-content',
  templateUrl: './course-content.component.html',
  styleUrls: ['./course-content.component.scss'],
})
export class CourseContentComponent implements OnInit, AfterViewInit {
  private subscription: Subscription;
  @ViewChild('myInput', { static: false }) myInput: ElementRef;
  @ViewChild('chatContainer') chatContainer: ElementRef;
  @ViewChild('videoPlayerElement', { static: false }) videoPlayerElement: any;
  @ViewChild('menuMore', { static: false }) menuMore!: NzDropdownMenuComponent;

  _httpConstants: HttpConstants = new HttpConstants();
  sectionReview: SectionReview = new SectionReview();
  courseChat: Array<CourseChat> = [];
  courseNote: Array<CourseNote> = [];
  courseChatHistory: Array<any> = [];
  questionAndAnswers: Array<any> = [];
  questionAnswers: QuestionAnswers;
  completeReview: CompleteReview;
  courseId: any;
  courseTitle: any;
  courseUrl?: any;
  shareURL: any;
  activePanel: number | null = null;
  isLoading: boolean = true; // Add a loading flag
  totalQuestions: number | null = 0;
  totalReviewElements: number | null = 0;
  summary: string = '';
  askQuestion: any;
  chatTopicId: any;
  chatTopicTime: any;
  chatSentTime: any;
  noteText: string = '';
  questionText: string = '';
  replyText: string = '';
  noteDisabled: boolean = true;
  currentSelectedTopicId: any;
  currentSelectedTopic: any;
  currentSelectedTopicType: any;
  currentSelectedSection: any;
  currentSelectedSectionId: any;
  currentCourse: any;
  currentCourseType: any;
  courseType = CourseType;
  currentCategory: any;
  courseProgress: any = 0;
  selectedChatId: any;
  defaultSection: any;
  defaultTopic: any;
  showAlternateCollapse: boolean = false;
  showCertificate: boolean = false;
  fullWidth: boolean = true;
  toCourseId?: any;
  toSectionId?: any;
  duplicateSection?: any;
  courseChatPresent = false;
  duplicateSectionIndex?: any;
  currentVideoTime: any;
  showSpinner: boolean = false;
  showCourseContentTab: boolean = false;
  isFirstTime: boolean = true;
  currentTopicIndices: { [sectionId: number]: number } = {}; // Keep track of the index of the currently playing topic in each section
  isSubscribed: boolean;
  isTooltipVisible: boolean = true;
  subscriptionModalOpened?: boolean = false;
  queryString?: any;
  tabIndex?: any = 0;
  courseReviewTotalPages?: any;
  questionAnswersTotalPages?: any;
  CourseType = CourseType;
  isReviewing: boolean = false; //need to be false
  questionAnswersQuiz: any = [];
  showCongratsScreen?: boolean = false;

  courseReviewPayLoad: any = {
    courseId: null,
    pageNo: 0,
    pageSize: 15,
  };

  loggedInUser: any = {
    fullName: '',
    email: '',
    profilePicture: null,
  };

  sectionPanelList: Array<any> = [
    {
      topicList: [],
    },
  ];

  alternateSectionPanelList: Array<any> = [
    {
      topicList: [],
    },
  ];

  generateCertificateEnable?: any = false;
  accumulatedTime: number = 0;
  twoMinutesInSeconds: number = 10;
  checkInterval: any;
  routeSubscription: Subscription;
  isInView: boolean = false;
  alternateSectionRouting = true;
  watchTime?: any = 0;

  questionPayLoad?: any = {
    courseId: null,
    pageNo: 0,
    pageSize: 10,
  };
  quizQuestions: any[];
  hasMoreComments = true;
  courseContentType = CourseContentType;
  selectedContentType?: any;

  constructor(
    private _courseService: CourseService,
    private _router: Router,
    private cdr: ChangeDetectorRef,
    private _activatedRoute: ActivatedRoute,
    private _authService: AuthService,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private _messageService: MessageService,
    private _cacheService: CacheService,
    private _sharedService: SharedService,
    private _certificateService: CertificateService,
    private metaService: Meta,
    private titleService: Title,
    private viewportScroller: ViewportScroller
  ) {
    this.loggedInUser.email = this._authService?.getLoggedInEmail();
    this.loggedInUser.fullName = this._authService.getLoggedInName();
    this.loggedInUser.profile = this._authService.getUserProfile();
    setTimeout(() => {
      this.loggedInUser.profilePicture = this._authService.getLoggedInPicture();
    }, 1000);

    this.subscription = this._sharedService
      .getSectionRatingAndReviews()
      .subscribe(() => {
        this.getSectionRatingAndReviews();
      });
  }
  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.checkTooltipVisibility();
    if (this.isDropdownVisible) {
      this.closeDropdown(); // Close dropdown when scrolling
    }
    if (this.isDropdownProfileVisible) {
      this.closeProfileDropdown(); // Close dropdown when scrolling
    }
  }

  isDropdownVisible = false;
  isDropdownProfileVisible = false;

  // Triggered when the visibility of the dropdown changes (e.g., user clicks)
  onDropdownVisibilityChange(visible: boolean): void {
    this.isDropdownVisible = visible;
  }

  // Manually close the dropdown
  closeDropdown(): void {
    this.isDropdownVisible = false;
  }

  // Manually open the dropdown
  openDropdown(): void {
    this.isDropdownVisible = true;
  }
  onDropdownProfileVisibilityChange(visible: boolean): void {
    this.isDropdownProfileVisible = visible;
  }

  // Manually close the dropdown
  closeProfileDropdown(): void {
    this.isDropdownProfileVisible = false;
  }

  // Manually open the dropdown
  openProfileDropdown(): void {
    this.isDropdownProfileVisible = true;
  }
  ngAfterViewInit() {
    // this.checkIfInView();
  }
  isInputFocused(): boolean {
    const activeElement = document.activeElement;
    return (
      activeElement &&
      (activeElement.tagName === 'INPUT' ||
        activeElement.tagName === 'TEXTAREA')
    );
  }

  ngOnInit(): void {
    // this.getCourseIdFromRoute();
    this.userIsSubscribed();
    this.onResize({ target: window });
    window.addEventListener(
      'beforeunload',
      this.beforeUnloadHandler.bind(this)
    );
    window.addEventListener('offline', this.onOffline.bind(this));
    this.routeSubscription = this._activatedRoute?.paramMap?.subscribe(
      (params: ParamMap) => {
        this.getCourseIdFromRoute();
      }
    );
  }

  ngOnDestroy(): void {
    this.manageSeekTime();
    this.manageWatchTime();
    window.removeEventListener(
      'beforeunload',
      this.beforeUnloadHandler.bind(this)
    );
    window.removeEventListener('offline', this.onOffline.bind(this));
    // this.saveCurrentCourseData();
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeUnloadHandler(event: Event): void {
    this.manageSeekTime();
    this.manageWatchTime();
    // this.saveCurrentCourseData();
  }

  onOffline(event: Event): void {
    this.manageSeekTime();
    this.manageWatchTime();
    // this.saveCurrentCourseData();
  }

  saveCurrentCourseData() {
    setTimeout(() => {
      this.currentSelectedTopic.seekTime = this.getTimeInSec(
        this.currentVideoTime
      );
      let currentCourseData = {
        courseId: this.courseId,
        section: this.currentSelectedSection,
        index: this.sectionPanelList.findIndex(
          (obj) => obj.sectionId === this.currentSelectedSection.sectionId
        ),
        topic: this.currentSelectedTopic,
      };

      this._cacheService.saveCourseData('currentCourseData', currentCourseData);
    }, 0);
  }

  userIsSubscribed() {
    if (this._authService.isSubscribed()) {
      this.isSubscribed = true;
    } else {
      this.isSubscribed = false;
    }
  }

  getCourseIdFromRoute() {
    this.courseUrl = this._activatedRoute?.snapshot?.paramMap?.get('courseUrl');
    // this.queryString = Object.values(this._activatedRoute?.snapshot?.queryParams).join('');

    this._activatedRoute.fragment?.subscribe((fragment) => {
      this.queryString = fragment;
    });

    this.getCourseByUrl(this.courseUrl);
    this.metaService.updateTag({
      name: `${this.courseTitle}`,
      content: 'Course Content page of Fastlearner.ai',
    });
  }

  getCourseByUrl(courseUrl?: any) {
    this.isLoading = true; // Start loading
    this._courseService?.getCourseByUrl(courseUrl)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (
            response?.data?.course?.courseType === this.CourseType.PREMIUM &&
            !response?.data?.canAccess
          ) {
            this._router.navigate(['student/course-details', courseUrl]);
          } else {
            this.isLoading = false;
            this.courseId = response.data?.course?.id;
            this.courseTitle = response?.data?.activeUrl;
            this.currentCourseType = response?.data?.course?.courseType;
            this.selectedContentType = response?.data?.course?.contentType;
            this.titleService.setTitle(
              `${response?.data?.course?.title?.replace(/\b\w/g, (char) =>
                char.toUpperCase()
              )} | Contents | FastLearner.ai`
            );
            if (this.courseId) {
              this.enrolledInCourse();
            }
            if (
              this.queryString &&
              (this.queryString == 'COURSE_QnA_DISCUSSION' ||
                this.queryString == 'QnA_REPLY')
            ) {
              this.tabIndex = 2;
              this.onToggleTabs(this.tabIndex);
              setTimeout(() => {
                this.scrollToTab('qa-header');
              }, 900);
            } else if (
              this.queryString &&
              (this.queryString == 'NOTIFY_LIKE_DISLIKED_REVIEW' ||
                this.queryString == 'COURSE_REVIEW' ||
                this.queryString == 'COURSE_REVIEW_UPDATED')
            ) {
              this.tabIndex = 4;
              this.onToggleTabs(this.tabIndex);
              setTimeout(() => {
                this.scrollToTab('feedback-header');
              }, 900);
            }
          }
        }
      },
      error: (error: any) => {
        if (
          error?.error?.status !=
          this._httpConstants.REQUEST_STATUS.UNAUTHORIZED_401.CODE
        ) {
          this.isLoading = false;
          this._router.navigate(['']);
        }
      },
    });
  }

  scrollToTab(scroll?: any) {
    const tabElement = document.getElementById(scroll);
    if (tabElement) {
      tabElement.scrollIntoView();
    }
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
    this.isTooltipVisible = visibleHeight >= tooltipHeight / 3;
  }

  onToggleTabs(event: any) {
    switch (event) {
      case 0:
        this.getSectionTopicsAndChatQuestion();
        break;
      case 1:
        this.getNotes();
        break;
      case 2:
        this.getQuestionList(false);
        break;
      case 3:
        this.getSummaryReport();
        break;
      case 4:
        this.completeReview = null;
        this.courseReviewPayLoad.pageNo = 0;
        this.getCourseCompleteReview();
        break;
    }
  }

  //*****************************Side Panel Methods********************************* */

  getCourseProgress() {
    this._courseService.courseProgress(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseProgress = response?.data;
          this.generateCertificateEnable = this.courseProgress === 100;
        }
      },
      error: (error: any) => {
        // this._messageService.error(error?.error?.message);
      },
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const screenWidth = event.target.innerWidth;
    if (screenWidth >= 992) {
      this.showCourseContentTab = false;
    } else {
      this.showCourseContentTab = true;
    }
  }

  skipQuiz(event) {
    const currentSectionIndex = this.sectionPanelList.findIndex(
      (section) => section.sectionId === this.currentSelectedSection.sectionId
    );

    if (currentSectionIndex !== -1) {
      const currentSection = this.sectionPanelList[currentSectionIndex];
      const currentTopicIndex = currentSection?.topicList?.findIndex(
        (topic) => topic === this.currentSelectedTopic
      );
      const nextTopicIndex = currentTopicIndex + 1;
      this.currentSelectedTopic.isCompleted = true
      this.completeTopic(
        this.currentSelectedSection,
        this.currentSelectedTopic,
        true,
        this.currentSelectedTopic?.topicId,
        this.currentSelectedTopic?.seekTime,
        this.currentSelectedTopic?.topicType
      );
      // if (!this.currentSelectedTopic.isCompleted) {
      //   this._courseService
      //     .markTopicComplete({
      //       isCompleted: true,
      //       topicId: this.currentSelectedTopic.topicId,
      //       seekTime: 0,
      //     })
      //     ?.subscribe({
      //       next: (response: any) => {
      //         if (
      //           response?.status ==
      //           this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
      //         ) {
      //           this.getCourseProgress();
      //           this.currentSelectedTopic.isCompleted = true;
      //         }
      //       },
      //       error: (error: any) => {},
      //     });
      // }

      setTimeout(() => {
        if (nextTopicIndex < currentSection.topicList.length) {
          const nextTopic = currentSection.topicList[nextTopicIndex];

          this.currentSelectedTopic = nextTopic;
          this.currentSelectedTopicType = nextTopic.topicType;
          this.currentSelectedTopicId = nextTopic.topicId;
          this.currentSelectedSection = currentSection;
          this.currentSelectedSectionId = currentSection.sectionId;
        } else {
          const nextSectionIndex = currentSectionIndex + 1;
          if (nextSectionIndex < this.sectionPanelList.length) {
            const nextSection = this.sectionPanelList[nextSectionIndex];
            if (nextSection.free) {
              this.isPanelActive(nextSection, true, nextSectionIndex);
              setTimeout(() => {
                this.currentSelectedTopic = nextSection?.topicList[0];
                this.currentSelectedTopicType =
                  this.currentSelectedTopic.topicType;
                this.currentSelectedTopicId = this.currentSelectedTopic.topicId;
                this.currentSelectedSection = nextSection;
                this.currentSelectedSectionId = nextSection.sectionId;
              }, 1000);
            } else {
              this._messageService.error(
                'You have to get a subscription, next section is not free.'
              );
            }
          } else {
            if (this.courseProgress === 100) {
              this.getCertificateData();
              return;
            }
            this._messageService.info('No more sections');
          }
        }
      }, 1000);
    } else {
      this._messageService.info('No active section found.');
    }
  }

  getCourseSectionList(sectionId?: number | null, topicId?: string | null) {
    this._courseService.getCourseSections(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.sectionPanelList = response?.data?.sectionDetails.map(
            (section: any) => {
              return {
                ...section,
                active: false,
                showHeader: true,
                alternateSectionPanelList: [],
                topics: [], // Initialize topics if needed
              };
            }
          );

          this.currentCategory = response?.data?.category;
          this.currentCourse = response?.data?.title;

          this.showCertificate = response?.data?.hasCertificate;

          if (sectionId) {
            // If sectionId is provided, activate the section
            const sectionIndex = this.sectionPanelList.findIndex(
              (section: any) => section.sectionId === sectionId
            );

            if (sectionIndex !== -1) {
              this.sectionPanelList[sectionIndex].active = true;
              this.defaultSection = this.sectionPanelList[sectionIndex];
              this.getSectionTopicList(
                this.defaultSection,
                sectionIndex,
                true,
                topicId ? { topicId: topicId } : null // Pass topicId if available
              );
            } else {
              this.setDefaultSection(response?.data?.sectionDetails);
            }
          } else if (topicId) {
            // If topicId is provided, find the section containing that topic
            const sectionContainingTopic = this.sectionPanelList.find(
              (section: any) =>
                section.topics.some((topic: any) => topic.topicId === topicId)
            );

            if (sectionContainingTopic) {
              sectionContainingTopic.active = true;
              this.defaultSection = sectionContainingTopic;

              // Fetch topics for the section if not already done
              this.getSectionTopicList(
                sectionContainingTopic,
                this.sectionPanelList.indexOf(sectionContainingTopic),
                true,
                { topicId: topicId }
              );
            } else {
              this.setDefaultSection(response?.data?.sectionDetails);
            }
          } else {
            // Fallback to default section if no params
            this.setDefaultSection(response?.data?.sectionDetails);
          }
        }
      },
      error: (error: any) => {
        this._messageService.error(error?.error?.message);
      },
    });
  }

  setDefaultSection(sectionDetails: any) {
    const defaultSectionIndex = sectionDetails.findIndex(
      (section: any) => section.free
    );

    const defaultSection =
      defaultSectionIndex !== -1
        ? sectionDetails[defaultSectionIndex]
        : sectionDetails[0];

    this.sectionPanelList[
      defaultSectionIndex !== -1 ? defaultSectionIndex : 0
    ].active = true;
    this.defaultSection = defaultSection;

    this.getSectionTopicList(
      this.defaultSection,
      defaultSectionIndex !== -1 ? defaultSectionIndex : 0,
      true,
      null
    );
  }

  videoCompleted(event) {
    if (!this.currentSelectedTopic?.isCompleted) {
      this.currentSelectedTopic.isCompleted = true;
      this.manageWatchTime();
      this.completeTopic(
        this.currentSelectedSection,
        this.currentSelectedTopic,
        true,
        this.currentSelectedTopic?.topicId,
        this.currentSelectedTopic?.seekTime,
        this.currentSelectedTopic?.topicType
      );
    }
    this.playNextVideo();
  }

  routeToNotificationPage() {
    this._router.navigate(['/user/notifications']);
  }
  routeToLandingPage() {
    this._router?.navigate(['']);
  }

  playNextVideo(): void {
    // Get the current section ID
    const currentSectionId = this.currentSelectedSectionId;

    // Find the index of the current section
    const currentSectionIndex = this.sectionPanelList.findIndex(
      (section) => section.sectionId === currentSectionId
    );

    // If we have a valid section index
    if (currentSectionIndex !== -1) {
      const currentSection = this.sectionPanelList[currentSectionIndex];
      let currentTopicIndex = currentSection?.topicList?.findIndex(
        (topic) => topic === this.currentSelectedTopic
      );

      // If there is no active topic but we have a current index, try to play the next topic
      if (currentTopicIndex === -1) {
        currentTopicIndex = 0;
      }

      // If there is a next topic in the current section
      if (currentTopicIndex < currentSection.topicList.length - 1) {
        this.currentSelectedTopic =
          currentSection.topicList[currentTopicIndex + 1];
        this.currentSelectedTopicType =
          currentSection.topicList[currentTopicIndex + 1].topicType;
        this.currentSelectedTopicId =
          currentSection.topicList[currentTopicIndex + 1].topicId; // Start from the first topic of the next section
      } else {
        // If there are no more topics in the current section, move to the next section
        const nextSectionIndex = currentSectionIndex + 1;

        // If there is a next section
        if (nextSectionIndex < this.sectionPanelList.length) {
          const nextSection = this.sectionPanelList[nextSectionIndex];

          // If the next section is free, play its first topic
          if (nextSection.free) {
            this.isPanelActive(nextSection, true, nextSectionIndex);
            setTimeout(() => {
              this.currentSelectedTopic = nextSection.topicList[0];
              this.currentSelectedTopicType =
                this.currentSelectedTopic?.topicType;
              this.currentSelectedTopicId = this.currentSelectedTopic?.topicId; // Start from the first topic of the next section
              this.currentSelectedSection = nextSection;
              this.currentSelectedSection.active = true;
              this.currentSelectedSectionId = nextSection?.sectionId;
            }, 1000);
          } else {
            // If the next section is not free, show an error message
            this._messageService.error(
              'User have to get a subscription, next section is not free.'
            );
            // You can use your messaging service to show an error message here if available
          }
        } else {
          if (this.courseProgress === 100) {
            this.getCertificateData();
            return;
          }
          this._messageService.info('No more sections');
        }
      }
    } else {
      // If there is no valid section index, console log "Invalid section"
      this._messageService.info('Invalid section.');
    }
  }

  //TODO: get topic and section from URL
  getSectionTopicList(section: any, i: number, isFirst: boolean, topic?: any) {
    this._courseService
      .getSectionTopics(this.courseId, section.sectionId)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ===
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.sectionPanelList[i].topicList = response?.data;

            // Convert topic durations
            this.sectionPanelList[i].topicList.forEach((el: any) => {
              el.topicDuration = this.convertSecondsToHoursAndMinutes(
                el.topicDuration
              );
            });

            // If a specific topic is provided (from URL or passed in), select it; otherwise, use the first topic
            this.defaultTopic = topic
              ? this.sectionPanelList[i].topicList.find(
                  (el: any) => el.topicId === topic?.topicId
                )
              : this.sectionPanelList[i].topicList[0]; // Default to the first topic if no topic is provided

            // Proceed with selecting the topic if found
            if (this.defaultTopic) {
              if (isFirst) {
                this.onSelectTopicFromPlayList(section, this.defaultTopic);
              }
            } else {
              console.error('Topic not found');
            }
          } else {
            console.error('Failed to load topics');
          }
        },
        error: (error: any) => {
          section['active'] = false;

          if (!this.subscriptionModalOpened) {
            this.subscriptionModalOpened = true;
            this.currentSelectedTopicType = 'Video';
            this.openSubscriptionPlan();
          }
        },
      });
  }

  openSubscriptionPlan(): void {
    const modal = this._modal.create({
      nzContent: SubscriptionPlanComponent,
      nzComponentParams: {
        fromSubscriptionPlan: true,
        showFreePlan: false,
      },
      nzViewContainerRef: this._viewContainerRef,
      nzFooter: null,
      nzKeyboard: true,
      // nzWidth: this.fullWidth ? '80%' : '100%',
      nzWidth: '80%',
    });
    modal.afterClose?.subscribe((result) => {
      this.subscriptionModalOpened = false;
    });
  }

  toggleSectionPanel(event: any, section: any, index: number) {
    if (event) {
      this.chatSectionPosition(section?.sectionId);
      this.getSectionTopicList(section, index, false, null);
      this.chatSectionPosition(section.sectionId);
    }
  }

  isPanelActive(section, event, index) {
    section['active'] = event;
    if (event) {
      // this.sectionPanelList.forEach((sectionP) => {
      //   sectionP.active = sectionP.sectionId === section.sectionId;
      // });

      this.chatSectionPosition(section?.sectionId);
      this.getSectionTopicList(section, index, false, null);
      this.chatSectionPosition(section.sectionId);
    }
  }
  isAlternatePanelActive(section?: any, event?: any, index?: any) {
    if (this.alternateSectionRouting) {
      const courseUrlParam = section.courseUrl;
      this._router
        .navigate(['student/course-content', courseUrlParam])
        .then(() => {
          // Force a reload if needed
          this._router.routeReuseStrategy.shouldReuseRoute = () => false;
          this._router.onSameUrlNavigation = 'reload';
        });
      section['active'] = event;
    } else {
      this.alternateSectionRouting = true;
    }
    return;
  }

  onSelectTopicFromPlayList(section: any, topic: any) {
    const targetDiv = document.getElementById(`topic-selected`);
    if (targetDiv) {
      // Scroll smoothly to the target div
      targetDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    // Manage seek time if a section is already selected
    if (this.currentSelectedSection) {
      this.manageSeekTime();
      this.manageWatchTime();
    }

    // Set current section and topic details
    this.currentSelectedSection = section;
    this.currentSelectedSectionId = section?.sectionId;
    this.currentSelectedTopic = topic;
    this.currentSelectedTopicType = topic?.topicType;
    this.currentSelectedTopicId = topic?.topicId;

    // Fetch notes if the topic type is 'Quiz'
    if (this.currentSelectedTopicType === 'Quiz') {
      this.getNotes();
    }

    // Fetch other necessary data related to the section and course
    this.getSectionRatingAndReviews();
    this.getCourseProgress();
    this.getSummaryReport();

    // Set default value for courseChatPresent to false
    this.courseChatPresent = false;

    // Loop through courseChat to find the matching section and topic
    for (let course of this.courseChat) {
      const matchingSection =
        course.sectionId === this.currentSelectedSectionId;
      if (matchingSection) {
        this.courseChatPresent = true;

        const matchingTopic = course.topics.find(
          (t: any) => t.topicId === this.currentSelectedTopicId
        );

        if (matchingTopic && matchingTopic.chatTopicHistory.length) {
          const firstChatHistory = matchingTopic.chatTopicHistory[0];
          this.selectedChatId = firstChatHistory.chatId;

          // Fetch chat history for the selected topic
          this.getCourseChatHistory(
            firstChatHistory.chatId,
            firstChatHistory.time,
            matchingTopic.topicId
          );
        }
        break; // Stop searching once a match is found
      }
    }

    // Fetch section topics and chat questions
    this.getSectionTopicsAndChatQuestion();
  }

  completeTopic(
    section: any,
    topic: any,
    isComplete: boolean,
    topicId: any,
    seekTime: any,
    topicType: any
  ) {
    
    if (section != null) {
      if (isComplete) {
        if (section.totalTopicCompleted < section.topicList.length) {
          section.totalTopicCompleted += 1;
        }
      } else {
        section.totalTopicCompleted = Math.max(section.totalTopicCompleted - 1, 0);
      }
    }

    let completePayLoad = {};
    if (topicType == 'Video') {
      completePayLoad = {
        isCompleted: isComplete,
        topicId: topicId,
        seekTime:
          isComplete || topicId != this.currentSelectedTopicId
            ? 0
            : this.getTimeInSec(this.currentVideoTime),
      };
    } else {
      completePayLoad = {
        isCompleted: isComplete,
        topicId: topicId,
        seekTime: 0,
      };
    }
    this._courseService.markTopicComplete(completePayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.getCourseProgress();
        }
      },
      error: (error: any) => {
        topic.isCompleted = !isComplete;
        // this._messageService.error(error?.error?.message);
      },
    });
  }

  getVideoTime(eventData: {
    videoTime: any;
    isVideoPlaying: any;
    playbackRate: any;
  }) {
    if (eventData?.videoTime >= 1 && eventData?.isVideoPlaying) {
      this.watchTime += 1 * eventData?.playbackRate;
    }
    this.accumulatedTime = eventData.videoTime;
    this.manageSeekTimeAfterEveryTwoMins(eventData.videoTime);
    this.currentVideoTime = this.secondsToHms(eventData.videoTime);
  }

  getVideoCurrentTimeFromVideoPlayer() {
    this._courseService.getTime();
  }

  secondsToHms(d: any) {
    d = Number(d);
    let h = Math.floor(d / 3600);
    let m = Math.floor((d % 3600) / 60);
    let s = Math.floor((d % 3600) % 60);

    if (h > 0) {
      return h + 'h' + ':' + m + 'm' + ':' + s + 's';
    } else if (m > 0) {
      return m + 'm' + ':' + s + 's';
    } else {
      return s + 's';
    }
  }

  getTimeInSec(time?: any): any {
    const parts = time?.split(':');

    let totalSeconds = 0;
    parts?.forEach((part) => {
      if (part.includes('h')) {
        const hours = parseInt(part);
        totalSeconds += hours * 3600;
      } else if (part.includes('m')) {
        const minutes = parseInt(part);
        totalSeconds += minutes * 60;
      } else if (part.includes('s')) {
        const seconds = parseInt(part);
        totalSeconds += seconds;
      }
    });
    return totalSeconds;
  }

  //*********************************Chat Methods********************************** */

  getSectionTopicsAndChatQuestion() {
    this._courseService
      .getSectionAndTopicsChatQuestion(this.courseId)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.courseChat = response?.data;
            this.courseChat.map((course) => {
              course?.topics.map((topic) => {
                if (topic.topicId === this.currentSelectedTopic?.topicId) {
                  topic?.chatTopicHistory.map((chatTopicHistory) => {
                    if (chatTopicHistory.time == this.chatSentTime) {
                      this.selectedChatId = chatTopicHistory?.chatId;
                    }
                    if (chatTopicHistory.chatId === this.selectedChatId) {
                      topic['active'] = true;
                    }
                  });
                }
              });
            });
            if (this.courseChat.length && this.isFirstTime) {
              this.isFirstTime = false;
              this.courseChatPresent = true;
              this.getCourseChatHistory(
                this.courseChat[0]?.topics[0]?.chatTopicHistory[0]?.chatId,
                this.courseChat[0]?.topics[0]?.chatTopicHistory[0]?.time,
                this.courseChat[0]?.topics[0]?.topicId
              );
            }
          }
        },
        error: (error: any) => {
          this.courseChat = [];
          this.courseChatHistory = [];
          this.courseChatPresent = false;
          return;
        },
      });
  }

  getCourseChatHistory(chatId: any, chatTime: any, topicId: any) {
    this.chatTopicId = topicId;
    this.selectedChatId = chatId;
    this.chatTopicTime = chatTime;
    this.currentSelectedTopicId = topicId;
    this._courseService.getCourseChatHistory(chatId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseChatHistory = [];
          this.courseChatHistory = response?.data;
          this.courseChatPresent = true;
          setTimeout(() => {
            this.scrollToBottom();
          }, 1000);
        }
      },
      error: (error: any) => {
        // this._messageService.error(error?.error?.message);
      },
    });
  }

  sendMessage() {
    this.showSpinner = true;
    let chatPayLoad = {
      courseId: this.courseId,
      topicId: this.currentSelectedTopicId,
      question: this.askQuestion,
      time: this.currentVideoTime ? this.currentVideoTime : this.chatTopicTime,
    };
    this.chatSentTime = this.currentVideoTime;
    this.askQuestion = '';
    this._courseService.sendMessageInChat(chatPayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (this.courseChatPresent) {
            this.courseChatHistory.push(response?.data);
          } else {
            this.courseChatHistory = [];
            this.courseChatHistory.push(response.data);
          }

          if (this) this.showSpinner = false;
          this.courseChatPresent = true;
          this.getSectionTopicsAndChatQuestion();
          setTimeout(() => {
            this.scrollToBottom();
          }, 1000);
        }
      },
      error: (error: any) => {
        this.showSpinner = false;
        // this._messageService.error(error?.error?.message);
      },
    });
  }

  openChatModal() {
    const modal = this._modal.create({
      nzContent: ChatModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: this.courseId,
        title: 'Chat',
        topicId: this.currentSelectedTopicId,
        topicTime: this.chatTopicTime,
        isFirstTime: this.isFirstTime,
        chatTopicTime: this.chatTopicTime,
        courseChatPresent: this.courseChatPresent,
        currentVideoTime: this.currentVideoTime,
        selectedChatId: this.selectedChatId,
        profilePicture: this.loggedInUser.profile?.profilePicture,
      },
      nzFooter: null,
      nzKeyboard: true,
      nzWidth: '90%',
      nzCentered: true,
      nzCloseIcon: '../../../assets/icons/chat-collapse',
    });
    modal?.afterClose?.subscribe((event) => {
      if (!event) {
        this.getSectionTopicsAndChatQuestion();
      }
    });
  }

  chatSectionPosition(sectionId: any) {
    this.courseChat.forEach((d: any, i: any) => {
      if (d.sectionId == sectionId) {
        let firstElement = this.courseChat[i];
        this.courseChat.splice(i, 1);
        this.courseChat.unshift(firstElement);
      }
    });
  }

  commentActions(action?: any, reviewId?: any) {
    let reviewActionPayload = {
      reviewId: reviewId,
      action: action,
    };
    this._courseService
      .likeAndDislikeReviewSection(reviewActionPayload)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.updateCourseReview(response?.data);
            // this.getCourseCompleteReview(true);
            // this._messageService.success(`Review ${action} successfully`);
          }
        },
        error: (error: any) => {
          // this._messageService.error(error?.error?.message);
        },
      });
  }

  updateCourseReview(feedback?: any) {
    if (!feedback) {
      return;
    }

    if (
      !this.completeReview?.feedbackComments?.some(
        (comment: any) => comment?.reviewId === feedback?.reviewId
      )
    ) {
      this.completeReview?.feedbackComments?.push(feedback);
    } else {
      this.completeReview?.feedbackComments?.forEach((el: any) => {
        if (el?.reviewId == feedback?.reviewId) {
          el.comment = feedback.comment;
          el.userName = feedback.userName;
          el.rating = feedback.rating;
          el.createdAt = feedback.createdAt;
          el.likes = feedback.likes;
          el.dislikes = feedback.dislikes;
          el.profileImage = feedback.profileImage;
        }
      });
    }
  }

  //*********************************Note Methods*********************************** */

  getNotes() {
    let body = {
      courseId: this.courseId,
      pageNo: 0,
      pageSize: 10,
    };
    this._courseService.getTopicNotes(body)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseNote = [];
          response?.data?.topicNotes.forEach((note: any) => {
            note.disable = true;
            this.courseNote.push(note);
          });
        }
      },
      error: (error: any) => {},
    });
  }

  addNote() {
    let notePayLoad = {
      courseId: this.courseId,
      topicId: this.currentSelectedTopicId,
      note: this.noteText,
      time: this.currentVideoTime,
    };
    this._courseService.createTopicNote(notePayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.getNotes();
          this.noteText = '';
        }
      },
      error: (error: any) => {
        // this._messageService.error(error?.error?.message);
      },
    });
  }

  editNote(noteId: any) {
    this.courseNote.find((note: any) => {
      if (note?.topicNotesId == noteId) {
        note.disable = false;
        this.myInput?.nativeElement?.focus();
      } else {
        note.disable = true;
      }
    });
  }

  updateNote(noteId) {
    let updateNotePayLoad = {};
    let findElement = this.courseNote.find(
      (note: any) => note.topicNotesId == noteId
    );
    if (findElement != undefined) {
      updateNotePayLoad = {
        courseId: this.courseId,
        topicId: this.currentSelectedTopicId,
        note: findElement.notes,
        time: findElement.time,
        topicNotesId: noteId,
      };
      this._courseService.createTopicNote(updateNotePayLoad)?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.getNotes();
          }
        },
        error: (error: any) => {
          // this._messageService.error(error?.error?.message);
        },
      });
    }
  }

  deleteNote(noteId: any) {
    let deleteNotePayLoad = {
      courseId: this.courseId,
      topicId: this.currentSelectedTopicId,
      topicNoteId: noteId,
    };
    this._courseService.deleteTopicNote(deleteNotePayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          if (this.courseNote.length == 1) {
            this.courseNote = [];
            return;
          }
          this.getNotes();
        }
      },
      error: (error: any) => {
        // this._messageService.error(error?.error.message);
      },
    });
  }

  //********************************Summary Methods*********************************** */
  getSummaryReport() {
    this._courseService
      .getTopicSummary(this.currentSelectedTopicId)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.summary = response?.data;
          }
        },
        error: (error: any) => {
          return;
        },
      });
  }

  //********************************Question&Answers Methods**************************** */

  getQuestionList(flag?: boolean) {
    this.questionPayLoad.courseId = this.courseId;
    this._courseService.getQuestions(this.questionPayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.questionAnswersTotalPages = response?.data?.totalPages;
          this.totalQuestions = response?.data?.totalElements;
          if (!this.questionAnswers || flag) {
            this.questionAndAnswers = null;
            this.questionAnswers = response?.data;
          } else {
            response?.data?.questionDetails?.forEach((el: any) => {
              this.questionAnswers.questionDetails.push(el);
            });
          }

          this.questionAnswers.questionDetails.forEach((element: any) => {
            element.showReply = false;
          });
          this.removeDuplicateQuestions();
        }
      },
      error: (error: any) => {
        return;
      },
    });
  }

  removeDuplicateQuestions() {
    if (this.questionAnswers?.questionDetails) {
      const uniqueQuestionsMap = new Map<number, QuestionDetail>();

      this.questionAnswers.questionDetails.forEach((el: QuestionDetail) => {
        if (el.questionId && !uniqueQuestionsMap.has(el.questionId)) {
          uniqueQuestionsMap.set(el.questionId, el);
        }
      });
      this.questionAnswers.questionDetails = Array.from(
        uniqueQuestionsMap.values()
      );
    }
  }

  getQuestionReplies(question: any) {
    question.showReply = false;
    let index = this.questionAnswers?.questionDetails?.findIndex(
      (item: any) => item?.questionId == question?.questionId
    );
    let questionRepliesPayLoad = {
      courseId: this.courseId,
      questionId: question?.questionId,
      pageNo: 0,
      pageSize: 20,
    };
    this._courseService.getQuestionsReplies(questionRepliesPayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.questionAnswers.questionDetails[index].answerDetail =
            response?.data?.answerDetail;
          this.questionAnswers.questionDetails[index].answerDetail.forEach(
            (item: any) => (item.questionId = question?.questionId)
          );
          if (
            this.questionAnswers &&
            this.questionAnswers.questionDetails &&
            this.questionAnswers.questionDetails[index].showQuestionAnswers ==
              undefined
          ) {
            this.questionAnswers.questionDetails[index].showQuestionAnswers =
              this.questionAnswers.questionDetails[index]
                .showQuestionAnswers === undefined
                ? false
                : true;
          }
          this.questionAnswers.questionDetails[index].showQuestionAnswers = this
            .questionAnswers.questionDetails[index].showQuestionAnswers
            ? false
            : true;
        }
      },
      error: (error: any) => {
        return;
      },
    });
  }

  addQuestion() {
    let questionPayLoad = {
      courseId: this.courseId,
      topicId: this.currentSelectedTopicId,
      text: this.questionText,
    };
    this._courseService.createQuestion(questionPayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.questionText = '';
          this.questionAnswers?.questionDetails?.push(response?.data);
          this.removeDuplicateQuestions();
          this.getQuestionList(true);
        }
      },
      error: (error: any) => {
        // this._messageService.error(error.error.message);
      },
    });
  }

  replyQuestion(question: any) {
    let replyPayLoad = {
      courseId: this.courseId,
      answerId: question?.answerId ? question?.answerId : null,
      questionId: question?.questionId,
      text: question?.replyText,
    };

    this._courseService.replyQuestion(replyPayLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.replyText = '';
          // this.getQuestionList(true);
          question.totalReplies += 1;
          this.getQuestionReplies(question);
        }
      },
      error: (error: any) => {
        // this._messageService.error(error.error.message);
      },
    });
  }

  //*******************************Review Methods********************************** */

  getCourseCompleteReview() {
    this.courseReviewPayLoad.courseId = this.courseId;
    this._courseService
      ?.getCourseRatingReviewAndFeedback(this.courseReviewPayLoad)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.courseReviewTotalPages = response?.data?.totalPages;
            this.totalReviewElements = response?.data?.totalElements;

            if (!this.completeReview) {
              this.completeReview = {};
            }

            if (response?.data?.feedback) {
              this.completeReview = {
                ...this.completeReview,
                ...response.data.feedback,
                feedbackComments: response.data.feedback.feedbackComments || [],
              };
            }
            

            this.hasMoreComments =
              this.courseReviewPayLoad.pageNo < this.courseReviewTotalPages - 1;

            this.cdr.detectChanges();
          }
        },
        error: (error: any) => {
          console.error('Error fetching course reviews', error);
        },
      });
  }

  openRatingModal() {
    const modal = this._modal.create({
      nzContent: RatingModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: {
          courseId: this.courseId,
          sectionId: this.currentSelectedSectionId,
        },
        title: 'Rating',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  }

  openReviewModal() {
    const modal = this._modal.create({
      nzContent: ReviewModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: this.courseId,
        title: 'Review',
      },
      nzFooter: null,
      nzKeyboard: true,
    });

    modal.afterClose.subscribe((result: any) => {
      if (result?.data) {
        this.getCourseCompleteReview();
      }
    });
  }

  openShareCourseModal() {
    const modal = this._modal.create({
      nzContent: ShareModalComponent,
      nzViewContainerRef: this._viewContainerRef,
      nzComponentParams: {
        data: this.courseId,
        url: null,
        title: 'Share this course',
        label: 'Share the course URL',
      },
      nzFooter: null,
      nzKeyboard: true,
    });
  }

  getSectionRatingAndReviews() {
    this._courseService
      .getSectionRatingAndReview(this.currentSelectedSectionId)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.sectionReview = response?.data;
          }
        },
        error: (error: any) => {
          if (
            error?.error?.status ==
            this._httpConstants.REQUEST_STATUS.REQUEST_NOT_FOUND_404.CODE
          ) {
            this.sectionReview.value = 0;
            this.sectionReview.totalReviews = 0;
          }
        },
      });
  }

  toggleFavoriteCourse() {
    this._courseService.addOrRemoveCourseToFavorite(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
        }
      },
      error: (error: any) => {
        // this._messageService.error(error.error.message);
      },
    });
  }

  openAlternateCollapseDropdown(sectionId?: any, index?: any) {}

  getAlternateInstructorSections(section?: any) {
    this._courseService
      ?.getAlternateInstructorSections(this.courseId, section?.sectionId)
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.alternateSectionPanelList = response?.data?.details;
            section.alternateSectionPanelList = this.alternateSectionPanelList;
            section.alternateSectionPanelList.forEach((element, index) => {
              if (element?.isFree) {
                this.getAlternateSectionTopicList(section, index);
              }
            });
          }
        },
        error: (error: any) => {
          section.active = false;
          section.showHeader = true;
          this._messageService.error(error?.error?.message);
        },
      });
  }

  pinAlternateInstructor(fromCourseId?: any, fromSectionId?: any) {
    if (this.alternateSectionRouting) {
      this.alternateSectionRouting = false;
      this._courseService
        .pinAlternateInstructor(
          this.toCourseId,
          this.toSectionId,
          fromCourseId,
          fromSectionId
        )
        .subscribe({
          next: (response: any) => {
            if (
              response?.status ==
              this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
            ) {
              this._messageService.success(response?.message);
              this.getCourseSectionList();
            }
          },
          error: (error: any) => {
            // this._messageService.error(error?.error?.message);
          },
        });
    } else {
      this.alternateSectionRouting = true;
    }
  }

  unPinAlternateInstructor(sectionId?: any) {
    this._courseService
      .unPinAlternateInstructor(this.courseId, sectionId)
      .subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this._messageService.success(response?.message);
            this.getCourseSectionList();
          }
        },
        error: (error: any) => {
          // this._messageService.error(error.error.message);
        },
      });
  }

  getAlternateSectionTopicList(section: any, i: number) {
    this._courseService
      ?.getSectionTopics(
        this.courseId,
        section.alternateSectionPanelList[i].sectionId
      )
      ?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            const completedTopics = response?.data.filter(
              (el) => el.isCompleted === true
            );
            let sectionDuration = 0;
            response?.data.forEach((el) => {
              sectionDuration += el.topicDuration;
            });

            section.alternateSectionPanelList[i] = {
              active: false,
              alternateButtonHeight:
                (93 * section.alternateSectionPanelList.length).toString() +
                'px',
              courseId: section.alternateSectionPanelList[i].courseId,
              instructorId: section.alternateSectionPanelList[i].instructorId,
              instructorName:
                section.alternateSectionPanelList[i].instructorName,
              instructorImage:
                section.alternateSectionPanelList[i].instructorImage,
              totalReviewer: section.alternateSectionPanelList[i].totalReviewer,
              totalReviews: section.alternateSectionPanelList[i].totalReviews,
              sectionId: section.alternateSectionPanelList[i].sectionId,
              sectionName: section.alternateSectionPanelList[i].sectionName,
              isFree: section.alternateSectionPanelList[i].isFree,
              totalTopicCompleted: completedTopics.length,
              sectionDuration:
                this.convertSecondsToHoursAndMinutes(sectionDuration),
              courseTitle: section.alternateSectionPanelList[i].courseTitle,
              courseUrl: section.alternateSectionPanelList[i].courseUrl,
              topicList: response?.data,
            };
            section.alternateSectionPanelList[i].topicList.forEach(
              (el: any) => {
                el.topicDuration = this.convertSecondsToHoursAndMinutes(
                  el.topicDuration
                );
              }
            );
          }
        },
        error: (error: any) => {
          // this._messageService.error(error?.error?.message);
        },
      });
  }

  openAlternatePanel(event?: any, section?: any, index?: any) {
    if (!section.free) {
      return;
    }
    section.active = false;
    section.showHeader = false;
    this.toCourseId = this.courseId;
    this.toSectionId = section?.sectionId;

    this.getAlternateInstructorSections(section);
  }

  closeAlternateCollapseDropdown(event?: any, section?: any) {
    section.active = true;
    section.showHeader = true;
    section.alternateSectionPanelList = [];
    event.preventDefault();
    event.stopPropagation();
  }

  alternateToggleSectionPanel(alternateSection: any) {
    alternateSection.active = !alternateSection.active;
    this.alternateSectionRouting = false;
  }

  scrollToBottom(): void {
    try {
      const containerElement = this.chatContainer.nativeElement;
      containerElement.scrollTop = containerElement.scrollHeight;
    } catch (err) {}
  }

  get getInitialOfLoggedInUser() {
    return this._authService.getLoggedInName();
  }

  get getLoggedInPicture() {
    return this._authService.getLoggedInPicture();
  }

  routeToFavoriteCourses() {
    this._router.navigate(['student/favorite-courses']);
  }

  routeToMyCourses() {
    this._router.navigate(['student/my-courses']);
  }

  routeToUpdateProfile() {
    this._router.navigate(['user/update-profile']);
  }

  routeToSubscription() {
    this._router.navigate(['subscription']);
  }

  signOut() {
    this._authService.signOut()?.subscribe({
      next: (response: any) => {
        this._cacheService.clearCache();
        this._router.navigate(['']);
        this._authService.changeNavState(false);
      },
      error: (error: any) => {
        // this._messageService.error(error?.error.message);
      },
    });
  }

  openDoc(doc?: any) {
    window.open(doc.url, '_blank');
  }

  routeToInstructorDashboard() {
    this._authService.isLoggedIn()
      ? this._router.navigate(['/instructor/instructor-dashboard'])
      : this._router.navigate(['/auth/sign-in']);
  }

  getCertificateData() {
    this._certificateService.getCertificateData(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.routeToGenerateCertificate();
        }
      },
      error: (error: any) => {
        // this._messageService.error(error?.error.message);
      },
    });
  }

  routeToGenerateCertificate() {
    this._router.navigate(['student/generate-certificate'], {
      queryParams: { courseId: this.courseId },
    });
  }

  enrolledInCourse() {
    let sectionId;
    let topicId;
    this._activatedRoute?.queryParams?.subscribe((params) => {
      sectionId = parseInt(params['sectionId']);
      topicId = parseInt(params['topicId']);
    });
    this._courseService.enrolledInCourse(this.courseId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          // this._messageService.success(
          //   'You are successfully enrolled in this course'
          // );

          //TODO:
          if (sectionId) {
            this.getCourseSectionList(sectionId, null);
          } else if (topicId) {
            this.getCourseSectionList(null, topicId);
          } else {
            // If neither is present, default behavior
            this.getCourseSectionList(null, null);
          }
          // this.getCourseSectionList();
          this.getSectionTopicsAndChatQuestion();
          this.getCourseProgress();
        }
      },
      error: (error: any) => {
        //TODO:
        if (sectionId && topicId) {
          this.getCourseSectionList(sectionId, topicId);
        } else if (topicId) {
          this.getCourseSectionList(null, topicId);
        } else if (sectionId) {
          this.getCourseSectionList(sectionId, null);
        } else {
          // If neither is present, default behavior
          this.getCourseSectionList(null, null);
        }
        // this.getCourseSectionList();
        this.getSectionTopicsAndChatQuestion();
        this.getCourseProgress();
      },
    });
  }

  manageSeekTime() {
    if (
      this.currentSelectedTopic?.topicType == 'Video' &&
      !this.currentSelectedTopic?.isCompleted
    ) {
      let completePayLoad = {};
      completePayLoad = {
        isCompleted: this.currentSelectedTopic.isCompleted,
        topicId: this.currentSelectedTopicId,
        seekTime: this.getTimeInSec(this.currentVideoTime),
      };
      this.currentSelectedTopic.seekTime = this.getTimeInSec(
        this.currentVideoTime
      );

      this._courseService.markTopicComplete(completePayLoad)?.subscribe({
        next: (response: any) => {
          if (
            response?.status ==
            this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
          ) {
            this.getCourseProgress();
          }
        },
        error: (error: any) => {},
      });
    }
  }

  manageWatchTime() {
    const watchTime = this.watchTime;
    this.watchTime = 0;
    this._courseService.manageWatchTime(this.courseId, watchTime)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.getCourseProgress();
        }
      },
      error: (error: any) => {},
    });
  }

  convertSecondsToHoursAndMinutes(seconds: number): string {
    if (seconds === 0) {
      return '0 sec';
    }

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;

    let result = '';

    if (hours > 0) {
      result += `${hours} hr `;
    }

    if (minutes > 0 || hours > 0) {
      // Ensure minutes are displayed if hours are shown
      result += `${minutes} min `;
    }

    if (remainingSeconds > 0 || (hours === 0 && minutes === 0)) {
      result += `${remainingSeconds} sec`;
    }

    return result.trim();
  }

  manageSeekTimeAfterEveryTwoMins(time) {
    if (time != 0 && (Math.round(time) / 60) % 2 == 0) {
      // Call the function after every two minutes
      this.manageSeekTime();
      this.manageWatchTime();

      // Reset the accumulated time
      this.accumulatedTime = 0;
    }
  }

  routeToInsructorProfile(profileUrl?: any) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: profileUrl },
    });
  }

  onQuestionScroll(event: any): void {
    if (this.questionAnswers.questionDetails.length != this.totalQuestions) {
      const element = event.target;
      const threshold = 100;
      const nearBottom =
        element.scrollHeight - element.scrollTop <=
        element.clientHeight + threshold;

      if (nearBottom) {
        this.questionPayLoad.pageNo += 1;
        if (this.questionPayLoad.pageNo < this.questionAnswersTotalPages) {
          this.getQuestionList(false);
        } else {
          this.questionPayLoad.pageNo -= 1;
        }
      }
    }
  }

  onReviewScroll(event: any): void {
    if (
      this.completeReview.feedbackComments.length < this.totalReviewElements
    ) {
      const element = event.target;
      const threshold = 100;
      const nearBottom =
        element.scrollHeight - element.scrollTop <=
        element.clientHeight + threshold;

      if (nearBottom) {
        if (
          this.courseReviewPayLoad?.pageNo <
          this.courseReviewTotalPages - 1
        ) {
          this.courseReviewPayLoad.pageNo += 1;
          this.getCourseCompleteReview(); // Fetch next page
        }
      }
    }
  }
  handleRatingClick(section: any): void {
    if (!section.free) {
      this.openSubscriptionPlan();
    } else {
      this.openRatingModal();
    }
  }

  reviewCallBack(event: any) {
    if (!event) {
      this.isReviewing = false;
      this.showCongratsScreen = true;
    } else {
      this.isReviewing = true;
      this.questionAnswersQuiz = event;
    }
  }

  retakeQuiz(currentSelectedTopic?: any) {
    this.showCongratsScreen = false;
    this.isReviewing = false;

    let section = this.sectionPanelList.find(
      (section?: any) => section.sectionId == currentSelectedTopic.sectionId
    );
    this.getSectionTopicList(section, 0, true, currentSelectedTopic);
  }

  deleteChat(chat?: any) {
    this._courseService.deleteChat(this.courseId, chat?.chatId)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ==
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.courseChatHistory = [];
          this.getSectionTopicsAndChatQuestion();
        }
      },
      error: (error: any) => {},
    });
  }

  toggleReplyAndScroll(question: any): void {
  question.showReply = !question.showReply;

  setTimeout(() => {
    const element = document.getElementById('reply-input-' + question.questionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      (element as HTMLInputElement).focus();
    }
  }, 100);
}
}
