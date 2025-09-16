import { TestBed } from '@angular/core/testing';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { of, Subject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { Subscription } from 'rxjs';

describe('NotificationsRoutingModule', () => {
  let router: Router;
  let titleService: Title;
  let module: NotificationsRoutingModule;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [Title , NotificationsRoutingModule]
    });

    router = TestBed.inject(Router);
    titleService = TestBed.inject(Title);
    module = TestBed.inject(NotificationsRoutingModule);
  });

  describe('ngAfterViewInit', () => {
    it('should set title on navigation end', () => {
      const setTitleSpy = spyOn(titleService, 'setTitle');
      const mockEvent = new NavigationEnd(1, '/', '/');

      // Create a Subject to mock the router events observable
      const events$ = new Subject<any>();

      // Spy on router.events.subscribe and return an observable
      spyOn(router.events, 'subscribe').and.callFake((callback) => {
        // Emit the mock event
        events$.subscribe(callback);
        return new Subscription();  // Return a proper Subscription object
      });

      const getTitleSpy = spyOn(module, 'getTitle').and.returnValue(['Notifications']);

      module.ngAfterViewInit();

      // Emit the mock event
      events$.next(mockEvent);

      expect(getTitleSpy).toHaveBeenCalledWith(router.routerState, router.routerState.root);
      expect(setTitleSpy).toHaveBeenCalledWith('Notifications');
    });
  });

  describe('getTitle', () => {
    it('should return title from the root if available', () => {
      const state = {};  // Mock state
      const parent = {
        snapshot: {
          data: {
            title: 'Test Title'
          }
        },
        children: []
      };

      const title = module.getTitle(state, parent);

      expect(title).toEqual(['Test Title']);
    });

    it('should return title from nested child', () => {
      const state = {};  // Mock state
      const parent = {
        snapshot: {
          data: {}
        },
        children: [{
          snapshot: {
            data: {
              title: 'Child Title'
            }
          },
          children: []  // No further children
        }]
      };

      const title = module.getTitle(state, parent);

      expect(title).toEqual(['Child Title']);
    });

    it('should return an empty array if no title is found', () => {
      const state = {};  // Mock state
      const parent = {
        snapshot: {
          data: {}
        },
        children: []
      };

      const title = module.getTitle(state, parent);

      expect(title).toEqual([]);
    });
  });
});
