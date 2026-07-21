import {
  ComponentFixture,
  fakeAsync,
  flush,
  TestBed,
  tick,
} from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { of, throwError, Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

import { UserProfileComponent } from './user-profile.component';
import { AuthService } from 'src/app/core/services/auth.service';
import { CacheService } from 'src/app/core/services/cache.service';
import { FileManager } from 'src/app/core/services/file-manager.service';
import { MessageService } from 'src/app/core/services/message.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { UserService } from 'src/app/core/services/user.service';
import { User } from 'src/app/core/models/user.model';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NzUploadXHRArgs } from 'ng-zorro-antd/upload';
import { ProfileUploaderModalComponent } from '../../dynamic-modals/profile-uploader-modal/profile-uploader-modal.component';
import { By } from '@angular/platform-browser';

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let cacheService: jasmine.SpyObj<CacheService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let userService: jasmine.SpyObj<UserService>;
  let socialAuthService: jasmine.SpyObj<SocialAuthService>;
  let modalService: jasmine.SpyObj<NzModalService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let fileManagerService: jasmine.SpyObj<FileManager>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'signUp',
      'signUpWithSocialAccount',
      'changeNavState',
      'saveRole',
      'isSubscribed',
    ]);
    const cacheServiceSpy = jasmine.createSpyObj('CacheService', [
      'getDataFromCache',
      'saveInCache',
    ]);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', [
      'success',
      'error',
    ]);
    const userServiceSpy = jasmine.createSpyObj('UserService', [
      'getUserProfile',
      'updateUserProfile',
    ]);
    const socialAuthServiceSpy = jasmine.createSpyObj('SocialAuthService', [], {
      authState: of(null),
    });
    const modalServiceSpy = jasmine.createSpyObj('NzModalService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const activatedRouteSpy = jasmine.createSpyObj('ActivatedRoute', [], {
      snapshot: { queryParams: {} },
    });
    const fileManagerServiceSpy = jasmine.createSpyObj('FileManager', [
      'uploadFile',
    ]);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, HttpClientTestingModule, FormsModule],
      declarations: [UserProfileComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CacheService, useValue: cacheServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: SocialAuthService, useValue: socialAuthServiceSpy },
        { provide: NzModalService, useValue: modalServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRouteSpy },
        { provide: FileManager, useValue: fileManagerServiceSpy },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    cacheService = TestBed.inject(CacheService) as jasmine.SpyObj<CacheService>;
    messageService = TestBed.inject(
      MessageService
    ) as jasmine.SpyObj<MessageService>;
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    socialAuthService = TestBed.inject(
      SocialAuthService
    ) as jasmine.SpyObj<SocialAuthService>;
    modalService = TestBed.inject(
      NzModalService
    ) as jasmine.SpyObj<NzModalService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(
      ActivatedRoute
    ) as jasmine.SpyObj<ActivatedRoute>;
    fileManagerService = TestBed.inject(
      FileManager
    ) as jasmine.SpyObj<FileManager>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the user profile on init', () => {
    const mockUserProfile = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: { fullName: 'Test User', email: 'test@example.com' },
    };
    userService.getUserProfile.and.returnValue(of(mockUserProfile));
    component.ngOnInit();
    expect(userService.getUserProfile).toHaveBeenCalled();
    expect(component.user).toEqual(mockUserProfile.data);
  });

  it('should handle error during profile fetch', () => {
    userService.getUserProfile.and.returnValue(throwError('Error'));
    component.ngOnInit();
    expect(userService.getUserProfile).toHaveBeenCalled();
    expect(component.user).toEqual(new User()); // Ensure user remains in its initial state
  });

  it('should update user profile successfully', () => {
    const mockUpdateResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      message: 'Profile updated successfully',
      data: { fullName: 'Test User Updated' },
    };
    const modalSpy = spyOn(component, 'openShareCourseModal');
    userService.updateUserProfile.and.returnValue(of(mockUpdateResponse));
    cacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ name: 'Test User' })
    );
    component.user = { fullName: 'Test User', email: 'test@example.com' };
    component.updateUserProfile();
    expect(userService.updateUserProfile).toHaveBeenCalled();
    expect(modalSpy).toHaveBeenCalled();
  });

  it('should handle error during profile update', () => {
    const modalSpy = spyOn(component, 'openShareCourseModal');
    userService.updateUserProfile.and.returnValue(throwError('Error'));
    component.user = { fullName: 'Test User', email: 'test@example.com' };
    component.updateUserProfile();
    expect(userService.updateUserProfile).toHaveBeenCalled();
    expect(modalSpy).not.toHaveBeenCalled();
  });

  it('should open share course modal on successful update', () => {
    const mockUpdateResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      message: 'Profile updated successfully',
      data: { fullName: 'Test User Updated' },
    };
    userService.updateUserProfile.and.returnValue(of(mockUpdateResponse));
    cacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ name: 'Test User' })
    );
    component.user = { fullName: 'Test User', email: 'test@example.com' };
    component.updateUserProfile();
    expect(modalService.create).toHaveBeenCalled();
  });

  it('should handle invalid profile update', () => {
    const modalSpy = spyOn(component, 'openShareCourseModal');
    const invalidUpdateResponse = {
      status: component._httpConstants.REQUEST_STATUS.BAD_REQUEST_400.CODE,
      message: 'Invalid profile data',
    };
    userService.updateUserProfile.and.returnValue(of(invalidUpdateResponse));
    component.user = { fullName: '', email: 'invalid-email' };
    component.updateUserProfile();
    expect(modalSpy).not.toHaveBeenCalled();
  });

  it('should navigate to student landing page on modal close', () => {
    const modalRef = modalService.create({
      nzContent: 'Modal Content',
    });
    component.afterCloseSubscription = new Subscription();
    component.afterCloseSubscription.add(
      modalRef?.afterClose.subscribe(() => {
        component.routeToStudentLandingPage();
        expect(router.navigate).toHaveBeenCalledWith(['student']);
      })
    );
    component.openShareCourseModal();
  });

  it('should handle custom image upload request', () => {
    const mockFile = new File([''], 'filename', { type: 'image/jpeg' });
    const mockResponse = { data: 'image-url' };
    fileManagerService.uploadFile.and.returnValue(of(mockResponse));
    const mockUploadArgs = { file: mockFile } as unknown as NzUploadXHRArgs;
    const subscription = component.customRequestImage(mockUploadArgs);
    subscription.add(() => {
      expect(component.user.profilePicture).toBe('image-url');
    });
  });

  it('should open the image uploader modal and update profile picture', () => {
    // Arrange
    const modalRef = {
      afterClose: of({ profilePicture: 'new-picture-url' }),
    };

    // Spy on the `create` method of NzModalService
    modalService.create.and.returnValue(modalRef as any);
    // Act
    component.openImageUploaderModal();

    expect(modalService.create).toHaveBeenCalledWith(
      jasmine.objectContaining({
        nzTitle: '',
        nzContent: ProfileUploaderModalComponent,
        nzFooter: null,
      })
    );

    modalRef.afterClose.subscribe(() => {
      expect(component.user.profilePicture).toBe('new-picture-url');
    });
  });

  it('should set validation message for full name length', () => {
    const inputElement = fixture.debugElement.query(
      By.css('input[name="fullname"]')
    ).nativeElement;
    inputElement.value = 'A'.repeat(51);
    inputElement.dispatchEvent(new Event('input'));

    expect(component.userNameValidationMsg).toBe(
      'Full Name cannot be greater than 50 characters long.'
    );
  });

  it('should call updateUserProfile on Update button click', () => {
    spyOn(component, 'updateUserProfile').and.callThrough();
    const updateButton = fixture.debugElement.query(
      By.css('.sign-in-btn')
    ).nativeElement;
    updateButton.click();
    expect(component.updateUserProfile).toHaveBeenCalled();
  });

  it('should fetch user profile from server on cache miss', () => {
    cacheService.getDataFromCache.and.returnValue(null);
    const mockUserProfile = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      data: { fullName: 'Cached User', email: 'cached@example.com' },
    };
    userService.getUserProfile.and.returnValue(of(mockUserProfile));
    component.ngOnInit();
    expect(userService.getUserProfile).toHaveBeenCalled();
    expect(component.user).toEqual(mockUserProfile.data);
  });

  it('should not update profile if any field exceeds length constraints', () => {
    component.user = {
      fullName: 'A'.repeat(51), // Exceeds 50 characters
      specialization: 'B'.repeat(501), // Exceeds 500 characters
      qualification: 'C'.repeat(501), // Exceeds 500 characters
      experience: 'D'.repeat(501), // Exceeds 500 characters
      headline: 'E'.repeat(101), // Exceeds 100 characters
      aboutMe: 'F'.repeat(1001), // Exceeds 1000 characters
    };
    const modalSpy = spyOn(component, 'openShareCourseModal');

    spyOn(component, 'updateUserProfile').and.callThrough();
    component.updateUserProfile();
    expect(modalSpy).not.toHaveBeenCalled();
  });

  it('should update profile successfully when all fields are within length constraints', fakeAsync(() => {
    // Arrange: Set up user data with fields within length constraints
    component.user = {
      fullName: 'John Doe',
      specialization: 'Software Developer',
      qualification: "Bachelor's Degree in Computer Science",
      experience: '5 years of experience in web development',
      headline: 'Experienced Web Developer',
      aboutMe:
        'Passionate about building scalable web applications and improving user experiences.',
    };
    const modalSpy = spyOn(component, 'openShareCourseModal');
    const mockUpdateResponse = {
      status: component._httpConstants.REQUEST_STATUS.SUCCESS_200.CODE,
      message: 'Profile updated successfully',
      data: { fullName: 'John Doe' },
    };
    userService.updateUserProfile.and.returnValue(of(mockUpdateResponse));
    cacheService.getDataFromCache.and.returnValue(
      JSON.stringify({ name: 'John Doe' })
    );
    component.user = { fullName: 'John Doe' };
    component.updateUserProfile();
    expect(userService.updateUserProfile).toHaveBeenCalled();
    expect(modalSpy).toHaveBeenCalled();
  }));

  it('should set userNameValidationMsg when input length exceeds 50 characters', () => {
    // Arrange: Create a mock input event
    const inputElement = fixture.debugElement.query(By.css('input[name="fullname"]')).nativeElement as HTMLInputElement;
    const event = new Event('input');
    inputElement.value = 'a'.repeat(51); // Set value to 51 characters
    inputElement.dispatchEvent(event);

    // Act: Call the method with the mock event
    component.checkInput(event);

    // Assert: Check that userNameValidationMsg is set correctly
    expect(component.userNameValidationMsg).toBe('Full Name cannot be greater than 50 characters long.');
  });

  it('should clear userNameValidationMsg when input length is within 50 characters', () => {
    // Arrange: Create a mock input event
    const inputElement = fixture.debugElement.query(By.css('input[name="fullname"]')).nativeElement as HTMLInputElement;
    const event = new Event('input');
    inputElement.value = 'a'.repeat(50); // Set value to 50 characters
    inputElement.dispatchEvent(event);

    // Act: Call the method with the mock event
    component.checkInput(event);

    // Assert: Check that userNameValidationMsg is cleared
    expect(component.userNameValidationMsg).toBe('');
  });
  
  it('should not prevent default for allowed characters', () => {
    // Arrange: Create a mock KeyboardEvent with an allowed key
    const event = new KeyboardEvent('keydown', { key: 'a' });
    spyOn(event, 'preventDefault');

    component.checkLength(event);
    expect(event.preventDefault).not.toHaveBeenCalled();
  });
});
