import { TestBed } from '@angular/core/testing';
import { RoleGuard } from './role.guard';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Route, UrlSegment } from '@angular/router';

describe('RoleGuard', () => {
  let guard: RoleGuard;
  let route: ActivatedRouteSnapshot;
  let state: RouterStateSnapshot;
  let childRoute: ActivatedRouteSnapshot;
  let currentRoute: ActivatedRouteSnapshot;
  let nextState: RouterStateSnapshot;
  let routeMock: Route;
  let segments: UrlSegment[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RoleGuard]
    });
    guard = TestBed.inject(RoleGuard);

    // Create mock objects for parameters
    route = {} as ActivatedRouteSnapshot;
    state = {} as RouterStateSnapshot;
    childRoute = {} as ActivatedRouteSnapshot;
    currentRoute = {} as ActivatedRouteSnapshot;
    nextState = {} as RouterStateSnapshot;
    routeMock = {} as Route;
    segments = [];
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow activation (canActivate)', () => {
    const result = guard.canActivate(route, state);
    expect(result).toBeTrue();
  });

  it('should allow child activation (canActivateChild)', () => {
    const result = guard.canActivateChild(childRoute, state);
    expect(result).toBeTrue();
  });

  it('should allow deactivation (canDeactivate)', () => {
    const result = guard.canDeactivate({}, currentRoute, state, nextState);
    expect(result).toBeTrue();
  });

  it('should allow matching route (canMatch)', () => {
    const result = guard.canMatch(routeMock, segments);
    expect(result).toBeTrue();
  });

  it('should allow loading of route (canLoad)', () => {
    const result = guard.canLoad(routeMock, segments);
    expect(result).toBeTrue();
  });
});
