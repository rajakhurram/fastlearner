import { TestBed } from '@angular/core/testing';
import { CookieService } from 'ngx-cookie-service';
import { CookiesService } from './cookie.service';

describe('CookiesService', () => {
  let service: CookiesService;
  let cookieService: jasmine.SpyObj<CookieService>;

  beforeEach(() => {
    cookieService = jasmine.createSpyObj('CookieService', [
      'set',
      'get',
      'delete',
      'deleteAll',
    ]);

    TestBed.configureTestingModule({
      providers: [
        CookiesService,
        { provide: CookieService, useValue: cookieService },
      ],
    });

    service = TestBed.inject(CookiesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set and get token', () => {
    cookieService.get.and.returnValue('token-value');

    service.setToken('token-value');

    expect(cookieService.set).toHaveBeenCalledWith('token', 'token-value');
    expect(service.getToken()).toBe('token-value');
  });

  it('should set and get refresh token', () => {
    cookieService.get.and.returnValue('refresh-value');

    service.setRefreshToken('refresh-value');

    expect(cookieService.set).toHaveBeenCalledWith(
      'refreshToken',
      'refresh-value',
    );
    expect(service.getRefresToken()).toBe('refresh-value');
  });

  it('should remove tokens', () => {
    service.removeToken();
    service.removeRefresToken();

    expect(cookieService.delete).toHaveBeenCalledWith('token', '/');
    expect(cookieService.delete).toHaveBeenCalledWith('refreshToken', '/');
  });

  it('should remove all cookies', () => {
    service.removeAllData();
    expect(cookieService.deleteAll).toHaveBeenCalled();
  });
});
