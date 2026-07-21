import { ActivatedRoute, Event, Router } from '@angular/router';
import { of, Subject } from 'rxjs';

export function createRouterEventsMock(
  url = '/',
): jasmine.SpyObj<Pick<Router, 'navigate' | 'parseUrl'>> & {
  url: string;
  events: ReturnType<Subject<Event>['asObservable']>;
} {
  const events = new Subject<Event>();
  return jasmine.createSpyObj('Router', ['navigate', 'parseUrl'], {
    url,
    events: events.asObservable(),
  });
}

export function createActivatedRouteMock(
  snapshot: Partial<ActivatedRoute['snapshot']> = {},
): Partial<ActivatedRoute> {
  return {
    snapshot: snapshot as ActivatedRoute['snapshot'],
    params: of({}),
    queryParams: of({}),
  };
}
