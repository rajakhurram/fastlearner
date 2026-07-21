import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AdminGuard } from './admin.guard';

describe('AdminGuard', () => {
  let guard: AdminGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', [
      'isLoggedIn',
      'isSuperAdmin',
    ]);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        AdminGuard,
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });

    guard = TestBed.inject(AdminGuard);
  });

  it('should redirect unauthenticated users to sign-in', () => {
    const urlTree = {} as UrlTree;
    authService.isLoggedIn.and.returnValue(false);
    router.createUrlTree.and.returnValue(urlTree);

    expect(guard.canActivate({} as any, {} as any)).toBe(urlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/sign-in']);
  });

  it('should redirect non-super-admins to home', () => {
    const urlTree = {} as UrlTree;
    authService.isLoggedIn.and.returnValue(true);
    authService.isSuperAdmin.and.returnValue(false);
    router.createUrlTree.and.returnValue(urlTree);

    expect(guard.canLoad({} as any, [])).toBe(urlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/']);
  });

  it('should allow super admins', () => {
    authService.isLoggedIn.and.returnValue(true);
    authService.isSuperAdmin.and.returnValue(true);

    expect(guard.canActivate({} as any, {} as any)).toBeTrue();
  });
});
