import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpBackend,
  HttpHeaders,
  HttpClientModule,
} from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { FileManager } from './file-manager.service';
import { AuthService } from './auth.service';
import { environment } from 'src/environments/environment.development';
import { SocialAuthService } from '@abacritt/angularx-social-login';

describe('FileManager', () => {
  let service: FileManager;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;
  let socialAuthServiceSpy: jasmine.SpyObj<SocialAuthService>;

  beforeEach(() => {
    // Create spies for HttpClient, HttpBackend, and AuthService
    const spyHttpClient = jasmine.createSpyObj('HttpClient', [
      'post',
      'delete',
    ]);
    socialAuthServiceSpy = jasmine.createSpyObj('SocialAuthService', [
      'signOut',
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [
        FileManager,
        { provide: HttpClient, useValue: spyHttpClient },
        { provide: SocialAuthService, useValue: socialAuthServiceSpy },
      ],
    });

    service = TestBed.inject(FileManager);
    httpClientSpy = TestBed.inject(HttpClient) as jasmine.SpyObj<HttpClient>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#uploadFile', () => {
    it('should upload a file successfully', () => {
      const file = new File([''], 'filename.txt');
      const fileType = 'text/plain';
      const response = { success: true };

      httpClientSpy.post.and.returnValue(of(response));

      service.uploadFile(file, fileType).subscribe((result) => {
        expect(result).toEqual(response);
      });

      // Verify that HttpClient.post was called with correct URL and FormData
      expect(httpClientSpy.post).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/`,
        jasmine.any(FormData)
      );
    });

    it('should handle upload file error', () => {
      const file = new File([''], 'filename.txt');
      const fileType = 'text/plain';
      const errorResponse = { status: 400, statusText: 'Bad Request' };

      httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

      service.uploadFile(file, fileType).subscribe({
        next: () => fail('Expected an error, not a successful response'),
        error: (error) => {
          expect(error).toEqual(errorResponse);
        },
      });

      // Verify that HttpClient.post was called with correct URL and FormData
      expect(httpClientSpy.post).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/`,
        jasmine.any(FormData)
      );
    });
  });

  describe('#deleteFile', () => {
    it('should delete a file successfully', () => {
      const id = '123';
      const url = 'http://example.com/file';
      const topicId = '456';
      const fileType = 'text/plain';
      const response = { success: true };

      httpClientSpy.delete.and.returnValue(of(response));

      service.deleteFile(id, url, topicId, fileType).subscribe((result) => {
        expect(result).toEqual(response);
      });

      // Verify that HttpClient.delete was called with correct URL, headers, and body
      expect(httpClientSpy.delete).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/`,
        {
          headers: jasmine.any(HttpHeaders),
          body: { id, url, topicId, fileType },
        }
      );
    });

    it('should handle delete file error', () => {
      const id = '123';
      const url = 'http://example.com/file';
      const topicId = '456';
      const fileType = 'text/plain';
      const errorResponse = { status: 404, statusText: 'Not Found' };

      httpClientSpy.delete.and.returnValue(throwError(() => errorResponse));

      service.deleteFile(id, url, topicId, fileType).subscribe({
        next: () => fail('Expected an error, not a successful response'),
        error: (error) => {
          expect(error).toEqual(errorResponse);
        },
      });

      // Verify that HttpClient.delete was called with correct URL, headers, and body
      expect(httpClientSpy.delete).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/`,
        {
          headers: jasmine.any(HttpHeaders),
          body: { id, url, topicId, fileType },
        }
      );
    });
  });

  describe('#regenerateSummary', () => {
    it('should regenerate summary successfully', () => {
      const url = 'http://example.com/file';
      const fileType = 'text/plain';
      const response = { success: true };

      httpClientSpy.post.and.returnValue(of(response));

      service.regenerateSummary(url, fileType).subscribe((result) => {
        expect(result).toEqual(response);
      });

      // Verify that HttpClient.post was called with correct URL and FormData
      expect(httpClientSpy.post).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/regenerate`,
        jasmine.any(FormData)
      );
    });

    it('should handle regenerate summary error', () => {
      const url = 'http://example.com/file';
      const fileType = 'text/plain';
      const errorResponse = {
        status: 500,
        statusText: 'Internal Server Error',
      };

      httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

      service.regenerateSummary(url, fileType).subscribe({
        next: () => fail('Expected an error, not a successful response'),
        error: (error) => {
          expect(error).toEqual(errorResponse);
        },
      });

      // Verify that HttpClient.post was called with correct URL and FormData
      expect(httpClientSpy.post).toHaveBeenCalledWith(
        `${environment.baseUrl}uploader/regenerate`,
        jasmine.any(FormData)
      );
    });
  });
});
