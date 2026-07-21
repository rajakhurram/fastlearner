import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CacheService } from '../services/cache.service';
import { PermissionGuard } from './permission.guard';

describe('PermissionGuard', () => {
  let guard: PermissionGuard;
  let cacheService: jasmine.SpyObj<CacheService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    cacheService = jasmine.createSpyObj('CacheService', ['getDataFromCache']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        PermissionGuard,
        { provide: AuthService, useValue: {} },
        { provide: CacheService, useValue: cacheService },
        { provide: Router, useValue: router },
      ],
    });

    guard = TestBed.inject(PermissionGuard);
  });

  it('should allow load when permission exists', () => {
    cacheService.getDataFromCache.and.returnValue(
      JSON.stringify(['CREATE_COURSE']),
    );

    const canLoad = guard.canLoad(
      { data: { requiredPermission: 'CREATE_COURSE' } } as any,
      [],
    );

    expect(canLoad).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect when permission is missing', () => {
    cacheService.getDataFromCache.and.returnValue(JSON.stringify([]));

    const canLoad = guard.canLoad(
      { data: { requiredPermission: 'CREATE_COURSE' } } as any,
      [],
    );

    expect(canLoad).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/sign-in']);
  });
});
