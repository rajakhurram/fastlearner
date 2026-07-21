import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  ViewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import { AppConstants } from 'src/app/core/constants/app.constants';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SubscriptionService } from 'src/app/core/services/subscription.service';
import { environment } from 'src/environments/environment.development';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { forkJoin } from 'rxjs';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';

@Component({
  selector: 'app-pricing-page',
  templateUrl: './pricing-page.component.html',
  styleUrls: ['./pricing-page.component.scss']
})
export class PricingPageComponent implements OnInit, OnDestroy {

  @Input() fromSubscriptionPlan = false;
  @Input() showFreePlan?: boolean = true;
  @Input() showStandardPlan?: boolean = true;
  @Input() currentPlanId?: string | null;
  _httpConstants: HttpConstants = new HttpConstants();
  _appConstants: AppConstants = new AppConstants();


  logos: string[] = [
    'assets/icons/01.svg',
    'assets/icons/02.svg',
    'assets/icons/03.svg',
    'assets/icons/04.svg',
    'assets/icons/05.svg',
    'assets/icons/06.svg',
    'assets/icons/07.svg',
    'assets/icons/08.svg',
    'assets/icons/09.svg',
  ];
  faqContainerSectionsLoaded = true;

  features = [
    {
      title: 'AI Grader',
      shortDescription: 'Smarter Evaluation, Faster Results',
      longDescription:
        'Revolutionize assessments with our powerful AI Grader — capable of evaluating both digital and handwritten copies in seconds. Delivering instant, precise, and bias-free feedback, it helps educators save hours while improving grading accuracy and consistency.',
      image: 'assets/images/AI-Grader.png',
    },
    {
      title: 'Chat with AI',
      shortDescription: 'Your Intelligent Learning Partner — Available 24/7',
      longDescription:
        'Experience the future of learning with FastLearner.ai’s built-in AI chat assistant. Ask questions, clear concepts, and explore new ideas in real time. Whether you need instant explanations, subject guidance, or quick study support, our AI ensures you never learn alone.',
      image: 'assets/images/Laptop.png',
    },
    {
      title: 'Standard course access',
      shortDescription: 'Unlimited Knowledge — One Simple Subscription',
      longDescription:
        'Unlock a rich collection of standard-tier courses with a single subscription. No more paying per course — just continuous, affordable learning designed to keep you ahead in your field.',
      image: 'assets/images/Chat.png',
    },
    {
      title: 'Alternate instructors',
      shortDescription: 'Learn from the Best — in Every Style',
      longDescription:
        ' FastLearner.ai enhances your learning journey by recommending alternate instructors who teach the same subject in different ways. Gain diverse insights, compare teaching approaches, and build a deeper, more complete understanding of every topic.',
      image: 'assets/images/Alternate.png',
    },
    {
      title: 'Platform branded certificate',
      shortDescription: 'Earn Certificates That Build Credibility',
      longDescription:
        ' Celebrate your progress with official FastLearner.ai certificates, awarded upon course or test completion. Professionally designed and globally shareable, they showcase your verified achievements and strengthen your professional profile on LinkedIn, résumés, and portfolios.',
      image: 'assets/images/Platform.png',
    },
    // {
    //   title: 'Email support',
    //   shortDescription: 'Fast, Friendly, and Reliable Assistance',
    //   longDescription:
    //     'Our dedicated support team is always ready to help. From technical issues to course access and billing questions, we ensure timely responses and seamless solutions — because your learning experience matters most.',
    //   image: 'assets/images/AI-Grader.png',
    // },

  ];

  items: AccordionItems[] = [
    {
      title:
        'What makes Fast Learner different from the other online learning platforms?',
      description: `FastLearner differs from other online learning platforms due to its innovative features, such as AI-powered Q&A for swift answers, customizable learning paths for a tailored experience, and video summaries for quick review.
      `,
      isExpanded: false,
    },
    {
      title: 'Is Fast Learner boring like traditional textbook learning?',
      description: `Not at all! Fast Learner provides bite-sized lessons, remote education prospects, interactive content (think videos and quizzes!), and customizing courses by mixing and matching instructors to make the learning experience engaging. So you can improve your skills without feeling stuck at any point.
      `,
      isExpanded: false,
    },
    {
      title: 'How much does Fast Learner cost?',
      description: `Fast Learner's digital AI learning platform operates on a subscription model. For a single monthly fee, you can enjoy personalized learning and unlimited access to our complete course library.
      `,
      isExpanded: false,
    },
    {
      title: 'Can I learn at my own pace with Fast Learner?',
      description: `Yes! Fast Learner prioritizes personal and professional development with flexible learning. You can take courses anytime, anywhere, and alter the speed to suit your schedule.
      `,
      isExpanded: false,
    },
    {
      title: 'Do I get any certification upon finishing a course?',
      description: `Absolutely! Fast Learner awards certificates upon successful course completion. These certificates display your accomplishments of skill development and devotion to professional growth.
      `,
      isExpanded: false,
    },
    {
      title: 'How do I start with Fast Learner?',
      description: `Simply create an account on our digital learning platform and discover a vast course library! Many courses provide free previews, and you can subscribe whenever you are ready to unlock the complete learning experience.
      `,
      isExpanded: false,
    },
    {
      title: 'What is a quick learning ability?',
      description: `A quick learner is a person who can understand new information in a short amount of time. When someone learns quickly, they typically have excellent communication and listening abilities. FastLearner.ai will help you learn quickly by providing innovative AI-based learning tools.
      `,
      isExpanded: false,
    },
    {
      title: 'What are the benefits of fast learning?',
      description: `Quick learning benefits professional development, and professionals who learn quickly tend to be more productive. This is because they can adapt to new situations faster and successfully. The key to learning quickly is having a high level of curiosity and enthusiasm to ask questions when required.
      `,
      isExpanded: false,
    },
  ];
  isAcrdionExpanded: boolean = false;

