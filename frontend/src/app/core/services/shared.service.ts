import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment.development';
import { filter, map, mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class SharedService {
  constructor(
    private _http: HttpClient,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private titleService: Title
  ) {}

  private updateNavBar = new Subject<void>();

  private updateSectionRatingAndReviewsDetails = new Subject<void>();

  private updateFavouriteCourseMenu = new Subject<void>();

  updateNavDetail() {
    this.updateNavBar.next();
  }

  getNavDetail() {
    return this.updateNavBar.asObservable();
  }

  updateSectionRatingAndReviews() {
    this.updateSectionRatingAndReviewsDetails.next();
  }

  getSectionRatingAndReviews() {
    return this.updateSectionRatingAndReviewsDetails.asObservable();
  }

  updateFavCourseMenu() {
    this.updateFavouriteCourseMenu.next();
  }

  getFavCourseMenu() {
    return this.updateFavouriteCourseMenu.asObservable();
  }

  subscribeNewsLetter(email?: any): Observable<any> {
    const url = `${environment.baseUrl}newsletter-subscription/subscribe?email=${email}`;
    return this._http.post(url, null);
  }

  initialize(): void {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => this.activatedRoute),
        map((route) => {
          while (route.firstChild) route = route.firstChild;
          return route;
        }),
        mergeMap((route) => route.data)
      )
      .subscribe((data) => {
        this.titleService.setTitle(
          data['title'] ||
            'Fast Learner, AI-Enabled Learning Platform Transforming Education'
        );
      });
  }
}
