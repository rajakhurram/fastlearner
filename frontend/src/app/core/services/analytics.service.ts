// analytics.service.ts
import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import {
  googleTrackingId,
} from '../constants/http.constants';

declare let gtag: Function;

@Injectable({
  providedIn: 'root',
})
export class AnalyticsService {
  constructor(private router: Router) {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.sendPageView(event.urlAfterRedirects);
      });
  }

  sendPageView(url: string) {
    if (typeof gtag === 'function') {
      gtag('config', googleTrackingId, {
        page_path: url,
      });
    }
  }

  sendEvent(
    eventCategory: string,
    eventAction: string,
    eventLabel: string = null,
    value: number = null
  ) {
    if (typeof gtag === 'function') {
      gtag('event', eventAction, {
        event_category: eventCategory,
        event_label: eventLabel,
        value: value,
      });
    }
  }
}
