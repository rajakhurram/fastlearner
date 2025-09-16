import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';
import { WelcomePageInstructorComponent } from './welcome-page-instructor.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpBackend, HttpClient, HttpHandler } from '@angular/common/http';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { of } from 'rxjs';

describe('WelcomePageInstructorComponent', () => {
  let component: WelcomePageInstructorComponent;
  let fixture: ComponentFixture<WelcomePageInstructorComponent>;
  let routerSpy: jasmine.SpyObj<Router>;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(async () => {
    const spySocial = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });
    const spy = jasmine.createSpyObj('Router', ['navigate']);
    
    await TestBed.configureTestingModule({
      declarations: [WelcomePageInstructorComponent],
      providers: [
        { provide: Router, useValue: spy },
        { provide: SocialAuthService, useValue: spySocial },
        AuthService,
        HttpClient,
        HttpHandler,
        HttpBackend
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(WelcomePageInstructorComponent);
    component = fixture.componentInstance;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to course creation on routeToCreateCourse', () => {
    component.routeToCreateCourse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/instructor/course']);
  });

  it('should navigate to instructor dashboard on skip', () => {
    component.skip();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/instructor/instructor-dashboard']);
  });
});
