import {
  Component,
  ElementRef,
  HostListener,
  Input,
  Renderer2,
  ViewChild,
  OnInit,
  EventEmitter,
  Output,
  OnChanges,
  ChangeDetectorRef,
} from '@angular/core';
import { Router } from '@angular/router';
import { ViewAllMap } from 'src/app/core/enums/course-status';

@Component({
  selector: 'app-card-slider',
  templateUrl: './card-slider.component.html',
  styleUrls: ['./card-slider.component.scss'],
})
export class CardSliderComponent implements OnInit, OnChanges {
  @ViewChild('slider', { static: false }) slider: ElementRef;
  @ViewChild('carousel', { static: false }) carousel!: ElementRef;

  @ViewChild('carouselCard', { static: false }) carouselCard!: ElementRef;

  @Input() courses: any[] = [];
  @Input() isLoggedIn: boolean = false;
  @Input() showButtonWhenLoggedIn: boolean = false;
  @Input() courseButtonName: string;
  @Input() cardsToShow: number = 4;
  @Input() count: number = 0;
  @Input() heartFilled: string;
  @Input() heartUnFilled: string;
  @Input() buttonTheme: string;
  @Input() moveButtonRight: boolean;
  @Input() showButton: boolean;
  @Input() isPremium: boolean = false;
  @Input() applyMargin: boolean = false;
  @Output() rightClickEmitter: EventEmitter<any> = new EventEmitter();
  @Output() leftClickEmitter: EventEmitter<any> = new EventEmitter();
  viewAllMap = ViewAllMap;
  visibleCourses = [];
  currentIndex = 0;
  isAnimationInProgress = false;
  enableRightButton = false;
  enableLeftButton = false;
  isRightBtnEnable = true;
  isLeftBtnEnable = false;
  // private startX: number = 0;
  endX: number = 0;

  constructor(
    private _router: Router,
    private renderer: Renderer2,
    private cdr: ChangeDetectorRef
  ) {}

  // ngOnInit() {
  //   this.updateCardsToShow();
  //   this.updateVisibleCourses();
  // }

  ngOnChanges() {
    this.updateVisibleCourses();
  }

  // Adjust number of visible cards based on screen size
  @HostListener('window:resize')
  updateCardsToShow() {
    this.updateVisibleCourses();
  }

  updateVisibleCourses() {
    this.visibleCourses = this.courses.slice(
      this.currentIndex,
      this.currentIndex + this.cardsToShow
    );
  }

  // @HostListener('mousedown', ['$event'])
  // onMouseDown(event?: MouseEvent) {
  //   this.startX = event.clientX;
  //   this.renderer.addClass(this.slider?.nativeElement, 'no-select'); // Disable text selection
  // }

  // @HostListener('mouseup', ['$event'])
  // onMouseUp(event: MouseEvent) {
  //   this.endX = event?.clientX;
  //   this.handleSwipe();
  //   this.renderer.removeClass(this.slider?.nativeElement, 'no-select'); // Re-enable text selection
  // }

  handleSwipe() {
    const distance = this.startX - this.endX;
    const swipeThreshold = 60; // Minimum swipe distance to trigger slide

    if (Math.abs(distance) > swipeThreshold) {
      if (distance > 0) {
        this.slideRight(); // Swipe left to slide right
      } else {
        this.slideLeft(); // Swipe right to slide left
      }
    }
  }

  routeToCourseEmitter(courseUrl: string) {
    this._router.navigate(['student/course-details', courseUrl]);
  }

  routeToInsructorProfile(event) {
    this._router.navigate(['user/profile'], {
      queryParams: { url: event?.profileUrl },
    });
    event?.event?.stopPropagation();
  }

  routeToCourseList(selection?) {
    this._router.navigate(['student/courses'], {
      queryParams: {
        selection: selection,
      },
    });
  }

  // slideLeft() {
  //   if (this.currentIndex > 0 && !this.isAnimationInProgress) {
  //     this.isAnimationInProgress = true;

  //     const newCardIndex = this.currentIndex - 1; // Index of the new left-most card
  //     this.currentIndex -= 1;
  //     this.updateVisibleCourses();

  //     setTimeout(() => {
  //       const sliderElement = this.slider?.nativeElement;
  //       const children = sliderElement?.querySelectorAll('.course'); // Get all `.course` elements
  //       const newCard = sliderElement.querySelector(`.course:nth-child(1)`);

  //       if (newCard) {
  //         this.renderer.addClass(newCard, 'slide-in-left');

  //         newCard.addEventListener(
  //           'animationend',
  //           () => {
  //             this.renderer.removeClass(newCard, 'slide-in-left');
  //             this.renderer.removeClass('', 'opacity-class');
  //             this.isAnimationInProgress = false;
  //           },
  //           { once: true }
  //         );
  //       }
  //     }, 10);
  //   }
  // }

  // slideRight() {
  //   if (
  //     this.currentIndex < this.courses.length - this.cardsToShow &&
  //     !this.isAnimationInProgress
  //   ) {
  //     this.isAnimationInProgress = true;

