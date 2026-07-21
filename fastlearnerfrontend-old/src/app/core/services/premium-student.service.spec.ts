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
      const search = 'Test Search';
      const pageNo = 1;
      const pageSize = 5;

      service
        .getPremiumStudents({searchValue: search, pageNo, pageSize})
        .subscribe((response) => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpMock.expectOne(request => 
        request.url === `${environment.baseUrl}premium-students/` &&
        request.params.get('search') === 'Test Search' &&
        request.params.get('pageNo') === '1' &&
        request.params.get('pageSize') === '5'
      );

      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should call getPremiumStudents with correct query parameters', () => {
      const pageNo = 1;
      const pageSize = 5;
      const searchValue = 'Test Search';

      const mockResponse = { data: 'sample data' };

      service.getPremiumStudents({
        pageNo,
        pageSize,
        searchValue, // <-- correct key
      }).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne((request) =>
        request.url === `${environment.baseUrl}premium-students/` &&
        request.params.get('pageNo') === '1' &&
        request.params.get('pageSize') === '5' &&
        request.params.get('search') === 'Test Search'
      );

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

      service.downloadExcel({
        pageNo: 0,
        pageSize: 10,
      }).subscribe((response) => {
        expect(response).toEqual(mockBlob);
      });

      const req = httpMock.expectOne((request) =>
        request.url === `${environment.baseUrl}premium-students/export/premium-students` &&
        request.params.get('pageNo') === '0' &&
        request.params.get('pageSize') === '10'
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');

      req.flush(mockBlob);
    });
  });
});
