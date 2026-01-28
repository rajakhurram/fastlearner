import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { GetStartedComponent } from './get-started.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { HttpConstants } from 'src/app/core/constants/http.constants';
import { Role } from 'src/app/core/enums/Role';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('GetStartedComponent', () => {
  let component: GetStartedComponent;
  let fixture: ComponentFixture<GetStartedComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'saveRole',
      'verifyUserSubscription',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'saveInCache',
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      declarations: [GetStartedComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    });

    fixture = TestBed.createComponent(GetStartedComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    authService.verifyUserSubscription.and.returnValue(
      of({ status: 200, data: true })
    );
    authService.saveRole.and.returnValue(of({ status: 200 }));
    cacheService.saveInCache.and.callThrough();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should call verifyUserIsSubscribed on initialization', () => {
      spyOn(component, 'verifyUserIsSubscribed').and.callThrough();
      component.ngOnInit();
      expect(component.verifyUserIsSubscribed).toHaveBeenCalled();
    });
  });

  describe('verifyUserIsSubscribed', () => {
    it('should set isSubscribed to true if API call is successful', () => {
      component.verifyUserIsSubscribed();
      expect(component.isSubscribed).toBeTrue();
    });

    it('should not set isSubscribed if API call fails', () => {
      authService.verifyUserSubscription.and.returnValue(
        throwError(() => new Error('Error'))
      );
      component.verifyUserIsSubscribed();
      expect(component.isSubscribed).toBeUndefined();
    });
  });

  describe('saveUserRole', () => {
    beforeEach(() => {
      component.selectedRoleStudent = false;
      component.selectedRoleInstructor = false;
    });

    it('should set saveRole to STUDENT if selectedRoleStudent is true', () => {
      component.selectedRoleStudent = true;
      component.saveUserRole();
      expect(component.saveRole).toBe('STUDENT');
      expect(cacheService.saveInCache).toHaveBeenCalledWith('role', 'STUDENT');
      expect(authService.saveRole).toHaveBeenCalledWith('STUDENT');
    });

    it('should set saveRole to INSTRUCTOR if selectedRoleInstructor is true', () => {
      component.selectedRoleInstructor = true;
      component.saveUserRole();
      expect(component.saveRole).toBe('INSTRUCTOR');
      expect(cacheService.saveInCache).toHaveBeenCalledWith(
        'role',
        'INSTRUCTOR'
      );
      expect(authService.saveRole).toHaveBeenCalledWith('INSTRUCTOR');
    });

    it('should navigate to correct route based on role and subscription status', () => {
      component.selectedRoleStudent = true;
      component.isSubscribed = true;
      component.saveUserRole();
      expect(router.navigate).toHaveBeenCalledWith(['student']);

      component.isSubscribed = false;
      component.saveUserRole();
      expect(router.navigate).toHaveBeenCalledWith(['subscription-plan']);

      component.selectedRoleStudent = false;
      component.selectedRoleInstructor = true;
      component.saveUserRole();
      expect(router.navigate).toHaveBeenCalledWith(['instructor']);
    });

    it('should handle API errors', () => {
      authService.saveRole.and.returnValue(
        throwError(() => new Error('Error'))
      );
      component.saveUserRole();
      // Optionally, check if the error is handled appropriately
    });
  });

  describe('toggleRole', () => {
    it('should toggle selectedRoleStudent when role is STUDENT', () => {
      component.toggleRole('STUDENT');
      expect(component.selectedRoleStudent).toBeTrue();
      component.toggleRole('STUDENT');
      expect(component.selectedRoleStudent).toBeFalse();
    });

    it('should toggle selectedRoleInstructor when role is INSTRUCTOR', () => {
      component.toggleRole('INSTRUCTOR');
      expect(component.selectedRoleInstructor).toBeTrue();
      component.toggleRole('INSTRUCTOR');
      expect(component.selectedRoleInstructor).toBeFalse();
    });
  });
});