  //     const newCardIndex = this.currentIndex + this.cardsToShow; // Index of the new right-most card
  //     this.currentIndex += 1;
  //     this.updateVisibleCourses();
  //     setTimeout(() => {
  //       const sliderElement = this.slider?.nativeElement;
  //       const children = sliderElement?.querySelectorAll('.course'); // Get all `.course` elements
  //       const newCard = sliderElement.querySelector(
  //         `.course:nth-child(${this.cardsToShow})`
  //       );

  //       if (newCard) {
  //         this.renderer.addClass(newCard, 'slide-in-right');
  //         newCard.addEventListener(
  //           'animationend',
  //           () => {
  //             this.renderer.removeClass(newCard, 'slide-in-right');
  //             this.isAnimationInProgress = false;
  //           },
  //           { once: true }
  //         );
  //       }
  //     }, 10);
  //   }
  // }

  autoPlayTimeout: any;
  isDragging = false;
  startX = 0;
  startScrollLeft = 0;

  // ngOnInit() {
  //   this.autoPlay();
  // }

  // ngOnDestroy() {
  //   clearTimeout(this.autoPlayTimeout);
  // }

  // slideLeft() {
  //   const carousel = this.carousel?.nativeElement;
  //   carousel.scrollLeft -= 350;
  // }

  // slideRight() {
  //   const carousel = this.carousel?.nativeElement;
  //   carousel.scrollLeft += 350;
  // }

  // dragStart(event: MouseEvent) {
  //   this.isDragging = true;
  //   this.startX = event.pageX;
  //   this.startScrollLeft = this.carousel?.nativeElement?.scrollLeft;
  //   this.carousel.nativeElement.classList.add('dragging');
  // }

  // dragging(event: MouseEvent) {
  //   if (!this.isDragging) return;
  //   const deltaX = event.pageX - this.startX;
  //   this.carousel.nativeElement.scrollLeft = this.startScrollLeft - deltaX;
  // }

  // dragStop() {
  //   this.isDragging = false;
  //   this.carousel?.nativeElement?.classList?.remove('dragging');
  // }

  // autoPlay() {
  //   if (window?.innerWidth < 800) return;

  //   const carousel = this.carousel?.nativeElement;
  //   const maxScrollLeft = carousel?.scrollWidth - carousel?.offsetWidth;

  //   if (carousel?.scrollLeft >= maxScrollLeft) return;

  //   this.autoPlayTimeout = setTimeout(() => {
  //     carousel.scrollLeft += carousel?.offsetWidth;
  //     this.autoPlay();
  //   }, 2500);
  // }

  ngOnInit() {
    // this.autoPlay();
  }

  ngOnDestroy() {
    clearTimeout(this.autoPlayTimeout);
  }

  ngAfterViewInit() {
    this.updateButtonStates();
  }

  slideLeft() {
    const carousel = this.carousel?.nativeElement;
    const carouselCard = this.carouselCard?.nativeElement;
    const carouselCardWidth = carouselCard?.offsetWidth;
    this.isRightBtnEnable = true;

    carousel.scrollBy({
      left: -carouselCardWidth,
      behavior: 'smooth',
    });

    const isAtStart = Math.ceil(carousel.scrollLeft - carouselCardWidth) <= 0;

    if (isAtStart) {
      this.isLeftBtnEnable = false;
    }

    this.updateButtonStates();
  }

  slideRight() {
    const carousel = this.carousel?.nativeElement;
    const carouselCard = this.carouselCard?.nativeElement;
    const carouselCardWidth = carouselCard?.offsetWidth;

    carousel.scrollBy({
      left: carouselCardWidth,
      behavior: 'smooth',
    });

    this.isLeftBtnEnable = true;
    const isOneCardBeforeEnd =
      Math.ceil(
        carousel.scrollLeft + carousel.clientWidth + carouselCardWidth
      ) >= carousel.scrollWidth;

    if (isOneCardBeforeEnd) {
      this.isRightBtnEnable = false;
    }

    this.updateButtonStates();
  }

  updateButtonStates() {
    const carousel = this.carousel?.nativeElement;
    if (!carousel) return;

    const maxScrollLeft = carousel?.scrollWidth - carousel?.offsetWidth;
    this.enableLeftButton = carousel?.scrollLeft <= 0;
    this.enableRightButton = carousel?.scrollLeft >= maxScrollLeft;

    this.cdr.detectChanges();
  }

  autoPlay() {
    if (window?.innerWidth < 800) return;

    const carousel = this.carousel?.nativeElement;
    const maxScrollLeft = carousel?.scrollWidth - carousel?.offsetWidth;

    if (carousel?.scrollLeft >= maxScrollLeft) return;

    this.autoPlayTimeout = setTimeout(() => {
      carousel.scrollLeft += carousel?.offsetWidth;
      this.updateButtonStates();
      this.autoPlay();
    }, 2500);
  }
}
