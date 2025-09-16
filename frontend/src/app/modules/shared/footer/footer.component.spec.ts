import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FooterComponent } from './footer.component';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;
  let router: jasmine.SpyObj<Router>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const sharedServiceSpy = jasmine.createSpyObj('SharedService', [
      'subscribeNewsLetter',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [FooterComponent],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: SharedService, useValue: sharedServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: ActivatedRoute, useValue: {} }, // Provide a stub for ActivatedRoute
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    sharedService = TestBed.inject(
      SharedService
    ) as jasmine.SpyObj<SharedService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set activeLink based on the current route', () => {
    const routeSpy = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    routeSpy.snapshot = { _routerState: { url: 'contact-us' } } as any;

    component.setActiveLink();
    expect(component.activeLink).toBe('contact-us');
  });

  it('should navigate to landing page', () => {
    component.routeToLandingPage();
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });

  it('should navigate to about us page', () => {
    component.routeToAboutUs();
    expect(router.navigate).toHaveBeenCalledWith(['about-us']);
  });

  it('should open Instagram link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToInstagram();
    expect(window.open).toHaveBeenCalledWith(
      'https://www.instagram.com/fastlearnerai/',
      '_blank'
    );
  });

  it('should validate email correctly', () => {
    component.validateEmail({ target: { value: 'test@example.com' } });
    expect(component.emailValid).toBe(true);

    component.validateEmail({ target: { value: 'invalid-email' } });
    expect(component.emailValid).toBe(false);
  });

  it('should subscribe to newsletter and show success message on success', () => {
    component.subscribeEmail = 'test@example.com';
    component.emailValid = true;

    sharedService.subscribeNewsLetter.and.returnValue(
      of({ message: 'Subscribed successfully' })
    );

    component.subscribeNewsLetter();

    expect(sharedService.subscribeNewsLetter).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(messageService.success).toHaveBeenCalledWith(
      'Subscribed successfully'
    );
    expect(component.subscribeEmail).toBe('');
    expect(component.emptyEmail).toBe(false);
  });

  it('should show error message and clear email on failure', () => {
    component.subscribeEmail = 'test@example.com';
    component.emailValid = true;

    sharedService.subscribeNewsLetter.and.returnValue(
      throwError(() => ({ error: { message: 'Subscription failed' } }))
    );

    component.subscribeNewsLetter();

    expect(sharedService.subscribeNewsLetter).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(messageService.error).toHaveBeenCalledWith('Subscription failed');
    expect(component.subscribeEmail).toBe('');
    expect(component.emptyEmail).toBe(false);
  });

  it('should scroll to the courses section', () => {
    spyOn(document, 'getElementById').and.returnValue({
      scrollIntoView: jasmine.createSpy(),
    } as any);
    component.scrollToCoursesSection();
    expect(document.getElementById).toHaveBeenCalledWith('courses-section');
    expect(
      (document.getElementById as jasmine.Spy).calls.mostRecent().returnValue
        .scrollIntoView
    ).toHaveBeenCalledWith({
      behavior: 'smooth',
      block: 'start',
      inline: 'nearest',
    });
  });

  it('should set activeLink based on the current route', () => {
    const routeSpy = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    routeSpy.snapshot = { _routerState: { url: 'contact-us' } } as any;

    component.setActiveLink();
    expect(component.activeLink).toBe('contact-us');
  });

  it('should navigate to landing page', () => {
    component.routeToLandingPage();
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });

  it('should navigate to about us page', () => {
    component.routeToAboutUs();
    expect(router.navigate).toHaveBeenCalledWith(['about-us']);
  });

  it('should open Instagram link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToInstagram();
    expect(window.open).toHaveBeenCalledWith(
      'https://www.instagram.com/fastlearnerai/',
      '_blank'
    );
  });

  it('should open LinkedIn link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToLinkedIn();
    expect(window.open).toHaveBeenCalledWith(
      'https://www.linkedin.com/company/fastlearner/',
      '_blank'
    );
  });

  it('should open Twitter link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToTwitter();
    expect(window.open).toHaveBeenCalledWith(
      'https://x.com/fastlearner_ai?s=11&t=Vt_WkfQUCv78CQwfkOBmGw',
      '_blank'
    );
  });

  it('should open Facebook link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToFacebook();
    expect(window.open).toHaveBeenCalledWith(
      'http://www.facebook.com/FastlearnerAI',
      '_blank'
    );
  });

  it('should open Blogs link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToBlogs();
    expect(window.open).toHaveBeenCalledWith(
      'https://blog.fastlearner.ai/',
      '_blank'
    );
  });

  it('should navigate to the Privacy Policy page', () => {
    component.routeToPrivacyPolicy();
    expect(router.navigate).toHaveBeenCalledWith(['privacy-policy']);
  });

  it('should navigate to the Terms and Conditions page', () => {
    component.routeToTermsandConditions();
    expect(router.navigate).toHaveBeenCalledWith(['terms-and-conditions']);
  });

  it('should navigate to the Courses List page', () => {
    component.routeToCoursesList('CATEGORY_COURSES');
    expect(router.navigate).toHaveBeenCalledWith(['/student/courses'], {
      queryParams: { selection: 'CATEGORY_COURSES' },
    });
  });

  it('should navigate to the Instructor Welcome page', () => {
    component.routeToInstructorWelcomePage();
    expect(router.navigate).toHaveBeenCalledWith(['become-instructor']);
  });

  it('should navigate to the Contact Us page', () => {
    component.routeToContactUs();
    expect(router.navigate).toHaveBeenCalledWith(['contact-us']);
  });

  it('should open Vinncorp link in a new tab', () => {
    spyOn(window, 'open');
    component.routeToVinncorp();
    expect(window.open).toHaveBeenCalledWith('https://vinncorp.com/', '_blank');
  });

  it('should validate email correctly', () => {
    component.validateEmail({ target: { value: 'test@example.com' } });
    expect(component.emailValid).toBe(true);

    component.validateEmail({ target: { value: 'invalid-email' } });
    expect(component.emailValid).toBe(false);
  });

  it('should subscribe to newsletter and show success message on success', () => {
    component.subscribeEmail = 'test@example.com';
    component.emailValid = true;

    sharedService.subscribeNewsLetter.and.returnValue(
      of({ message: 'Subscribed successfully' })
    );

    component.subscribeNewsLetter();

    expect(sharedService.subscribeNewsLetter).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(messageService.success).toHaveBeenCalledWith(
      'Subscribed successfully'
    );
    expect(component.subscribeEmail).toBe('');
    expect(component.emptyEmail).toBe(false);
  });

  it('should show error message and clear email on failure', () => {
    component.subscribeEmail = 'test@example.com';
    component.emailValid = true;

    sharedService.subscribeNewsLetter.and.returnValue(
      throwError(() => ({ error: { message: 'Subscription failed' } }))
    );

    component.subscribeNewsLetter();

    expect(sharedService.subscribeNewsLetter).toHaveBeenCalledWith(
      'test@example.com'
    );
    expect(messageService.error).toHaveBeenCalledWith('Subscription failed');
    expect(component.subscribeEmail).toBe('');
    expect(component.emptyEmail).toBe(false);
  });

  it('should scroll to the courses section', () => {
    spyOn(document, 'getElementById').and.returnValue({
      scrollIntoView: jasmine.createSpy(),
    } as any);
    component.scrollToCoursesSection();
    expect(document.getElementById).toHaveBeenCalledWith('courses-section');
    expect(
      (document.getElementById as jasmine.Spy).calls.mostRecent().returnValue
        .scrollIntoView
    ).toHaveBeenCalledWith({
      behavior: 'smooth',
      block: 'start',
      inline: 'nearest',
    });
  });
});
