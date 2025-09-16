// src/app/services/class.service.ts

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class AiGraderService {
  viewClass(classId: any, editValue: any) {
    throw new Error('Method not implemented.');
  }
  constructor(private _http: HttpClient) {}

  public createClass(body): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}ai-grader/create-class`,
      body
    );
  }

  public getInstructorClasses(): Observable<any[]> {
    return this._http
      .get<any>(`${environment.baseUrl}ai-grader/get-class`)
      .pipe(map((response) => response?.data || []));
  }

  public getAssessment(
    body: any,
    pageNo: number,
    pageSize: number
  ): Observable<any> {
    return this._http.post<any>(
      `${environment.baseUrl}assessment/?pageNo=${pageNo}&pageSize=${pageSize}`,
      body
    );
  }

  public createAssessment(body): Observable<any> {
    return this._http.post(`${environment.baseUrl}assessment/create`, body);
  }

  public editEmail(aiResultId: number, email: string): Observable<any> {
    return this._http.put(
      `${environment.baseUrl}ai-result/update/email?aiResultId=${aiResultId}&email=${email}`,
      null
    );
  }

  startGrading(formData: FormData) {
    return this._http.post<any>(
      `${environment.baseUrl}ai-result/create`,
      formData
    );
  }

  startGradingLandingPage(formData: FormData) {
    return this._http.post<any>(
      `${environment.baseUrl}ai-result/create-landing-page`,
      formData
    );
  }
  getFilterSearch(body: any) {
    return this._http.post<any>(`${environment.baseUrl}ai-result/`, body);
  }

  public sendEmail(email: string, aiResultId: number): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}ai-result/send-email?aiResultId=${aiResultId}&email=${email}`,
      null
    );
  }

  public deleteStudentResult(aiResultId: number): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}ai-result/delete?aiResultId=${aiResultId}`
    );
  }

  public getClassResult(body: any, payLoad?: any): Observable<any> {
    return this._http.post<any>(
      `${environment.baseUrl}ai-result/?pageNo=${payLoad?.pageNo}&pageSize=${payLoad?.pageSize}`,
      body
    );
  }

  public exportAiResults(body: any, payLoad?: any): Observable<Blob> {
    return this._http.post<any>(
      `${environment.baseUrl}ai-grader/export-class?pageNo=${payLoad?.pageNo}&pageSize=${payLoad?.pageSize}`,
      body,
      {
        responseType: 'blob' as 'json',
      }
    );
  }

  public getClasses(body?: any): Observable<any> {
    const { classId, pageNo, pageSize } = body || {};
    let queryParams = `?pageNo=${pageNo ?? 0}&pageSize=${pageSize ?? 10}`;
    if (classId) {
      queryParams += `&classId=${classId}`;
    }
    const url = `${environment.baseUrl}ai-grader/all${queryParams}`;
    return this._http.post(url, null);
  }

  public getClassesStudent(body?: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}ai-grader/student?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`,
      null
    );
  }

  public getAssessments(assessment?: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}assessment/?pageNo=${assessment?.pageNo}&pageSize=${assessment?.pageSize}`,
      assessment
    );
  }

  public getAssessmentsByClassIdAndAssessmentId(
    data?: any,
    body?: any
  ): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}assessment/class-id-and-assessment-id?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`,
      data
    );
  }

  public editClass(classId?: any, editValue?: any): Observable<any> {
    return this._http.put(
      `${environment.baseUrl}ai-grader/update?classId=${classId}&name=${editValue}`,
      null
    );
  }

  public deleteClass(classId?: any): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}ai-grader/?classId=${classId}`
    );
  }

  public deleteAssessment(assessmentId?: any): Observable<any> {
    return this._http.delete(
      `${environment.baseUrl}assessment/delete?id=${assessmentId}`
    );
  }

  public editAssessment(assessmentId?: any, editValue?: any): Observable<any> {
    return this._http.put(
      `${environment.baseUrl}assessment/update?aiAssessmentId=${assessmentId}&name=${editValue}`,
      null
    );
  }

  public getAiStudentResult(aiResultId: number): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}ai-result/questions?aiResultId=${aiResultId}`
    );
  }

  public getAssessmentsDetails(data?: any, body?: any): Observable<any> {
    let params: string[] = [];

    if (data?.classId) {
      params.push(`classId=${data.classId}`);
    }
    if (data?.assessmentId) {
      params.push(`assessmentId=${data.assessmentId}`);
    }
    if (body?.pageNo !== undefined) {
      params.push(`pageNo=${body.pageNo}`);
    }
    if (body?.pageSize !== undefined) {
      params.push(`pageSize=${body.pageSize}`);
    }

    const queryString = params.join('&');

    return this._http.get(
      `${environment.baseUrl}assessment/details?${queryString}`
    );
  }

  public getResultQuestions(resultId?: any, data?: any): Observable<any> {
    return this._http.get(
      `${environment.baseUrl}ai-result/questions?aiResultId=${resultId}&pageNo=${data?.pageNo}&pageSize=${data?.pageSize}`
    );
  }

  public getResultByClassAndAssessmentId(
    data?: any,
    body?: any
  ): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}ai-result/?pageNo=${body?.pageNo}&pageSize=${body?.pageSize}`,
      data
    );
  }

  public approveResult(resultId?: any): Observable<any> {
    return this._http.put(
      `${environment.baseUrl}ai-result/update/status?aiResultId=${resultId}`,
      null
    );
  }

  public updateQuestion(
    aiResultQuestionId?: any,
    score?: any
  ): Observable<any> {
    return this._http.put(
      `${environment.baseUrl}ai-result/question/update?aiResultQuestionId=${aiResultQuestionId}&score=${score}`,
      null
    );
  }

  public getNoOfPagesUsed(): Observable<any> {
    return this._http.get(`${environment.baseUrl}user/user-details`);
  }

  public retryGrading(resultId?: any): Observable<any> {
    return this._http.post(
      `${environment.baseUrl}ai-result/retry-grading?resultId=${resultId}`,
      null
    );
  }
}
