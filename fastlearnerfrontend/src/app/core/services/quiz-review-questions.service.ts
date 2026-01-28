import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';
import { environment } from 'src/environments/environment.development';

export interface PreviewReportResponse {
  status: number
  message: string
  code: string
  data: Data
}

export interface Data {
  id: number
  quizAttemptId: number
  status: string
  html: any
  pdfPath: any
  quizRandomId: string
  createdAt: string
  updatedAt: string
}

@Injectable({
  providedIn: 'root'
})
export class QuizReviewQuestionsService {

  constructor(
    private readonly httpClient: HttpClient
  ) { }

  public fetchReport({quizAttemptId}: {quizAttemptId: string}): Observable<PreviewReportResponse> {
    const url = `${environment.baseUrl}`
    return this.httpClient.post<PreviewReportResponse>(`${url}quiz-report/`, {}, {
      params: {
        quizAttemptId
      }
    })
  }
}
