import { TestBed } from '@angular/core/testing';
import { CacheService } from './cache.service';
import { CommunicationService } from './communication.service';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('CacheService', () => {
  let service: CacheService;
  let communicationServiceSpy: jasmine.SpyObj<CommunicationService>;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(() => {
    communicationServiceSpy = jasmine.createSpyObj('CommunicationService', ['removeEmitter']);
    socialAuthServiceSpy = jasmine.createSpyObj('SocialAuthService', ['signOut']);

    TestBed.configureTestingModule({
      providers: [
        CacheService,
        { provide: CommunicationService, useValue: communicationServiceSpy },
        { provide: SocialAuthService, useValue: socialAuthServiceSpy },
      ],
    });

    service = TestBed.inject(CacheService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should save and retrieve data from localStorage', () => {
    const key = 'testKey';
    const value = 'testValue';

    service.saveInCache(key, value);
    const result = service.getDataFromCache(key);

    expect(result).toBe(value);
  });

  it('should remove data from localStorage', () => {
    const key = 'testKey';
    const value = 'testValue';

    service.saveInCache(key, value);
    service.removeFromCache(key);

    const result = service.getDataFromCache(key);
    expect(result).toBe('');
  });

  it('should clear localStorage and call signOut and removeEmitter', () => {
    service.clearCache();

    expect(localStorage.length).toBe(0);
    expect(socialAuthServiceSpy.signOut).toHaveBeenCalled();
  });

  it('should cache login data correctly', () => {
    const data = {
      user: { id: '123', role: 'admin', userType: 'premium' },
      token: 'mockToken',
      expiryTime: '2024-08-15T12:00:00Z'
    };

    service.cacheLoginData(data);

    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.USER)).toBe(JSON.stringify(data.user));
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.ROLE)).toBe(JSON.stringify(data.user.role));
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.TOKEN)).toBe(data.token);
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.EXPIRY_TIME)).toBe(data.expiryTime);
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.USER_ID)).toBe(data.user.id);
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.USER_TYPE)).toBe(data.user.userType);
  });

  it('should cache user details correctly', () => {
    const data = {
      user: { id: '123' },
      groupName: 'group',
    };

    service.cacheUserDetails(data);

    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.USER)).toBe(JSON.stringify(data.user));
    expect(service.getDataFromCache('GroupName')).toBe(data.groupName);
  });

  it('should save and retrieve notifications as an array', () => {
    const key = 'notifications';
    const data = [{ id: 1, message: 'Test notification' }];

    service.saveNotifications(key, data);
    const result = service.getNotifications(key);

    expect(result).toEqual(data);
  });

  it('should save and retrieve course data', () => {
    const key = 'courseData';
    const data = { courseId: 1, title: 'Test Course' };

    service.saveCourseData(key, data);
    const result = service.getCourseData(key);

    expect(result).toEqual(data);
  });

  it('should handle null or undefined values in cache operations', () => {
    const key = 'testKey';
    service.saveInCache(key, null);
    const result = service.getDataFromCache(key);

    expect(result).toBe('null');

    service.saveInCache(key, undefined);
    const resultAfterUndefined = service.getDataFromCache(key);

    expect(resultAfterUndefined).toBe('undefined');
  });
  it('should handle empty notifications gracefully', () => {
    const key = 'notifications';

    service.saveNotifications(key, []);
    const result = service.getNotifications(key);

    expect(result).toEqual([]);
  });

  it('should handle empty course data gracefully', () => {
    const key = 'courseData';

    service.saveCourseData(key, {});
    const result = service.getCourseData(key);

    expect(result).toEqual({});
  });

  it('should return empty string if key does not exist in cache', () => {
    const result = service.getDataFromCache('nonExistingKey');
    expect(result).toBe('');
  });

  it('should handle null values in cacheUserDetails', () => {
    const data = { user: null, groupName: null };
    service.cacheUserDetails(data);
    expect(service.getDataFromCache(service.constantDataHolder.CACHE_KEYS.USER)).toBe('null');
    expect(service.getDataFromCache('GroupName')).toBe('null');
  });

  it('should use consistent cache keys for user data', () => {
    const keys = [
      service.constantDataHolder.CACHE_KEYS.USER,
      service.constantDataHolder.CACHE_KEYS.ROLE,
      service.constantDataHolder.CACHE_KEYS.TOKEN,
      service.constantDataHolder.CACHE_KEYS.EXPIRY_TIME,
      service.constantDataHolder.CACHE_KEYS.USER_ID,
      service.constantDataHolder.CACHE_KEYS.USER_TYPE
    ];

    keys.forEach(key => {
      service.saveInCache(key, 'testValue');
      expect(service.getDataFromCache(key)).toBe('testValue');
    });
  });
});
