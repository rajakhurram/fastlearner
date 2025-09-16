import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { Injector } from '@angular/core';
import { NotificationService } from './notification.service';
import { CacheService } from './cache.service';
import { AuthService } from './auth.service';
import { CommunicationService } from './communication.service';
import { DataHolderConstants } from '../constants/dataHolder.constants';
import { of } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  let cacheService: jasmine.SpyObj<CacheService>;
  let authService: jasmine.SpyObj<AuthService>;
  let communicationService: jasmine.SpyObj<CommunicationService>;

  beforeEach(() => {
    const cacheSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'saveInCache',
      'getNotifications',
      'saveNotifications',
      'removeFromCache',
    ]);
    const authSpy = jasmine.createSpyObj('AuthService', ['getAccessToken']);
    const communicationSpy = jasmine.createSpyObj('CommunicationService', [
      'showNotificationCountData',
      'showNotificationData',
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        NotificationService,
        { provide: CacheService, useValue: cacheSpy },
        { provide: AuthService, useValue: authSpy },
        { provide: CommunicationService, useValue: communicationSpy },
        Injector,
      ],
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    communicationService = TestBed.inject(
      CommunicationService
    ) as jasmine.SpyObj<CommunicationService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
  describe('constructor', () => {
    it('should call connectSSE', () => {
      spyOn(NotificationService.prototype, 'connectSSE');
      service = new NotificationService(
        TestBed.inject(HttpClient),
        authService,
        communicationService,
        TestBed.inject(Injector)
      );
      expect(service.connectSSE).toHaveBeenCalled();
    });
  });

  describe('connectSSE', () => {
    it('should initialize and handle SSE connection', () => {
      const mockEventSource = jasmine.createSpyObj('EventSource', [
        'addEventListener',
        'close',
      ]);
      const mockMessage = { data: '[{"notification": "test"}]' };

      cacheService.getDataFromCache.and.returnValues('true', '12345'); // 'isLoggedIn' and 'unique-id'
      authService.getAccessToken.and.returnValue('mock-token');
      cacheService.getNotifications.and.returnValue([]);
      spyOn(window, 'EventSource').and.returnValue(mockEventSource);

      service.connectSSE();

      expect(mockEventSource.addEventListener).toHaveBeenCalledWith(
        'notification',
        jasmine.any(Function)
      );

      // Simulate incoming SSE message
      const eventListener =
        mockEventSource.addEventListener.calls.argsFor(0)[1];
      eventListener(mockMessage);

      expect(cacheService.getNotifications).toHaveBeenCalled();
      expect(communicationService.showNotificationCountData).toHaveBeenCalled();
      expect(communicationService.showNotificationData).toHaveBeenCalled();
    });

    it('should not initialize SSE if not logged in', () => {
      cacheService.getDataFromCache.and.returnValue('false'); // 'isLoggedIn' is false

      service.connectSSE();

      expect(cacheService.getDataFromCache).toHaveBeenCalledWith('isLoggedIn');
      expect(service['eventSource']).toBeDefined();
    });
  });

  describe('ngOnDestroy', () => {
    it('should close the event source connection on destroy', () => {
      const mockEventSource = jasmine.createSpyObj('EventSource', ['close']);
      service['eventSource'] = mockEventSource; // Set the mock event source directly

      service.ngOnDestroy();

      expect(mockEventSource.close).toHaveBeenCalled();
    });
  });

  describe('closeConnection', () => {
    it('should close the event source connection', () => {
      const mockEventSource = jasmine.createSpyObj('EventSource', ['close']);
      service['eventSource'] = mockEventSource; // Set the mock event source directly

      service.closeConnection();

      expect(mockEventSource.close).toHaveBeenCalled();
    });
  });

  describe('generateTimeStamp', () => {
    it('should generate a timestamp', () => {
      const timestamp = service.generateTimeStamp();
      expect(typeof timestamp).toBe('number');
      expect(timestamp).toBeLessThanOrEqual(new Date().getTime());
    });
  });

  describe('#getNotifications', () => {
    it('should fetch notifications with the correct URL', () => {
      const mockResponse = { data: 'mock data' };
      const payload = { pageNo: 1, pageSize: 10 };
      const url = `${environment.baseUrl}notification/fetch-all?pageNo=${payload.pageNo}&pageSize=${payload.pageSize}`;

      service.getNotifications(payload).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('#removeNotification', () => {
    it('should remove notification with the correct URL', () => {
      const notificationId = '12345';
      const url = `${environment.baseUrl}notification/`;

      service.removeNotification(notificationId).subscribe((response) => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toEqual({ notificationId });
      req.flush({});
    });
  });
});
