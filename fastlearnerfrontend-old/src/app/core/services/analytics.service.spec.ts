import { TestBed } from '@angular/core/testing';
import { Router, NavigationEnd } from '@angular/router';
import { AnalyticsService } from './analytics.service';
import { Observable, of } from 'rxjs';
import { filter } from 'rxjs/operators';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let router: Router;
  let gtagSpy: jasmine.Spy;

  beforeEach(() => {
    const routerMock = {
      events: of(new NavigationEnd(0, '/test', '/test')).pipe(
        filter((event) => event instanceof NavigationEnd)
      ),
      navigate: jasmine.createSpy('navigate')
    };

    gtagSpy = jasmine.createSpy('gtag');

    TestBed.configureTestingModule({
      providers: [
        AnalyticsService,
        { provide: Router, useValue: routerMock }
      ],
      schemas :[NO_ERRORS_SCHEMA , CUSTOM_ELEMENTS_SCHEMA]
    });

    service = TestBed.inject(AnalyticsService);
    router = TestBed.inject(Router);

    // Assign the global gtag function
    (window as any).gtag = gtagSpy;
  });

  afterEach(() => {
    // Clean up the global gtag function after each test
    delete (window as any).gtag;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call gtag with event tracking data', () => {
    const eventCategory = 'button';
    const eventAction = 'click';
    const eventLabel = 'example button';
    const value = 10;

    service.sendEvent(eventCategory, eventAction, eventLabel, value);

    expect(gtagSpy).toHaveBeenCalledWith('event', eventAction, {
      event_category: eventCategory,
      event_label: eventLabel,
      value: value,
    });
  });

  it('should handle event tracking without optional parameters', () => {
    const eventCategory = 'button';
    const eventAction = 'click';

    service.sendEvent(eventCategory, eventAction);

    expect(gtagSpy).toHaveBeenCalledWith('event', eventAction, {
      event_category: eventCategory,
      event_label: null,
      value: null,
    });
  });
});
