// import {
//   ComponentFixture,
//   fakeAsync,
//   TestBed,
//   tick,
// } from '@angular/core/testing';
// import { ShareModalComponent } from './share-modal.component';
// import { NzModalService } from 'ng-zorro-antd/modal';
// import { Location } from '@angular/common';
// import { CourseService } from 'src/app/core/services/course.service';
// import { MessageService } from 'src/app/core/services/message.service';
// import { HttpConstants } from 'src/app/core/constants/http.constants';
// import { of, throwError } from 'rxjs';
// import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

// describe('ShareModalComponent', () => {
//   let component: ShareModalComponent;
//   let fixture: ComponentFixture<ShareModalComponent>;
//   let location: jasmine.SpyObj<Location>;
//   let modalService: jasmine.SpyObj<NzModalService>;

//   beforeEach(async () => {
//     location = jasmine.createSpyObj('Location', ['prepareExternalUrl', 'path']);
//     modalService = jasmine.createSpyObj('NzModalService', ['closeAll']);

//     await TestBed.configureTestingModule({
//       declarations: [ShareModalComponent],
//       providers: [
//         { provide: NzModalService, useValue: modalService },
//         { provide: CourseService, useValue: {} },
//         { provide: MessageService, useValue: {} },
//         { provide: Location, useValue: location },
//         {
//           provide: HttpConstants,
//           useValue: { REQUEST_STATUS: { SUCCESS_200: { CODE: 200 } } },
//         },
//       ],
//       schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
//     }).compileComponents();

//     fixture = TestBed.createComponent(ShareModalComponent);
//     component = fixture.componentInstance;

//     location.prepareExternalUrl.and.returnValue('http://example.com');
//     location.path.and.returnValue('/current-path');

//     fixture.detectChanges();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should initialize shareURL with provided URL', () => {
//     component.url = 'http://test-url.com';
//     component.ngOnInit();
//     expect(component.shareURL).toBe('http://test-url.com');
//   });

//   it('should initialize shareURL with current URL when URL is not provided', () => {
//     component.url = undefined;
//     component.ngOnInit();
//     expect(component.shareURL).toBe('http://localhost:9876/context.html');
//   });
// });

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ShareModalComponent } from './share-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { CourseService } from 'src/app/core/services/course.service';
import { MessageService } from 'src/app/core/services/message.service';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ShareModalComponent', () => {
  let component: ShareModalComponent;
  let fixture: ComponentFixture<ShareModalComponent>;
  let location: Location;
  let modalService: NzModalService;
  let courseService: CourseService;
  let messageService: MessageService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShareModalComponent],
      providers: [
        Location,
        { provide: NzModalService, useValue: {} },
        { provide: CourseService, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: Router, useValue: { url: '/test-route' } },
      ],
      schemas : [CUSTOM_ELEMENTS_SCHEMA , NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShareModalComponent);
    component = fixture.componentInstance;
    location = TestBed.inject(Location);
    modalService = TestBed.inject(NzModalService);
    courseService = TestBed.inject(CourseService);
    messageService = TestBed.inject(MessageService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should set shareURL to the provided url if url is defined', () => {
      component.url = 'https://example.com';
      component.ngOnInit();
      expect(component.shareURL).toBe('https://example.com');
    });

    it('should set shareURL to the current window URL if url is undefined', () => {
      component.url = undefined;
      spyOn(location, 'prepareExternalUrl').and.returnValue('/current-path');
      component.ngOnInit();
      expect(component.shareURL).toBe(window.location.href);
    });
  });

  describe('copyURL', () => {
    it('should copy the input field value to the clipboard', () => {
      const inputElement = document.createElement('input');
      inputElement.value = 'https://example.com';
      spyOn(document, 'getElementById').and.returnValue(inputElement);
      spyOn(navigator.clipboard, 'writeText');
      component.copyURL();
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(
        'https://example.com'
      );
    });
  });

  describe('unsecuredCopyToClipboard', () => {
    it('should copy the text to clipboard using fallback method', () => {
      const text = 'https://example.com';
      spyOn(document, 'execCommand').and.returnValue(true);
      spyOn(document.body, 'appendChild').and.callThrough();
      spyOn(document.body, 'removeChild').and.callThrough();

      component.unsecuredCopyToClipboard(text);

      expect(document.body.appendChild).toHaveBeenCalled();
      expect(document.execCommand).toHaveBeenCalledWith('copy');
      expect(document.body.removeChild).toHaveBeenCalled();
    });

    it('should log an error if copy fails', () => {
      const text = 'https://example.com';
      spyOn(document, 'execCommand').and.throwError('copy failed');
      spyOn(console, 'error');

      component.unsecuredCopyToClipboard(text);

      expect(console.error).toHaveBeenCalledWith(
        'Unable to copy to clipboard',
        jasmine.any(Error)
      );
    });
  });

  describe('copyToClipboard', () => {
    it('should use standard clipboard API if secure context', () => {
      const inputElement = document.createElement('input');
      inputElement.value = 'https://example.com';
      spyOn(document, 'getElementById').and.returnValue(inputElement);
      spyOn(navigator.clipboard, 'writeText');
      spyOn(component, 'unsecuredCopyToClipboard');

      spyOnProperty(window, 'isSecureContext', 'get').and.returnValue(true);

      component.copyToClipboard();

      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(
        'https://example.com'
      );
      expect(component.unsecuredCopyToClipboard).not.toHaveBeenCalled();
    });

    it('should use fallback method if not secure context', () => {
      const inputElement = document.createElement('input');
      inputElement.value = 'https://example.com';
      spyOn(document, 'getElementById').and.returnValue(inputElement);
      spyOn(navigator.clipboard, 'writeText').and.callThrough();
      spyOn(component, 'unsecuredCopyToClipboard');

      spyOnProperty(window, 'isSecureContext', 'get').and.returnValue(false);

      component.copyToClipboard();

      expect(component.unsecuredCopyToClipboard).toHaveBeenCalledWith(
        'https://example.com'
      );
    });
  });
});
