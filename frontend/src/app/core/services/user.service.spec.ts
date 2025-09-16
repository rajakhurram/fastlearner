import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { of } from 'rxjs';

import { UserService } from './user.service';
import { environment } from 'src/environments/environment.development';

describe('UserService', () => {
  let service: UserService;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(() => {
    // Create a spy for HttpClient
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get', 'post']);

    TestBed.configureTestingModule({
      imports: [HttpClientModule], // Import HttpClientModule for HttpClient
      providers: [
        UserService,
        { provide: HttpClient, useValue: httpClientSpy }
      ]
    });

    service = TestBed.inject(UserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return user profile data when getUserProfile is called', () => {
    const expectedProfile = { name: 'John Doe', email: 'john.doe@example.com' };

    // Set up the spy to return an observable of the expected data
    httpClientSpy.get.and.returnValue(of(expectedProfile));

    service.getUserProfile().subscribe(profile => {
      expect(profile).toEqual(expectedProfile);
    });

    expect(httpClientSpy.get.calls.count()).toBe(1, 'one call to get');
  });

  it('should call post with the correct URL and body when updateUserProfile is called', () => {
    const updateBody = { name: 'Jane Doe' };
    const response = { success: true };

    // Set up the spy to return an observable of the response
    httpClientSpy.post.and.returnValue(of(response));

    service.updateUserProfile(updateBody).subscribe(res => {
      expect(res).toEqual(response);
    });

    expect(httpClientSpy.post.calls.count()).toBe(1, 'one call to post');
    expect(httpClientSpy.post.calls.mostRecent().args[0]).toBe(`${environment.baseUrl}user-profile/update`, 'correct URL');
    expect(httpClientSpy.post.calls.mostRecent().args[1]).toEqual(updateBody, 'correct body');
  });

  it('should call post with the correct URL and body when changeUserPassword is called', () => {
    const changePasswordBody = { oldPassword: 'oldPass', newPassword: 'newPass' };
    const response = { success: true };

    // Set up the spy to return an observable of the response
    httpClientSpy.post.and.returnValue(of(response));

    service.changeUserPassword(changePasswordBody).subscribe(res => {
      expect(res).toEqual(response);
    });

    expect(httpClientSpy.post.calls.count()).toBe(1, 'one call to post');
    expect(httpClientSpy.post.calls.mostRecent().args[0]).toBe(`${environment.baseUrl}user/change-password`, 'correct URL');
    expect(httpClientSpy.post.calls.mostRecent().args[1]).toEqual(changePasswordBody, 'correct body');
  });
});
