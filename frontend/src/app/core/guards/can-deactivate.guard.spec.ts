import { TestBed } from '@angular/core/testing';
import { CanDeactivateGuard } from './can-deactivate.guard';
import { CanComponentDeactivate } from './can-deactivate.guard';
import { of } from 'rxjs';

class MockComponent implements CanComponentDeactivate {
  canDeactivate(): boolean {
    return true;
  }
}

describe('CanDeactivateGuard', () => {
  let guard: CanDeactivateGuard;
  let mockComponent: MockComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CanDeactivateGuard],
    });
    guard = TestBed.inject(CanDeactivateGuard);
    mockComponent = new MockComponent();
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should return true when canDeactivate method returns true', () => {
    spyOn(mockComponent, 'canDeactivate').and.returnValue(true);
    expect(guard.canDeactivate(mockComponent)).toBeTrue();
  });

  it('should return false when canDeactivate method returns false', () => {
    spyOn(mockComponent, 'canDeactivate').and.returnValue(false);
    expect(guard.canDeactivate(mockComponent)).toBeFalse();
  });

});
