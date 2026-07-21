import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class CommunicationService {
  private doucmentSummarySubject = new Subject<string>();
  documentSummary$ = this.doucmentSummarySubject.asObservable();

  private videoSummarySubject = new Subject<string>();
  videoSummary$ = this.videoSummarySubject.asObservable();

  private articleSummarySubject = new Subject<string>();
  articleSummary$ = this.articleSummarySubject.asObservable();

  private videoTranscriptSubject = new Subject<string>();
  videoTranscript$ = this.videoTranscriptSubject.asObservable();

  private notificationSubject = new Subject<string>();
  notificationData$ = this.notificationSubject.asObservable();

  private notificationCountSubject = new Subject<string>();
  notificationCountData$ = this.notificationCountSubject.asObservable();

  private removeEmitterSubject = new Subject<string>();
  removeEmitterData$ = this.removeEmitterSubject.asObservable();

  private closeCompletionSubject = new Subject<string>();
  closeCompletionData$ = this.closeCompletionSubject.asObservable();

  private instructorCourseUpdateSubject = new Subject<string>();
  instructorCourseUpdate$ = this.instructorCourseUpdateSubject.asObservable();

  private instructorTabChangeSubject = new Subject<string>();
  instructorTabChange$ = this.instructorTabChangeSubject.asObservable();

  private flLoginSubject = new Subject<string>();
  flLoginSubject$ = this.flLoginSubject.asObservable();

  private navbarAndFooterStateSubject = new Subject<string>();
  navbarAndFooterStateSubject$ = this.navbarAndFooterStateSubject.asObservable();

  sendDocumentSummary(document: string) {
    this.doucmentSummarySubject.next(document);
  }

  sendVideoSummary(videoData: any) {
    this.videoSummarySubject.next(videoData);
  }

  sendArticleSummary(article: any) {
    this.articleSummarySubject.next(article);
  }

  sendVideoTranscript(videoData: any) {
    this.videoTranscriptSubject.next(videoData);
  }

  showNotificationData() {
    this.notificationSubject.next(null);
  }

  showNotificationCountData() {
    this.notificationCountSubject.next(null);
  }

  removeEmitter() {
    this.removeEmitterSubject.next(null);
  }

  closeCourseCompletionModal() {
    this.closeCompletionSubject.next(null);
  }

  updateInstructorCourse() {
    this.instructorCourseUpdateSubject.next(null);
  }

  instructorTabChange(tab?: any) {
    this.instructorTabChangeSubject.next(tab);
  }

  flLoginStateChange(flag?: any) {
    this.flLoginSubject.next(flag);
  }

  navbarAndFooterStateChange(flag?: any) {
    this.navbarAndFooterStateSubject.next(flag);
  }

}