  selectedFeature = this.features[0];

  selectFeature(feature: any): void {
    this.selectedFeature = feature;
  }

  @ViewChild('logoCarousel') logoCarousel!: ElementRef;
  @ViewChild('faq_container') faq_container!: ElementRef;
  @ViewChild('carousel') carousel!: ElementRef;
  autoPlayTimeout: any;

  subscriptionList: Array<any> = [];
  noSubscriptionPresent: boolean = false;
  isFreePlanSelected: boolean = false;
  isPlanSelected?: boolean = false;
  displayAnnual: boolean = true; // Track the plan type (Annual or Monthly)
  // switchValue?: boolean = false;
  constructor(
    private _changeDetectorRef: ChangeDetectorRef,
    private _router: Router,
    @Optional() private modalRef: NzModalRef,
    private _authService: AuthService,
    private _messageService: MessageService,
    private _cacheService: CacheService,
    private _subscriptionService: SubscriptionService,
    private _modal: NzModalService
  ) { }

  ngOnInit(): void {
    if (!this.subscriptionList?.length) {
      this.getSubscriptionPlanList();
    }
    this.autoPlayLogos();

  }

  ngOnDestroy(): void {
    // if (
    //   !this.isPlanSelected &&
    //   !this.fromSubscriptionPlan &&
    //   !this.isFreePlanSelected
    // ) {
    //   this._authService.verifyUserSubscription().subscribe({
    //     next: (response: any) => {
    //       if (
    //         !response?.data?.currentPlan ||
    //         response?.data?.currentPlan !== 'Free Plan'
    //       ) {
    //         this.handleFreePlan();
    //       }
    //     },
    //     error: (error: any) => {
    //       console.log('Failed to verify subscription:', error);
    //     },
    //   });
    // }
  }

  togglePlanType(isAnnual: boolean): void {
    this.displayAnnual = isAnnual;
  }

