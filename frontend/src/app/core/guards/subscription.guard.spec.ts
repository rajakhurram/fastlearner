import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { SubscriptionGuard } from './subscription.guard';
import { of } from 'rxjs';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('SubscriptionGuard', () => {
  let guard: SubscriptionGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const spy = jasmine.createSpyObj('SocialAuthService', ['signIn'], {
      authState: of(null),
    });

    TestBed.configureTestingModule({
      providers: [
        SubscriptionGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: SocialAuthService, useValue: spy },
        HttpClient,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    });

    guard = TestBed.inject(SubscriptionGuard);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  describe('canActivate', () => {
    it('should return true if user is logged in', () => {
      authService.isLoggedIn.and.returnValue(true);

      const result = guard.canActivate({} as any, {} as any);
      expect(result).toBeTrue();
    });

    it('should return false and navigate to sign-in if user is not logged in', () => {
      authService.isLoggedIn.and.returnValue(false);

      const result = guard.canActivate({} as any, {} as any);
      expect(result).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/auth/sign-in']);
    });
  });

  describe('canActivateChild', () => {
    it('should return true for child routes', () => {
      const result = guard.canActivateChild({} as any, {} as any);
      expect(result).toBeTrue();
    });
  });

  describe('canDeactivate', () => {
    it('should return true for deactivation', () => {
      const result = guard.canDeactivate({}, {} as any, {} as any, {} as any);
      expect(result).toBeTrue();
    });
  });

  describe('canMatch', () => {
    it('should return true for route matching', () => {
      const result = guard.canMatch({} as any, [] as any);
      expect(result).toBeTrue();
    });
  });

  describe('canLoad', () => {
    it('should return true for lazy loading', () => {
      const result = guard.canLoad({} as any, [] as any);
      expect(result).toBeTrue();
    });
  });
});
