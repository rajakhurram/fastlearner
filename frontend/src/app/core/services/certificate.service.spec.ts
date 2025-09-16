import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CertificateService } from './certificate.service';
import { AuthService } from './auth.service';
import { environment } from 'src/environments/environment.development';

describe('CertificateService', () => {
  let service: CertificateService;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CertificateService,
        {
          provide: AuthService,
          useValue: { getAccessToken: () => 'mock-token' }
        }
      ]
    });

    service = TestBed.inject(CertificateService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch certificate data with correct URL', () => {
    const courseId = 123;
    const mockResponse = { data: 'certificate data' };
    const url = `${environment.baseUrl}certificate/generate/${courseId}`;

    service.getCertificateData(courseId).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should generate correct certificate URL', () => {
    const courseId = 123;
    const isDownloadable = true;
    const token = 'mock-token';
    const expectedUrl = `${environment.baseUrl}certificate/download?courseId=${courseId}&isDownloadable=${isDownloadable}&token=${token}`;

    const url = service.getCertificateUrl(courseId, isDownloadable);
    expect(url).toBe(expectedUrl);
  });

  it('should verify certificate with correct URL', () => {
    const uuid = 'uuid-1234';
    const mockResponse = { valid: true };
    const url = `${environment.baseUrl}certificate/verify/to/${uuid}`;

    service.verifyCertificate(uuid).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should handle error when getting certificate data', () => {
    const courseId = 123;
    const url = `${environment.baseUrl}certificate/generate/${courseId}`;

    service.getCertificateData(courseId).subscribe({
      next: () => fail('Expected an error, not certificate data'),
      error: error => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(url);
    req.flush('Error occurred', { status: 500, statusText: 'Server Error' });
  });

  it('should handle error when verifying certificate', () => {
    const uuid = 'uuid-1234';
    const url = `${environment.baseUrl}certificate/verify/to/${uuid}`;

    service.verifyCertificate(uuid).subscribe({
      next: () => fail('Expected an error, not certificate verification result'),
      error: error => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne(url);
    req.flush('Error occurred', { status: 500, statusText: 'Server Error' });
  });
});
