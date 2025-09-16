import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from 'src/environments/environment.development';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { PremiumStudentsService } from './premium-student.service';

describe('PremiumStudentsService', () => {
  let service: PremiumStudentsService;
  let httpMock: HttpTestingController;

  const mockResponse = { data: 'sample data' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PremiumStudentsService],
      schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA],
    });
    service = TestBed.inject(PremiumStudentsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getPremiumStudents', () => {
    it('should call the API with correct parameters', () => {
      const title = 'Test Title';
      const search = 'Test Search';
      const pageNo = 1;
      const pageSize = 5;

      service
        .getPremiumStudents(title, search, pageNo, pageSize)
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      const encodedTitle = encodeURIComponent(title);
      const encodedSearch = encodeURIComponent(search);

      const req = httpMock.expectOne(
        `${environment.baseUrl}premium-students/?pageNo=${pageNo}&pageSize=${pageSize}&title=${encodedTitle}&search=${encodedSearch}`
      );

      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should call getPremiumStudents with correct query parameters', () => {
      const title = 'Test Title';
      const search = 'Test Search';
      const pageNo = 1;
      const pageSize = 5;
      const mockResponse = { data: 'sample data' };

      service
        .getPremiumStudents(title, search, pageNo, pageSize)
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      // Adjust the expected URL to match the actual encoded format
      const expectedUrl = `${
        environment.baseUrl
      }premium-students/?pageNo=${pageNo}&pageSize=${pageSize}&title=${encodeURIComponent(
        title
      )}&search=${encodeURIComponent(search)}`;
      const req = httpMock.expectOne(expectedUrl);

      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getPremiumStudentsByDate', () => {
    it('should call the API with date range parameters', () => {
      const startDate = '2024-01-01';
      const endDate = '2024-01-31';
      const pageNo = 1;
      const pageSize = 5;

      service
        .getPremiumStudentsByDate(startDate, endDate, pageNo, pageSize)
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpMock.expectOne(
        `${environment.baseUrl}premium-students/by-date?startDate=${startDate}&endDate=${endDate}&pageNo=${pageNo}&pageSize=${pageSize}`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('downloadExcel', () => {
    it('should download an Excel file as a blob', () => {
      const mockBlob = new Blob(['sample data'], {
        type: 'application/vnd.ms-excel',
      });

      service.downloadExcel().subscribe((response) => {
        expect(response).toEqual(mockBlob);
      });

      const req = httpMock.expectOne(
        `${environment.baseUrl}premium-students/export/premium-students`
      );
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');
      req.flush(mockBlob);
    });
  });
});
