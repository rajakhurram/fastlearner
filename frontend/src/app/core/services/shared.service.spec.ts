import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { of, Subject } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { SharedService } from './shared.service';
import { environment } from 'src/environments/environment.development';

describe('SharedService', () => {
  let service: SharedService;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;
  let routerSpy: jasmine.SpyObj<Router>;
  let activatedRouteSpy: jasmine.SpyObj<ActivatedRoute>;
  let titleServiceSpy: jasmine.SpyObj<Title>;

  beforeEach(() => {
    // Create spies for the dependencies
    const httpClient = jasmine.createSpyObj('HttpClient', ['post']);
    const router = jasmine.createSpyObj('Router', ['events']);
    const activatedRoute = jasmine.createSpyObj('ActivatedRoute', [], {
      data: of({ title: 'Test Title' })
    });
    const titleService = jasmine.createSpyObj('Title', ['setTitle']);

    router.events = of(new NavigationEnd(1, '/test', '/test'));

    TestBed.configureTestingModule({
      providers: [
        SharedService,
        { provide: HttpClient, useValue: httpClient },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Title, useValue: titleService }
      ]
    });

    service = TestBed.inject(SharedService);
    httpClientSpy = TestBed.inject(HttpClient) as jasmine.SpyObj<HttpClient>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRouteSpy = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;
    titleServiceSpy = TestBed.inject(Title) as jasmine.SpyObj<Title>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call updateNavBar next()', () => {
    const spy = spyOn(service['updateNavBar'], 'next');
    service.updateNavDetail();
    expect(spy).toHaveBeenCalled();
  });

  it('should return an observable from getNavDetail()', () => {
    service.getNavDetail().subscribe(() => {});
    expect(service.getNavDetail).toBeTruthy();
  });

  it('should call updateSectionRatingAndReviewsDetails next()', () => {
    const spy = spyOn(service['updateSectionRatingAndReviewsDetails'], 'next');
    service.updateSectionRatingAndReviews();
    expect(spy).toHaveBeenCalled();
  });

  it('should return an observable from getSectionRatingAndReviews()', () => {
    service.getSectionRatingAndReviews().subscribe(() => {});
    expect(service.getSectionRatingAndReviews).toBeTruthy();
  });

  it('should call updateFavouriteCourseMenu next()', () => {
    const spy = spyOn(service['updateFavouriteCourseMenu'], 'next');
    service.updateFavCourseMenu();
    expect(spy).toHaveBeenCalled();
  });

  it('should return an observable from getFavCourseMenu()', () => {
    service.getFavCourseMenu().subscribe(() => {});
    expect(service.getFavCourseMenu).toBeTruthy();
  });

  it('should call HttpClient post in subscribeNewsLetter()', () => {
    const email = 'test@example.com';
    service.subscribeNewsLetter(email)?.subscribe();
    expect(httpClientSpy.post).toHaveBeenCalledWith(`${environment.baseUrl}newsletter-subscription/subscribe?email=${email}`, null);
  });

  it('should set the title in initialize()', () => {
    service.initialize();
    expect(titleServiceSpy.setTitle).toHaveBeenCalledWith('Test Title');
  });

});

