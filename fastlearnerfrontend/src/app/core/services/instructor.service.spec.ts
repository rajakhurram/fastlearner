import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { InstructorService } from './instructor.service';
import { environment } from 'src/environments/environment.development';

describe('InstructorService', () => {
  let service: InstructorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [InstructorService]
    });

    service = TestBed.inject(InstructorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#generator', () => {
    it('should send a POST request to the correct URL with the correct body', () => {
      const input = 'test-input';
      const url = `${environment.baseUrl}ai-generator/?input=${input}`;

      service.generator(input).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
    });

    it('should handle the response from the server', () => {
      const input = 'test-input';
      const mockResponse = { result: 'mock result' };
      const url = `${environment.baseUrl}ai-generator/?input=${input}`;

      service.generator(input).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(url);
      req.flush(mockResponse);
    });

    it('should handle errors from the server', () => {
      const input = 'test-input';
      const url = `${environment.baseUrl}ai-generator/?input=${input}`;

      service.generator(input).subscribe({
        next: () => fail('should have failed with a 500 error'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(url);
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getTopicTypes', () => {
    it('should send a GET request to the correct URL', () => {
      const url = `${environment.baseUrl}topic-type/`;

      service.getTopicTypes().subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('GET');
    });

    it('should handle the response from the server', () => {
      const mockResponse = [{ id: 1, name: 'Topic 1' }, { id: 2, name: 'Topic 2' }];
      const url = `${environment.baseUrl}topic-type/`;

      service.getTopicTypes().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(url);
      req.flush(mockResponse);
    });

    it('should handle errors from the server', () => {
      const url = `${environment.baseUrl}topic-type/`;

      service.getTopicTypes().subscribe({
        next: () => fail('should have failed with a 500 error'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(url);
      req.flush('Server error', { status: 500, statusText: 'Server Error' });
    });
  });
});