  getSubscriptionPlanList() {
    this._authService.getSubscriptionPlans()?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          this.subscriptionList = response?.data?.filter(
            (plan: any) =>
              (!this.currentPlanId || plan.id !== this.currentPlanId) &&
              (this.showStandardPlan || plan.planType !== 'STANDARD')
          );

          this.noSubscriptionPresent = false;
          this._changeDetectorRef.detectChanges();
        } else {
          this.noSubscriptionPresent = true;
          this.isPlanSelected = false;
        }
      },
      error: (error: any) => {
        this.noSubscriptionPresent = true;
        console.error(error);
      },
    });
  }

  onSelectPlan(
    subscriptionPlanType: any,
    subscriptionId: any,
    paypalPlanId: any
  ) {
    this.isPlanSelected = true;
    if (subscriptionPlanType == 'Free Plan') {
      this.isFreePlanSelected = true;
      let payLoad = {
        paypalPlanId: paypalPlanId,
        subscriptionId: subscriptionId,
      };
      this.subscribeToPlan(payLoad, subscriptionPlanType);
    } else {
      this._router.navigate(['payment-method'], {
        queryParams: { subscriptionId: subscriptionId },
      });
    }
  }

  handleFreePlan(): void {
    this._authService.verifyUserSubscription().subscribe({
      next: (response: any) => {
        const isSubscribed = response?.data === true;
        const currentPlan = response?.data?.currentPlan;

        if (!isSubscribed) {
          this.isFreePlanSelected = true;
          this._authService.newUserSubscription().subscribe({
            next: () => {
              forkJoin([
                this._subscriptionService.loadSubscriptionPermissions(),
                this._subscriptionService.fetchCurrentSubscriptionPlanType(),
              ]).subscribe({
                next: () => {
                  this.navigateToLandingPage();
                },
                error: () => {
                  this._messageService.error('Error while processing APIs.');
                },
              });
            },
            error: () =>
              this._messageService.error('Error processing free subscription.'),
          });
        } else if (currentPlan && currentPlan !== 'Free Plan') {
          this.isFreePlanSelected = true;
          this._authService.cancelSubscription().subscribe({
            next: (res: any) => {
              if (
                res?.status ===
                this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
              ) {
                this.navigateToLandingPage();
              }
            },
          });
        } else {
          this.isFreePlanSelected = true;
          this.modalRef?.close();
        }
      },
      error: () =>
        this._messageService.error('Failed to fetch subscription details.'),
    });
    this.navigateToLandingPage();

  }

  // Helper method to handle navigation
  private navigateToLandingPage(): void {
    const redirectUrl = this._cacheService.getDataFromCache('redirectUrl');
    if (redirectUrl) {
      this._router.navigateByUrl(environment.basePath); // Redirect to base path (landing page)
    } else {
      this._router.navigate(['/']); // Fallback to root if no redirect URL is found
    }
  }

  subscribeToPlan(payLoad: any, subscriptionPlanType: any) {
    if (
      subscriptionPlanType ===
      this._appConstants.SUBSCRIPTION_PLAN_TYPE.FREE_PLAN
    ) {
      this.handleFreePlan();
    }

    this._authService.createSubscription(payLoad)?.subscribe({
      next: (response: any) => {
        if (
          response?.status ===
          this._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE
        ) {
          forkJoin([
            this._subscriptionService.loadSubscriptionPermissions(),
            this._subscriptionService.fetchCurrentSubscriptionPlanType(),
          ]).subscribe({
            next: () => {
              switch (subscriptionPlanType) {
                case this._appConstants.SUBSCRIPTION_PLAN_TYPE.FREE_PLAN:
                  this._messageService.success(
                    'You are Successfully Subscribed To Free Plan'
                  );
                  this._subscriptionService.updateUserSubscriptionCheck(true);

                  const redirectUrl =
                    this._cacheService.getDataFromCache('redirectUrl');
                  if (redirectUrl) {
                    this._cacheService.removeFromCache('redirectUrl');
                    this._router.navigateByUrl(redirectUrl);
                  } else {
                    this._router.navigateByUrl(environment.basePath);
                  }
                  break;

                case this._appConstants.SUBSCRIPTION_PLAN_TYPE.STANDARD_PLAN:
                  this._messageService.success(
                    'You are Successfully Subscribed To Standard Plan'
                  );
                  window.open(response?.data, '_self');
                  break;

                case this._appConstants.SUBSCRIPTION_PLAN_TYPE.ANNUAL_PLAN:
                  this._messageService.success(
                    'You are Successfully Subscribed To Annual Plan'
                  );
                  window.open(response?.data, '_self');
                  break;
              }

              this.isPlanSelected = true;
            },
            error: () => {
              this._messageService.error('Error while processing APIs.');
            },
          });
        }
      },
      error: (error: any) => {
        console.log(error);
        if (
          error?.error?.status ===
          this._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE
        ) {
          this._messageService.error(error?.error?.message);
        }
      },
    });
  }

  getSvgForCard(planType: string): string {
    switch (planType) {
      case 'PREMIUM':
        return '../../../../assets/icons/premium-sparkle.svg';
      case 'ULTIMATE':
        return '../../../../assets/icons/ultimate-sparkle.svg';
      default:
        return '../../../../assets/icons/sparkle.svg';
    }
  }

  toggleSwitch(value: boolean) {
    this.displayAnnual = value;
  }
  autoPlayLogos() {
    const carousel = this.logoCarousel?.nativeElement;
    if (!carousel) return;

    const maxScrollLeft = carousel.scrollWidth - carousel.offsetWidth;

    if (carousel.scrollLeft >= maxScrollLeft) {
      carousel.scrollLeft = 0; // 🔁 loop to start
    } else {
      carousel.scrollBy({
        left: 200, // adjust scroll step
        behavior: 'smooth',
      });
    }

    this.autoPlayTimeout = setTimeout(() => {
      this.autoPlayLogos();
    }, 2500);
  }

  expand(faq, event: Event) {
    const container = (event.currentTarget as HTMLElement).parentElement;
    const descriptionEl = container?.querySelector('.acc-description');

    if (!descriptionEl) return;

    if (faq.isExpanded) {
      descriptionEl.classList.remove('expanded');
      faq.isExpanded = false;
    } else {
      descriptionEl.classList.add('expanded');
      faq.isExpanded = true;
    }
  }

  loadFAQContainer() {
    this.faqContainerSectionsLoaded = true;
  }



  scrollToComparison(): void {
    const element = document.getElementById('comparison-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  scrollToSubscription(): void {
    const element = document.getElementById('subscription');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  goToAiGrader() {
    this.isPlanSelected = true;
    this._router.navigate(['/ai-grader']);
  }


}