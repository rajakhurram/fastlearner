import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from 'src/environments/environment.development';
import { AiGraderService } from './ai-grader.service';

describe('AiGraderService', () => {
  let service: AiGraderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AiGraderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a class', () => {
    const body = { name: 'Math 101' };

    service.createClass(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}ai-grader/create-class`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should get result by id', () => {
    service.getResultById(10).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}ai-result/10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should map instructor classes from response data', () => {
    let result: any[] = [];

    service.getInstructorClasses().subscribe((classes) => {
      result = classes;
    });

    const req = httpMock.expectOne(`${environment.baseUrl}ai-grader/get-class`);
    req.flush({ data: [{ id: 1 }] });
    expect(result).toEqual([{ id: 1 }]);
  });

  it('should start grading from landing page', () => {
    const formData = new FormData();

    service.startGradingLandingPage(formData).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/create-landing-page`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should edit email field', () => {
    service.editField(5, 'email', 'student@example.com').subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/update?aiResultId=5&email=student%40example.com`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should delete student result', () => {
    service.deleteStudentResult(9).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/delete?aiResultId=9`,
    );
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should approve result', () => {
    service.approveResult(12).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/update/status?aiResultId=12`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({ status: 200 });
  });

  it('should fetch result questions', () => {
    service.getResultQuestions(4, { pageNo: 0, pageSize: 10 }).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/questions?aiResultId=4&pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({ status: 200, data: {} });
  });

  it('should export ai results', () => {
    service
      .exportAiResults(
        { assignmentId: 1, classId: 2 },
        { pageNo: 0, pageSize: 10 },
      )
      .subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-grader/export-class?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    req.flush(new Blob());
  });

  it('should delete class by id', () => {
    service.deleteClass(3).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}ai-grader/?classId=3`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should get assessment with pagination', () => {
    const body = { classId: 1 };

    service.getAssessment(body, 0, 10).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}assessment/?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should create assessment', () => {
    const body = { name: 'Midterm' };

    service.createAssessment(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}assessment/create`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should edit email via editEmail', () => {
    service.editEmail(7, 'new@test.com').subscribe();

    const req = httpMock.expectOne((request) =>
      request.url.includes('ai-result/update') &&
      request.url.includes('aiResultId=7') &&
      request.url.includes('email=new'),
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should edit student name field', () => {
    service.editField(5, 'name', 'Alice').subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/update?aiResultId=5&studentName=Alice`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should edit roll number field', () => {
    service.editField(5, 'rollNumber', 'R-101').subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/update?aiResultId=5&studentRollNumber=R-101`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should start grading from instructor uploader', () => {
    const formData = new FormData();

    service.startGrading(formData).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}ai-result/create`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should filter search results', () => {
    const body = { search: 'alice' };

    service.getFilterSearch(body).subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}ai-result/`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should send result email', () => {
    service.sendEmail('student@test.com', 12).subscribe();

    const req = httpMock.expectOne((request) =>
      request.url.includes('ai-result/send-email') &&
      request.url.includes('aiResultId=12') &&
      request.url.includes('email=student'),
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get class result with pagination', () => {
    const body = { classId: 1, assignmentId: 2 };
    const payLoad = { pageNo: 0, pageSize: 10 };

    service.getClassResult(body, payLoad).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get classes with optional classId', () => {
    service.getClasses({ classId: 5, pageNo: 1, pageSize: 20 }).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-grader/all?pageNo=1&pageSize=20&classId=5`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get student classes', () => {
    service.getClassesStudent({ pageNo: 0, pageSize: 10 }).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-grader/student?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get assessments list', () => {
    const assessment = { pageNo: 0, pageSize: 10, classId: 1 };

    service.getAssessments(assessment).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}assessment/?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should get assessments by class and assessment id', () => {
    const data = { classId: 1, assessmentId: 2 };
    const body = { pageNo: 0, pageSize: 10 };

    service.getAssessmentsByClassIdAndAssessmentId(data, body).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}assessment/class-id-and-assessment-id?pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should edit class name', () => {
    service.editClass(4, 'Physics').subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-grader/update?classId=4&name=Physics`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should delete assessment', () => {
    service.deleteAssessment(8).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}assessment/delete?id=8`,
    );
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should edit assessment name', () => {
    service.editAssessment(9, 'Final Exam').subscribe();

    const req = httpMock.expectOne((request) =>
      request.url.includes('assessment/update') &&
      request.url.includes('aiAssessmentId=9') &&
      request.url.includes('name=Final'),
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should get ai student result questions', () => {
    service.getAiStudentResult(15).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/questions?aiResultId=15`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should get assessment details', () => {
    service
      .getAssessmentsDetails(
        { classId: 1, assessmentId: 2 },
        { pageNo: 0, pageSize: 10 },
      )
      .subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}assessment/details?classId=1&assessmentId=2&pageNo=0&pageSize=10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should get result by class and assessment id', () => {
    const data = { classId: 1, assignmentId: 2, studEmail: 's@test.com' };
    const body = { pageNo: 0, pageSize: 1 };

    service.getResultByClassAndAssessmentId(data, body).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/?pageNo=0&pageSize=1`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should update question marks', () => {
    service.updateQuestion(3, 8, 10).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/question/update?aiResultQuestionId=3&score=8&totalMarks=10`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should create manual question', () => {
    const body = { aiResultId: 1, questionNumber: 2, obtainedMarks: 5 };

    service.createManualQuestion(body).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/question/manual`,
    );
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('should update manual question', () => {
    const body = { obtainedMarks: 6 };

    service.updateManualQuestion(11, body).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/question/manual?aiResultQuestionId=11`,
    );
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('should delete manual question', () => {
    service.deleteManualQuestion(22).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/question/manual?aiResultQuestionId=22`,
    );
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('should get number of pages used', () => {
    service.getNoOfPagesUsed().subscribe();

    const req = httpMock.expectOne(`${environment.baseUrl}user/user-details`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: { noOfPagesUsed: 10, allowedPages: 100 } });
  });

  it('should retry grading', () => {
    service.retryGrading(99).subscribe();

    const req = httpMock.expectOne(
      `${environment.baseUrl}ai-result/retry-grading?resultId=99`,
    );
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
