import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  ElementRef,
  NO_ERRORS_SCHEMA,
  Renderer2,
} from '@angular/core';
import { CardSliderComponent } from './card-slider.component';
import { Router } from '@angular/router';

describe('CardSliderComponent', () => {
  let component: CardSliderComponent;
  let fixture: ComponentFixture<CardSliderComponent>;
  let mockRenderer: jasmine.SpyObj<Renderer2>;

  beforeEach(async () => {
    mockRenderer = jasmine.createSpyObj('Renderer2', ['listen', 'setStyle']);

    await TestBed.configureTestingModule({
      declarations: [CardSliderComponent],
      imports: [RouterTestingModule],
      providers: [{ provide: Renderer2, useValue: mockRenderer }],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CardSliderComponent);
    component = fixture.componentInstance;
    component.courses = [
      { id: 1, name: 'Course 1' },
      { id: 2, name: 'Course 2' },
      { id: 3, name: 'Course 3' },
      { id: 4, name: 'Course 4' },
      { id: 5, name: 'Course 5' },
    ];
    component.cardsToShow = 3;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });
  it('should update visible courses on ngOnChanges', () => {
    component.currentIndex = 0;
    component.ngOnChanges();
    expect(component.visibleCourses).toEqual([
      { id: 1, name: 'Course 1' },
      { id: 2, name: 'Course 2' },
      { id: 3, name: 'Course 3' },
    ]);
  });

  it('should update button states without ExpressionChangedAfterItHasBeenCheckedError', fakeAsync(() => {
    component.carousel = {
      nativeElement: {
        scrollLeft: 0,
        scrollWidth: 1500,
        offsetWidth: 1000,
      },
    } as ElementRef;

    component.updateButtonStates();
    tick(); // Ensure async operations are resolved
    expect(component.enableLeftButton).toBeTrue();
    expect(component.enableRightButton).toBeFalse();
  }));
  it('should slide left and update button states', () => {
    component.carousel = {
      nativeElement: {
        scrollLeft: 700,
        scrollWidth: 2000,
        offsetWidth: 1000,
        scrollBy: jasmine.createSpy('scrollBy'),
      },
    } as ElementRef;
    spyOn(component, 'updateButtonStates');

    component.slideLeft();
    expect(component.carousel.nativeElement.scrollBy).toHaveBeenCalledWith({
      left: -350,
      behavior: 'smooth',
    });
    expect(component.updateButtonStates).toHaveBeenCalled();
    expect(component.updateButtonStates).toHaveBeenCalled();
  });
  it('should slide right and update button states', () => {
    component.carousel = {
      nativeElement: {
        scrollLeft: 350,
        scrollWidth: 2000,
        offsetWidth: 1000,
        scrollBy: jasmine.createSpy('scrollBy'),
      },
    } as ElementRef;
    spyOn(component, 'updateButtonStates');

    component.slideRight();

    expect(component.carousel.nativeElement.scrollBy).toHaveBeenCalledWith({
      left: 350,
      behavior: 'smooth',
    });
    expect(component.updateButtonStates).toHaveBeenCalled();
  });
  it('should correctly update button states', () => {
    component.carousel = {
      nativeElement: {
        scrollLeft: 0,
        scrollWidth: 1500,
        offsetWidth: 1000,
      },
    } as ElementRef;

    component.updateButtonStates();
    expect(component.enableLeftButton).toBeTrue();
    expect(component.enableRightButton).toBeFalse();

    component.carousel.nativeElement.scrollLeft = 500;
    component.updateButtonStates();
    expect(component.enableLeftButton).toBeFalse();

    component.carousel.nativeElement.scrollLeft = 500;
    component.carousel.nativeElement.scrollWidth = 1000;
    component.updateButtonStates();
    expect(component.enableRightButton).toBeTrue();
  });
  it('should slide right on left swipe', () => {
    component.startX = 200;
    component.endX = 100; // Swiped left
    spyOn(component, 'slideRight');
    spyOn(component, 'slideLeft');

    component.handleSwipe();
    expect(component.slideRight).toHaveBeenCalled();
    expect(component.slideLeft).not.toHaveBeenCalled();
  });

  it('should slide left on right swipe', () => {
    component.startX = 100;
    component.endX = 200; // Swiped right
    spyOn(component, 'slideRight');
    spyOn(component, 'slideLeft');

    component.handleSwipe();
    expect(component.slideLeft).toHaveBeenCalled();
    expect(component.slideRight).not.toHaveBeenCalled();
  });
  it('should navigate to course details page', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate');

    const courseUrl = 'course-1';
    component.routeToCourseEmitter(courseUrl);

    expect(navigateSpy).toHaveBeenCalledWith([
      'student/course-details',
      courseUrl,
    ]);
  });
});
