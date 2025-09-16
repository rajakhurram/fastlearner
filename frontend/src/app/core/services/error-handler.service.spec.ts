import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { ErrorHandlerService } from './error-handler.service';
import { MessageService } from './message.service';

describe('ErrorHandlerService', () => {
  let service: ErrorHandlerService;
  let routerSpy: jasmine.SpyObj<Router>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  beforeEach(() => {
    const spyRouter = jasmine.createSpyObj('Router', ['navigate']);
    const spyMessageService = jasmine.createSpyObj('MessageService', ['showMessage']); // Assuming a method `showMessage` exists

    TestBed.configureTestingModule({
      providers: [
        ErrorHandlerService,
        { provide: Router, useValue: spyRouter },
        { provide: MessageService, useValue: spyMessageService },
      ],
    });

    service = TestBed.inject(ErrorHandlerService);
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    messageServiceSpy = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should handle 500 errors', () => {
    const error = new HttpErrorResponse({ status: 500, statusText: 'Internal Server Error' });
    spyOn(service as any, 'createErrorMessage'); // Spy on private method

    service.handleError(error);

    expect(service['createErrorMessage']).toHaveBeenCalledWith(error);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/500']);
  });

  it('should handle 404 errors', () => {
    const error = new HttpErrorResponse({ status: 404, statusText: 'Not Found' });
    spyOn(service as any, 'createErrorMessage'); // Spy on private method

    service.handleError(error);

    expect(service['createErrorMessage']).toHaveBeenCalledWith(error);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/404']);
  });

  it('should handle other errors', () => {
    const error = new HttpErrorResponse({ status: 400, statusText: 'Bad Request' });
    spyOn(service as any, 'createErrorMessage'); // Spy on private method

    service.handleError(error);

    expect(service['createErrorMessage']).toHaveBeenCalledWith(error);
    expect(routerSpy.navigate).not.toHaveBeenCalled(); // Should not navigate
  });

  it('should create error message', () => {
    const error = new HttpErrorResponse({ error: 'Error message', statusText: 'Error' });

    (service as any).createErrorMessage(error);

    expect(service.errorMessage).toBe('Error message');
  });

  it('should create default error message when error message is not provided', () => {
    const error = new HttpErrorResponse({ statusText: 'Default error' });

    (service as any).createErrorMessage(error);

    expect(service.errorMessage).toBe('Default error');
  });
});
